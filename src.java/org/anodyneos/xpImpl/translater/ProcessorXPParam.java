package org.anodyneos.xpImpl.translater;

import java.util.Map;

import org.anodyneos.xp.tagext.TagAttributeInfo;
import org.anodyneos.xpImpl.util.CodeWriter;
import org.anodyneos.xpImpl.util.CoerceUtil;
import org.anodyneos.xpImpl.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ProcessorXPParam extends ProcessorForType {

    private String tagVar;
    private String paramName;
    private Map attributeInfos;

    public static final String A_NAME = "name";

    public ProcessorXPParam (TranslaterContext ctx, String type, Map attributeInfos, String tagVar) throws SAXException {
        super(ctx, type);
        this.attributeInfos = attributeInfos;
        this.tagVar = tagVar;
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {

        super.startElement(uri, localName, qName, attributes);

        paramName = attributes.getValue(A_NAME);
        if (null == paramName || "".equals(paramName)) {
            throw new SAXException("xp:param requires attribute @name.");
        }

        TagAttributeInfo attrInfo = (TagAttributeInfo) attributeInfos.get(paramName);
        if (null == attrInfo) {
            throw new SAXException("attribute '" + paramName + "' not allowed in tag " + qName);
        }
        String type = attrInfo.getType();
        type = CoerceUtil.simplifyType(type);
        setType(type);
    }

    public void process(String xpOutVar, String expr) throws SAXException {
        CodeWriter out = getTranslaterContext().getCodeWriter();
        out.printIndent().println(tagVar + "." + Util.toSetMethod(paramName) + "(" + expr + ");");
    }

}
