package org.anodyneos.xpImpl;

import java.util.Enumeration;
import java.util.List;

import org.anodyneos.xp.XpContentHandler;
import org.anodyneos.xpImpl.runtime.NamespaceMappings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * XpContentHandler wraps a SAX ContentHandler and adds XP specific support. The
 * following features are provided:
 *
 * 1. Attributes may be set using XpContentHandler any time after startElement
 * is called, but before any other node is added.
 *
 * 2. setNamespacePrefixes() controls the passing of xmlns attributes to the
 * wrapped content handler. This will normally be set by an XMLReader when the
 * feature "http://xml.org/sax/features/namespaces" is set. This class will both
 * filter attributes that are passed to it and generate new attributes as
 * necessary. The default value is false (same as XMLReader's default.)
 *
 * 3. Start and end prefix mapping calls to the wrapped ContentHandler are
 * managed by this class.  Calls to startPrefixMapping may only be followed by
 * additional calls to startPrefixMapping or startElement(). Calls to
 * endPrefixMapping() are ignored - this class will track mappings and make the
 * necessary calls to the wrapped ContentHandler.
 *
 * 4. Convenience methods such as characters(String s).
 *
 * 5. Tracking of prefix to namespace URI mappings.
 *
 * 6. Convenient StartElement and EndElement methods that take uri and qName. See
 * comments for parameter rules and prefix calculation.
 *
 * @author John Vasileff
 *
 * TODO: implement text-only output stack.
 *
 * TODO: handle clearing of default namespace when non-XP code uses the contentHandler.
 *
 * TODO: provide runtime support for excludeResultPrefixes - all mappings should be tracked and available to
 * the runtime code, but output for the excluded prefixes should be suppressed as best possbile.  One implementation
 * would be to use an XMLFilter in order to avoid this class becoming even harder to read.
 */
public final class XpContentHandlerImpl implements XpContentHandler {

    private static final String NULL_STRING = "null";

    private static final Log logger = LogFactory.getLog(XpContentHandlerImpl.class);

    // instance variables to test for logging for performance.
    private boolean logDebugEnabled = logger.isDebugEnabled();

    /**
     * current setting for the SAX feature "http://xml.org/sax/features/namespace-prefixes".
     */
    private boolean namespacePrefixes = false;

    /**
     * tracks prefix to namespace URI mappings.
     */
    private NamespaceMappings namespaceMappings = new NamespaceMappings();

    private int lastEvent = -1;

    /**
     * holds values for the next element, set by startElement().  When flush() is called, these values
     * are passed to the wrapped ContentHandler's startElemement() methods and then set to null.
     */
    private String bufferedElLocalName;
    private String bufferedElQName;
    private String bufferedElNamespaceURI;

    /**
     * holds attributes relevent to the "nextEl".  When nextElLocalName is null this object will be empty.
     */
    private AttributesImpl bufferedElAttributes = new AttributesImpl();

    private ContentHandler wrappedContentHandler;

    private static final int EVENT_CHARACTERS = 0;
    private static final int EVENT_END_DOCUMENT = 1;
    private static final int EVENT_END_ELEMENT = 2;
    private static final int EVENT_END_PREFIX_MAPPING = 3;
    private static final int EVENT_IGNORABLE_WHITESPACE = 4;
    private static final int EVENT_PROCESSING_INSTRUCTION = 5;
    private static final int EVENT_SKIPPED_ENTITY = 6;
    private static final int EVENT_START_DOCUMENT = 7;
    private static final int EVENT_START_ELEMENT = 8;
    private static final int EVENT_START_PREFIX_MAPPING = 9;

    private static final int EVENT_PUSH_PHANTOM_PREFIX_MAPPING = 10;
    private static final int EVENT_POP_PHANTOM_PREFIX_MAPPING = 11;

    XpContentHandlerImpl(ContentHandler contentHandler) {
        this.wrappedContentHandler = contentHandler;
    }

    XpContentHandlerImpl(ContentHandler contentHandler, boolean namespacePrefixes) {
        this.wrappedContentHandler = contentHandler;
        setNamespacePrefixes(namespacePrefixes);
    }

    ////////////////////////////////////////////////////////////////////////////////
    //
    // phantom prefix push/pop
    //
    ////////////////////////////////////////////////////////////////////////////////

    public void pushPhantomPrefixMapping(String prefix, String uri) throws SAXException {
        if(logDebugEnabled) {
            logger.debug("pushPhantomPrefixMapping(\"" + prefix + "\", \"" + uri + "\") called");
        }
        flush(EVENT_PUSH_PHANTOM_PREFIX_MAPPING);
        namespaceMappings.pushPhantomPrefix(prefix, uri);
    }

    public void popPhantomPrefixMapping() throws SAXException {
        if(logDebugEnabled) {
            logger.debug("popPhantomPrefixMapping() called");
        }
        flush(EVENT_POP_PHANTOM_PREFIX_MAPPING);
        namespaceMappings.popPhantomPrefix();
    }

    ////////////////////////////////////////////////////////////////////////////////
    //
    // SAX Methods (managed)
    //
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * startPrefixMapping() must be followed only by zero or more startPrefixMapping
     * calls followed by a startElement() call
     */
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if(logDebugEnabled) {
            logger.debug("startPrefixMapping(\"" + prefix + "\", \"" + uri + "\") called");
        }
        flush(EVENT_START_PREFIX_MAPPING);

        namespaceMappings.declarePrefix(prefix, uri);
    }

    public void startElement( String namespaceURI, String localName, String qName, Attributes atts)
    throws SAXException {

        // **** WARNING **** Any changes made here should also be made in the other startElement() method.

        if(logDebugEnabled) {
            logger.debug("startElement("
                    + namespaceURI
                    + ", " + localName
                    + ", " + qName
                    + ", " + atts
                    + ") called");
        }
        flush(EVENT_START_ELEMENT);

        // buffer this element to allow attributes to be added
        bufferedElNamespaceURI = namespaceURI;
        bufferedElLocalName = localName;
        bufferedElQName = qName;
        bufferedElAttributes.clear();
        if (atts != null) {
            bufferedElAttributes.setAttributes(atts);
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        if(logDebugEnabled) {
            logger.debug("endElement("
                    + namespaceURI
                    + ", " + localName
                    + ", " + qName
                    + ") called");
        }
        flush(EVENT_END_ELEMENT);

        // call endElement on the wrappedContentHandler
        if(logDebugEnabled) {
            logger.debug("   wrappedContentHandler.endElement("
                    + namespaceURI + ", " + localName + ", " + qName + ")");
        }
        wrappedContentHandler.endElement(namespaceURI, localName, qName);

        // endPrefixMapping calls for wrappedContentHandler
        List prefixes = namespaceMappings.popContext2();

        for (int i=0; i < prefixes.size();) {
            String prefix = (String) prefixes.get(i++);
            if(logDebugEnabled) {
                logger.debug("   wrappedContentHandler.endPrefixMapping('" + prefix + "')");
            }
            wrappedContentHandler.endPrefixMapping(prefix);
        }
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        if(logDebugEnabled) {
            logger.debug("endPrefixMapping(\"" + prefix + "\") called");
        }
        flush(EVENT_END_PREFIX_MAPPING);
        // no op: we handle endPrefixMapping calls to the wrapped contentHandler automatically
    }

    ////////////////////////////////////////////////////////////////////////////////
    //
    // Convenience Methods (managed)
    //
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * This method tries to be forgiving within reason; the following rules apply:
     *
     * 1. When uri == null && qName has a prefix: The prefix must be in scope.
     * The namespace URI in the output will be that of the namespace associated
     * with the prefix. If the prefix is not in scope, a SAXException is thrown.
     *
     * 2. When uri == null && qName has no prefix: The qName is used as is with
     * no namespace URI.
     *
     * 3. When uri == "": If a prefix exists, an exception is thrown.  Otherwise,
     * the attribute will have no namespace and be output without a prefix.
     *
     * 4. When uri == someURI: A prefix will be given to the attribute in the
     * following priority:
     *
     * 4.A) the provided prefix if one was provided and it is currently mapped to
     * the uri.
     *
     * 4.B) any prefix that is currently mapped to the URI.
     *
     * 4.C) the provided prefix if it is not currently mapped to some other URI.
     *
     * 4.D) a generated prefix. In the case of C or D, a new namespace mapping
     * will be created.
     */
    public void addAttribute(final String uri, final String qName, final String value)
    throws SAXException {

        // this method currently must be in this class as it violates the contract that startPrefixMapping() must
        // not occur after the startElement() it applies to.  This contract cannot be changed as it would break
        // SAX compatibility.  In addition, this method needs direct access to the internal bufferedAttributes
        // structure.

        if(logDebugEnabled) {
            logger.debug("addAttribute("
                    + uri
                    + ", " + qName
                    + ", value) called");
        }

        if (null == bufferedElLocalName) {
            throw new SAXException("Cannot addAttribute() unless directly after startElement().");
        } else if (qName.equals("xmlns") || qName.startsWith("xmlns:")) {
            // flush() automatically handles namespace prefixes.  We already know about the namespace
            // from startPrefixMapping().
            return;
        } else {
            // lets put this code here, not in flush().  This way the internal state is kept current and we get
            // immediate feedback on errors.

            final String myQName;
            final String myURI;
            final String prefix = parsePrefix(qName);
            final String localName = parseLocalName(qName);

            if (localName.length() == 0) {
                throw new SAXException("Could not determine localName for attribute: '" + qName + "'.");
            }

            if (null == uri) {
                if (prefix.length() == 0) {
                    myQName = localName;
                    myURI = "";
                } else {
                    // a prefix was provided
                    // the prefix must be in scope to determine URI
                    // the prefix may be a phantom
                    String u = namespaceMappings.getURI(prefix, true);
                    if (null == u) {
                        throw new SAXException("Cannot find URI for '" + qName + "' and none was provided.");
                    } else {
                        String existingURI = namespaceMappings.getURI(prefix, false);
                        if (null == existingURI) {
                            myQName = prefix + ":" + localName;
                            myURI = u;
                            // declare the phantom prefix
                            namespaceMappings.declarePrefix(prefix, myURI);
                        } else if (existingURI.equals(u)) {
                            // no need to make new declaration
                            myQName = prefix + ":" + localName;
                            myURI = u;
                        } else {
                            // existingURI does not match calculated URI for provided prefix
                            String genP = genPrefix();
                            myQName = genP + ":" + localName;
                            myURI = u;
                            // declare the generated prefix
                            namespaceMappings.declarePrefix(genP, myURI);
                        }
                    }
                }
            } else if (uri.length() == 0) {
                if (prefix.length() != 0) {
                    throw new SAXException("Prefix not allowed for '" + qName + "' when namespace URI = ''.");
                }
                // else, use "" URI and no prefix
                myQName = localName;
                myURI = "";
            } else {
                // we have a uri, lets find a good prefix
                // don't search phantoms
                if (prefix.length() != 0 && uri.equals(namespaceMappings.getURI(prefix, false))) {
                    // Case A: prefix was provided and uri matches
                    myQName = qName;
                    myURI = uri;
                } else {
                    // don't search phantoms
                    final String existingPrefix = namespaceMappings.getPrefix(uri, false);
                    if (null != existingPrefix) {
                        // Case B: we already have a perfectly good prefix
                        myQName = existingPrefix + ":" + localName;
                        myURI = uri;
                    // don't search phantoms
                    } else if (prefix.length() != 0 && (null == namespaceMappings.getURI(prefix, false))) {
                        // Case C: the provided prefix will do; create new namespace mapping
                        if (logDebugEnabled) {
                            logger.debug("   addAttribute calls declarePrefix('" + prefix + "', '" + uri + "')");
                        }
                        namespaceMappings.declarePrefix(prefix, uri);
                        myQName = qName;
                        myURI = uri;
                    } else {
                        // Case D1: try a phantom prefix
                        // Case D2: or, generate a new prefix
                        String p = namespaceMappings.getPrefix(uri, true);
                        // p may be a phantom-prefix; if it conflicts with a non-phantom prefix, we can't use it
                        if (null == p || null != namespaceMappings.getURI(p, false)) {
                            p = genPrefix();
                        }
                        if (logDebugEnabled) {
                            logger.debug("   addAttribute calls declarePrefix('" + p + "', '" + uri + "')");
                        }
                        namespaceMappings.declarePrefix(p, uri);
                        myQName = p + ":" + localName;
                        myURI = uri;
                    }
                }
            }
            bufferedElAttributes.addAttribute(myURI, localName, myQName, "CDATA", value);
        }
    }

    /**
     * This method tries to be forgiving, the following rules apply:
     *
     * 1. When uri == null && qName has a prefix: The prefix must be in scope.
     * The namespace URI in the output will be that of the namespace associated
     * with the prefix. If the prefix is not in scope, a SAXException is thrown.
     *
     * 2. When uri == null && qName has no prefix: The qName is used as is with
     * the current default namespace URI.
     *
     * 3. When uri == "": The element will have no namespace and the default xmlns
     * will be set to "".
     *
     * 4. When uri == someURI: A prefix will be given to the attribute in the
     * following priority:
     *
     * 4.a) If the uri is the current default namespace, no prefix will be used.
     *
     * 4.b) the provided prefix if one was provided and it is currently mapped to
     * the uri.
     *
     * 4.c) a prefix that is currently mapped to the URI.
     *
     * 4.d) the provided prefix if it is not currently mapped to another URI.  A new
     * mapping will be created.
     *
     * 4.e) a generated prefix. In the case of D or E, a new namespace mapping
     * will be created.
     */
    public void startElement(String uri, String qName) throws SAXException {
        // we don't have to call flush() as long as we let other methods do the SAX specific work.

        if(logDebugEnabled) {
            logger.debug("startElement(" + uri + ", " + qName + ") called");
        }
        String[] elData;
        elData = resolveElementPrefix(uri, qName);

        String myURI = elData[0];
        String localName = elData[1];
        String myQName = elData[2];
        String prefix = parsePrefix(myQName);

        // this will flush() and take care of the mapping
        startPrefixMapping(prefix, myURI);

        // this takes care of buffering the element.
        startElement(myURI, localName, myQName, null);
    }

    /**
     * This method corresponds to startElement(uri, qName);
     *
     * @param uri
     * @param qName
     */
    public void endElement(String uri, String qName) throws SAXException {
        // TODO: make sure qName is the same as what was used for startElement... currently this is buggy.
        // possible fixes include writing a ns mapper like NamespaceHelper that can make a guarantee on
        // getPrefix(uri), perhaps using a TreeMap to store namespace -> uri.  Otherwise, we'll simply have to
        // maintain a stack of qNames for start/end element.

        String[] elData;
        elData = resolveElementPrefix(uri, qName);

        String myURI = elData[0];
        String localName = elData[1];
        String myQName = elData[2];

        endElement(myURI, localName, myQName);
    }

    // this method may return undeclared or phantom prefixes
    private String[] resolveElementPrefix(final String uri, final String qName) throws SAXException {
        final String myQName;
        final String myURI;
        final String localName = parseLocalName(qName);
        final String prefix = parsePrefix(qName);

        if (localName.length() == 0) {
            throw new SAXException("Could not determine localName for element: '" + qName + "'.");
        }

        if (null == uri) {
            if (prefix.length() == 0) {
                myQName = localName;
                String u = namespaceMappings.getURI("", true);
                if (null == u) {
                    u = "";
                }
                myURI = u;
            } else {
                // FIXME: this is ok for startElement, but are there any issues for endElement?  Really endElement
                // needs to be improved anyway to make sure the same prefix is used on both ends.
                myURI = namespaceMappings.getURI(prefix, true);
                if (null == myURI) {
                    throw new SAXException("Cannot find URI for '" + qName + "' and none was provided.");
                }
                myQName = qName;
            }
        } else if (uri.length() == 0) {
            // use "" URI and no prefix
            myQName = localName;
            myURI = "";
        } else {
            // we have a uri, lets find a good prefix
            if (uri.equals(namespaceMappings.getURI("", true))) {
                // Case A: the uri is currently the default namespace; don't use a prefix
                myQName = localName;
                myURI = uri;
            } else if (prefix.length() != 0 && uri.equals(namespaceMappings.getURI(prefix, true))) {
                // Case B: prefix was provided and uri matches
                myQName = qName;
                myURI = uri;
            } else {
                String p = namespaceMappings.getPrefix(uri, true);
                if (null != p) {
                    // Case C: we already have a perfectly good prefix
                    myQName = p + ":" + localName;
                    myURI = uri;
                } else if (prefix.length() != 0 && (null == namespaceMappings.getURI(prefix, true))) {
                    // Case D: the provided prefix will do; create new namespace mapping
                    myQName = qName;
                    myURI = uri;
                } else {
                    // Case E: punt... generate a new prefix for the attribute
                    p = genPrefix();
                    myQName = p + ":" + localName;
                    myURI = uri;
                }
            }
        }

        return new String[] {myURI, localName, myQName};
    }

    ////////////////////////////////////////////////////////////////////////////////
    //
    // SAX Methods (simple pass through)
    //
    ////////////////////////////////////////////////////////////////////////////////

    public void characters(char[] ch, int start, int length) throws SAXException {
        flush(EVENT_CHARACTERS);
        if (ch != null) {
            wrappedContentHandler.characters(ch, start, length);
        }
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        flush(EVENT_IGNORABLE_WHITESPACE);
        wrappedContentHandler.ignorableWhitespace(ch, start, length);
    }

    public void processingInstruction(String target, String data) throws SAXException {
        flush(EVENT_PROCESSING_INSTRUCTION);
        wrappedContentHandler.processingInstruction(target, data);
    }

    public void skippedEntity(String name) throws SAXException {
        flush(EVENT_SKIPPED_ENTITY);
        wrappedContentHandler.skippedEntity(name);
    }

    public void setDocumentLocator(Locator locator) {
        wrappedContentHandler.setDocumentLocator(locator);
    }

    public void endDocument() throws SAXException {
        flush(EVENT_END_DOCUMENT);
        // should calls to this method be ignored?
    }

    public void startDocument() throws SAXException {
        flush(EVENT_START_DOCUMENT);
        // should calls to this method be ignored?
    }

    ////////////////////////////////////////////////////////////////////////////////
    //
    // Xp specific getters/setters
    //
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * This method should be used carefully; output should not be made directly
     * to the wrapped <code>ContentHandler</code>.
     *
     * @return the wrapped <code>ContentHandler</code>
     */
    public ContentHandler getWrappedContentHandler() {
        return wrappedContentHandler;
    }

    public boolean isNamespacePrefixes() {
        return namespacePrefixes;
    }

    public void setNamespacePrefixes(boolean namespacePrefixes) {
        this.namespacePrefixes = namespacePrefixes;
    }

    ////////////////////////////////////////////////////////////////////////////////
    //
    // private utility methods
    //
    ////////////////////////////////////////////////////////////////////////////////

    private void flush(final int event) throws SAXException {
        // NOTE: to handle http://xml.org/sax/features/namespace-prefixes, we will first
        // remove all "xmlns" and xmlns:xxx" attributes, then add required attributes from
        // namespaceSupport if the feature is set to "true"

        // This method is called at the start of _every_ event except addAttribute.
        // There are two main concerns:
        //
        // 1. if bufferedElLocalName != null we need to declare prefixes and output
        // the element with accumulated attributes.
        //
        // 2. if another element has come or is about to come: We need to pushContext()
        // if we haven't yet, but wait until we process bufferedEl if it exits.

        try {

            if (EVENT_START_PREFIX_MAPPING == lastEvent &&
                    EVENT_START_PREFIX_MAPPING != event && EVENT_START_ELEMENT != event) {
                throw new IllegalStateException("Only startPrefixMapping() or startElement()" +
                        " SAX events may follow startPrefixMapping()");
            }

            if (EVENT_PUSH_PHANTOM_PREFIX_MAPPING == event || EVENT_POP_PHANTOM_PREFIX_MAPPING == event) {
                // do nothing; these can be called at any time except between startPrefixMapping and startElement
                return;
            }

            // Note: flush() is called by startElement PRIOR to setting bufferedElLocalName.  So, this test is for a
            // a bufferedElLocalName set by a previous startElement call.

            if (null != bufferedElLocalName) {
                if(logDebugEnabled) {
                    logger.debug("   outputing bufferd element");
                }
                for (int i = 0; i < bufferedElAttributes.getLength(); i++) {
                    String qName = bufferedElAttributes.getQName(i);
                    if (qName.equals("xmlns") || qName.startsWith("xmlns:")) {
                        bufferedElAttributes.removeAttribute(i);
                    }
                }

                // start prefix mappings

                // namespaceMappings.push() was already called by the startElement()
                // that saved the bufferedElLocalName we are about to output

                // we don't want to output new phantoms; since push() was already called, phantoms
                // that are relevent to bufferedEl have already been "promoted" to declared status

                Enumeration e = namespaceMappings.getDeclaredPrefixes(false);
                while (e.hasMoreElements()) {
                    String prefix = (String) e.nextElement();
                    String uri = namespaceMappings.getURI(prefix, false);
                    if (null == uri) {
                        uri = "";
                    }
                    if(logDebugEnabled) {
                        logger.debug("      wrappedContentHandler.startPrefixMapping("
                                + "'"   + prefix + "'"
                                + ", '" + uri + "'"
                                + ")");
                    }
                    wrappedContentHandler.startPrefixMapping(prefix, uri);
                    if (namespacePrefixes) {
                        String qName;
                        if (prefix.length() == 0) {
                            qName = "xmlns";
                        } else {
                            qName = "xmlns:" + prefix;
                        }
                        if(logDebugEnabled) {
                            logger.debug("      adding namespace-prefix attribute " + qName + "= '" + uri + "')");
                        }
                        bufferedElAttributes.addAttribute("", "", qName, "CDATA", uri);
                    }
                }
                if(logDebugEnabled) {
                    logger.debug("      wrappedContentHandler.startElement("
                            + "'"   + bufferedElNamespaceURI + "'"
                            + ", '" + bufferedElLocalName + "'"
                            + ", '" + bufferedElQName + "'"
                            + ", bufferedElAttributes"
                            + ")");
                }

                wrappedContentHandler.startElement(bufferedElNamespaceURI, bufferedElLocalName,
                        bufferedElQName, bufferedElAttributes);
                bufferedElNamespaceURI = null;
                bufferedElLocalName = null;
                bufferedElQName = null;
                bufferedElAttributes.clear();
            }

            switch (event) {
                case EVENT_START_PREFIX_MAPPING:
                    if (EVENT_START_PREFIX_MAPPING != lastEvent) {
                        namespaceMappings.pushContext();
                    }
                    break;
                case EVENT_START_ELEMENT:
                    if (EVENT_START_PREFIX_MAPPING != lastEvent) {
                        namespaceMappings.pushContext();
                    }
                    break;
            }
        } finally {
            lastEvent = event;
        }
    }

    private static final String parsePrefix(String qName) {
        if (null == qName || qName.length() == 0) {
            return "";
        } else {
            int colon = qName.indexOf(':');
            if (-1 == colon) {
                return "";
            } else {
                return qName.substring(0, colon);
            }
        }
    }

    private static final String parseLocalName(String qName) {
        if (null == qName || qName.length() == 0) {
            return "";
        } else {
            int colon = qName.indexOf(':');
            if (-1 == colon) {
                return qName;
            } else {
                return qName.substring(colon + 1);
            }
        }
    }

    private int prefixNum = 100;
    private String genPrefix() {
        String prefix;
        do {
            // comment this out. Better to be repeatable.
            //prefix = "n" + Integer.toString((int) (Math.random() *
            // Integer.MAX_VALUE), 36);
            prefix = "n" + prefixNum++;
        // we may as well consider phantoms here in order to keep them around (not mask them) in case someone cares
        } while (null != namespaceMappings.getURI(prefix, true));

        return prefix;
    }

    ////////////////////////////////////////////////////////////////////////////////
    //
    // characters(xxx) convenience methods
    //
    ////////////////////////////////////////////////////////////////////////////////

    public void characters(String s) throws SAXException {
        if (null != s) {
            characters(s.toCharArray(), 0, s.length());
        } else {
            characters(NULL_STRING.toCharArray(), 0, NULL_STRING.length());
        }
    }

    public void characters(Object x) throws SAXException {
        if (null != x) {
            characters(x.toString());
        } else {
            characters((String) null);
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

    ////////////////////////////////////////////////////////////////////////////////
    //
    // methods for XpNamespaceMappings
    //
    // These methods ALWAYS include phantoms since they will be used by code
    // that cares about phantoms to do things like EL prefix resolution.
    //
    ////////////////////////////////////////////////////////////////////////////////

    public String getPrefix(String uri) {
        return namespaceMappings.getPrefix(uri, true);
    }

    public Enumeration getPrefixes() {
        return namespaceMappings.getPrefixes(true);
    }

    public Enumeration getPrefixes(String uri) {
        return namespaceMappings.getPrefixes(uri, true);
    }

    public String getURI(String prefix) {
        return namespaceMappings.getURI(prefix, true);
    }

    ////////////////////////////////////////////////////////////////////////////////
    //
    // our namespace mappings
    //
    ////////////////////////////////////////////////////////////////////////////////

    public boolean isNamespaceContextCompatible(XpContentHandler ch, boolean parentElClosed, int contextVersion,
            int ancestorsWithPrefixMasking, int phantomPrefixCount) {
        if (logDebugEnabled) {
            logger.debug("isNamespaceContextCompatible() called");
        }
        if (this != ch) {
            // no quick way to tell since our namespace mapping did not produce these values.
            if (logDebugEnabled) {
                logger.debug("   namespace is not compatible: original XpCH != current XpCH");
            }
            return false;
        } else if (phantomPrefixCount != getPhantomPrefixCount()) {
            // don't bother trying anything else if phantomPrefixCounts don't match.
            if (logDebugEnabled) {
                logger.debug("   namespace is not compatible: phantom prefix count doesn't match (short-circuit.)");
            }
            return false;
        } else if (contextVersion == getContextVersion() && phantomPrefixCount != getPhantomPrefixCount()) {
            // they are _exactly_ the same
            return true;
        } else if (parentElClosed) {
            // exact version comparison is the only we can detect compatibility when we
            // are no longer a decendent of the fragment's parent.
            if (logDebugEnabled) {
                logger.debug("   namespace is not compatible: versions not the same and parentElClosed");
            }
            return false;
        } else if (ancestorsWithPrefixMasking == getAncestorsWithPrefixMasking()) {
            // current context is a descendent of the target's parent and no prefixes have been masked
            return true;
        }
        if (logDebugEnabled) {
            logger.debug("   namespace is not compatible: prefixes have been masked");
        }
        return false;
    }

    public int getContextVersion() {
        return namespaceMappings.getContextVersion();
    }

    public int getAncestorsWithPrefixMasking() {
        return namespaceMappings.getAncestorsWithPrefixMasking();
    }

    public int getPhantomPrefixCount() {
        return namespaceMappings.getPhantomPrefixCount();
    }
}
