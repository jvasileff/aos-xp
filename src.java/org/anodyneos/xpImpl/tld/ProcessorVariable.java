package org.anodyneos.xpImpl.tld;

import org.anodyneos.commons.xml.sax.CDATAProcessor;
import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.xp.tagext.TagVariableInfo;
import org.xml.sax.SAXException;

class ProcessorVariable extends TLDProcessor {

    private TagVariableInfo variableInfo;

    private CDATAProcessor descriptionProcessor;
    private CDATAProcessor nameFromAttributeProcessor;
    private CDATAProcessor aliasProcessor;
    private CDATAProcessor scopeProcessor;

    public static final String E_DESCRIPTION = "description";
    public static final String E_NAME_FROM_ATTRIBUTE = "name-from-attribute";
    public static final String E_ALIAS = "alias";
    public static final String E_SCOPE = "scope";

    public static final String S_NESTED = "NESTED";
    public static final String S_AT_BEGIN = "AT_BEGIN";
    public static final String S_AT_END = "AT_END";

    public ProcessorVariable(TLDContext ctx) {
        super(ctx);
        descriptionProcessor = new CDATAProcessor(ctx);
        nameFromAttributeProcessor = new CDATAProcessor(ctx);
        aliasProcessor = new CDATAProcessor(ctx);
        scopeProcessor = new CDATAProcessor(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName) throws SAXException {
        if (E_DESCRIPTION.equals(localName)) {
            return descriptionProcessor;
        } else if(E_NAME_FROM_ATTRIBUTE.equals(localName)) {
            return nameFromAttributeProcessor;
        } else if (E_ALIAS.equals(localName)) {
            return aliasProcessor;
        } else if(E_SCOPE.equals(localName)) {
            return scopeProcessor;
        } else {
            return super.getProcessorFor(uri, localName, qName);
        }
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        int iScope;

        String sScope = null == scopeProcessor.getCDATA() ? null : scopeProcessor.getCDATA().trim();
        if (S_AT_BEGIN.equals(sScope)) {
            iScope = TagVariableInfo.SCOPE_AT_BEGIN;
        } else if (S_AT_END.equals(sScope)) {
            iScope = TagVariableInfo.SCOPE_AT_END;
        } else if (S_NESTED.equals(sScope)) {
            iScope = TagVariableInfo.SCOPE_NESTED;
        } else {
            iScope = TagVariableInfo.SCOPE_NESTED;
        }

        variableInfo = new TagVariableInfo(
                descriptionProcessor.getCDATA(),
                nameFromAttributeProcessor.getCDATA(),
                aliasProcessor.getCDATA(),
                iScope);

        descriptionProcessor = null;
        nameFromAttributeProcessor = null;
        aliasProcessor = null;
        scopeProcessor = null;
    }

    public TagVariableInfo getTagVariableInfo() {
        return variableInfo;
    }

}
