package org.anodyneos.xpImpl.translater;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.anodyneos.commons.xml.sax.ElementProcessor;
import org.anodyneos.xpImpl.util.CodeWriter;
import org.anodyneos.xpImpl.util.Util;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * @author jvas
 */
public class ProcessorFragment extends TranslaterProcessorNonResultContent {

    private StringBuffer sb;

    private ProcessorResultContent resultContentProcessor;
    private Map savedPrefixMappings;

    private boolean fragmentStarted = false;
    private int fragmentId = -1;

    public ProcessorFragment(TranslaterContext ctx) {
        super(ctx);

        // most of our reall setup is done in startFragment();
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName)
            throws SAXException {

        if (!fragmentStarted) {
            throw new IllegalStateException("getProcessorFor() called prior to startFragment()");
        }

        final ElementProcessor p;

        p = resultContentProcessor.getProcessorFor(uri, localName, qName);
        // since no exception, start fragment if not already started. Dump characters.
        // Return p (p may be the bodyFragmentProcessor itself if the content is result
        // content, but bodyFragmentProcessor made the decision.)
        if(null != sb) {
            char[] chars = sb.toString().toCharArray();
            resultContentProcessor.characters(chars, 0, chars.length);
            sb = null;
            // make sure to flush characters in processor
            resultContentProcessor.flushCharacters();
        }
        return p;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {

        // this processor may be used with or without an element.  startElement is called in cases
        // such as <xp:body>.  The only thing the outer element provides is possible namespace mappings.

        if (!fragmentStarted) {
            throw new IllegalStateException("startElement() called prior to startFragment()");
        }

        // the super-class adds phantom prefix mappings, so we don't have to do anything.
    }

    public void characters(char[] ch, int start, int length) {
        if (!fragmentStarted) {
            throw new IllegalStateException("startElement() called prior to startFragment()");
        }

        if (null == sb) {
            sb = new StringBuffer();
        }
        sb.append(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (!fragmentStarted) {
            throw new IllegalStateException("startElement() called prior to startFragment()");
        }
        if (null != sb && ! sb.toString().trim().equals("")) {
            char[] chars = sb.toString().toCharArray();
            resultContentProcessor.characters(chars, 0, chars.length);
            sb = null;
            // make sure to flush characters in processor
            resultContentProcessor.flushCharacters();
        }

        resultContentProcessor.flushCharacters();

        // the super-class pops phantom prefix mappings, so we don't have to do anything.

    }

    public void endFragment() throws SAXException {
        if (!fragmentStarted) {
            throw new IllegalStateException("startElement() called prior to startFragment()");
        }

        if (null != sb && ! sb.toString().trim().equals("")) {
            char[] chars = sb.toString().toCharArray();
            resultContentProcessor.characters(chars, 0, chars.length);
            sb = null;
            // make sure to flush characters in processor
            resultContentProcessor.flushCharacters();
        }

        resultContentProcessor.flushCharacters();

        CodeWriter out = getTranslaterContext().getCodeWriter();

        out.println();
        out.printIndent().println("if (! namespaceCompat) {");
        out.indentPlus();
        Iterator it = savedPrefixMappings.keySet().iterator();
        for(int i = savedPrefixMappings.size(); i > 0; i--) {
            out.printIndent().println("xpCH.popPhantomPrefixMapping();");
        }
        out.endBlock();

        getTranslaterContext().endFragment();
    }

    public void startFragment() {
        if (fragmentStarted) {
            throw new IllegalStateException("startFragment() called when already started.");
        }

        TranslaterContext ctx = getTranslaterContext();

        resultContentProcessor = new ProcessorResultContent(ctx);
        // when we write the fragment, we will need to copy the source tree's prefix mappings into the output
        // tree in case the fragment is used outside of the immediate parent in the source tree.
        savedPrefixMappings = new HashMap();
        NamespaceSupport ns = getTranslaterContext().getNamespaceSupport();
        Enumeration e = ns.getPrefixes();
        while (e.hasMoreElements()) {
            String nsPrefix = (String) e.nextElement();
            String nsURI = ns.getURI(nsPrefix);
            savedPrefixMappings.put(nsPrefix, nsURI);
        }
        String uri = ns.getURI("");
        if(null == uri) {
            uri = "";
        }
        savedPrefixMappings.put("", uri);

        fragmentId = getTranslaterContext().startFragment();
        CodeWriter out = getTranslaterContext().getCodeWriter();
        // check to see if we need to declare our prefixes
        out.printIndent().println("boolean namespaceCompat = xpCH.isNamespaceContextCompatible(origXpCH, parentElClosed, origContextVersion, origAncestorsWithPrefixMasking, origPhantomPrefixCount);");
        out.printIndent().println("if (! namespaceCompat) {");
        out.indentPlus();
        Iterator it = savedPrefixMappings.keySet().iterator();
        while (it.hasNext()) {
            String nsPrefix = (String) it.next();
            String nsURI = (String) savedPrefixMappings.get(nsPrefix);
            out.printIndent().println(
                    "xpCH.pushPhantomPrefixMapping("
                +        Util.escapeStringQuoted(nsPrefix)
                + ", " + Util.escapeStringQuoted(nsURI)
                + ");");
        }
        out.endBlock();
        out.println();

        fragmentStarted = true;
    }

    public int getFragmentId() {
        if (!fragmentStarted) {
            throw new IllegalStateException("getFragmentId() called prior to startFragment()");
        }
        return fragmentId;
    }

    public boolean isFragmentStarted() {
        return fragmentStarted;
    }

}
