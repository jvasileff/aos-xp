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
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * ProcessorFragment creates fragments for elements that declare a fragment (ie xp:body, xp:content)
 * or mixed content.  For mixed content, the fragments boundaries must be set using
 * startFragment() and endFragment().
 *
 * @author jvas
 */
public abstract class ProcessorFragment extends TranslaterProcessorNonResultContent {

    private StringBuffer sb;

    private ProcessorResultContent resultContentProcessor;
    private Map savedPrefixMappings;

    private boolean inFragment = false;
    private boolean fragmentExists = false;
    private int fragmentId = -1;

    private Attributes attributes;

    public ProcessorFragment(TranslaterContext ctx) {
        super(ctx);
    }

    public ElementProcessor getProcessorFor(String uri, String localName, String qName)
            throws SAXException {

        if (!inFragment) {
            throw new IllegalStateException("getProcessorFor() called prior to startFragment()");
        }

        final ElementProcessor p;

        p = resultContentProcessor.getProcessorFor(uri, localName, qName);
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

        if (inFragment) {
            throw new IllegalStateException("startElement() called after startFragment()");
        }
        this.attributes = new AttributesImpl(attributes);
        startFragment();
    }

    public void characters(char[] ch, int start, int length) {
        if (!inFragment) {
            throw new IllegalStateException("characters() called prior to startFragment()");
        }

        if (null == sb) {
            sb = new StringBuffer();
        }
        sb.append(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (!inFragment) {
            throw new IllegalStateException("endElement() called prior to startElement()");
        }
        if (null != sb && ! sb.toString().trim().equals("")) {
            char[] chars = sb.toString().toCharArray();
            resultContentProcessor.characters(chars, 0, chars.length);
            sb = null;
            // make sure to flush characters in processor
            resultContentProcessor.flushCharacters();
        }

        resultContentProcessor.flushCharacters();

        endFragment();
    }

    public void endFragment() throws SAXException {
        if (!inFragment) {
            throw new IllegalStateException("endFragment() called prior to startFragment()");
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
        out = null; // fragment closed, out no longer valid.

        inFragment = false;
        fragmentExists = true;

        // we need to create the FragmentHelper here to make sure it appears between
        // push and pop phantom prefix mappings in the code.  This is because endPrefixMapping
        // is called after endElement.
        String parentTagVar = "xpTagParent";
        String origXpChVar = "xpCH";
        if (! getTranslaterContext().inFragment()) {
            // for the root fragment there is no parentTag and the contentHandler has no initial
            // namespace mappings.
            parentTagVar = "null";
            origXpChVar = "null";
        }

        process("new FragmentHelper("
                + fragmentId + ", xpContext, " + parentTagVar + ", " + origXpChVar + ")");
    }

    public abstract void process(String expr) throws SAXException;

    public void startFragment() {
        if (inFragment || fragmentExists) {
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

        inFragment = true;
    }

    public boolean isFragmentStarted() {
        return inFragment;
    }
    public boolean isFragmentExists() {
        return fragmentExists;
    }

    public Attributes getAttributes() {
        return attributes;
    }

}
