package org.anodyneos.xpImpl.registry;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.anodyneos.commons.xml.sax.CDATAProcessor;
import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.xpImpl.XpTranslationException;
import org.xml.sax.SAXException;

class ProcessorTaglib extends RegistryProcessor {

    private CDATAProcessor uriProcessor;

    private CDATAProcessor locationProcessor;

    public static final String E_TAGLIB_URI = "taglib-uri";

    public static final String E_TAGLIB_LOCATION = "taglib-location";

    public ProcessorTaglib(RegistryContext ctx) {
        super(ctx);
        uriProcessor = new CDATAProcessor(ctx);
        locationProcessor = new CDATAProcessor(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName)
            throws SAXException {
        if (E_TAGLIB_URI.equals(localName)) {
            return uriProcessor;
        } else if (E_TAGLIB_LOCATION.equals(localName)) {
            return locationProcessor;
        } else {
            return super.getProcessorFor(uri, localName, qName);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        URI locationURI;
        try {
            locationURI = getContext().uriFromRelative(locationProcessor.getCDATA().trim());
        } catch (URISyntaxException e) {
            throw new SAXException("Malformed URI for taglib: " + locationProcessor.getCDATA(), e);
        }

        try {
            getRegistryContext().getRegistry().addTaglib(uriProcessor.getCDATA().trim(),
                    locationURI.toString());
        } catch (IOException e) {
            // @TODO: check to see if this error handling is good enough. Do
            // parse errors get through ok?

            //throw new SAXException("Cannot process taglib: " + locationProcessor.getCDATA(), e);
            throw new XpTranslationException("Cannot process taglib: " + locationProcessor.getCDATA(), getLocator(), e);
        }
    }
}
