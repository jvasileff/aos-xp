package org.anodyneos.xp;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public final class XpOutput {

    public static final int XML_MODE = 0;
    public static final int RAW_MODE = 1;

    private final XpContentHandler ch;
    private int mode = XML_MODE;

    /*
    public XpOutput(XpContentHandler ch) {
        this.ch = ch;
    }

    public XpOutput(XpContentHandler ch, int mode) {
        this.ch = ch;
        setMode(mode);
    }
    */

    public XpOutput(ContentHandler ch) {
        this.ch = XpContentHandlerFactory.getDefaultFactory().getXpContentHandler(ch);
    }

    public XpOutput(ContentHandler ch, int mode) {
        this.ch = XpContentHandlerFactory.getDefaultFactory().getXpContentHandler(ch);
        setMode(mode);
    }

    private void setMode(int mode) {
        if (mode < 0 || mode > 1) {
            throw new IllegalArgumentException("Invalid mode: " + mode);
        } else {
            this.mode = mode;
        }
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

    public static final char CR = '\r';
    public static final char LF = '\n';

    /**
     * Writes text content to the result XML document with proper CRLF conversions.
     *
     * @param ch
     * @param start
     * @param length
     * @throws SAXException
     */
    public void write(char[] ch, int start, int length) throws SAXException {
        if(null == ch || length < 1) {
            return;
        }

        if (RAW_MODE == mode) {
            // perform no CRLF translation
            this.ch.characters(ch, start, length);
        } else {
            int lastSent = start - 1;
            boolean lastWasCR = false;
            char current;

            // send out one chunk at a time
            for (int i = start; i < start + length; i++) {
                current = ch[i];
                if (current == CR) {
                    if (lastWasCR) {
                        // two CR's back-to-back, send LF for the last one
                        this.ch.characters(LF);
                    }
                    lastWasCR = true;

                    // we won't be outputing this CR, so send previous characters now,
                    // but not if length == 0
                    if (! (lastSent + 1 == i)) {
                        this.ch.characters(ch, lastSent + 1, i - lastSent - 1);
                    }
                    lastSent = i;
                } else if (lastWasCR && current == LF) {
                    // the last CR matches this LF; lets keep the LF but dump the CR
                    lastWasCR = false;
                    assert lastSent == i - 1;
                } else if (lastWasCR) {
                    // last was a CR that needs to be a LF
                    lastWasCR = false;
                    this.ch.characters(LF);
                    assert lastSent == i - 1;
                }
            }
            if (lastWasCR) {
                this.ch.characters(LF);
                assert lastSent == start + length - 1;
            } else  if (lastSent + 1 < start + length) {
                this.ch.characters(ch, lastSent + 1, length - (lastSent - start + 1));
            }
        }
    }

    /**
     * Writes text content to the result XML document with proper CRLF conversions.
     *
     * @param s
     * @throws SAXException
     */
    public void write(String s) throws SAXException {
        if (null != s) {
            write(s.toCharArray(), 0, s.length());
        } else {
            // no CRLF conversion needed, use this.ch
            this.ch.characters((String) null);
        }
    }

    /**
     * Writes text content to the result XML document with proper CRLF conversions.
     *
     * @param x
     * @throws SAXException
     */
    public void write(Object x) throws SAXException {
        if (null != x) {
            write(x.toString());
        } else {
            write((String) null);
        }
    }

    public void write(char x) throws SAXException {
        this.ch.characters(x);
    }

    public void write(byte x) throws SAXException {
        this.ch.characters(x);
    }

    public void write(boolean x) throws SAXException {
        this.ch.characters(x);
    }

    public void write(int x) throws SAXException {
        this.ch.characters(x);
    }

    public void write(long x) throws SAXException {
        this.ch.characters(x);
    }

    public void write(float x) throws SAXException {
        this.ch.characters(x);
    }

    public void write(double x) throws SAXException {
        this.ch.characters(x);
    }

}
