package org.anodyneos.xpImpl.translater;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.xp.tagext.TagAttributeInfo;
import org.anodyneos.xp.tagext.TagInfo;
import org.anodyneos.xpImpl.util.CodeWriter;
import org.anodyneos.xpImpl.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * ProcessorTag handles custom Tags
 *
 * <li>startElement: instantiate tag. Validate names and types of attributes
 * vs TLD, call setters.
 *
 * <li>getProcessorFor: if xp:attribute, get processor that can handle
 * attributes for regular content or fragments. If anything else, this the
 * first element in a fragment, so create fragment and get new
 * ProcessorResultContent.
 *
 * <li>characters: may be useless whitespace, tag-specific text content, or
 * part of a fragment.  For now, populate StringBuffer for later use.
 *
 * @author jvas
 */
public class ProcessorTag extends TranslaterProcessor {

    private StringBuffer sb;

    private ProcessorResultContent bodyFragmentProcessor;
    private Map bodyFragmentPrefixMap;

    private boolean bodyFragmentStarted = false;
    int bodyFragmentId = -1;
    Set handledAttributes = new HashSet();
    TagInfo tagInfo = null;
    Map attributeInfos = null;
    String localVarName = null;

    public static final String E_ATTRIBUTE = "attribute";

    public ProcessorTag(TranslaterContext ctx) {
        super(ctx);
        bodyFragmentProcessor = new ProcessorResultContent(ctx);
        // when we write the fragment, we will need to copy the source tree's prefix mappings into the output
        // tree in case the fragment is used outside of the immediate parent in the source tree.
        bodyFragmentPrefixMap = new HashMap();
        NamespaceSupport ns = getTranslaterContext().getNamespaceSupport();
        Enumeration e = ns.getPrefixes();
        while (e.hasMoreElements()) {
            String nsPrefix = (String) e.nextElement();
            String nsURI = ns.getURI(nsPrefix);
            bodyFragmentPrefixMap.put(nsPrefix, nsURI);
        }
        String uri = ns.getURI("");
        if(null == uri) {
            uri = "";
        }
        bodyFragmentPrefixMap.put("", uri);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName)
            throws SAXException {
        // TODO: consider xp:tagAttribute to allow subelements to provide values for tag attributes.
        ElementProcessor p = bodyFragmentProcessor.getProcessorFor(uri, localName, qName);
        // since no exception, start fragment if not already started. Dump characters.
        // Return p (p may be the bodyFragmentProcessor itself if the content is result
        // content, but bodyFragmentProcessor made the decision.)
        if (! bodyFragmentStarted) {
            startBodyFragment();
            bodyFragmentStarted = true;
        }
        if(null != sb) {
            char[] chars = sb.toString().toCharArray();
            bodyFragmentProcessor.characters(chars, 0, chars.length);
            sb = null;
            // make sure to flush characters in processor
            bodyFragmentProcessor.flushCharacters();
        }
        return p;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {

        // TODO: TranslaterProcessor (our super-class) buffers startPrefixMapping calls.  We cannot call
        // xpCH.startPrefixMapping since we are not going to call xpCH.startElement, but we need to figure out a way
        // to communicate these "phantom" mappings to the xpCH so that the mappings are available in case they are
        // used at runtime to evaluate EL expressions in attributes of this method.
        // for now, lets just "trash" them:
        getTranslaterContext().clearBufferedStartPrefixMappings();

        TranslaterContext ctx = getTranslaterContext();
        this.tagInfo = ctx.getTagLibraryRegistry().getTagLibraryInfo(uri).getTagInfo(localName);
        this.attributeInfos = toMap(tagInfo.getTagAttributeInfos());
        String tagImplClass = tagInfo.getClassName();
        this.localVarName = ctx.getVariableForTag(tagImplClass);
        CodeWriter out = ctx.getCodeWriter();

        // instantiate tag
        out.printIndent().println(tagImplClass + " " + localVarName + " = new " + tagImplClass + "();");
        if(ctx.inFragment()) {
            out.printIndent().println(localVarName + ".setParent(xpTagParent);");
        } else {
            out.printIndent().println("// " + localVarName + ".setParent(null); // no parent tag");
        }
        out.printIndent().println(localVarName + ".setXpContext(xpContext);");

        // add attributes
        for (int i = 0; i < attributes.getLength(); i++) {
            String prefix = parsePrefix(attributes.getQName(i));
            String name = parseLocalName(attributes.getQName(i));
            if (prefix.length() == 0 && ! name.equals("xmlns")) {
                String value = attributes.getValue(i);
                TagAttributeInfo attrInfo = (TagAttributeInfo) attributeInfos.get(name);
                if (null == attrInfo) {
                    throw new SAXException("attribute '" + name + "' not allowed in tag " + qName);
                }
                // add attribute
                String codeValue;
                if (Util.hasEL(value) && ! attrInfo.isRequestTimeOK()) {
                    throw new SAXException("attribute '" + name + "' cannot have EL");
                } else {
                    codeValue = Util.elExpressionCode(value, attrInfo.getType());
                }
                out.printIndent().println(localVarName + "." + Util.toSetMethod(name) + "(" + codeValue + ");");
                this.handledAttributes.add(name);
            }
        }

    }

    public void characters(char[] ch, int start, int length) {
        if (null == sb) {
            sb = new StringBuffer();
        }
        sb.append(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        // end element
        if (null != sb && ! sb.toString().trim().equals("")) {
            if (! bodyFragmentStarted) {
                startBodyFragment();
                bodyFragmentStarted = true;
            }
            char[] chars = sb.toString().toCharArray();
            bodyFragmentProcessor.characters(chars, 0, chars.length);
            sb = null;
            // make sure to flush characters in processor
            bodyFragmentProcessor.flushCharacters();
        }

        if (bodyFragmentStarted) {
            bodyFragmentProcessor.flushCharacters();

            CodeWriter out = getTranslaterContext().getCodeWriter();

            out.println();
            out.printIndent().println("if (! namespaceCompat) {");
            out.indentPlus();
            Iterator it = bodyFragmentPrefixMap.keySet().iterator();
            for(int i = bodyFragmentPrefixMap.size(); i > 0; i--) {
                String nsPrefix = (String) it.next();
                String nsURI = (String) bodyFragmentPrefixMap.get(nsPrefix);
                out.printIndent().println("xpCH.popPhantomPrefixMapping();");
            }
            out.endBlock();

            getTranslaterContext().endFragment();
            out = getTranslaterContext().getCodeWriter();
            out.printIndent().println(
                    localVarName + ".setXpBody(new FragmentHelper(" + bodyFragmentId + ", xpContext, " + localVarName + ", xpCH));");
        }

        CodeWriter out = getTranslaterContext().getCodeWriter();
        out.printIndent().println(localVarName + ".doTag(xpOut);");
        out.printIndent().println(localVarName + " = null;");

    }

    private Map toMap(TagAttributeInfo[] infos) {
        Map map = new HashMap();
        if (null != infos) {
            for(int i = 0; i < infos.length; i++) {
                map.put(infos[i].getName(), infos[i]);
            }
        }
        return map;
    }

    private void startBodyFragment() {
        assert (! bodyFragmentStarted);
        bodyFragmentId = getTranslaterContext().startFragment();
        CodeWriter out = getTranslaterContext().getCodeWriter();
        // check to see if we need to declare our prefixes
        out.printIndent().println("boolean namespaceCompat = xpCH.isNamespaceContextCompatible(origXpCH, parentElClosed, origContextVersion, origAncestorsWithPrefixMasking, origPhantomPrefixCount);");
        out.printIndent().println("if (! namespaceCompat) {");
        out.indentPlus();
        Iterator it = bodyFragmentPrefixMap.keySet().iterator();
        while (it.hasNext()) {
            String nsPrefix = (String) it.next();
            String nsURI = (String) bodyFragmentPrefixMap.get(nsPrefix);
            out.printIndent().println(
                    "xpCH.pushPhantomPrefixMapping("
                +        Util.escapeStringQuoted(nsPrefix)
                + ", " + Util.escapeStringQuoted(nsURI)
                + ");");
        }
        out.endBlock();
        out.println();
    }

    private static final String parseLocalName(String qName) {
        if (null == qName || qName.length() == 0) {
            return "";
        } else {
            int colon = qName.indexOf(':');
            if (-1 == colon) {
                return qName;
            } else {
                return qName.substring(colon + 1);
            }
        }
    }

    private static final String parsePrefix(String qName) {
        if (null == qName || qName.length() == 0) {
            return "";
        } else {
            int colon = qName.indexOf(':');
            if (-1 == colon) {
                return "";
            } else {
                return qName.substring(0, colon);
            }
        }
    }
}
