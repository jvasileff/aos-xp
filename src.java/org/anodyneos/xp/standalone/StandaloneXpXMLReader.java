package org.anodyneos.xp.standalone;

//import org.xml.sax.SAXException;
//import org.xml.sax.InputSource;

import javax.servlet.jsp.el.ELException;

import org.anodyneos.xp.XpContentHandler;
import org.anodyneos.xp.XpException;
import org.anodyneos.xp.XpPage;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class StandaloneXpXMLReader implements XMLReader {

    // Xp specific
    private XpPage xp;

    public StandaloneXpXMLReader(XpPage xp) {
        this.xp = xp;
    }

    public void parse(InputSource input) throws SAXException {
        // create XpContext
        //XpContextImpl xpContext = new XpContextImpl();
        //xpContext.initialize(new XPContentHandlerImpl(getContentHandler()));
        StandaloneXpContext xpContext = StandaloneXpFactory.getDefaultFactory().getStandaloneXpContext();
        XpContentHandler out = new XpContentHandler(getContentHandler());

        // process
        getContentHandler().startDocument();
        try {
            xp.service(xpContext, out);
        } catch (XpException e) {
            throw new SAXException(e);
        } catch (ELException e) {
            throw new SAXException(e);
        }
        getContentHandler().endDocument();
    }

    public void parse(String systemId) {
        throw new java.lang.UnsupportedOperationException();
    }

    // generic stuff
    protected ContentHandler contentHandler;
    protected DTDHandler dtdHandler;
    protected EntityResolver entityResolver;
    protected ErrorHandler errorHandler;

    public ContentHandler getContentHandler() {
        return contentHandler;
    }
    public DTDHandler getDTDHandler() {
        return dtdHandler;
    }
    public EntityResolver getEntityResolver() {
        return entityResolver;
    }
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }
    public boolean getFeature(java.lang.String name) {
        System.out.println("----------- getFeature " + name);
        return true;
    }
    public java.lang.Object getProperty(java.lang.String name) {
        return null;
    }
    public void setContentHandler(ContentHandler handler) {
        this.contentHandler = handler;
    }
    public void setDTDHandler(DTDHandler handler) {
        this.dtdHandler = handler;
    }
    public void setEntityResolver(EntityResolver resolver) {
        this.entityResolver = resolver;
    }
    public void setErrorHandler(ErrorHandler handler) {
        this.errorHandler = handler;
    }
    public void setFeature(java.lang.String name, boolean value) {
        System.out.println("----------- setFeature " + name);
    }
    public void setProperty(java.lang.String name, java.lang.Object value) {
    }




//    /**
//      * Utility to start elements without namespace info.
//      */
//    protected void startElement(String qName, Attributes attributes) throws SAXException {
//        if (null == attributes) {
//            attributes = new AttributesImpl();
//        }
//        contentHandler.startElement("", "", qName, attributes);
//    }
//
//    /**
//      * Utility to end elements without namespace info.
//      */
//    protected void endElement(String qName) throws SAXException {
//        contentHandler.endElement("", "", qName);
//    }
//
//    /**
//      * Utility method to abstract SAX interfaces.
//      */
//    protected void startDocument() throws SAXException {
//        contentHandler.startDocument();
//    }
//
//    /**
//      * Utility method to abstract SAX interfaces.
//      */
//    protected void endDocument() throws SAXException {
//        contentHandler.endDocument();
//    }

    /////////////// write methods ///////////////
    /**
      * Utility method to write characters to the ContentHandler.
      */
    protected void write(String s) throws SAXException {
        contentHandler.characters(s.toCharArray(), 0, s.length());
    }
    protected void write(char x) throws SAXException {
        write(String.valueOf(x));
    }
    protected void write(byte x) throws SAXException {
        write(String.valueOf(x));
    }
    protected void write(boolean x) throws SAXException {
        write(String.valueOf(x));
    }
    protected void write(int x) throws SAXException {
        write(String.valueOf(x));
    }
    protected void write(long x) throws SAXException {
        write(String.valueOf(x));
    }
    protected void write(float x) throws SAXException {
        write(String.valueOf(x));
    }
    protected void write(double x) throws SAXException {
        write(String.valueOf(x));
    }
    protected void write(Object x) throws SAXException {
        write(x.toString());
    }


}
