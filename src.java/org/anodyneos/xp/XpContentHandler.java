package org.anodyneos.xp;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.EmptyStackException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.ArrayStack;
import org.apache.commons.collections.iterators.IteratorEnumeration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.NamespaceSupport;

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
 * managed by this class. Calls to startPrefixMapping() may be made at any time
 * and will take effect when the next call to startElement is made. Calls to
 * endPrefixMapping() are ignored - this class will track mappings and make the
 * necessary calls to the wrapped ContentHandler as necessary.
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
 * TODO: do not allow direct instantiation, use a factory.  This should be an interface with only the methods
 * needed by tag authors.  Implementation specific methods should be hidden.
 *
 * TODO: fix documentation - much of it is out of date.
 *
 * TODO: handle clearing of default namespace when non-XP code uses the contentHandler.
 *
 * TODO: provide runtime support for excludeResultPrefixes - all mappings should be tracked and available to
 * the runtime code, but output for the excluded prefixes should be suppressed as best possbile.  One implementation
 * would be to use an XMLFilter in order to avoid this class becoming even harder to read.
 */
public final class XpContentHandler implements ContentHandler, XpNamespaceMapper {

    private static final String NULL_STRING = "null";

    private static final Log logger = LogFactory.getLog(XpContentHandler.class);

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

    private boolean lastEventWasStartPrefixMapping = false;

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

    /**
     * holds a stack of phantom prefix mappings.  If there are no new phantom prefixes, the stack depth is not
     * increased if it is currently empty.  Contents are Maps, map keys are prefix names, values are uris.
     * ISSUE: this won't work: we need to be able to unwind all prefixes, even if a prefix is declared twice.
     * ISSUE#2: we need both start and end as we do not have convenient element boundaries to help us. OR, we
     * can rely on the XP code to push and pop phantom contexts.  But we still need to track which phantom prefixes
     * have been converted to "real" prefixes so we can ignore the top part of the stack.
     */

    public XpContentHandler(ContentHandler contentHandler) {
        this.wrappedContentHandler = contentHandler;
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
        // this method cannot be called between startPrefixMapping and startElement events.
        flush(EVENT_PUSH_PHANTOM_PREFIX_MAPPING);
        namespaceMappings.pushPhantomPrefix(prefix, uri);
    }

    public void popPhantomPrefixMapping() throws SAXException {
        if(logDebugEnabled) {
            logger.debug("popPhantomPrefixMapping() called");
        }
        // this method cannot be called between startPrefixMapping and startElement events.
        flush(EVENT_POP_PHANTOM_PREFIX_MAPPING);
        namespaceMappings.popPhantomPrefix();
    }

