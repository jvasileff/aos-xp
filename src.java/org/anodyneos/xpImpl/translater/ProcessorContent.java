package org.anodyneos.xpImpl.translater;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.xpImpl.util.CodeWriter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * ProcessorContent handles the xp:content element.
 *
 * <pre>
 *  &lt;xp:content&gt;
 *      Mixed content consisting of character data, output elements, and tagextensions.
 *      Future support for native handling of standard xp tags will exist.
 *  &lt;/xp:content&gt;
 * </pre>
 *
 * This class outputs the java method signature for the main content of the xp:page.
 * The body of the method will be handled by ProcessorResultContent.
 *
 * @author jvas
 */
public class ProcessorContent extends TranslaterProcessor {

    private StringBuffer sb;

    private ProcessorResultContent resultContentProcessor;

    public ProcessorContent(TranslaterContext ctx) {
        super(ctx);
        resultContentProcessor = new ProcessorResultContent(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName)
            throws SAXException {
        return resultContentProcessor.getProcessorFor(uri, localName, qName);
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        CodeWriter out = getTranslaterContext().getCodeWriter();
        out.printIndent().println("public final void _xpService (org.anodyneos.xp.standalone.StandaloneXpContext xpContext, org.anodyneos.xp.XpContentHandler xpCH) throws org.anodyneos.xp.XpException, javax.servlet.jsp.el.ELException, org.xml.sax.SAXException {");
        out.indentPlus();
        out.printIndent().println("javax.servlet.jsp.el.ExpressionEvaluator elEvaluator = xpContext.getExpressionEvaluator();");
        out.printIndent().println("javax.servlet.jsp.el.VariableResolver varResolver = xpContext.getVariableResolver();");
        out.println();
    }

    public void characters(char[] ch, int start, int length) {
        // @TODO: We don't take raw characters, do we?  For XML, this would be an error (no root element).
        // But if we want to support text output we need to.
    }

    public void endElement(String uri, String localName, String qName) {
        // end method block
        CodeWriter out = getTranslaterContext().getCodeWriter();
        out.endBlock();
        out.println();
    }
}
