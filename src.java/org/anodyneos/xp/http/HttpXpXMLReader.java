package org.anodyneos.xp.http;

import javax.servlet.jsp.el.ELException;

import org.anodyneos.xp.XpContentHandler;
import org.anodyneos.xp.XpException;
import org.anodyneos.xp.XpPage;
import org.anodyneos.xp.XpContext;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class HttpXpXMLReader implements XMLReader {

  private XpPage xp;
  private XpContext xpContext;

  public HttpXpXMLReader(XpPage xp, XpContext xpContext) {
      this.xp = xp;
      this.xpContext = xpContext;
  }

  public void parse(InputSource input) throws SAXException {

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
