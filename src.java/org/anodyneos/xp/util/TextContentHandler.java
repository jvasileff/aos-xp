package org.anodyneos.xp.util;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * <code>TextContentHandler</code> can be used to accumulate character data while ignoring all
 * other XML data such as elements, attributes, processing instructions, etc.  The <code>getText()</code>
 * method may be called to recieved accumulated text.  This class is usefull for constructing a
 * <code>XpContentHandler</code> when only text output is required.
 *
 * @author jvas
 */
public class TextContentHandler extends DefaultHandler {
    private StringBuffer text = new StringBuffer();


    public void characters(char[] ch, int start, int length) throws SAXException {
        text.append(ch, start, length);
    }

    public String getText() {
        return text.toString();
    }

}
