package org.anodyneos.xpImpl.standalone;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

import org.anodyneos.xp.XpContentHandler;
import org.anodyneos.xp.standalone.StandaloneXpContext;
import org.apache.commons.el.ExpressionEvaluatorImpl;

public class StandaloneXpContextImpl extends StandaloneXpContext {

    /**
     * Page Context
     */
    private Map[] scopeMaps = new Map[] { new HashMap(), new HashMap() };
    private VariableResolver variableResolver;
    private XpContentHandler xpCH;

    public StandaloneXpContextImpl() {
    }

    public void initialize(XpContentHandler xpCH) {
        this.xpCH = xpCH;
        this.variableResolver = new StandaloneVariableResolverImpl(this);
    }

    public void release() {
        this.xpCH = null;
        this.variableResolver = null;
    }

    // XpContext methods

    public XpContentHandler getXpContentHandler() {
        return xpCH;
    }

    public Object getAttribute(String name) {
        return scopeMaps[PAGE_SCOPE].get(name);
    }

    public Object getAttribute(String name, int scope) {
        try {
            return scopeMaps[scope].get(name);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("invalid scope: " + scope);
        }
    }

    public void removeAttribute(String name) {
        scopeMaps[PAGE_SCOPE].remove(name);
    }

    public void removeAttribute(String name, int scope) {
        try {
            scopeMaps[scope].remove(name);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("invalid scope: " + scope);
        }
    }

    public void setAttribute(String name, Object obj) {
        scopeMaps[PAGE_SCOPE].put(name, obj);
    }

    public void setAttribute(String name, Object obj, int scope) {
        try {
            scopeMaps[scope].put(name, obj);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("invalid scope: " + scope);
        }
    }

    public Enumeration getAttributeNamesInScope(int scope){
        try {
            Set keys = scopeMaps[scope].keySet();
            final String[] array = (String[]) keys.toArray(new String[keys.size()]);
            return new Enumeration() {
                private int next = 0;

                public boolean hasMoreElements() {
                    if(next >= array.length) {
                        return false;
                    } else {
                        return true;
                    }
                }

                public Object nextElement() throws NoSuchElementException {
                    if(! hasMoreElements()) {
                        throw new NoSuchElementException("no more elements.");
                    }
                    return array[next++];
                }
            };
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("invalid scope: " + scope);
        }
    }

    public VariableResolver getVariableResolver() {
        return variableResolver;
    }

    public ExpressionEvaluator getExpressionEvaluator() {
        return new ExpressionEvaluatorImpl();
    }

    public int resolveScope(String scope) {
        if (GLOBAL_SCOPE_STRING.equals(scope)) {
           return GLOBAL_SCOPE;
        } else if (PAGE_SCOPE_STRING.equals(scope)) {
            return PAGE_SCOPE;
        } else {
            throw new IllegalArgumentException("invalid scope string: " + scope);
        }
    }
}
