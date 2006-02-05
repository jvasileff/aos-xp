public class Page implements org.anodyneos.xp.XpPage {

    private static final java.util.Properties defaultProperties = new java.util.Properties();

    static {
    }

    private java.util.Properties outputProperties = new java.util.Properties(defaultProperties);


    private static org.anodyneos.xpImpl.runtime.XpFunctionResolver fResolver = new org.anodyneos.xpImpl.runtime.XpFunctionResolver();
    static {
        fResolver.mapFunctionWithURI("http://www.anodyneos.org/xmlns/xp/function", "indexOf", org.apache.taglibs.standard.functions.Functions.class, "indexOf", new Class[] {java.lang.String.class, java.lang.String.class});
        fResolver.mapFunctionWithURI("http://www.anodyneos.org/xmlns/xp/function", "toUpperCase", org.apache.taglibs.standard.functions.Functions.class, "toUpperCase", new Class[] {java.lang.String.class});
        fResolver.mapFunctionWithURI("http://www.anodyneos.org/xmlns/xp/function", "contains", org.apache.taglibs.standard.functions.Functions.class, "contains", new Class[] {java.lang.String.class, java.lang.String.class});
        fResolver.mapFunctionWithURI("http://www.anodyneos.org/xmlns/xp/function", "trim", org.apache.taglibs.standard.functions.Functions.class, "trim", new Class[] {java.lang.String.class});
        fResolver.mapFunctionWithURI("http://www.anodyneos.org/xmlns/xp/function", "substring", org.apache.taglibs.standard.functions.Functions.class, "substring", new Class[] {java.lang.String.class, int.class, int.class});
        fResolver.mapFunctionWithURI("http://www.anodyneos.org/xmlns/xp/function", "length", org.apache.taglibs.standard.functions.Functions.class, "length", new Class[] {java.lang.Object.class});
        fResolver.mapFunctionWithURI("http://www.anodyneos.org/xmlns/xp/function", "substringBefore", org.apache.taglibs.standard.functions.Functions.class, "substringBefore", new Class[] {java.lang.String.class, java.lang.String.class});
        fResolver.mapFunctionWithURI("http://www.anodyneos.org/xmlns/xp/function", "split", org.apache.taglibs.standard.functions.Functions.class, "split", new Class[] {java.lang.String.class, java.lang.String.class});
        fResolver.mapFunctionWithURI("http://www.anodyneos.org/xmlns/xp/function", "endsWith", org.apache.taglibs.standard.functions.Functions.class, "endsWith", new Class[] {java.lang.String.class, java.lang.String.class});
        fResolver.mapFunctionWithURI("http://www.anodyneos.org/xmlns/xp/function", "substringAfter", org.apache.taglibs.standard.functions.Functions.class, "substringAfter", new Class[] {java.lang.String.class, java.lang.String.class});
        fResolver.mapFunctionWithURI("http://www.anodyneos.org/xmlns/xp/function", "escapeXml", org.apache.taglibs.standard.functions.Functions.class, "escapeXml", new Class[] {java.lang.String.class});
        fResolver.mapFunctionWithURI("http://www.anodyneos.org/xmlns/xp/function", "toLowerCase", org.apache.taglibs.standard.functions.Functions.class, "toLowerCase", new Class[] {java.lang.String.class});
        fResolver.mapFunctionWithURI("http://www.anodyneos.org/xmlns/xp/function", "containsIgnoreCase", org.apache.taglibs.standard.functions.Functions.class, "containsIgnoreCase", new Class[] {java.lang.String.class, java.lang.String.class});
        fResolver.mapFunctionWithURI("http://www.anodyneos.org/xmlns/xp/function", "startsWith", org.apache.taglibs.standard.functions.Functions.class, "startsWith", new Class[] {java.lang.String.class, java.lang.String.class});
        fResolver.mapFunctionWithURI("http://www.anodyneos.org/xmlns/xp/function", "join", org.apache.taglibs.standard.functions.Functions.class, "join", new Class[] {java.lang.reflect.Array.newInstance(java.lang.String.class,0).getClass(), java.lang.String.class});
        fResolver.mapFunctionWithURI("http://www.anodyneos.org/xmlns/xp/function", "replace", org.apache.taglibs.standard.functions.Functions.class, "replace", new Class[] {java.lang.String.class, java.lang.String.class, java.lang.String.class});
    }

    public Page() {
        initOutputProperties();
    }

    public static void main(String[] args) throws Exception {
        long start;
        start = System.currentTimeMillis();
        Page obj = new Page();
        org.anodyneos.xp.standalone.StandaloneXpContext xpContext = org.anodyneos.xp.standalone.StandaloneXpFactory.getDefaultFactory().getStandaloneXpContext();
        org.anodyneos.xp.util.XMLStreamer.process(new org.anodyneos.xp.XpXMLReader(obj, xpContext), System.out);
        System.out.println("Completed in " + (System.currentTimeMillis() - start) + " milliseconds");
    }

    public final void service(org.anodyneos.xp.XpContext xpContext, org.anodyneos.xp.XpOutput xpOut) throws org.anodyneos.xp.XpException, javax.servlet.jsp.el.ELException, org.xml.sax.SAXException {
        javax.servlet.jsp.el.ExpressionEvaluator elEvaluator = xpContext.getExpressionEvaluator();
        javax.servlet.jsp.el.VariableResolver varResolver = xpContext.getVariableResolver();
        org.anodyneos.xp.XpContentHandler xpCH = xpOut.getXpContentHandler();

        xpCH.pushPhantomPrefixMapping("fmt", "http://www.anodyneos.org/xmlns/xp/fmt");
        xpCH.pushPhantomPrefixMapping("xml", "http://www.w3.org/XML/1998/namespace");
        xpCH.pushPhantomPrefixMapping("fn", "http://www.anodyneos.org/xmlns/xp/function");
        xpCH.pushPhantomPrefixMapping("xp", "http://www.anodyneos.org/xmlns/xp");
        xpCH.pushPhantomPrefixMapping("c", "http://www.anodyneos.org/xmlns/xp/core");
        xpCH.pushPhantomPrefixMapping("", "http://xhtml.com");
        xpCH.startElement("http://xhtml.com", "html", "html", null);
        org.anodyneos.xp.tag.core.IfTag tag0 = new org.anodyneos.xp.tag.core.IfTag();
        // tag0.setParent(null); // no parent tag
        tag0.setXpContext(xpContext);
        tag0.setTest(((Boolean) elEvaluator.evaluate("${true}", Boolean.class, varResolver, fResolver.getFunctionMapper(xpCH))).booleanValue());
        tag0.setXpBody(new FragmentHelper(0, xpContext, tag0, xpCH));
        tag0.doTag(xpOut);
        tag0 = null;
        org.anodyneos.xp.tag.core.IfTag tag1 = new org.anodyneos.xp.tag.core.IfTag();
        // tag1.setParent(null); // no parent tag
        tag1.setXpContext(xpContext);
        tag1.setTest(((Boolean) elEvaluator.evaluate("${true}", Boolean.class, varResolver, fResolver.getFunctionMapper(xpCH))).booleanValue());
        tag1.setXpBody(new FragmentHelper(1, xpContext, tag1, xpCH));
        tag1.doTag(xpOut);
        tag1 = null;
        org.anodyneos.xp.tag.core.IfTag tag2 = new org.anodyneos.xp.tag.core.IfTag();
        // tag2.setParent(null); // no parent tag
        tag2.setXpContext(xpContext);
        tag2.setTest(((Boolean) elEvaluator.evaluate("${true}", Boolean.class, varResolver, fResolver.getFunctionMapper(xpCH))).booleanValue());
        tag2.setXpBody(new FragmentHelper(2, xpContext, tag2, xpCH));
        tag2.doTag(xpOut);
        tag2 = null;
        xpCH.startElement("http://xhtml.com", "trimTest", "trimTest", null);
        xpCH.addAttribute(null,"notrim","  \nabcd  ");
        xpCH.addAttribute(null,"notrim2",(String) "  \n" + elEvaluator.evaluate("${fn:toUpperCase('  abcd  ')}", String.class, varResolver, fResolver.getFunctionMapper(xpCH)) + "  ");
        org.anodyneos.xp.XpOutput savedXPOut0 = xpOut;
        xpOut = new org.anodyneos.xp.XpOutput(new org.anodyneos.xp.util.TextContentHandler(), xpCH);
        xpCH = xpOut.getXpContentHandler();
        xpCH.startElement("http://xhtml.com", "a", "a", null);
        xpCH.characters("abcd");
        xpCH.endElement("http://xhtml.com", "a", "a");
        savedXPOut0.addAttribute(null,"notrim3",((org.anodyneos.xp.util.TextContentHandler) xpCH.getWrappedContentHandler()).getText());
        xpOut = savedXPOut0;
        xpCH = xpOut.getXpContentHandler();
        savedXPOut0 = null;
        xpCH.addAttribute(null,"notrim4","  \nabcd  ");
        xpCH.addAttribute(null,"trim","abcd");
        xpCH.addAttribute(null,"trim2",((String) "  \n" + elEvaluator.evaluate("${fn:toUpperCase('  abcd  ')}", String.class, varResolver, fResolver.getFunctionMapper(xpCH)) + "  ").trim());
        org.anodyneos.xp.XpOutput savedXPOut1 = xpOut;
        xpOut = new org.anodyneos.xp.XpOutput(new org.anodyneos.xp.util.TextContentHandler(), xpCH);
        xpCH = xpOut.getXpContentHandler();
        xpCH.startElement("http://xhtml.com", "a", "a", null);
        xpCH.characters("abcd");
        xpCH.endElement("http://xhtml.com", "a", "a");
        savedXPOut1.addAttribute(null,"trim3",((org.anodyneos.xp.util.TextContentHandler) xpCH.getWrappedContentHandler()).getText().trim());
        xpOut = savedXPOut1;
        xpCH = xpOut.getXpContentHandler();
        savedXPOut1 = null;
        xpCH.addAttribute(null,"trim4","abcd");
        xpCH.endElement("http://xhtml.com", "trimTest", "trimTest");
        xpCH.startElement("http://xhtml.com", "h1", "h1", null);
        xpCH.characters("====== TEST ATTRIBUTE =====");
        xpCH.endElement("http://xhtml.com", "h1", "h1");
        xpCH.startElement("http://xhtml.com", "testElement", "testElement", null);
        org.anodyneos.xp.tag.core.IfTag tag3 = new org.anodyneos.xp.tag.core.IfTag();
        // tag3.setParent(null); // no parent tag
        tag3.setXpContext(xpContext);
        tag3.setTest(((Boolean) elEvaluator.evaluate("${true}", Boolean.class, varResolver, fResolver.getFunctionMapper(xpCH))).booleanValue());
        tag3.setXpBody(new FragmentHelper(3, xpContext, tag3, xpCH));
        tag3.doTag(xpOut);
        tag3 = null;
        xpCH.endElement("http://xhtml.com", "testElement", "testElement");
        xpCH.startElement("http://xhtml.com", "h1", "h1", null);
        xpCH.characters("====== TEST FUNCTIONS =====");
        xpCH.endElement("http://xhtml.com", "h1", "h1");
        xpCH.startElement("http://xhtml.com", "pre", "pre", null);
        xpCH.characters("${fn:startsWith('asdf', 'a')} = ");
        xpOut.write((String) elEvaluator.evaluate("${fn:startsWith('asdf', 'a')}", String.class, varResolver, fResolver.getFunctionMapper(xpCH)));
        xpCH.characters("\n                ${fn:endsWith('asdf', 'b')} = ");
        xpOut.write((String) elEvaluator.evaluate("${fn:endsWith('asdf', 'b')}", String.class, varResolver, fResolver.getFunctionMapper(xpCH)));
        xpCH.characters("\n                ${fn:join(fn:split('a,b,c', ','), ';')} = ");
        xpOut.write((String) elEvaluator.evaluate("${fn:join(fn:split('a,b,c', ','), ';')}", String.class, varResolver, fResolver.getFunctionMapper(xpCH)));
        xpCH.endElement("http://xhtml.com", "pre", "pre");
        xpCH.startElement("http://xhtml.com", "h1", "h1", null);
        xpCH.characters("====== NAMESPACE TEST -1 ======");
        xpCH.endElement("http://xhtml.com", "h1", "h1");
        xpCH.startPrefixMapping("ns1","http://origns1.example.com");
        xpCH.startElement("http://xhtml.com", "test0", "test0", null);
        xpCH.addAttribute("", "xmlns:ns1", "http://origns1.example.com");
        xpCH.pushPhantomPrefixMapping("ns1","http://ns1.example.com");
        org.anodyneos.xp.tag.core.IfTag tag4 = new org.anodyneos.xp.tag.core.IfTag();
        // tag4.setParent(null); // no parent tag
        tag4.setXpContext(xpContext);
        tag4.setTest(((Boolean) elEvaluator.evaluate("${true}", Boolean.class, varResolver, fResolver.getFunctionMapper(xpCH))).booleanValue());
        tag4.setXpBody(new FragmentHelper(4, xpContext, tag4, xpCH));
        tag4.doTag(xpOut);
        tag4 = null;
        xpCH.popPhantomPrefixMapping();
        xpCH.endElement("http://xhtml.com", "test0", "test0");
        xpCH.startElement("http://xhtml.com", "h1", "h1", null);
        xpCH.characters("====== NAMESPACE TEST 0 ======");
        xpCH.endElement("http://xhtml.com", "h1", "h1");
        xpCH.startElement("http://xhtml.com", "test0", "test0", null);
        xpCH.pushPhantomPrefixMapping("ns1","http://ns1.example.com");
        org.anodyneos.xp.tag.core.IfTag tag5 = new org.anodyneos.xp.tag.core.IfTag();
        // tag5.setParent(null); // no parent tag
        tag5.setXpContext(xpContext);
        tag5.setTest(((Boolean) elEvaluator.evaluate("${true}", Boolean.class, varResolver, fResolver.getFunctionMapper(xpCH))).booleanValue());
        tag5.setXpBody(new FragmentHelper(5, xpContext, tag5, xpCH));
        tag5.doTag(xpOut);
        tag5 = null;
        xpCH.popPhantomPrefixMapping();
        xpCH.endElement("http://xhtml.com", "test0", "test0");
        xpCH.startElement("http://xhtml.com", "h1", "h1", null);
        xpCH.characters("====== NAMESPACE TEST 0b ======");
        xpCH.endElement("http://xhtml.com", "h1", "h1");
        xpCH.startPrefixMapping("ns1","http://ns1.example.com");
        xpCH.startElement("http://xhtml.com", "test0", "test0", null);
        xpCH.addAttribute("", "xmlns:ns1", "http://ns1.example.com");
        xpCH.addAttribute("http://ns1.example.com", "ns1:att0", "");
        xpCH.pushPhantomPrefixMapping("ns1","http://ns1.example.com");
        org.anodyneos.xp.tag.core.IfTag tag6 = new org.anodyneos.xp.tag.core.IfTag();
        // tag6.setParent(null); // no parent tag
        tag6.setXpContext(xpContext);
        tag6.setTest(((Boolean) elEvaluator.evaluate("${true}", Boolean.class, varResolver, fResolver.getFunctionMapper(xpCH))).booleanValue());
        tag6.setXpBody(new FragmentHelper(6, xpContext, tag6, xpCH));
        tag6.doTag(xpOut);
        tag6 = null;
        xpCH.popPhantomPrefixMapping();
        xpCH.endElement("http://xhtml.com", "test0", "test0");
        xpCH.startElement("http://xhtml.com", "h1", "h1", null);
        xpCH.characters("====== NAMESPACE TEST 0c ======");
        xpCH.endElement("http://xhtml.com", "h1", "h1");
        xpCH.startPrefixMapping("ns1","http://origns1.example.com");
        xpCH.startElement("http://xhtml.com", "test0", "test0", null);
        xpCH.addAttribute("", "xmlns:ns1", "http://origns1.example.com");
        xpCH.addAttribute("http://origns1.example.com", "ns1:att0", "");
        xpCH.pushPhantomPrefixMapping("ns1","http://ns1.example.com");
        org.anodyneos.xp.tag.core.IfTag tag7 = new org.anodyneos.xp.tag.core.IfTag();
        // tag7.setParent(null); // no parent tag
        tag7.setXpContext(xpContext);
        tag7.setTest(((Boolean) elEvaluator.evaluate("${true}", Boolean.class, varResolver, fResolver.getFunctionMapper(xpCH))).booleanValue());
        tag7.setXpBody(new FragmentHelper(7, xpContext, tag7, xpCH));
        tag7.doTag(xpOut);
        tag7 = null;
        xpCH.popPhantomPrefixMapping();
        xpCH.endElement("http://xhtml.com", "test0", "test0");
        xpCH.startElement("http://xhtml.com", "h1", "h1", null);
        xpCH.characters("====== NAMESPACE TEST 1 ======");
        xpCH.endElement("http://xhtml.com", "h1", "h1");
        xpCH.startElement("http://xhtml.com", "noNS1", "noNS1", null);
        xpCH.pushPhantomPrefixMapping("ns1","http://ns1.example.com");
        org.anodyneos.xp.tag.core.IfTag tag8 = new org.anodyneos.xp.tag.core.IfTag();
        // tag8.setParent(null); // no parent tag
        tag8.setXpContext(xpContext);
        tag8.setTest(((Boolean) elEvaluator.evaluate("${true}", Boolean.class, varResolver, fResolver.getFunctionMapper(xpCH))).booleanValue());
        tag8.setXpBody(new FragmentHelper(8, xpContext, tag8, xpCH));
        tag8.doTag(xpOut);
        tag8 = null;
        xpCH.popPhantomPrefixMapping();
        xpCH.endElement("http://xhtml.com", "noNS1", "noNS1");
        xpCH.startElement("http://xhtml.com", "h1", "h1", null);
        xpCH.characters("====== NAMESPACE TEST 2 ======");
        xpCH.endElement("http://xhtml.com", "h1", "h1");
        xpCH.startElement("http://xhtml.com", "noNS1", "noNS1", null);
        xpCH.pushPhantomPrefixMapping("ns1","http://ns1.example.com");
        xpCH.pushPhantomPrefixMapping("yp","http://www.anodyneos.org/xmlns/xp");
        if (((Boolean) elEvaluator.evaluate("${true}", Boolean.class, varResolver, fResolver.getFunctionMapper(xpCH))).booleanValue()) {
            xpCH.addAttribute(null,"att1","");
            xpCH.startElement("http://xhtml.com", "elWithNS1", "elWithNS1", null);
            xpCH.startElement("http://ns1.example.com", "innerElWithNS1", "ns1:innerElWithNS1", null);
            xpCH.endElement("http://ns1.example.com", "innerElWithNS1", "ns1:innerElWithNS1");
            xpCH.startElement("http://ns1.example.com", "innerElWithNS1", "ns1:innerElWithNS1", null);
            xpCH.endElement("http://ns1.example.com", "innerElWithNS1", "ns1:innerElWithNS1");
            xpCH.endElement("http://xhtml.com", "elWithNS1", "elWithNS1");
        }
        xpCH.popPhantomPrefixMapping();
        xpCH.popPhantomPrefixMapping();
        xpCH.endElement("http://xhtml.com", "noNS1", "noNS1");
        xpCH.startElement("http://xhtml.com", "h1", "h1", null);
        xpCH.characters("====== NAMESPACE TEST 3 ======");
        xpCH.endElement("http://xhtml.com", "h1", "h1");
        xpCH.startElement("http://xhtml.com", "noNS1", "noNS1", null);
        xpCH.pushPhantomPrefixMapping("ns1","http://ns1.example.com");
        if (((Boolean) elEvaluator.evaluate("${true}", Boolean.class, varResolver, fResolver.getFunctionMapper(xpCH))).booleanValue()) {
            xpCH.characters("This is some inner text.");
        }
        xpCH.popPhantomPrefixMapping();
        xpCH.endElement("http://xhtml.com", "noNS1", "noNS1");
        xpCH.startElement("http://xhtml.com", "h1", "h1", null);
        xpCH.characters("====== NAMESPACE TEST 4 ======");
        xpCH.endElement("http://xhtml.com", "h1", "h1");
        xpCH.startElement("http://xhtml.com", "noNS1", "noNS1", null);
        xpCH.pushPhantomPrefixMapping("ns1","http://ns1.example.com");
        if (((Boolean) elEvaluator.evaluate("${true}", Boolean.class, varResolver, fResolver.getFunctionMapper(xpCH))).booleanValue()) {
            xpCH.addAttribute(null,"att1","");
            xpCH.startElement("http://xhtml.com", "elWithNS1", "elWithNS1", null);
            xpCH.startElement("http://ns1.example.com", "innerElWithNS1", "ns1:innerElWithNS1", null);
            xpCH.endElement("http://ns1.example.com", "innerElWithNS1", "ns1:innerElWithNS1");
            xpCH.startElement("http://ns1.example.com", "innerElWithNS1", "ns1:innerElWithNS1", null);
            xpCH.endElement("http://ns1.example.com", "innerElWithNS1", "ns1:innerElWithNS1");
            xpCH.endElement("http://xhtml.com", "elWithNS1", "elWithNS1");
        }
        xpCH.popPhantomPrefixMapping();
        xpCH.endElement("http://xhtml.com", "noNS1", "noNS1");
        xpCH.endElement("http://xhtml.com", "html", "html");
        xpCH.popPhantomPrefixMapping();
        xpCH.popPhantomPrefixMapping();
        xpCH.popPhantomPrefixMapping();
        xpCH.popPhantomPrefixMapping();
        xpCH.popPhantomPrefixMapping();
        xpCH.popPhantomPrefixMapping();
    }

    private void initOutputProperties() {
        outputProperties.setProperty("indent", "yes");
        outputProperties.setProperty("omitXmlDeclaration", "no");
        outputProperties.setProperty("mediaType", "text/xml");
        outputProperties.setProperty("encoding", "US-ASCII");
        outputProperties.setProperty("xhtmlCompat", "yes");
        outputProperties.setProperty("method", "xml");
    }

    public java.util.Properties getOutputProperties() {
        return new java.util.Properties(outputProperties);
    }

    public static java.util.List getDependents(){
        java.util.List dependents = new java.util.ArrayList();
        return dependents;
    }

    private final long loadTime = System.currentTimeMillis();
    public long getLoadTime(){
        return loadTime;
    }
    private final class FragmentHelper extends org.anodyneos.xp.tagext.XpFragment {

        private int fragNum;
        private org.anodyneos.xp.XpContext xpContext;
        private org.anodyneos.xp.tagext.XpTag xpTagParent;
        private boolean parentElClosed = false;
        private org.anodyneos.xp.XpContentHandler origXpCH;
        private int origContextVersion;
        private int origAncestorsWithPrefixMasking;
        private int origPhantomPrefixCount;

        public FragmentHelper(int fragNum, org.anodyneos.xp.XpContext xpContext, org.anodyneos.xp.tagext.XpTag xpTagParent, org.anodyneos.xp.XpContentHandler origXpCH) {
            this.fragNum = fragNum;
            this.xpContext = xpContext;
            this.xpTagParent = xpTagParent;
            this.origXpCH = origXpCH;
            this.origContextVersion = origXpCH.getContextVersion();
            this.origAncestorsWithPrefixMasking = origXpCH.getAncestorsWithPrefixMasking();
            this.origPhantomPrefixCount = origXpCH.getPhantomPrefixCount();
        }

        public org.anodyneos.xp.XpContext getXpContext() {
            return xpContext;
        }

        public void setParentElClosed(boolean closed) {
            this.parentElClosed = closed;
        }

        public void invoke(org.anodyneos.xp.XpOutput out) throws org.anodyneos.xp.XpException, javax.servlet.jsp.el.ELException, org.xml.sax.SAXException {
            switch(this.fragNum) {
                case 0:
                    invoke0(out);
                    break;
                case 1:
                    invoke1(out);
                    break;
                case 2:
                    invoke2(out);
                    break;
                case 3:
                    invoke3(out);
                    break;
                case 4:
                    invoke4(out);
                    break;
                case 5:
                    invoke5(out);
                    break;
                case 6:
                    invoke6(out);
                    break;
                case 7:
                    invoke7(out);
                    break;
                case 8:
                    invoke8(out);
                    break;
            }
        }

        public void invoke0(org.anodyneos.xp.XpOutput xpOut) throws org.anodyneos.xp.XpException, javax.servlet.jsp.el.ELException, org.xml.sax.SAXException  {
            javax.servlet.jsp.el.ExpressionEvaluator elEvaluator = xpContext.getExpressionEvaluator();
            javax.servlet.jsp.el.VariableResolver varResolver = xpContext.getVariableResolver();
            org.anodyneos.xp.XpContentHandler xpCH = xpOut.getXpContentHandler();

            boolean namespaceCompat = xpCH.isNamespaceContextCompatible(origXpCH, parentElClosed, origContextVersion, origAncestorsWithPrefixMasking, origPhantomPrefixCount);
            if (! namespaceCompat) {
                xpCH.pushPhantomPrefixMapping("c", "http://www.anodyneos.org/xmlns/xp/core");
                xpCH.pushPhantomPrefixMapping("fmt", "http://www.anodyneos.org/xmlns/xp/fmt");
                xpCH.pushPhantomPrefixMapping("xml", "http://www.w3.org/XML/1998/namespace");
                xpCH.pushPhantomPrefixMapping("xp", "http://www.anodyneos.org/xmlns/xp");
                xpCH.pushPhantomPrefixMapping("", "http://xhtml.com");
                xpCH.pushPhantomPrefixMapping("fn", "http://www.anodyneos.org/xmlns/xp/function");
            }

            xpCH.characters("Text #1");
            xpCH.startElement("http://xhtml.com", "tag", "tag", null);
            xpCH.endElement("http://xhtml.com", "tag", "tag");
            xpCH.characters("Text #2");
            xpCH.startElement("http://xhtml.com", "tag", "tag", null);
            xpCH.endElement("http://xhtml.com", "tag", "tag");
            xpCH.characters("Text #3");

            if (! namespaceCompat) {
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
            }
        }

        public void invoke1(org.anodyneos.xp.XpOutput xpOut) throws org.anodyneos.xp.XpException, javax.servlet.jsp.el.ELException, org.xml.sax.SAXException  {
            javax.servlet.jsp.el.ExpressionEvaluator elEvaluator = xpContext.getExpressionEvaluator();
            javax.servlet.jsp.el.VariableResolver varResolver = xpContext.getVariableResolver();
            org.anodyneos.xp.XpContentHandler xpCH = xpOut.getXpContentHandler();

            boolean namespaceCompat = xpCH.isNamespaceContextCompatible(origXpCH, parentElClosed, origContextVersion, origAncestorsWithPrefixMasking, origPhantomPrefixCount);
            if (! namespaceCompat) {
                xpCH.pushPhantomPrefixMapping("c", "http://www.anodyneos.org/xmlns/xp/core");
                xpCH.pushPhantomPrefixMapping("fmt", "http://www.anodyneos.org/xmlns/xp/fmt");
                xpCH.pushPhantomPrefixMapping("xml", "http://www.w3.org/XML/1998/namespace");
                xpCH.pushPhantomPrefixMapping("xp", "http://www.anodyneos.org/xmlns/xp");
                xpCH.pushPhantomPrefixMapping("", "http://xhtml.com");
                xpCH.pushPhantomPrefixMapping("fn", "http://www.anodyneos.org/xmlns/xp/function");
            }

            xpCH.characters("Some text");

            if (! namespaceCompat) {
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
            }
        }

        public void invoke2(org.anodyneos.xp.XpOutput xpOut) throws org.anodyneos.xp.XpException, javax.servlet.jsp.el.ELException, org.xml.sax.SAXException  {
            javax.servlet.jsp.el.ExpressionEvaluator elEvaluator = xpContext.getExpressionEvaluator();
            javax.servlet.jsp.el.VariableResolver varResolver = xpContext.getVariableResolver();
            org.anodyneos.xp.XpContentHandler xpCH = xpOut.getXpContentHandler();

            boolean namespaceCompat = xpCH.isNamespaceContextCompatible(origXpCH, parentElClosed, origContextVersion, origAncestorsWithPrefixMasking, origPhantomPrefixCount);
            if (! namespaceCompat) {
                xpCH.pushPhantomPrefixMapping("c", "http://www.anodyneos.org/xmlns/xp/core");
                xpCH.pushPhantomPrefixMapping("fmt", "http://www.anodyneos.org/xmlns/xp/fmt");
                xpCH.pushPhantomPrefixMapping("xml", "http://www.w3.org/XML/1998/namespace");
                xpCH.pushPhantomPrefixMapping("xp", "http://www.anodyneos.org/xmlns/xp");
                xpCH.pushPhantomPrefixMapping("", "http://xhtml.com");
                xpCH.pushPhantomPrefixMapping("fn", "http://www.anodyneos.org/xmlns/xp/function");
            }

            xpCH.characters("${fn:startsWith('asdf', 'a')} = ");
            xpOut.write((String) elEvaluator.evaluate("${fn:startsWith('asdf', 'a')}", String.class, varResolver, fResolver.getFunctionMapper(xpCH)));

            if (! namespaceCompat) {
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
            }
        }

        public void invoke3(org.anodyneos.xp.XpOutput xpOut) throws org.anodyneos.xp.XpException, javax.servlet.jsp.el.ELException, org.xml.sax.SAXException  {
            javax.servlet.jsp.el.ExpressionEvaluator elEvaluator = xpContext.getExpressionEvaluator();
            javax.servlet.jsp.el.VariableResolver varResolver = xpContext.getVariableResolver();
            org.anodyneos.xp.XpContentHandler xpCH = xpOut.getXpContentHandler();

            boolean namespaceCompat = xpCH.isNamespaceContextCompatible(origXpCH, parentElClosed, origContextVersion, origAncestorsWithPrefixMasking, origPhantomPrefixCount);
            if (! namespaceCompat) {
                xpCH.pushPhantomPrefixMapping("c", "http://www.anodyneos.org/xmlns/xp/core");
                xpCH.pushPhantomPrefixMapping("fmt", "http://www.anodyneos.org/xmlns/xp/fmt");
                xpCH.pushPhantomPrefixMapping("xml", "http://www.w3.org/XML/1998/namespace");
                xpCH.pushPhantomPrefixMapping("xp", "http://www.anodyneos.org/xmlns/xp");
                xpCH.pushPhantomPrefixMapping("", "http://xhtml.com");
                xpCH.pushPhantomPrefixMapping("fn", "http://www.anodyneos.org/xmlns/xp/function");
            }

            org.anodyneos.xp.XpOutput savedXPOut2 = xpOut;
            xpOut = new org.anodyneos.xp.XpOutput(new org.anodyneos.xp.util.TextContentHandler(), xpCH);
            xpCH = xpOut.getXpContentHandler();
            xpCH.startElement("http://xhtml.com", "pre", "pre", null);
            xpCH.characters("${fn:startsWith('asdf', 'a')} = ");
            xpOut.write((String) elEvaluator.evaluate("${fn:startsWith('asdf', 'a')}", String.class, varResolver, fResolver.getFunctionMapper(xpCH)));
            xpCH.endElement("http://xhtml.com", "pre", "pre");
            savedXPOut2.addAttribute(null,"att1",((org.anodyneos.xp.util.TextContentHandler) xpCH.getWrappedContentHandler()).getText().trim());
            xpOut = savedXPOut2;
            xpCH = xpOut.getXpContentHandler();
            savedXPOut2 = null;

            if (! namespaceCompat) {
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
            }
        }

        public void invoke4(org.anodyneos.xp.XpOutput xpOut) throws org.anodyneos.xp.XpException, javax.servlet.jsp.el.ELException, org.xml.sax.SAXException  {
            javax.servlet.jsp.el.ExpressionEvaluator elEvaluator = xpContext.getExpressionEvaluator();
            javax.servlet.jsp.el.VariableResolver varResolver = xpContext.getVariableResolver();
            org.anodyneos.xp.XpContentHandler xpCH = xpOut.getXpContentHandler();

            boolean namespaceCompat = xpCH.isNamespaceContextCompatible(origXpCH, parentElClosed, origContextVersion, origAncestorsWithPrefixMasking, origPhantomPrefixCount);
            if (! namespaceCompat) {
                xpCH.pushPhantomPrefixMapping("ns1", "http://ns1.example.com");
                xpCH.pushPhantomPrefixMapping("c", "http://www.anodyneos.org/xmlns/xp/core");
                xpCH.pushPhantomPrefixMapping("fmt", "http://www.anodyneos.org/xmlns/xp/fmt");
                xpCH.pushPhantomPrefixMapping("xml", "http://www.w3.org/XML/1998/namespace");
                xpCH.pushPhantomPrefixMapping("xp", "http://www.anodyneos.org/xmlns/xp");
                xpCH.pushPhantomPrefixMapping("", "http://xhtml.com");
                xpCH.pushPhantomPrefixMapping("fn", "http://www.anodyneos.org/xmlns/xp/function");
            }

            xpCH.startElement("http://ns1.example.com", "ns1el", "ns1:ns1el", null);
            xpCH.endElement("http://ns1.example.com", "ns1el", "ns1:ns1el");

            if (! namespaceCompat) {
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
            }
        }

        public void invoke5(org.anodyneos.xp.XpOutput xpOut) throws org.anodyneos.xp.XpException, javax.servlet.jsp.el.ELException, org.xml.sax.SAXException  {
            javax.servlet.jsp.el.ExpressionEvaluator elEvaluator = xpContext.getExpressionEvaluator();
            javax.servlet.jsp.el.VariableResolver varResolver = xpContext.getVariableResolver();
            org.anodyneos.xp.XpContentHandler xpCH = xpOut.getXpContentHandler();

            boolean namespaceCompat = xpCH.isNamespaceContextCompatible(origXpCH, parentElClosed, origContextVersion, origAncestorsWithPrefixMasking, origPhantomPrefixCount);
            if (! namespaceCompat) {
                xpCH.pushPhantomPrefixMapping("ns1", "http://ns1.example.com");
                xpCH.pushPhantomPrefixMapping("c", "http://www.anodyneos.org/xmlns/xp/core");
                xpCH.pushPhantomPrefixMapping("fmt", "http://www.anodyneos.org/xmlns/xp/fmt");
                xpCH.pushPhantomPrefixMapping("xml", "http://www.w3.org/XML/1998/namespace");
                xpCH.pushPhantomPrefixMapping("xp", "http://www.anodyneos.org/xmlns/xp");
                xpCH.pushPhantomPrefixMapping("", "http://xhtml.com");
                xpCH.pushPhantomPrefixMapping("fn", "http://www.anodyneos.org/xmlns/xp/function");
            }

            xpCH.addAttribute(null,"ns1:att1","");
            xpCH.addAttribute("http://ns1.example.com","ns1:att2","");
            xpCH.addAttribute("http://newerns1.example.com","ns1:att3","");
            xpCH.startElement("http://xhtml.com", "elWithNS1", "elWithNS1", null);
            xpCH.startElement("http://ns1.example.com", "innerElWithNS1", "ns1:innerElWithNS1", null);
            xpCH.endElement("http://ns1.example.com", "innerElWithNS1", "ns1:innerElWithNS1");
            xpCH.startElement("http://ns1.example.com", "innerElWithNS1", "ns1:innerElWithNS1", null);
            xpCH.endElement("http://ns1.example.com", "innerElWithNS1", "ns1:innerElWithNS1");
            xpCH.endElement("http://xhtml.com", "elWithNS1", "elWithNS1");

            if (! namespaceCompat) {
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
            }
        }

        public void invoke6(org.anodyneos.xp.XpOutput xpOut) throws org.anodyneos.xp.XpException, javax.servlet.jsp.el.ELException, org.xml.sax.SAXException  {
            javax.servlet.jsp.el.ExpressionEvaluator elEvaluator = xpContext.getExpressionEvaluator();
            javax.servlet.jsp.el.VariableResolver varResolver = xpContext.getVariableResolver();
            org.anodyneos.xp.XpContentHandler xpCH = xpOut.getXpContentHandler();

            boolean namespaceCompat = xpCH.isNamespaceContextCompatible(origXpCH, parentElClosed, origContextVersion, origAncestorsWithPrefixMasking, origPhantomPrefixCount);
            if (! namespaceCompat) {
                xpCH.pushPhantomPrefixMapping("ns1", "http://ns1.example.com");
                xpCH.pushPhantomPrefixMapping("c", "http://www.anodyneos.org/xmlns/xp/core");
                xpCH.pushPhantomPrefixMapping("fmt", "http://www.anodyneos.org/xmlns/xp/fmt");
                xpCH.pushPhantomPrefixMapping("xml", "http://www.w3.org/XML/1998/namespace");
                xpCH.pushPhantomPrefixMapping("xp", "http://www.anodyneos.org/xmlns/xp");
                xpCH.pushPhantomPrefixMapping("", "http://xhtml.com");
                xpCH.pushPhantomPrefixMapping("fn", "http://www.anodyneos.org/xmlns/xp/function");
            }

            xpCH.addAttribute(null,"ns1:att1","");
            xpCH.addAttribute("http://ns1.example.com","ns1:att2","");
            xpCH.addAttribute("http://newerns1.example.com","ns1:att3","");
            xpCH.startElement("http://xhtml.com", "elWithNS1", "elWithNS1", null);
            xpCH.startElement("http://ns1.example.com", "innerElWithNS1", "ns1:innerElWithNS1", null);
            xpCH.endElement("http://ns1.example.com", "innerElWithNS1", "ns1:innerElWithNS1");
            xpCH.startElement("http://ns1.example.com", "innerElWithNS1", "ns1:innerElWithNS1", null);
            xpCH.endElement("http://ns1.example.com", "innerElWithNS1", "ns1:innerElWithNS1");
            xpCH.endElement("http://xhtml.com", "elWithNS1", "elWithNS1");

            if (! namespaceCompat) {
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
            }
        }

        public void invoke7(org.anodyneos.xp.XpOutput xpOut) throws org.anodyneos.xp.XpException, javax.servlet.jsp.el.ELException, org.xml.sax.SAXException  {
            javax.servlet.jsp.el.ExpressionEvaluator elEvaluator = xpContext.getExpressionEvaluator();
            javax.servlet.jsp.el.VariableResolver varResolver = xpContext.getVariableResolver();
            org.anodyneos.xp.XpContentHandler xpCH = xpOut.getXpContentHandler();

            boolean namespaceCompat = xpCH.isNamespaceContextCompatible(origXpCH, parentElClosed, origContextVersion, origAncestorsWithPrefixMasking, origPhantomPrefixCount);
            if (! namespaceCompat) {
                xpCH.pushPhantomPrefixMapping("ns1", "http://ns1.example.com");
                xpCH.pushPhantomPrefixMapping("c", "http://www.anodyneos.org/xmlns/xp/core");
                xpCH.pushPhantomPrefixMapping("fmt", "http://www.anodyneos.org/xmlns/xp/fmt");
                xpCH.pushPhantomPrefixMapping("xml", "http://www.w3.org/XML/1998/namespace");
                xpCH.pushPhantomPrefixMapping("xp", "http://www.anodyneos.org/xmlns/xp");
                xpCH.pushPhantomPrefixMapping("", "http://xhtml.com");
                xpCH.pushPhantomPrefixMapping("fn", "http://www.anodyneos.org/xmlns/xp/function");
            }

            xpCH.addAttribute(null,"ns1:att1","");
            xpCH.addAttribute("http://ns1.example.com","ns1:att2","");
            xpCH.addAttribute("http://newerns1.example.com","ns1:att3","");
            xpCH.startElement("http://xhtml.com", "elWithNS1", "elWithNS1", null);
            xpCH.startElement("http://ns1.example.com", "innerElWithNS1", "ns1:innerElWithNS1", null);
            xpCH.endElement("http://ns1.example.com", "innerElWithNS1", "ns1:innerElWithNS1");
            xpCH.startElement("http://ns1.example.com", "innerElWithNS1", "ns1:innerElWithNS1", null);
            xpCH.endElement("http://ns1.example.com", "innerElWithNS1", "ns1:innerElWithNS1");
            xpCH.endElement("http://xhtml.com", "elWithNS1", "elWithNS1");

            if (! namespaceCompat) {
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
            }
        }

        public void invoke8(org.anodyneos.xp.XpOutput xpOut) throws org.anodyneos.xp.XpException, javax.servlet.jsp.el.ELException, org.xml.sax.SAXException  {
            javax.servlet.jsp.el.ExpressionEvaluator elEvaluator = xpContext.getExpressionEvaluator();
            javax.servlet.jsp.el.VariableResolver varResolver = xpContext.getVariableResolver();
            org.anodyneos.xp.XpContentHandler xpCH = xpOut.getXpContentHandler();

            boolean namespaceCompat = xpCH.isNamespaceContextCompatible(origXpCH, parentElClosed, origContextVersion, origAncestorsWithPrefixMasking, origPhantomPrefixCount);
            if (! namespaceCompat) {
                xpCH.pushPhantomPrefixMapping("ns1", "http://ns1.example.com");
                xpCH.pushPhantomPrefixMapping("c", "http://www.anodyneos.org/xmlns/xp/core");
                xpCH.pushPhantomPrefixMapping("fmt", "http://www.anodyneos.org/xmlns/xp/fmt");
                xpCH.pushPhantomPrefixMapping("xml", "http://www.w3.org/XML/1998/namespace");
                xpCH.pushPhantomPrefixMapping("xp", "http://www.anodyneos.org/xmlns/xp");
                xpCH.pushPhantomPrefixMapping("", "http://xhtml.com");
                xpCH.pushPhantomPrefixMapping("fn", "http://www.anodyneos.org/xmlns/xp/function");
            }

            xpCH.addAttribute(null,"att1","");
            xpCH.startElement("http://xhtml.com", "elWithNS1", "elWithNS1", null);
            xpCH.startElement("http://ns1.example.com", "innerElWithNS1", "ns1:innerElWithNS1", null);
            xpCH.endElement("http://ns1.example.com", "innerElWithNS1", "ns1:innerElWithNS1");
            xpCH.startElement("http://ns1.example.com", "innerElWithNS1", "ns1:innerElWithNS1", null);
            xpCH.endElement("http://ns1.example.com", "innerElWithNS1", "ns1:innerElWithNS1");
            xpCH.endElement("http://xhtml.com", "elWithNS1", "elWithNS1");

            if (! namespaceCompat) {
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
                xpCH.popPhantomPrefixMapping();
            }
        }


    }


}