    ////////////////////////////////////////////////////////////////////////////////
    //
    // SAX Methods (managed)
    //
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * startPrefixMapping() must be called just prior to other startPrefixMapping calls that must be followed
     * by a startElement() call.
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
     * This method tries to be forgiving, the following rules apply:
     *
     * 1. When uri == null && qName has a prefix: The prefix must be in scope.
     * The namespace URI in the output will be that of the namespace associated
     * with the prefix. If the prefix is not in scope, a SAXException is thrown.
     *
     * 2. When uri == null && qName has no prefix: The qName is used as is with
     * no namespace URI.
     *
     * 3. When uri == "": The attribute will have no namespace and be output
     * without a prefix.
     *
     * 4. When uri == someURI: A prefix will be given to the attribute in the
     * following priority:
     *
     * 4.A) the provided prefix if one was provided and it is currently mapped to
     * the uri.
     *
     * 4.B) a prefix that is currently mapped to the URI.
     *
     * 4.C) the provided prefix if it is not currently mapped to another URI.
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
                    // the prefix must be in scope
                    // the prefix may be a phantom
                    myURI = namespaceMappings.getURI(prefix, false);
                    if (null != myURI) {
                        myQName = qName;
                    } else {
                        // TODO: we can probably simplify this code if we allow phantom prefixes to promote to
                        // their parent element when possible in the NamespaceMapper.
                        String u = namespaceMappings.getURI(prefix, true);
                        if (null == u) {
                            throw new SAXException("Cannot find URI for '" + qName + "' and none was provided.");
                        } else {
                            String p = namespaceMappings.getPrefix(u, true);
                            // p is only a candidate, but if it conflicts with an existing prefix, we don't want it
                            if (null == p || null != namespaceMappings.getURI(p, false)) {
                                p = genPrefix();
                            }
                            myQName = p + ":" + localName;
                        }
                    }
                }
            } else if (uri.length() == 0) {
                // use "" URI and no prefix
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
                    String p = namespaceMappings.getPrefix(uri, false);
                    if (null != p) {
                        // Case B: we already have a perfectly good prefix
                        myQName = p + ":" + localName;
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
                        // Case D: punt... generate a new prefix for the attribute
                        p = genPrefix();
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
     * will be set to "no namespace".
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
        // we don't have to call flush() as long as we let other methods do the SAX specific work.  This method
        // does not _have_ to be in this class - it doesn't violate the contract of calling startPrefixMapping
        // prior to startElement.

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
                // don't search phantoms
                String u = namespaceMappings.getURI("", false);
                if (null == u) {
                    u = "";
                }
                myURI = u;
            } else {
                // the prefix must be in scope
                // it is OK to search phantoms since they will be real for the new element.
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
            // we must search phantoms since they are about to be real.
            if (uri.equals(namespaceMappings.getURI("", true))) {
                // Case A: the uri is currently the default namespace; don't use a prefix
                myQName = localName;
                myURI = uri;
            // we must search phantoms since they are about to be real.
            } else if (prefix.length() != 0 && uri.equals(namespaceMappings.getURI(prefix, true))) {
                // Case B: prefix was provided and uri matches
                myQName = qName;
                myURI = uri;
            } else {
                // we must search phantoms since they are about to be real.
                String p = namespaceMappings.getPrefix(uri, true);
                if (null != p) {
                    // Case C: we already have a perfectly good prefix
                    myQName = p + ":" + localName;
                    myURI = uri;
                // we must search phantoms since they are about to be real.
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
        // TODO should calls to this method be ignored?
    }

    public void startDocument() throws SAXException {
        flush(EVENT_START_DOCUMENT);
        // TODO should calls to this method be ignored?
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

    private void flush(int event) throws SAXException {
        // NOTE: to handle http://xml.org/sax/features/namespace-prefixes, we will first
        // remove all "xmlns" and xmlns:xxx" attributes, then add required attributes from namespaceSupport if the
        // feature is set to "true"

        // This method is called at the start of _every_ event except addAttribute.  There are two main concerns:
        //
        // 1. bufferedElLocalName != null.  We need to declare prefixes and output
        // the element with accumulated attributes.
        //
        // 2. Another element has come or is about to come: We need to pushContext() if we haven't yet, but wait until
        // we process bufferedEl if it exits.

        if (lastEventWasStartPrefixMapping && event != EVENT_START_PREFIX_MAPPING && event != EVENT_START_ELEMENT) {
            throw new IllegalStateException("Only startPrefixMapping() or startElement()" +
                    " SAX events may follow startPrefixMapping()");
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

            // start prefix mappings; namespaceMappings.push() was already called at startElement()
            // we don't want phantoms - since push() was already called, they are alread available anyway.
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
                if (! lastEventWasStartPrefixMapping) {
                    namespaceMappings.pushContext();
                }
                lastEventWasStartPrefixMapping = true;
                break;
            case EVENT_START_ELEMENT:
                if (! lastEventWasStartPrefixMapping) {
                    namespaceMappings.pushContext();
                }
                lastEventWasStartPrefixMapping = false;
                break;
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

    private int prefixNum = 0;
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

    /**
     * Functions similarly to org.xml.sax.NamespaceSupport, except for the
     * following:
     *
     * 1. declarePrefix() first checks to see if the prefix is already mapped to
     * the uri (and not masked), and if so, does nothing.
     *
     * 2. namespace context versions are tracked efficiently. If
     * getNamespaceContextVersion() returns an <code>int</code> that can be
     * compared to a value returned from a previous call to find out if the
     * current mappings are identical to those at the time of the previous call.
     * IMPORTANT NOTE: versions will only be tracked for each push()/pop()
     * depth, so the save version number may be returned even after new prefix
     * mappings have been been made if push() or pop() has not been called since
     * the last new prefix mapping.
     *
     * 3. Extra checking is performed to ensure the NamespaceSupport contract
     * disallowing declarePrefix() after pop() but before push() is followed.
     *
     * 4. popContext2() returns all prefix declarations made in the popped context.
     *
     * BUG: apidocs for NamespaceSupport version "xml-commons-external-1_2_01
     * (revision: 1.2.6.2)" for declarePrefix() states:
     * "IllegalStateException when a prefix is declared after looking up a name
     * in the context, or after pushing another context on top of it."
     * This would be a problem if getURI() prevented future calls to
     * declarePrefix(), but the current code does not seem to mind.
     *
     * ISSUE: This may not be the most performant code, for example, NamespaceSupport uses Vectors!
     *
     * @author jvas
     */
    private final class NamespaceMappings {
        private NamespaceSupport namespaceSupport = new NamespaceSupport();
        private int ancestorsWithPrefixDeclarations = 0;
        private int ancestorsWithPrefixMasking = 0;
        private boolean declareOk = true;

        private BitSet prefixesDeclaredAtDepth = new BitSet();
        private BitSet prefixesMaskedAtDepth = new BitSet();
        private int contextDepth = 0;
        private IntStack versionStack;

        private int nextVersionNum = 0;
        private ArrayList tmpPrefixes = new ArrayList();

        // this only holds items when it is being used.  It holds null values whenever possible.
        // Entries are stacks that hold String[2] entries for prefix->uri mappings.
        private ArrayStack phantomPrefixes = new ArrayStack();

        public NamespaceMappings() {
            versionStack = new IntStack(3);
            versionStack.push(nextVersionNum++);
        }

        public void pushPhantomPrefix(String prefix, String uri) {
            // This may be called when phantomPrefixes is empty - if so, add an entry for the current context.
            // If not empty, the contents may be null.  If so, pop the null and push a new ArrayStack.

            // Note: we could add a boolean argument that if true would tell us
            // to promote the phantom to a real
            // mapping immediately. In this case we would call declarePrefix()
            // on our own and add a null to the prefixStack instead of the
            // String[2]. We would only do this if it would not mask a currently
            // declared prefix.

            ArrayStack prefixStack;
            if (phantomPrefixes.isEmpty()) {
                prefixStack = new ArrayStack();
                phantomPrefixes.push(prefixStack);
            } else {
                prefixStack = (ArrayStack) phantomPrefixes.peek();
                if (null == prefixStack) {
                    prefixStack = new ArrayStack();
                    phantomPrefixes.pop();
                    phantomPrefixes.push(prefixStack);
                }
            }
            prefixStack.push(new String[] {prefix, uri});
        }

        public void popPhantomPrefix() throws EmptyStackException {
            // we will require no arguments and provide no checking - we trust the calling code.
            ArrayStack prefixStack = (ArrayStack) phantomPrefixes.peek();
            prefixStack.pop();
        }

        public boolean declarePrefix(String prefix, String uri) {
            // Note: calls to declarePrefix may over-write phantomPrefixes that were added during push().  This should
            // be OK since obviously the calling code no longer cares about the phantomPrefix.

            if (! declareOk) {
                throw new IllegalStateException("Cannot declarePrefix after pop() without first calling push()");
            }
            // only perform op if prefix to uri is not alread set.
            String oldUriForPrefix = namespaceSupport.getURI(prefix);
            if (! uri.equals(oldUriForPrefix)) {
                if (namespaceSupport.declarePrefix(prefix, uri)) {
                    // if we haven't already flagged this depth for prefixesDeclared, do so:
                    if (! prefixesDeclaredAtDepth.get(contextDepth)) {
                        ancestorsWithPrefixDeclarations++;
                        versionStack.push(nextVersionNum++);
                        if (logDebugEnabled) {
                            logger.debug("   NamespaceMappings ancestorsWithPrefixDeclarations set to " + ancestorsWithPrefixDeclarations);
                            logger.debug("   NamespaceMappings namespaceVersion set to " + versionStack.peek());
                        }
                        prefixesDeclaredAtDepth.set(contextDepth);
                    }
                    // if we are masking a value, update the compatibility version:
                    if (oldUriForPrefix != null && ! prefixesMaskedAtDepth.get(contextDepth)) {
                        ancestorsWithPrefixMasking++;
                        if (logDebugEnabled) {
                            logger.debug("   NamespaceMappings ancestorsWithPrefixMasking set to "
                                    + ancestorsWithPrefixMasking);
                        }
                        prefixesMaskedAtDepth.set(contextDepth);
                    }
                    return true;
                } else {
                    return false;
                }
            }
            // NamespaceSupport returns false for "xml" and "xmlns". We should
            // not have to test for "xmlns" since that prefix is never tracked
            // by namespaceSupport and we would have already returned false
            // in the above code.
            if ("xml".equals(prefix)) {
                return false;
            } else {
                return true;
            }
        }

        private boolean phantomsExistForCurrentContext() {
            return (! phantomPrefixes.isEmpty() && null != phantomPrefixes.peek());

            // This check is not necessary - we will never have an empty stack
            // ((ArrayStack) phantomPrefixes.peek()).isEmpty());
        }

        public Enumeration getDeclaredPrefixes(boolean includePhantom) {
            // Consider changing this class to return both prefixes and uris since that would be the normal
            // use case.
            if (! includePhantom || ! phantomsExistForCurrentContext()) {
                return namespaceSupport.getDeclaredPrefixes();
            } else {
                // if a prefix is declared multiple times in phantom or in both phantom and namespaceSupport, that
                // is ok, but just return it once.
                Set prefixes = new HashSet();
                ArrayStack prefixStack = (ArrayStack) phantomPrefixes.peek();
                for (int i = 0; i < prefixStack.size(); i++) {
                    prefixes.add(((String[]) prefixStack.get(i))[0]);
                }
                Enumeration e = namespaceSupport.getDeclaredPrefixes();
                while (e.hasMoreElements()) {
                    prefixes.add((String) e.nextElement());
                }
                return new IteratorEnumeration(prefixes.iterator());
            }
        }

        public String getPrefix(String uri, boolean includePhantom) {
            if (! includePhantom || ! phantomsExistForCurrentContext()) {
                return namespaceSupport.getPrefix(uri);
            } else {
                // first search phantoms in LIFO order.
                ArrayStack prefixStack = (ArrayStack) phantomPrefixes.peek();
                for(int i = prefixStack.size() - 1; i >= 0; i--) {
                    String[] entry = (String[]) prefixStack.get(i);
                    if (entry[1].equals(uri)) {
                        return entry[0];
                    }
                }
                // not found yet, try namespaceSupport
                return namespaceSupport.getPrefix(uri);
            }
        }

        public Enumeration getPrefixes(boolean includePhantom) {
            if (! includePhantom || ! phantomsExistForCurrentContext()) {
                return namespaceSupport.getPrefixes();
            } else {
                // if a prefix is declared multiple times in phantom or in both phantom and namespaceSupport, that
                // is ok, but just return it once.
                Set prefixes = new HashSet();
                ArrayStack prefixStack = (ArrayStack) phantomPrefixes.peek();
                for (int i = 0; i < prefixStack.size(); i++) {
                    // IMPORTANT: Make sure not to return the "" prefix
                    String prefix = ((String[]) prefixStack.get(i))[0];
                    if (null != prefix && prefix.length() > 0) {
                        prefixes.add(prefix);
                    }
                }
                Enumeration e = namespaceSupport.getPrefixes();
                while (e.hasMoreElements()) {
                    prefixes.add((String) e.nextElement());
                }
                return new IteratorEnumeration(prefixes.iterator());
            }
        }

        public Enumeration getPrefixes(String uri, boolean includePhantom) {
            if (! includePhantom || ! phantomsExistForCurrentContext()) {
                return namespaceSupport.getPrefixes(uri);
            } else {
                // if a prefix is declared multiple times in phantom or in both phantom and namespaceSupport, that
                // is ok, but just return it once.
                Set prefixes = new HashSet();
                ArrayStack prefixStack = (ArrayStack) phantomPrefixes.peek();
                for (int i = 0; i < prefixStack.size(); i++) {
                    // IMPORTANT: Make sure not to return the "" prefix
                    String prefix = ((String[]) prefixStack.get(i))[0];
                    String[] entry = ((String[]) prefixStack.get(i));
                    if (entry[0].length() > 0 && uri.equals(entry[1])) {
                        prefixes.add(entry[0]);
                    }
                }
                Enumeration e = namespaceSupport.getPrefixes(uri);
                while (e.hasMoreElements()) {
                    prefixes.add((String) e.nextElement());
                }
                return new IteratorEnumeration(prefixes.iterator());
            }
        }

        public String getURI(String prefix, boolean includePhantom) {
            if (! includePhantom || ! phantomsExistForCurrentContext()) {
                return namespaceSupport.getURI(prefix);
            } else {
                // first search phantoms in LIFO order.
                ArrayStack prefixStack = (ArrayStack) phantomPrefixes.peek();
                for(int i = prefixStack.size() - 1; i >= 0; i--) {
                    String[] entry = (String[]) prefixStack.get(i);
                    if (entry[0].equals(prefix)) {
                        return entry[1];
                    }
                }
                // not found yet, try namespaceSupport
                return namespaceSupport.getURI(prefix);
            }
        }

        public void popContext() {
            if(logDebugEnabled) {
                logger.debug("   NamespaceMappings popContext()");
            }
            if (prefixesDeclaredAtDepth.get(contextDepth)) {
                ancestorsWithPrefixDeclarations--;
                versionStack.pop();
                if (logDebugEnabled) {
                    logger.debug("   NamespaceMappings ancestorsWithPrefixDeclarations set to " + ancestorsWithPrefixDeclarations);
                    logger.debug("   NamespaceMappings namespaceVersion set to " + versionStack.peek());
                }
                if (prefixesMaskedAtDepth.get(contextDepth)) {
                    ancestorsWithPrefixMasking--;
                    if (logDebugEnabled) {
                        logger.debug("   NamespaceMappings ancestorsWithPrefixMasking set to "
                                + ancestorsWithPrefixMasking);
                    }
                }
            }
            namespaceSupport.popContext();
            if (! phantomPrefixes.isEmpty()) {
                phantomPrefixes.pop();
            }
            contextDepth--;
            declareOk = false;
        }

        /**
         *
         * @return a List containing all prefixes declared in the context being popped.
         * The returned List will be reused by this class and must not be used by the calling code after
         * the next call to popContext2()
         */
        public List popContext2() {
            // decrement version if the current context has prefix mappings
            tmpPrefixes.clear();
            if (prefixesDeclaredAtDepth.get(contextDepth)) {
                // we only want to return actual declarations, not phantoms
                Enumeration e =  getDeclaredPrefixes(false);
                while (e.hasMoreElements()) {
                    String prefix = (String) e.nextElement();
                    tmpPrefixes.add(prefix);
                }
            }
            popContext();
            return tmpPrefixes;
        }

        public void pushContext() {
            if(logDebugEnabled) {
                logger.debug("   NamespaceMappings pushContext()");
            }
            namespaceSupport.pushContext();
            contextDepth++;
            prefixesDeclaredAtDepth.set(contextDepth, false);
            prefixesMaskedAtDepth.set(contextDepth, false);
            declareOk = true;

            // preload this context with phantoms, then inform phantomPrefixes that we have pushed()
            if (phantomsExistForCurrentContext()) {
                // Add these in FIFO order so that the most recent ones will over-write older ones.
                ArrayStack prefixStack = (ArrayStack) phantomPrefixes.peek();
                for (int i = 0; i < prefixStack.size(); i++) {
                    String[] entry = (String[]) prefixStack.get(i);
                    if (null != entry) {
                        declarePrefix(entry[0], entry[1]);
                    }
                }
            }
            if (! phantomPrefixes.isEmpty()) {
                phantomPrefixes.push(null);
            }
        }

        public int getContextVersion() {
            return versionStack.peek();
        }

        public int getAncestorsWithPrefixMasking() {
            return ancestorsWithPrefixMasking;
        }

        public int getPhantomPrefixCount() {
            if (! phantomsExistForCurrentContext()) {
                return 0;
            } else {
                return ((ArrayStack)phantomPrefixes.peek()).size();
            }
        }
    }

    private static final class IntStack {
        private int[] elementData;
        private int size = 0;

        public IntStack(int initialCapacity) {
            elementData = new int[initialCapacity];
        }

        public IntStack() {
            this(32);
        }

        public void push(int value) {
            ensureCapacity(size + 1);
            elementData[size++] = value;
        }

        public int pop() throws EmptyStackException {
            int ret = peek();
            size--;
            return ret;
        }

        public int peek() throws EmptyStackException {
            if (size == 0) {
                throw new EmptyStackException();
            } else {
                return elementData[size -1];
            }
        }

        public boolean empty() {
            return size == 0;
        }

        public int size() {
            return size;
        }

        public void ensureCapacity(int minCapacity) {
            int oldCapacity = elementData.length;
            if (minCapacity > oldCapacity) {
                int[] oldData = elementData;
                int newCapacity = (oldCapacity * 3)/2 + 1;
                if (newCapacity < minCapacity) {
                    newCapacity = minCapacity;
                }
                elementData = new int[newCapacity];
                System.arraycopy(oldData, 0, elementData, 0, size);
            }
        }
    }
}
