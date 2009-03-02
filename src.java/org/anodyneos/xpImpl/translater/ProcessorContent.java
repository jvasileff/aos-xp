package org.anodyneos.xpImpl.translater;

import java.util.Enumeration;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.xp.XpOutputKeys;
import org.anodyneos.xpImpl.util.CodeWriter;
import org.anodyneos.xpImpl.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.NamespaceSupport;

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
    private int phantomPrefixCount = 0;
    private boolean isTextOutput = false;

    private ProcessorResultContent resultContentProcessor;

    public ProcessorContent(TranslaterContext ctx) throws SAXException {
        super(ctx);
        resultContentProcessor = new ProcessorResultContent(ctx);
        if (null != ctx.getOutputProperties() && "text".equals(ctx.getOutputProperties().get(XpOutputKeys.METHOD))) {
            isTextOutput = true;
        }
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName)
            throws SAXException {
        return resultContentProcessor.getProcessorFor(uri, localName, qName);
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        CodeWriter out = getTranslaterContext().getCodeWriter();
        out.printIndent().println("public final void service(org.anodyneos.xp.XpContext xpContext, org.anodyneos.xp.XpOutput xpOut) throws org.anodyneos.xp.XpException, javax.servlet.jsp.el.ELException, org.xml.sax.SAXException {");
        out.indentPlus();
        out.printIndent().println("javax.servlet.jsp.el.ExpressionEvaluator elEvaluator = xpContext.getExpressionEvaluator();");
        out.printIndent().println("javax.servlet.jsp.el.VariableResolver varResolver = xpContext.getVariableResolver();");
        out.printIndent().println("org.anodyneos.xp.XpContentHandler xpCH = xpOut.getXpContentHandler();");
        out.println();

        // the xp document already has some namespace mappings.  Lets output them right before the root element.
        // skip prefixes included in excludeResultPrefixes output property.

        /* excludePrefixes will be going into XpContentHandler

        Set excludedPrefixes = new HashSet();
        Properties outputProperties = getTranslaterContext().getOutputProperties();
        if (null != outputProperties) {
            String s = (String) outputProperties.get(XpOutputKeys.EXCLUDE_RESULT_PREFIXES);
            if (null != s) {
                StringTokenizer st = new StringTokenizer(s);
                while (st.hasMoreTokens()) {
                    String tok = st.nextToken();
                    tok = tok.trim();
                    if (tok.length() > 0) {
                        excludedPrefixes.add(tok);
                    }
               }
            }
        }
        */

        // TODO: Technically, we should be using pushPhantomPrefixMapping here, but we need to either avoid doing
        // so or make XpContentHandler more efficient so it doesn't have to keep these around.
        NamespaceSupport ns = getTranslaterContext().getNamespaceSupport();
        Enumeration e = ns.getPrefixes();
        while (e.hasMoreElements()) {
            String nsPrefix = (String) e.nextElement();
            String nsURI = ns.getURI(nsPrefix);
            out.printIndent().println(
                    "xpCH.pushPhantomPrefixMapping("
                +        Util.escapeStringQuoted(nsPrefix)
                + ", " + Util.escapeStringQuoted(nsURI)
                + ");");
            phantomPrefixCount++;
        }

        // also handle default prefix...
        String defaultURI = getTranslaterContext().getNamespaceSupport().getURI("");
        if (null != defaultURI) {
            out.printIndent().println(
                    "xpCH.pushPhantomPrefixMapping("
                +        Util.escapeStringQuoted("")
                + ", " + Util.escapeStringQuoted(defaultURI)
                + ");");
            phantomPrefixCount++;
        }

        // Support method="text" where an outer element may not exist in the xp page.
        if (isTextOutput) {
            resultContentProcessor.startElement("", "dummy", "dummy", new AttributesImpl());
        }
    }

    public void characters(char[] ch, int start, int length) {
        // Support method="text" where an outer element may not exist in the xp page.
        if (isTextOutput) {
            resultContentProcessor.characters(ch, start, length);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        // Support method="text" where an outer element may not exist in the xp page.
        if (isTextOutput) {
            resultContentProcessor.endElement("", "dummy", "dummy");
        }

        CodeWriter out = getTranslaterContext().getCodeWriter();

        // close out phantom prefixes
        for (int i=0; i < phantomPrefixCount; i++) {
            out.printIndent().println("xpCH.popPhantomPrefixMapping();");
        }

        // end method block
        out.endBlock();
        out.println();
    }
}
