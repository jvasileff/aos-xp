/*
 * Created on May 9, 2004
 *
 */
package org.anodyneos.xpImpl;

import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;


/**
 * @author jvas
 *
 */
public class XpTranslationException extends SAXParseException {

    private static final long serialVersionUID = 3904959767838406707L;

    /**
     * @param message
     * @param locator
     */
    public XpTranslationException(String message, Locator locator) {
        super(message, locator);
    }

    /**
     * @param message
     * @param locator
     * @param e
     */
    public XpTranslationException(String message, Locator locator, Exception e) {
        super(message, locator, e);
    }

    /**
     * @param message
     * @param publicId
     * @param systemId
     * @param lineNumber
     * @param columnNumber
     */
    public XpTranslationException(String message, String publicId, String systemId, int lineNumber,
            int columnNumber) {
        super(message, publicId, systemId, lineNumber, columnNumber);
    }

    /**
     * @param message
     * @param publicId
     * @param systemId
     * @param lineNumber
     * @param columnNumber
     * @param e
     */
    public XpTranslationException(String message, String publicId, String systemId, int lineNumber,
            int columnNumber, Exception e) {
        super(message, publicId, systemId, lineNumber, columnNumber, e);
    }

}
