/*
 * Created on Jan 3, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.anodyneos.xpImpl.translater;

import java.util.Properties;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author jvas
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ProcessorOutput extends TranslaterProcessor {

    private Properties props = new Properties();

    public ProcessorOutput(TranslaterContext ctx) {
        super(ctx);
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        for (int i=0; i < attributes.getLength(); i++) {
            String attURI = attributes.getURI(i);
            String attValue = attributes.getValue(i);
            String attLocalName = attributes.getLocalName(i);
            String attQName = attributes.getQName(i);

            if (attURI == null || "".equals(attURI)) {
                props.setProperty(attLocalName, attValue);
            } else {
                props.setProperty("{" + attURI + "}" + attLocalName, attValue);
            }
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        getTranslaterContext().setOutputProperties(props);
    }
}
