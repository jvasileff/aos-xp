package org.anodyneos.xpImpl.tld;

import org.anodyneos.commons.xml.sax.CDATAProcessor;
import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.xp.tagext.FunctionInfo;
import org.xml.sax.SAXException;

class ProcessorFunction extends TLDProcessor {

    private FunctionInfo functionInfo;
    private CDATAProcessor nameProcessor;
    private CDATAProcessor functionClassProcessor;
    private CDATAProcessor functionSignatureProcessor;

    public static final String E_NAME = "name";
    public static final String E_FUNCTION_CLASS = "function-class";
    public static final String E_FUNCTION_SIGNATURE = "function-signature";

    public ProcessorFunction(TLDContext ctx) {
        super(ctx);
        nameProcessor = new CDATAProcessor(ctx);
        functionClassProcessor = new CDATAProcessor(ctx);
        functionSignatureProcessor = new CDATAProcessor(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName)
            throws SAXException {
        if (E_NAME.equals(localName)) {
            return nameProcessor;
        } else if (E_FUNCTION_CLASS.equals(localName)) {
            return functionClassProcessor;
        } else if (E_FUNCTION_SIGNATURE.equals(localName)) {
            return functionSignatureProcessor;
        } else {
            return new CDATAProcessor(getContext());
            //return super.getProcessorFor(uri, localName, qName);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        functionInfo = new FunctionInfo(nameProcessor.getCDATA(), functionClassProcessor.getCDATA(),
                functionSignatureProcessor.getCDATA());

        nameProcessor = null;
        functionClassProcessor = null;
        functionSignatureProcessor = null;
    }

    public FunctionInfo getFunctionInfo() {
        return functionInfo;
    }

}
