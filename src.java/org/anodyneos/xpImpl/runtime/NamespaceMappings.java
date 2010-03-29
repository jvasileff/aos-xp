package org.anodyneos.xpImpl.runtime;

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
import org.xml.sax.helpers.NamespaceSupport;

/**
 * Functions similarly to org.xml.sax.NamespaceSupport, except for the
 * following:
 *
 * 1. declarePrefix() first checks to see if the prefix is already mapped to the
 * uri (and not masked), and if so, does nothing.
 *
 * 2. namespace context versions are tracked efficiently. If
 * getNamespaceContextVersion() returns an <code>int</code> that can be
 * compared to a value returned from a previous call to find out if the current
 * mappings are identical to those at the time of the previous call. IMPORTANT
 * NOTE: versions will only be tracked for each push()/pop() depth, so the save
 * version number may be returned even after new prefix mappings have been been
 * made if push() or pop() has not been called since the last new prefix
 * mapping.
 *
 * 3. Extra checking is performed to ensure the NamespaceSupport contract
 * disallowing declarePrefix() after pop() but before push() is followed.
 *
 * 4. popContext2() returns all prefix declarations made in the popped context.
 *
 * BUG: apidocs for NamespaceSupport version "xml-commons-external-1_2_01
 * (revision: 1.2.6.2)" for declarePrefix() states: "IllegalStateException when
 * a prefix is declared after looking up a name in the context, or after pushing
 * another context on top of it." This would be a problem if getURI() prevented
 * future calls to declarePrefix(), but the current code does not seem to mind.
 *
 * ISSUE: This may not be the most performant code, for example,
 * NamespaceSupport uses Vectors!
 *
 * @author jvas
 */
public final class NamespaceMappings {

    private static final Log logger = LogFactory.getLog(NamespaceMappings.class);

    private NamespaceSupport namespaceSupport = new NamespaceSupport();

    private int ancestorsWithPrefixDeclarations = 0;

    private int ancestorsWithPrefixMasking = 0;

    private boolean declareOk = true;

    private BitSet prefixesDeclaredAtDepth = new BitSet();

    private BitSet prefixesMaskedAtDepth = new BitSet();

    /** current depth of context stack - tracks push/pop on namespaceSupport object */
    private int contextDepth = 0;

    /** stores version numbers for namespace contexts */
    private IntStack versionStack;

    /** nextVersionNum always increments - values are unique and stored in versionStack */
    private int nextVersionNum = 0;

    /** Used to hold return value for popContext2() */
    private ArrayList tmpPrefixes = new ArrayList();

    // this only holds items when it is being used. It holds null values
    // whenever possible.
    // Entries are stacks that hold String[2] entries for prefix->uri mappings.
    private ArrayStack phantomPrefixes = new ArrayStack();

    public NamespaceMappings() {
        versionStack = new IntStack(3);
        versionStack.push(nextVersionNum++);
    }

    public void pushPhantomPrefix(String prefix, String uri) {
        // This may be called when phantomPrefixes is empty - if so, add an
        // entry for the current context.
        // If not empty, the contents may be null. If so, pop the null and push
        // a new ArrayStack.

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
        prefixStack.push(new String[] { prefix, uri });
    }

    public void popPhantomPrefix() throws EmptyStackException {
        // we will require no arguments and provide no checking - we trust the
        // calling code.
        ArrayStack prefixStack = (ArrayStack) phantomPrefixes.peek();
        prefixStack.pop();
    }

    /*
    public void promotePhantomPrefix(String prefix) {
        final String declaredURI = getURI(prefix, false);
        final String currentURI = getURI(prefix, true);
        // do not allow promotions if real prefix already exists and is mapped to diff URI
        if (null != declaredURI && ! declaredURI.equals(currentURI)) {
            throw new IllegalStateException("Cannot promote phantom prefix '"+prefix+"'; prefix already exists.");
        }
        // do not allow promotions if phantom prefix does not exist
        if (null == currentURI) {
            throw new IllegalStateException("Phantom prefix '"+prefix+"' does not exist.");
        }
        declarePrefix(prefix, currentURI);
    }
    */

