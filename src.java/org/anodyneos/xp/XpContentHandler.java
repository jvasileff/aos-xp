package org.anodyneos.xp;

import java.util.Enumeration;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public interface XpContentHandler extends ContentHandler, XpNamespaceMapper {

    // //////////////////////////////////////////////////////////////////////////////
    //
    // phantom prefix push/pop
    //
    // //////////////////////////////////////////////////////////////////////////////

    void pushPhantomPrefixMapping(String prefix, String uri) throws SAXException;

    void popPhantomPrefixMapping() throws SAXException;

    // //////////////////////////////////////////////////////////////////////////////
    //
    // SAX Methods (managed)
    //
    // //////////////////////////////////////////////////////////////////////////////

    void startPrefixMapping(String prefix, String uri) throws SAXException;

    void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException;

    void endElement(String namespaceURI, String localName, String qName) throws SAXException;

    void endPrefixMapping(String prefix) throws SAXException;

    // //////////////////////////////////////////////////////////////////////////////
    //
    // Convenience Methods (managed)
    //
    // //////////////////////////////////////////////////////////////////////////////

    void addAttribute(final String uri, final String qName, final String value) throws SAXException;

    void startElement(String uri, String qName) throws SAXException;

    void endElement(String uri, String qName) throws SAXException;

    // //////////////////////////////////////////////////////////////////////////////
    //
    // SAX Methods (simple pass through)
    //
    // //////////////////////////////////////////////////////////////////////////////

    void characters(char[] ch, int start, int length) throws SAXException;

    void ignorableWhitespace(char[] ch, int start, int length) throws SAXException;

    void processingInstruction(String target, String data) throws SAXException;

    void skippedEntity(String name) throws SAXException;

    void setDocumentLocator(Locator locator);

    void endDocument() throws SAXException;

    void startDocument() throws SAXException;

    // //////////////////////////////////////////////////////////////////////////////
    //
    // Xp specific getters/setters
    //
    // //////////////////////////////////////////////////////////////////////////////

    ContentHandler getWrappedContentHandler();

    boolean isNamespacePrefixes();

    void setNamespacePrefixes(boolean namespacePrefixes);

    // //////////////////////////////////////////////////////////////////////////////
    //
    // characters(xxx) convenience methods
    //
    // //////////////////////////////////////////////////////////////////////////////

    void characters(String s) throws SAXException;

    void characters(Object x) throws SAXException;

    void characters(char x) throws SAXException;

    void characters(byte x) throws SAXException;

    void characters(boolean x) throws SAXException;

    void characters(int x) throws SAXException;

    void characters(long x) throws SAXException;

    void characters(float x) throws SAXException;

    void characters(double x) throws SAXException;

    // //////////////////////////////////////////////////////////////////////////////
    //
    // methods for XpNamespaceMappings
    //
    // //////////////////////////////////////////////////////////////////////////////

    String getPrefix(String uri);

    Enumeration getPrefixes();

    Enumeration getPrefixes(String uri);

    String getURI(String prefix);

    // //////////////////////////////////////////////////////////////////////////////
    //
    // our namespace mappings
    //
    // //////////////////////////////////////////////////////////////////////////////

    boolean isNamespaceContextCompatible(XpContentHandler ch, boolean parentElClosed, int contextVersion,
            int ancestorsWithPrefixMasking, int phantomPrefixCount);

    int getContextVersion();

    int getAncestorsWithPrefixMasking();

    int getPhantomPrefixCount();

}
