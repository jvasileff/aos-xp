/*
 * Created on Jul 7, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.anodyneos.xp.standard;

import java.io.PrintStream;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;


/**
 * Outputs events to provided PrintStream and forwards them to the given ContentHandler.
 * @author jvas
 */

public class DebugCH implements ContentHandler {

    PrintStream pw;
    ContentHandler ch;

    public DebugCH(PrintStream pw, ContentHandler ch) {
        this.pw = pw;
        this.ch = ch;
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        pw.println("[DEBUG] endDocument()");
        this.ch.endDocument();
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        pw.println("[DEBUG] startDocument()");
        this.ch.startDocument();
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] chArray, int start, int length) throws SAXException {
        pw.println("[DEBUG] characters()");
        this.ch.characters(chArray, start, length);

    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] chArray, int start, int length) throws SAXException {
        pw.println("[DEBUG] ignorableWhitespace()");
        this.ch.ignorableWhitespace(chArray, start, length);

    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    public void endPrefixMapping(String prefix) throws SAXException {
        pw.println("[DEBUG] endPrefixMapping()");
        this.ch.endPrefixMapping(prefix);

    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    public void skippedEntity(String name) throws SAXException {
        pw.println("[DEBUG] skippedEntity()");
        this.ch.skippedEntity(name);

    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    public void setDocumentLocator(Locator locator) {
        pw.println("[DEBUG] setDocumentLocator()");
        this.ch.setDocumentLocator(locator);

    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
     */
    public void processingInstruction(String target, String data) throws SAXException {
        pw.println("[DEBUG] processingInstruction()");
        this.ch.processingInstruction(target, data);

    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
     */
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        pw.println("[DEBUG] startPrefixMapping()");
        this.ch.startPrefixMapping(prefix, uri);

    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        pw.println("[DEBUG] endElement()");
        this.ch.endElement(namespaceURI, localName, qName);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        pw.println("[DEBUG] startElement()");
        this.ch.startElement(namespaceURI, localName, qName, atts);
    }

}