    public boolean declarePrefix(String prefix, String uri) {
        // Note: calls to declarePrefix may over-write phantomPrefixes that were
        // added during push(). This should
        // be OK since obviously the calling code no longer cares about the
        // phantomPrefix.

        if (!declareOk) {
            throw new IllegalStateException("Cannot declarePrefix after pop() without first calling push()");
        }
        // only perform op if prefix to uri is not alread set.
        String oldUriForPrefix = namespaceSupport.getURI(prefix);
        if (!uri.equals(oldUriForPrefix)) {
            if (namespaceSupport.declarePrefix(prefix, uri)) {
                // if we haven't already flagged this depth for
                // prefixesDeclared, do so:
                if (!prefixesDeclaredAtDepth.get(contextDepth)) {
                    ancestorsWithPrefixDeclarations++;
                    versionStack.push(nextVersionNum++);
                    if (logger.isDebugEnabled()) {
                        logger.debug("   NamespaceMappings ancestorsWithPrefixDeclarations set to "
                                + ancestorsWithPrefixDeclarations);
                        logger.debug("   NamespaceMappings namespaceVersion set to " + versionStack.peek());
                    }
                    prefixesDeclaredAtDepth.set(contextDepth);
                }
                // if we are masking a value, update the compatibility version:
                if (oldUriForPrefix != null && !prefixesMaskedAtDepth.get(contextDepth)) {
                    ancestorsWithPrefixMasking++;
                    if (logger.isDebugEnabled()) {
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
        return (!phantomPrefixes.isEmpty() && null != phantomPrefixes.peek());

        // This check is not necessary - we will never have an empty stack
        // ((ArrayStack) phantomPrefixes.peek()).isEmpty());
    }

    public Enumeration getDeclaredPrefixes(boolean includePhantom) {
        // Consider changing this class to return both prefixes and uris since
        // that would be the normal
        // use case.
        if (!includePhantom || !phantomsExistForCurrentContext()) {
            return namespaceSupport.getDeclaredPrefixes();
        } else {
            // if a prefix is declared multiple times in phantom or in both
            // phantom and namespaceSupport, that
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
        // FIXME don't return the default ("") prefix
        if (!includePhantom || !phantomsExistForCurrentContext()) {
            return namespaceSupport.getPrefix(uri);
        } else {
            // first search phantoms in LIFO order.
            ArrayStack prefixStack = (ArrayStack) phantomPrefixes.peek();
            for (int i = prefixStack.size() - 1; i >= 0; i--) {
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
        if (!includePhantom || !phantomsExistForCurrentContext()) {
            return namespaceSupport.getPrefixes();
        } else {
            // if a prefix is declared multiple times in phantom or in both
            // phantom and namespaceSupport, that
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
        if (!includePhantom || !phantomsExistForCurrentContext()) {
            return namespaceSupport.getPrefixes(uri);
        } else {
            // if a prefix is declared multiple times in phantom or in both
            // phantom and namespaceSupport, that
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
        if (!includePhantom || !phantomsExistForCurrentContext()) {
            return namespaceSupport.getURI(prefix);
        } else {
            // first search phantoms in LIFO order.
            ArrayStack prefixStack = (ArrayStack) phantomPrefixes.peek();
            for (int i = prefixStack.size() - 1; i >= 0; i--) {
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
        if (logger.isDebugEnabled()) {
            logger.debug("   NamespaceMappings popContext()");
        }
        if (prefixesDeclaredAtDepth.get(contextDepth)) {
            ancestorsWithPrefixDeclarations--;
            versionStack.pop();
            if (logger.isDebugEnabled()) {
                logger.debug("   NamespaceMappings ancestorsWithPrefixDeclarations set to "
                        + ancestorsWithPrefixDeclarations);
                logger.debug("   NamespaceMappings namespaceVersion set to " + versionStack.peek());
            }
            if (prefixesMaskedAtDepth.get(contextDepth)) {
                ancestorsWithPrefixMasking--;
                if (logger.isDebugEnabled()) {
                    logger
                            .debug("   NamespaceMappings ancestorsWithPrefixMasking set to "
                                    + ancestorsWithPrefixMasking);
                }
            }
        }
        namespaceSupport.popContext();
        if (!phantomPrefixes.isEmpty()) {
            phantomPrefixes.pop();
        }
        contextDepth--;
        declareOk = false;
    }

    /**
     *
     * @return a List containing all prefixes declared in the context being
     *         popped. The returned List will be reused by this class and must
     *         not be used by the calling code after the next call to
     *         popContext2()
     */
    public List popContext2() {
        // decrement version if the current context has prefix mappings
        tmpPrefixes.clear();
        if (prefixesDeclaredAtDepth.get(contextDepth)) {
            // we only want to return actual declarations, not phantoms
            Enumeration e = getDeclaredPrefixes(false);
            while (e.hasMoreElements()) {
                String prefix = (String) e.nextElement();
                tmpPrefixes.add(prefix);
            }
        }
        popContext();
        return tmpPrefixes;
    }

    public void pushContext() {
        if (logger.isDebugEnabled()) {
            logger.debug("   NamespaceMappings pushContext()");
        }
        namespaceSupport.pushContext();
        contextDepth++;
        prefixesDeclaredAtDepth.set(contextDepth, false);
        prefixesMaskedAtDepth.set(contextDepth, false);
        declareOk = true;

        // preload this context with phantoms, then inform phantomPrefixes that
        // we have pushed()
        if (phantomsExistForCurrentContext()) {
            // Add these in FIFO order so that the most recent ones will
            // over-write older ones.
            ArrayStack prefixStack = (ArrayStack) phantomPrefixes.peek();
            for (int i = 0; i < prefixStack.size(); i++) {
                String[] entry = (String[]) prefixStack.get(i);
                if (null != entry) {
                    declarePrefix(entry[0], entry[1]);
                }
            }
        }
        if (!phantomPrefixes.isEmpty()) {
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
        if (!phantomsExistForCurrentContext()) {
            return 0;
        } else {
            return ((ArrayStack) phantomPrefixes.peek()).size();
        }
    }
}
