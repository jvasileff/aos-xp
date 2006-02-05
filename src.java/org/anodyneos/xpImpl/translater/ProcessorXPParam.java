package org.anodyneos.xpImpl.translater;


import org.anodyneos.xpImpl.util.CodeWriter;
import org.anodyneos.xpImpl.util.Util;
import org.xml.sax.SAXException;

public class ProcessorXPParam extends ProcessorForType {

    private String tagVar;
    private String paramName;

    public static final String A_NAME = "name";

    public ProcessorXPParam (TranslaterContext ctx, String type, String paramName, String tagVar) throws SAXException {
        super(ctx, type);
        this.paramName = paramName;
        this.tagVar = tagVar;
    }

    public void process(String xpOutVar, String expr) throws SAXException {
        CodeWriter out = getTranslaterContext().getCodeWriter();
        out.printIndent().println(tagVar + "." + Util.toSetMethod(paramName) + "(" + expr + ");");
    }

}
