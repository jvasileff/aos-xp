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
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName)
            throws SAXException {
        // TODO: consider xp:tagAttribute to allow subelements to provide values for tag attributes.
        ElementProcessor p = bodyFragmentProcessor.getProcessorFor(uri, localName, qName);
        // since no exception, start fragment if not already started. Dump characters.
        // Return p (p may be the bodyFragmentProcessor itself if the content is result
        // content, but bodyFragmentProcessor made the decision.)
        if (! bodyFragmentStarted) {
            bodyFragmentId = getTranslaterContext().startFragment();
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
            out.printIndent().println("// " + localVarName + ".setParent(null);");
        }
        out.printIndent().println(localVarName + ".setXpContext(xpContext);");

        // add attributes
        for (int i = 0; i < attributes.getLength(); i++) {
            String name = attributes.getLocalName(i).trim();
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
                bodyFragmentId = getTranslaterContext().startFragment();
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
            getTranslaterContext().endFragment();
            CodeWriter out = getTranslaterContext().getCodeWriter();
            out.printIndent().println(
                    localVarName + ".setXpBody(new FragmentHelper(" + bodyFragmentId + ", xpContext, " + localVarName + "));");
        }

        CodeWriter out = getTranslaterContext().getCodeWriter();
        out.printIndent().println(localVarName + ".doTag(xpCH);");
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


}
