package org.anodyneos.xp.tag.fmt;

import java.io.InputStream;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

import javax.servlet.jsp.el.ELException;

import org.anodyneos.servlet.xsl.xalan.AutoHtmlParser;
import org.anodyneos.servlet.xsl.xalan.AutoHtmlParserUrlGen;
import org.anodyneos.servlet.xsl.xalan.AutoHtmlParserUrlGenDefault;
import org.anodyneos.servlet.xsl.xalan.ParseException;
import org.anodyneos.xp.XpContentHandler;
import org.anodyneos.xp.XpException;
import org.anodyneos.xp.tagext.XpTagSupport;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class AutoHtmlTag extends XpTagSupport {

    private String text;

    public void doTag(XpContentHandler out) throws XpException, ELException,
            SAXException {
        if (null == text) {
            text = getXpBody().invokeToString();
        }
        AutoHtmlParserSAX ahps = new AutoHtmlParserSAX(new java.io.StringReader(text), out);
        try {
            ahps.process();
        } catch (Exception e) {
            // TODO:
        }
    }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    protected static AutoHtmlParserUrlGen urlGenDefault = new AutoHtmlParserUrlGenDefault();
    protected static Attributes emptyAttributes = new AttributesImpl();
    protected class AutoHtmlParserSAX extends AutoHtmlParser {

        // TODO: buffering was from DOM, we should output immediately.
        private StringBuffer sb;
        private ContentHandler ch;
        private AutoHtmlParserUrlGen urlGen;

        protected AutoHtmlParserSAX(InputStream stream, ContentHandler ch) {
            super(stream);
            this.ch = ch;
        }

        protected AutoHtmlParserSAX(java.io.Reader stream, ContentHandler ch) {
            super(stream);
            this.ch = ch;
        }

        protected void process() throws ParseException {
            if (urlGen == null) {
                urlGen = urlGenDefault;
            }
            Input();
            flushText();
        }

        protected void processWord(String s)   { addText(s); }
        protected void processSpace(String s)  { addSpace(s); }
        protected void processEol(String s)    { addEol(s); }
        protected void processEmail(String s)  { addHref(urlGen.emailToUrl(s), s); }
        protected void processUrl(String s)    { addHref(urlGen.urlToUrl(s), s); }
        protected void processFtp(String s)    { addHref(urlGen.ftpToUrl(s), s); }
        protected void processWww(String s)    { addHref(urlGen.wwwToUrl(s), s); }

        private void addSpace(String s) {
            addText(' ');
        }

        private void addText(char c) {
            if (null == sb) {
                sb = new StringBuffer();
            }
            sb.append(c);
        }

        private void addText(String s) {
            if (null == sb) {
                sb = new StringBuffer();
            }
            sb.append(s);
        }

        private void flushText() {
            // Write contents of sb to node.
            try {
                if (null != sb) {
                    ch.characters(sb.toString().toCharArray(), 0, sb.length());
                    sb.setLength(0);
                }
            } catch (SAXException e) {
                // FIXME: should not throw away exception
            }
        }

        private void addHref(String href, String display) {
            // first, write out cached text to node
            flushText();

            // href attribute
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("", "href", "href", "CDATA", href);

            try {
                // "a" element
                ch.startPrefixMapping("", "http://www.w3.org/1999/xhtml");
                ch.startElement("http://www.w3.org/1999/xhtml", "a", "a", atts);
                ch.characters(display.toCharArray(), 0, display.length());
                ch.endElement("http://www.w3.org/1999/xhtml", "a", "a");
                ch.endPrefixMapping("");
            } catch (SAXException e) {
                // FIXME: should not throw away exception
            }
        }

        private void addEol(String s) {
            flushText();
            try {
                // "br" element
                ch.startPrefixMapping("", "http://www.w3.org/1999/xhtml");
                ch.startElement("http://www.w3.org/1999/xhtml", "br", "br", emptyAttributes);
                ch.endElement("http://www.w3.org/1999/xhtml", "br", "br");
                ch.endPrefixMapping("");
            } catch (SAXException e) {
                // FIXME: should not throw away exception
            }
        }

        public AutoHtmlParserUrlGen getUrlGen() {
            return urlGen;
        }

        public void setUrlGen(AutoHtmlParserUrlGen urlGen) {
            this.urlGen = urlGen;
        }
}
}
