package org.anodyneos.xpImpl.translater;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.xp.XpOutputKeys;
import org.anodyneos.xpImpl.util.CodeWriter;
import org.anodyneos.xpImpl.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
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
        out.printIndent().println("public final void service(org.anodyneos.xp.XpContext xpContext, org.anodyneos.xp.XpOutput xpOut) throws org.anodyneos.xp.XpException, javax.servlet.jsp.el.ELException, org.xml.sax.SAXException {");
        out.indentPlus();
        out.printIndent().println("javax.servlet.jsp.el.ExpressionEvaluator elEvaluator = xpContext.getExpressionEvaluator();");
        out.printIndent().println("javax.servlet.jsp.el.VariableResolver varResolver = xpContext.getVariableResolver();");
        out.printIndent().println("org.anodyneos.xp.XpContentHandler xpCH = xpOut.getXpContentHandler();");
        out.println();

        // the xp document already has some namespace mappings.  Lets output them right before the root element.
        // skip prefixes included in excludeResultPrefixes output property.

        // TODO: bug... if an xp page outputs an element or attribute with a prefix that is in excludeResultPrefixes,
        // that prefix must be added at the time of it's use.  This need to be fixed at either translation time or
        // runtime.

        // TODO: bug... Xalan checks the excluded result prefixes against namespace mappings to make sure they exist; we
        // should do the same.

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

        NamespaceSupport ns = getTranslaterContext().getNamespaceSupport();
        Enumeration e = ns.getPrefixes();
        while (e.hasMoreElements()) {
            String nsPrefix = (String) e.nextElement();
            String nsURI = ns.getURI(nsPrefix);
            if (! excludedPrefixes.contains(nsPrefix)) {
                out.printIndent().println(
                        "xpCH.startPrefixMapping("
                    +        Util.escapeStringQuoted(nsPrefix)
                    + ", " + Util.escapeStringQuoted(nsURI)
                    + ");");
            }
        }

        // also handle default prefix...
        String defaultURI = getTranslaterContext().getNamespaceSupport().getURI("");
        if (null != defaultURI) {
            out.printIndent().println(
                    "xpCH.startPrefixMapping("
                +        Util.escapeStringQuoted("")
                + ", " + Util.escapeStringQuoted(defaultURI)
                + ");");
        }
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
