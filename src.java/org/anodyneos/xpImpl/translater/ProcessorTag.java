package org.anodyneos.xpImpl.translater;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.xp.tagext.TagAttributeInfo;
import org.anodyneos.xp.tagext.TagInfo;
import org.anodyneos.xpImpl.util.CodeWriter;
import org.anodyneos.xpImpl.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * ProcessorTag handles custom Tags
 *
 * Contents may include one of the following:
 *
 * 1. Nothing
 * 2. Text content or a fragment
 * 3. Combination of 0 or more <xp:parameter> elements and 0 or 1 <xp:body> elements
 *
 * In case #3, xp:parameter content is either a fragment that is passed to the tag or content
 * whose result is processed to the tag.
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
public class ProcessorTag extends TranslaterProcessorNonResultContent {

    private StringBuffer sb;

    private ProcessorFragment bodyFragmentProcessor;

    private Set handledAttributes = new HashSet();
    private TagInfo tagInfo = null;
    private Map attributeInfos = null;
    private String localVarName = null;

    private static final int BODY_TYPE_EMPTY = 0;
    private static final int BODY_TYPE_FRAGMENT = 1;
    private static final int BODY_TYPE_TAGS = 2;

    private int bodyType = 0;

    public static final String E_PARAMETER = "parameter";
    public static final String E_BODY = "body";

    public ProcessorTag(TranslaterContext ctx) {
        super(ctx);
        bodyFragmentProcessor = new ProcessorFragment(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName)
            throws SAXException {

        final ElementProcessor p;

        // xp:parameter
        if (uri.equals(URI_XP) && (E_PARAMETER.equals(localName))) {
            if (BODY_TYPE_EMPTY != bodyType && BODY_TYPE_TAGS != bodyType) {
                // not allowed
                return super.getProcessorFor(uri, localName, qName);
            }
            bodyType = BODY_TYPE_TAGS;
            throw new NotImplementedException();
        // xp:body
        } else if (uri.equals(URI_XP) && (E_BODY.equals(localName))) {
            if (BODY_TYPE_EMPTY != bodyType && BODY_TYPE_TAGS != bodyType) {
                // not allowed
                return super.getProcessorFor(uri, localName, qName);
            }
            bodyType = BODY_TYPE_TAGS;
            p = bodyFragmentProcessor;
        // body content
        } else {
            if (BODY_TYPE_EMPTY != bodyType && BODY_TYPE_FRAGMENT != bodyType) {
                // not allowed
                return super.getProcessorFor(uri, localName, qName);
            }
            bodyType = BODY_TYPE_FRAGMENT;

            // start fragment if not already started. Dump characters.
            // Return p (p may be the bodyFragmentProcessor itself if the content is result
            // content, but bodyFragmentProcessor made the decision.)
            if (! bodyFragmentProcessor.isFragmentStarted()) {
                bodyFragmentProcessor.startFragment();
            }
            if(null != sb) {
                char[] chars = sb.toString().toCharArray();
                bodyFragmentProcessor.characters(chars, 0, chars.length);
                sb = null;
            }
            p = bodyFragmentProcessor.getProcessorFor(uri, localName, qName);
        }
        return p;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {

        TranslaterContext ctx = getTranslaterContext();
        CodeWriter out = ctx.getCodeWriter();

        this.tagInfo = ctx.getTagLibraryRegistry().getTagLibraryInfo(uri).getTagInfo(localName);
        this.attributeInfos = toMap(tagInfo.getTagAttributeInfos());
        String tagImplClass = tagInfo.getClassName();
        this.localVarName = ctx.getVariableForTag(tagImplClass);

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
        // TODO: non-whitespace characters only allowed when BODY_TYPE = fragment
        // For non-ws, set BODY_TYPE = fragment if possible, otherwise throw exception.
        if (null == sb) {
            sb = new StringBuffer();
        }
        sb.append(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (BODY_TYPE_FRAGMENT == bodyType || BODY_TYPE_EMPTY == bodyType) {
            // end element
            if (null != sb && ! sb.toString().trim().equals("")) {
                if (! bodyFragmentProcessor.isFragmentStarted()) {
                    bodyFragmentProcessor.startFragment();
                    bodyType = BODY_TYPE_FRAGMENT;
                }
                char[] chars = sb.toString().toCharArray();
                bodyFragmentProcessor.characters(chars, 0, chars.length);
                sb = null;
            }
        }

        if (BODY_TYPE_FRAGMENT == bodyType && bodyFragmentProcessor.isFragmentStarted()) {
            bodyFragmentProcessor.endFragment();
        }

        if (bodyFragmentProcessor.isFragmentExists()) {
            String bodyFragmentVar = bodyFragmentProcessor.getFragmentVar();
            CodeWriter out = getTranslaterContext().getCodeWriter();
            out.printIndent().println(localVarName + ".setXpBody(" + bodyFragmentVar + ");");
            out.printIndent().println(bodyFragmentVar + " = null;");
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
