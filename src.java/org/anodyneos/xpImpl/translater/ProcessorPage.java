package org.anodyneos.xpImpl.translater;

import java.util.Enumeration;
import java.util.List;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.xpImpl.util.CodeWriter;
import org.anodyneos.xpImpl.util.JavaClass;
import org.anodyneos.xpImpl.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
/**
 * ProcessorPage handles the xp:page element.
 *
 * <pre>
 *  <xp:page>
 *      <xp:output/>
 *      <xp:xsl/>...
 *      <xp:content/>
 *  </xp:page>
 * </pre>
 *
 * @author jvas
 */
class ProcessorPage extends TranslaterProcessor {

    private JavaClass jc = new JavaClass();

    //private ArrayList xmlPiplineProcessors = new ArrayList();
    private ProcessorOutput outputProcessor;
    private ProcessorContent contentProcessor;

    //static final String E_XML_PIPELINE = "xml-pipeline";
    public static final String E_OUTPUT = "output";
    public static final String E_CONTENT = "content";

    public ProcessorPage(TranslaterContext ctx) {
        super(ctx);
        outputProcessor = new ProcessorOutput(ctx);
        contentProcessor = new ProcessorContent(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName) throws SAXException {
        if (URI_XP.equals(uri)) {
            if (E_CONTENT.equals(localName)) {
                return contentProcessor;
            } else if (E_OUTPUT.equals(localName)) {
                return outputProcessor;
            } else {
                return super.getProcessorFor(uri, localName, qName);
            }
        } else {
            return super.getProcessorFor(uri, localName, qName);
        }
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        printJavaHeader();
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        printJavaFooter();
    }

    private void printJavaHeader() {
        // package, imports, class header
        JavaClass c = new JavaClass();
        c.setFullClassName(getTranslaterContext().getFullClassName());
        c.addInterface("org.anodyneos.xp.XpPage");

        CodeWriter out = getTranslaterContext().getCodeWriter();
        c.printHeader(out);

        // output properties
        out.printIndent().println("private static final java.util.Properties defaultProperties = new java.util.Properties();");
        out.println();
        out.printIndent().println("static {");
        out.indentPlus();

        /*
            TODO: should these properties have defaults?  It is probably better for them not to since the serializer (XSL) has its own defaults.

        out.printIndent().println("defaultProperties.setProperty(\"cdataSectionElements\", \"\");");
        out.printIndent().println("defaultProperties.setProperty(\"doctypePublic\", \"\");");
        out.printIndent().println("defaultProperties.setProperty(\"doctypeSystem\", \"\");");
        out.printIndent().println("defaultProperties.setProperty(\"encoding\", \"\");");
        out.printIndent().println("defaultProperties.setProperty(\"indent\", \"no\");");
        out.printIndent().println("defaultProperties.setProperty(\"mediaType\", \"text/xml\");");
        out.printIndent().println("defaultProperties.setProperty(\"method\", \"xml\");");
        out.printIndent().println("defaultProperties.setProperty(\"omitXmlDeclaration\", \"no\");");
        out.printIndent().println("defaultProperties.setProperty(\"xhtmlCompat\", \"\");");
        out.printIndent().println("defaultProperties.setProperty(\"xsltURI\", \"\");");
        */
        out.endBlock();

        out.println();
        out.printIndent().println("private java.util.Properties outputProperties = new java.util.Properties(defaultProperties);");
        out.println();

        // constructor()
        out.printIndent().println("public " + c.getClassName() + "() {");
        out.indentPlus();
        out.printIndent().println("initOutputProperties();");
        out.endBlock();
        out.println();

        // main()
        out.printIndent().println("public static void main(String[] args) throws Exception {");
        out.indentPlus();
        out.printIndent().println("long start;");
        out.printIndent().println("start = System.currentTimeMillis();");
        out.printIndent().println(c.getFullClassName() + " obj = new " + c.getFullClassName() + "();");
        out.printIndent().println("org.anodyneos.xp.standalone.StandaloneXpContext xpContext = org.anodyneos.xp.standalone.StandaloneXpFactory.getDefaultFactory().getStandaloneXpContext();");
        out.printIndent().println("org.anodyneos.xp.util.XMLStreamer.process(new org.anodyneos.xp.XpXMLReader(obj, xpContext), System.out);");
        out.printIndent().println("System.out.println(\"Completed in \" + (System.currentTimeMillis() - start) + \" milliseconds\");");
        out.endBlock();
        out.println();
    }

    private void printJavaFooter() {
        CodeWriter out = getTranslaterContext().getCodeWriter();

        // output properties
        out.printIndent().println("private void initOutputProperties() {");
        out.indentPlus();
        if (null != getTranslaterContext().getOutputProperties()) {
            Enumeration keys = getTranslaterContext().getOutputProperties().keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                String value = (String) outputProcessor.getTranslaterContext().getOutputProperties().get(key);
                out.printIndent().println(
                          "outputProperties.setProperty("
                        + Util.escapeStringQuoted(key)
                        + ", "
                        + Util.escapeStringQuoted(value)
                        + ");");
            }
        }
        out.endBlock();
        out.println();

        // implementing methods of XpPage interface
        out.printIndent().println("public java.util.Properties getOutputProperties() {");
        out.indentPlus();
        out.printIndent().println("return new java.util.Properties(outputProperties);");
        out.endBlock();
        out.println();

        out.printIndent().println("public static java.util.List getDependents(){");
        out.indentPlus();
        out.printIndent().println("java.util.List dependents = new java.util.ArrayList();");
        List dependents = getTranslaterContext().getDependents();
        for (int i = 0; i < dependents.size(); i++){
            out.printIndent().println("dependents.add(\"" + dependents.get(i) + "\");");
        }
        out.printIndent().println("return dependents;");
        out.endBlock();

        out.println();
        out.printIndent().println("private final long loadTime = System.currentTimeMillis();");
        out.printIndent().println("public long getLoadTime(){");
        out.indentPlus();
        out.printIndent().println("return loadTime;");
        out.endBlock();

        if(getTranslaterContext().getFragmentCount() > 0) {
            // Output Fragments
            out.flush();
            out.printIndent().println("private final class FragmentHelper extends org.anodyneos.xp.tagext.XpFragment {");
            out.indentPlus();
            out.println();
            out.printIndent().println("private int fragNum;");
            out.printIndent().println("private org.anodyneos.xp.XpContext xpContext;");
            out.printIndent().println("private org.anodyneos.xp.tagext.XpTag xpTagParent;");
            out.println();
            out.printIndent().println("public FragmentHelper(int fragNum, org.anodyneos.xp.XpContext xpContext, org.anodyneos.xp.tagext.XpTag xpTagParent) {");
            out.indentPlus();
            out.printIndent().println("this.fragNum = fragNum;");
            out.printIndent().println("this.xpContext = xpContext;");
            out.printIndent().println("this.xpTagParent = xpTagParent;");
            out.endBlock();
            out.println();
            out.printIndent().println("public org.anodyneos.xp.XpContext getXpContext() {");
            out.indentPlus();
            out.printIndent().println("return xpContext;");
            out.endBlock();
            out.println();
            out.printIndent().println("public void invoke(org.anodyneos.xp.XpContentHandler out) throws org.anodyneos.xp.XpException, javax.servlet.jsp.el.ELException, org.xml.sax.SAXException {");
            out.indentPlus();
            out.printIndent().println("switch(this.fragNum) {");
            out.indentPlus();
            for (int i = 0; i < getTranslaterContext().getFragmentCount(); i++) {
                out.printIndent().println("case " + i + ":");
                out.indentPlus();
                out.printIndent().println("invoke" + i + "(out);");
                out.printIndent().println("break;");
                out.indentMinus();
            }
            out.endBlock();
            out.endBlock();
            out.println();

            for (int i = 0; i < getTranslaterContext().getFragmentCount(); i++) {
                out.printIndent().println("public void invoke" + i + "(org.anodyneos.xp.XpContentHandler xpCH) throws org.anodyneos.xp.XpException, javax.servlet.jsp.el.ELException, org.xml.sax.SAXException  {");
                out.indentPlus();
                //out.printIndent().println("org.anodyneos.xp.XpContentHandler xpCH = xpContext.getXpContentHandler();");
                out.printIndent().println("javax.servlet.jsp.el.ExpressionEvaluator elEvaluator = xpContext.getExpressionEvaluator();");
                out.printIndent().println("javax.servlet.jsp.el.VariableResolver varResolver = xpContext.getVariableResolver();");
                out.println();
                out.print(getTranslaterContext().getFragment(i));
                out.endBlock();
                out.println();
            }
            out.println();
            out.endBlock();
            out.println();
        }

        // end of class definition
        out.println();
        out.endBlock();
    }
}
