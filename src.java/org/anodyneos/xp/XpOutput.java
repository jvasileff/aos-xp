package org.anodyneos.xp;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public final class XpOutput {

    private final XpContentHandler ch;

    public XpOutput(XpContentHandler ch) {
        this.ch = ch;
    }

    public XpOutput(ContentHandler ch) {
        this.ch = new XpContentHandler(ch);
    }

    public XpContentHandler getXpContentHandler() {
        return ch;
    }

    public void addAttribute(String uri, String qName, String value)
            throws SAXException {
        ch.addAttribute(uri, qName, value);
    }

    public void endElement(String uri, String qName) throws SAXException {
        ch.endElement(uri, qName);
    }

    public void startElement(String uri, String qName) throws SAXException {
        ch.startElement(uri, qName);
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {
        this.ch.characters(ch, start, length);
    }

    ////////////////////////////////////////////////////////////////////////////////
    //
    // characters(xxx) convenience methods
    //
    ////////////////////////////////////////////////////////////////////////////////

    public void characters(String s) throws SAXException {
        if (null != s) {
            characters(s.toCharArray(), 0, s.length());
        }
    }

    public void characters(Object x) throws SAXException {
        if (null != x) {
            characters(x.toString());
        }
    }

    public void characters(char x) throws SAXException {
        characters(String.valueOf(x));
    }

    public void characters(byte x) throws SAXException {
        characters(String.valueOf(x));
    }

    public void characters(boolean x) throws SAXException {
        characters(String.valueOf(x));
    }

    public void characters(int x) throws SAXException {
        characters(String.valueOf(x));
    }

    public void characters(long x) throws SAXException {
        characters(String.valueOf(x));
    }

    public void characters(float x) throws SAXException {
        characters(String.valueOf(x));
    }

    public void characters(double x) throws SAXException {
        characters(String.valueOf(x));
    }

}
