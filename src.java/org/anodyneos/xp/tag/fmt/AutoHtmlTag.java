package org.anodyneos.xp.tag.fmt;

import java.io.InputStream;

import org.xml.sax.Attributes;

import javax.servlet.jsp.el.ELException;

import org.anodyneos.servlet.xsl.xalan.AutoHtmlParser;
import org.anodyneos.servlet.xsl.xalan.AutoHtmlParserUrlGen;
import org.anodyneos.servlet.xsl.xalan.AutoHtmlParserUrlGenDefault;
import org.anodyneos.servlet.xsl.xalan.ParseException;
import org.anodyneos.xp.XpContentHandler;
import org.anodyneos.xp.XpException;
import org.anodyneos.xp.XpOutput;
import org.anodyneos.xp.tagext.XpTagSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class AutoHtmlTag extends XpTagSupport {

    private static final Log logger = LogFactory.getLog(AutoHtmlTag.class);

    private String text;

    public void doTag(XpContentHandler out) throws XpException, ELException,
            SAXException {
        if (null == text) {
            text = getXpBody().invokeToString();
        }
        AutoHtmlParserXp ahps = new AutoHtmlParserXp(new java.io.StringReader(text), new XpOutput(out));
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
    protected class AutoHtmlParserXp extends AutoHtmlParser {

        // TODO: buffering was from DOM, we should output immediately.
        private StringBuffer sb;
        private XpOutput out;
        private AutoHtmlParserUrlGen urlGen;

        protected AutoHtmlParserXp(InputStream stream, XpOutput out) {
            super(stream);
            this.out = out;
        }

        protected AutoHtmlParserXp(java.io.Reader stream, XpOutput out) {
            super(stream);
            this.out = out;
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
                    out.characters(sb.toString().toCharArray(), 0, sb.length());
                    sb.setLength(0);
                }
            } catch (SAXException e) {
                logger.error("FIXME: Thrown away SAXException.", e);
                // FIXME: should not throw away exception
            }
        }

        private void addHref(String href, String display) {
            // first, write out cached text to node
            flushText();

            try {
                // "a" element
                out.startElement("http://www.w3.org/1999/xhtml", "xhtml:a");
                out.addAttribute("", "href", href);
                out.characters(display.toCharArray(), 0, display.length());
                out.endElement("http://www.w3.org/1999/xhtml", "xhtml:a");
            } catch (SAXException e) {
                logger.error("FIXME: Thrown away SAXException.", e);
                // FIXME: should not throw away exception
            }
        }

        private void addEol(String s) {
            flushText();
            try {
                // "br" element
                out.startElement("http://www.w3.org/1999/xhtml", "xhtml:br");
                out.endElement("http://www.w3.org/1999/xhtml", "xhtml:br");
            } catch (SAXException e) {
                logger.error("FIXME: Thrown away SAXException.", e);
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
