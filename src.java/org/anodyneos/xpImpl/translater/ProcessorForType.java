package org.anodyneos.xpImpl.translater;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.xpImpl.util.CodeWriter;
import org.anodyneos.xpImpl.util.CoerceUtil;
import org.anodyneos.xpImpl.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This translater supports two modes:
 *
 * 2. Optimized mode if body contains only text.
 *
 * 3. If body contains output elements and/or tags, processes the content using <code>ProcessorResultContent</code> to
 * allow for runtime branching, etc.
 */
public abstract class ProcessorForType extends TranslaterProcessorNonResultContent {

    ProcessorResultContent processorResultContent;
    private boolean optimizedMode = true;
    private String savedXPOutVariable;
    private StringBuffer sb;

    private String type;

    private Attributes attributes;

    //TODO: whitespace handling for type = String
    private boolean trim = true;

    public ProcessorForType(TranslaterContext ctx, String type) throws SAXException {
        super(ctx);
        setType(type);
    }

    public void setType(String type) throws SAXException {
        this.type = CoerceUtil.simplifyType(type);
        if (! ("String".equals(this.type) || "Object".equals(this.type)
                ||  CoerceUtil.isNativeType(this.type) || CoerceUtil.isBoxClass(this.type))) {
            throw new SAXException("Invalid type: " + type);
        }
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName) throws SAXException {
        // new element is coming, so we cannot run in optimized mode.
        if (optimizedMode) {
            // switch to non-optimized mode
            optimizedMode = false;
            CodeWriter out = getTranslaterContext().getCodeWriter();
            savedXPOutVariable = getTranslaterContext().getVariableForSavedXPOut();
            out.printIndent().println( "org.anodyneos.xp.XpOutput " + savedXPOutVariable + " = xpOut;");
            out.printIndent().println( "xpOut = new org.anodyneos.xp.XpOutput(new org.anodyneos.xp.util.TextContentHandler(), xpCH);" );
            out.printIndent().println( "xpCH = xpOut.getXpContentHandler();");

            processorResultContent = new ProcessorResultContent(getTranslaterContext());
            if (null != sb) {
                processorResultContent.characters(sb.toString().toCharArray(), 0, sb.length());
                sb = null;
            }
        }
        return processorResultContent.getProcessorFor(uri, localName, qName);
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {

        this.attributes = new AttributesImpl(attributes);
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (optimizedMode) {
            if (null == sb) {
                sb = new StringBuffer();
            }
            sb.append(ch, start, length);
        } else {
            processorResultContent.characters(ch, start, length);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        CodeWriter out = getTranslaterContext().getCodeWriter();

        if (optimizedMode) {
            StringBuffer expr = new StringBuffer();
            String value = "";
            if (sb != null) {
                value = sb.toString();
            }
            expr.append(Util.elExpressionCode(value, type));
            sb = null;
            value = null;
            process("xpCH", expr.toString());
        } else {
            StringBuffer expr = new StringBuffer();

            processorResultContent.flushCharacters();
            expr.append("((org.anodyneos.xp.util.TextContentHandler) xpCH.getWrappedContentHandler()).getText()");

            if (CoerceUtil.isNativeType(type)) {
                String type2 = type.substring(0,1).toUpperCase() + type.substring(1);
                expr.insert(0, "org.anodyneos.xp.util.XpCoerce.coerceTo" + type2 + "Type(");
                expr.append(")");
            } else if (CoerceUtil.isBoxClass(type)) {
                expr.insert(0, "org.anodyneos.xp.util.XpCoerce.coerceTo" + type + "(");
                expr.append(")");
            }
            String dataVariable = getTranslaterContext().getVariableForData();

            process(savedXPOutVariable, expr.toString());

            out.printIndent().println( "xpOut = " + savedXPOutVariable + ";");
            out.printIndent().println( "xpCH = xpOut.getXpContentHandler();");
            out.printIndent().println( savedXPOutVariable + " = null;");
        }
    }

    public abstract void process(String savedXPOutVariable, String expr) throws SAXException;
}
