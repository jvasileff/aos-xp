package org.anodyneos.xpImpl.tld;

import org.anodyneos.commons.xml.sax.BooleanProcessor;
import org.anodyneos.commons.xml.sax.CDATAProcessor;
import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.xp.tagext.TagAttributeInfo;
import org.xml.sax.SAXException;

class ProcessorAttribute extends TLDProcessor {

    private TagAttributeInfo attributeInfo;

    private CDATAProcessor nameProcessor;
    private CDATAProcessor descriptionProcessor;
    private CDATAProcessor typeProcessor;
    private BooleanProcessor requiredProcessor;
    private BooleanProcessor requestTimeOKProcessor;
    private BooleanProcessor fragmentProcessor;

    public static final String E_NAME = "name";
    public static final String E_DESCRIPTION = "description";
    public static final String E_TYPE = "type";
    public static final String E_REQUIRED = "required";
    public static final String E_REQUEST_TIME_OK = "request-time-ok";
    public static final String E_FRAGMENT = "fragment";

    public ProcessorAttribute(TLDContext ctx) {
        super(ctx);
        nameProcessor = new CDATAProcessor(ctx);
        descriptionProcessor = new CDATAProcessor(ctx);
        typeProcessor = new CDATAProcessor(ctx);
        requiredProcessor = new BooleanProcessor(ctx);
        requestTimeOKProcessor = new BooleanProcessor(ctx);
        fragmentProcessor = new BooleanProcessor(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName)
            throws SAXException {
        if (E_NAME.equals(localName)) {
            return nameProcessor;
        } else if (E_DESCRIPTION.equals(localName)) {
            return descriptionProcessor;
        } else if (E_TYPE.equals(localName)) {
            return typeProcessor;
        } else if (E_REQUIRED.equals(localName)) {
            return requiredProcessor;
        } else if (E_REQUEST_TIME_OK.equals(localName)) {
            return requestTimeOKProcessor;
        } else if (E_FRAGMENT.equals(localName)) {
            return fragmentProcessor;
        } else {
            return super.getProcessorFor(uri, localName, qName);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        String type = typeProcessor.getCDATA();
        type = (null == type || "".equals(type)) ? "java.lang.String" : type;
        boolean required = requiredProcessor.getBoolean() == null ? false : requiredProcessor
                .getBoolean().booleanValue();
        boolean rtok = requestTimeOKProcessor.getBoolean() == null ? false : requestTimeOKProcessor
                .getBoolean().booleanValue();
        boolean isFragment = fragmentProcessor.getBoolean() == null ? false : fragmentProcessor
                .getBoolean().booleanValue();

        if (isFragment && !"org.anodyneos.xp.tagext.XpFragment".equals(type)) {
            throw new SAXException(
                "Attribute for fragment must be type org.anodyneos.xp.tagext.XpFragment");
        }

        attributeInfo = new TagAttributeInfo(
                nameProcessor.getCDATA(),
                descriptionProcessor.getCDATA(),
                type, required, rtok, isFragment);

        nameProcessor = null;
        descriptionProcessor = null;
        typeProcessor = null;
        requiredProcessor = null;
        requestTimeOKProcessor = null;
    }

    public TagAttributeInfo getTagAttributeInfo() {
        return attributeInfo;
    }

}
