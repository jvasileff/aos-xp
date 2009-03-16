package org.anodyneos.xp.tag.fmt;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

import javax.servlet.jsp.el.ELException;

import org.anodyneos.servlet.xsl.xalan.AutoHtmlParserUrlGen;
import org.anodyneos.servlet.xsl.xalan.AutoHtmlParserUrlGenDefault;
import org.anodyneos.servlet.xsl.xalan.BBCodeParser;
import org.anodyneos.servlet.xsl.xalan.ParseException;
import org.anodyneos.xp.XpException;
import org.anodyneos.xp.XpOutput;
import org.anodyneos.xp.tagext.XpFragment;
import org.anodyneos.xp.tagext.XpTagSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class BBCodeTag extends XpTagSupport {

    public static final String XHTML_NS = "http://www.w3.org/1999/xhtml";
    public static final String XHTML_PREFIX = "xhtml";

    public static final String MODE_HTML = "html";
    public static final String MODE_TEXT = "text";

    private static final Log logger = LogFactory.getLog(BBCodeTag.class);

    private String text;
    private String mode = MODE_HTML;

    public void doTag(XpOutput out) throws XpException, ELException,
            SAXException {
        if (null == text) {
            XpFragment body = getXpBody();
            if (null == body) {
                text = "";
            } else {
                text = body.invokeToString();
            }
        }
        if (! "".equals(text)) {
            try {
                if (MODE_TEXT.equals(getMode())) {
                    BBCodeParserXpText ahps = new BBCodeParserXpText(new java.io.StringReader(text), out);
                    ahps.process();
                } else {
                    BBCodeParserXp ahps = new BBCodeParserXp(new java.io.StringReader(text), out);
                    ahps.process();
                }
            } catch (Exception e) {
                // TODO:
            }
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

    protected class BBCodeParserXpText extends BBCodeParser {

        private XpOutput out;
        private StringBuffer sb;
        private String delayedUrl;

        protected BBCodeParserXpText(InputStream stream, XpOutput out) {
            super(stream);
            this.out = out;
        }

        protected BBCodeParserXpText(java.io.Reader stream, XpOutput out) {
            super(stream);
            this.out = out;
        }

        protected void process() throws ParseException {
            Input();
            flushText();
        }

        protected void processWord(String s)   { addText(s); }
        protected void processSpace(String s)  { addText(s); }
        protected void processEol(String s)    { addText(s); }
        protected void processEmail(String s)  { addText(s); }
        protected void processUrl(String s)    { addText(s); }
        protected void processFtp(String s)    { addText(s); }
        protected void processWww(String s)    { addText(s); }

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
                    out.write(sb.toString().toCharArray(), 0, sb.length());
                    sb.setLength(0);
                }
            } catch (SAXException e) {
                logger.error("FIXME: Thrown away SAXException.", e);
                // FIXME: should not throw away exception
            }
        }

        protected void processInvalidOpen(String arg0) { processWord(arg0); }

        protected void processCloseTag(String arg0) {
            if (BBCodeParser.TAG_URL.equals(arg0) && null != delayedUrl) {
                processWord(" <" + delayedUrl + ">");
                delayedUrl = null;
            } else if (BBCodeParser.TAG_QUOTE.equals(arg0) || BBCodeParser.TAG_CODE.equals(arg0)) {
                // parser removes whitespace around block tags (quote & code).  So, add some whitespace.
                addText('\n');
                addText('\n');
            }
        }

        protected void processOpenColorTag(String arg0) { /* noop */ }
        protected void processOpenSizeTag(String arg0) { /* noop */ }

        protected void processOpenSimpleTag(String arg0) {
            // parser removes whitespace around block tags (quote & code).  So, add some whitespace.
            if (BBCodeParser.TAG_QUOTE.equals(arg0) || BBCodeParser.TAG_CODE.equals(arg0)) {
                addText('\n');
                addText('\n');
            }
        }

        protected void processOpenUrlEmailTag(String arg0) { delayedUrl = arg0; }
        protected void processOpenUrlFtpTag(String arg0) { delayedUrl = arg0; }
        protected void processOpenUrlTag(String arg0) { delayedUrl = arg0; }
        protected void processOpenUrlWwwTag(String arg0) { delayedUrl = arg0; }
    }

    protected class BBCodeParserXp extends BBCodeParser {

        private XpOutput out;
        private StringBuffer sb;
        private AutoHtmlParserUrlGen urlGen;
        private List elementStack = new ArrayList();

        protected BBCodeParserXp(InputStream stream, XpOutput out) {
            super(stream);
            this.out = out;
        }

        protected BBCodeParserXp(java.io.Reader stream, XpOutput out) {
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
                    out.write(sb.toString().toCharArray(), 0, sb.length());
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
                out.startElement(XHTML_NS, XHTML_PREFIX + ":a");
                out.addAttribute("", "href", href);
                out.write(display.toCharArray(), 0, display.length());
                out.endElement(XHTML_NS, XHTML_PREFIX + ":a");
            } catch (SAXException e) {
                logger.error("FIXME: Thrown away SAXException.", e);
                // FIXME: should not throw away exception
            }
        }

        private void addEol(String s) {
            flushText();
            try {
                // "br" element
                out.startElement(XHTML_NS, XHTML_PREFIX + ":br");
                out.endElement(XHTML_NS, XHTML_PREFIX + ":br");
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

        protected void processCloseTag(String s) {
            flushText();
            Element[] elements = (Element[]) elementStack.remove(elementStack.size()-1);
            for(int i = 0; i < elements.length; i++) {
                Element el = elements[i];
                try {
                    out.endElement(el.namespace, el.qName);
                } catch (SAXException e) {
                    logger.error("FIXME: Thrown away SAXException.", e);
                    // FIXME: should not throw away exception
                }
            }
        }

        protected void processInvalidOpen(String s) {
            processWord(s);
        }

        protected void processOpenColorTag(String s) {
            flushText();
            try {
                Element el = new Element(XHTML_NS, XHTML_PREFIX + ":span");
                pushElements(new Element[] {el});
                startElement(el);
                out.addAttribute("", "style", "color:" + s + ";");
            } catch (SAXException e) {
                logger.error("FIXME: Thrown away SAXException.", e);
                // FIXME: should not throw away exception
            }
        }

        protected void processOpenSimpleTag(String s) {
            flushText();

            // <#SIMPLE_TAG:   "b" | "i" | "u" | "s" | "quote" | "code" >

            try {
                if (BBCodeParser.TAG_BOLD.equals(s)) {
                    Element el = new Element(XHTML_NS, XHTML_PREFIX + ":span");
                    pushElements(new Element[] {el});
                    startElement(el);
                    out.addAttribute("", "style", "font-weight:bold;");
                } else if (BBCodeParser.TAG_ITALICS.equals(s)) {
                    Element el = new Element(XHTML_NS, XHTML_PREFIX + ":span");
                    pushElements(new Element[] {el});
                    startElement(el);
                    out.addAttribute("", "style", "font-style:italic;");
                } else if (BBCodeParser.TAG_UNDERLINE.equals(s)) {
                    Element el = new Element(XHTML_NS, XHTML_PREFIX + ":span");
                    pushElements(new Element[] {el});
                    startElement(el);
                    out.addAttribute("", "style", "text-decoration:underline;");
                } else if (BBCodeParser.TAG_STRIKETHROUGH.equals(s)) {
                    Element el = new Element(XHTML_NS, XHTML_PREFIX + ":span");
                    pushElements(new Element[] {el});
                    startElement(el);
                    out.addAttribute("", "style", "text-decoration:line-through;");
                } else if (BBCodeParser.TAG_QUOTE.equals(s)) {
                    Element el1 = new Element(XHTML_NS, XHTML_PREFIX + ":blockquote");
                    Element el2 = new Element(XHTML_NS, XHTML_PREFIX + ":p");
                    pushElements(new Element[] {el2, el1});
                    startElement(el1);
                    startElement(el2);
                } else if (BBCodeParser.TAG_CODE.equals(s)) {
                    Element el = new Element(XHTML_NS, XHTML_PREFIX + ":pre");
                    pushElements(new Element[] {el});
                    startElement(el);
                }
            } catch (SAXException e) {
                logger.error("FIXME: Thrown away SAXException.", e);
                // FIXME: should not throw away exception
            }
        }

        protected void processOpenSizeTag(String s) {
            flushText();
            try {
                Element el = new Element(XHTML_NS, XHTML_PREFIX + ":span");
                pushElements(new Element[] {el});
                startElement(el);
                out.addAttribute("", "style", "font-size:" + s + "%;");
            } catch (SAXException e) {
                logger.error("FIXME: Thrown away SAXException.", e);
                // FIXME: should not throw away exception
            }
        }

        protected void processOpenUrlFtpTag(String s) { _processOpenUrlTag(urlGen.ftpToUrl(s)); }
        protected void processOpenUrlWwwTag(String s) { _processOpenUrlTag(urlGen.wwwToUrl(s)); }
        protected void processOpenUrlEmailTag(String s) { _processOpenUrlTag(urlGen.emailToUrl(s)); }
        protected void processOpenUrlTag(String s) { _processOpenUrlTag(urlGen.urlToUrl(s)); }

        protected void _processOpenUrlTag(String s) {
            flushText();
            try {
                Element el = new Element(XHTML_NS, XHTML_PREFIX + ":a");
                pushElements(new Element[] {el});
                startElement(el);
                out.addAttribute("", "href", s);
            } catch (SAXException e) {
                logger.error("FIXME: Thrown away SAXException.", e);
                // FIXME: should not throw away exception
            }
        }

        private void pushElements(Element[] elements) {
            this.elementStack.add(elements);
        }

        private void startElement(Element el) {
            try {
                out.startElement(el.namespace, el.qName);
            } catch (SAXException e) {
                logger.error("FIXME: Thrown away SAXException.", e);
                // FIXME: should not throw away exception
            }
        }
    }

    protected class Element {
        protected Element(String namespace, String qName) {
            this.namespace = namespace;
            this.qName = qName;
        }

        String namespace;
        String qName;
    }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

}
