package org.anodyneos.xp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

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
public final class XpContentHandler implements ContentHandler {
    private boolean haveNextElement = false;
    private String nextElNamespaceURI;
    private String nextElLocalName;
    private String nextElQName;
    private AttributesImpl nextElAttributes = new AttributesImpl();
    private List nextElManagedPrefixMappings = new ArrayList();

    private Stack elStack = new Stack();

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
        El el = (El) elStack.pop();
        // TODO: provide consistency checks in debug mode
        contentHandler.endElement(namespaceURI, localName, qName);
        List prefixMappings = el.getManagedPrefixMappings();
        if (prefixMappings != null) {
            for (int i = 0; i < prefixMappings.size(); i++) {
                endPrefixMapping((String) prefixMappings.get(i));
            }
        }
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
            throw new SAXException("Cannot addAttribute() unless directly after startElement().");
        }
    }

    public void addManagedAttribute(String name, String namespaceURI, String value) throws SAXException {
        if (! haveNextElement) {
            throw new SAXException("Cannot addAttribute() unless directly after startElement().");
        } else {
            String attLocalName;
            String attQName;
            String attURI;
            String attPrefix = "";
            boolean declareNamespace;
            if (name.indexOf(':') == -1) {
                if (null != namespaceURI) {
                    // no prefix provided but namespace specific
                    attLocalName = name;
                    // TODO: choose a prefix that isn't being used.
                    attPrefix = "ns0";
                    attQName = attPrefix + ":" + name;
                    attURI = namespaceURI;
                    declareNamespace = true;
                } else {
                    // no prefix or namespace
                    attLocalName = name;
                    attQName = name;
                    attURI = "";
                    declareNamespace = false;
                }
            } else {
                if (null != namespaceURI) {
                    // prefix provided and namespace specified
                    attPrefix = name.substring(0, name.indexOf(":"));
                    attLocalName = name.substring(name.indexOf(":") + 1);
                    attQName = name;
                    attURI = namespaceURI;
                    declareNamespace = true;
                } else {
                    // prefix provided and no namespace specified
                    // TODO: do we need validation on the namespace here or does the consumer
                    // of our SAX messages perform checks?
                    attLocalName = name;
                    attQName = name;
                    attURI = "";
                    declareNamespace = false;
                }
            }
            if (declareNamespace) {
                _startPrefixMapping(attPrefix, attURI);
                nextElManagedPrefixMappings.add(attPrefix);
            }
            addAttribute(attURI, attLocalName, attQName, "CDATA", value);
        }
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        flush();
        _startPrefixMapping(prefix, uri);
    }

    /**
     * This class is necessary in order to allow code in this class to bypass flush()
     * called by startPrefixMapping when necessary.
     *
     * @param prefix
     * @param uri
     * @throws SAXException
     */
    private void _startPrefixMapping(String prefix, String uri) throws SAXException {
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
            El el;
            if (nextElManagedPrefixMappings.size() > 0) {
                el = new El(nextElNamespaceURI, nextElLocalName, nextElQName, nextElManagedPrefixMappings);
                nextElManagedPrefixMappings.clear();
            } else {
                el = new El(nextElNamespaceURI, nextElLocalName, nextElQName);
            }
            elStack.push(el);
            contentHandler.startElement(nextElNamespaceURI, nextElLocalName,
                    nextElQName, nextElAttributes);
            nextElNamespaceURI = null;
            nextElLocalName = null;
            nextElQName = null;
            nextElAttributes.clear();
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

    /**
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        // TODO should calls to this method be ignored?

    }

    /**
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        // TODO should calls to this method be ignored?
    }

    private class El {
        private String namespaceURI;
        private String localName;
        private String qName;
        private List managedPrefixMappings;

        private El(String namespaceURI, String localName, String qName) {
            this.namespaceURI = namespaceURI;
            this.localName = localName;
            this.qName = qName;
        }

        private El(String namespaceURI, String localName, String qName, List managedPrefixMappings) {
            this.namespaceURI = namespaceURI;
            this.localName = localName;
            this.qName = qName;
            this.managedPrefixMappings = managedPrefixMappings;
        }
        private String getLocalName() {
            return localName;
        }
        private String getNamespaceURI() {
            return namespaceURI;
        }
        private String getQName() {
            return qName;
        }
        private List getManagedPrefixMappings() {
            return managedPrefixMappings;
        }
    }

}
