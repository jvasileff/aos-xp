package org.anodyneos.xpImpl.registry;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.xml.sax.SAXException;

class ProcessorRegistry extends RegistryProcessor {

    public static final String E_TAGLIB = "taglib";

    public ProcessorRegistry(RegistryContext ctx) {
        super(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName)
            throws SAXException {
        if (E_TAGLIB.equals(localName)) {
            return new ProcessorTaglib(getRegistryContext());
        } else {
            return super.getProcessorFor(uri, localName, qName);
            //throw new SAXException("line:" + getLocator().getLineNumber());
        }
    }

}
