package org.anodyneos.xp;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *  XpContentHandler abstracts a SAX ContentHandler and adds XP specific
 *  support.  Attributes may be set using XpContentHandler any time after
 *  startElement is called, but before any other node is added.  Namespace
 *  support...
 *
 *  @author John Vasileff
 */
public final class XpContentHandler {
    private boolean haveNextElement = false;
    private String nextElNamespaceURI;
    private String nextElLocalName;
    private String nextElQName;
    private AttributesImpl nextElAttributes = new AttributesImpl();

    protected ContentHandler contentHandler;

    public XpContentHandler(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        flush();
        if (ch != null) {
            contentHandler.characters(ch, start, length);
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        flush();
        contentHandler.endElement(namespaceURI, localName, qName);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        flush();
        contentHandler.endPrefixMapping(prefix);
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        flush();
        contentHandler.ignorableWhitespace(ch, start, length);
    }

    public void processingInstruction(String target, String data) throws SAXException {
        flush();
        contentHandler.processingInstruction(target, data);
    }

    public void setDocumentLocator(Locator locator) {
        contentHandler.setDocumentLocator(locator);
    }

    public void skippedEntity(String name) throws SAXException {
        flush();
        contentHandler.skippedEntity(name);
    }

    public void startElement(
            String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException {
        flush();
        haveNextElement = true;
        this.nextElNamespaceURI = namespaceURI;
        this.nextElLocalName = localName;
        this.nextElQName = qName;
        this.nextElAttributes.clear();
        if(null != atts) {
            this.nextElAttributes.setAttributes(atts);
        }
    }

    public void addAttribute(String uri, String localName,
            String qName, String type, String value) throws SAXException {
        if (haveNextElement) {
            nextElAttributes.addAttribute(uri, localName, qName, type, value);
        } else {
            // this should not happen with generated code.
            throw new SAXException("Cannot addAttribute() directly after startElement().");
        }
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        flush();
        contentHandler.startPrefixMapping(prefix, uri);
    }

    public void characters(String s) throws SAXException {
        flush();
        if (null != s) {
            contentHandler.characters(s.toCharArray(), 0, s.length());
        }
    }

    public void flush() throws SAXException {
        if (haveNextElement) {
            contentHandler.startElement(nextElNamespaceURI, nextElLocalName,
                    nextElQName, nextElAttributes);
            haveNextElement = false;
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

    public void characters(Object x) throws SAXException {
        if (null != x) {
            characters(x.toString());
        }
    }
}
