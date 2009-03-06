package org.anodyneos.xpImpl.standalone;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

import org.anodyneos.xp.standalone.StandaloneXpAppContext;
import org.anodyneos.xp.standalone.StandaloneXpContext;
import org.apache.commons.el.ExpressionEvaluatorImpl;

public class StandaloneXpContextImpl extends StandaloneXpContext {

    /**
     * Page Context
     */
    private Map[] scopeMaps = new Map[] { new HashMap(), new HashMap() };

    private StandaloneXpAppContext appCtx;
    private Map pageScopeMap = new HashMap();
    private VariableResolver variableResolver;
    private ExpressionEvaluator expEval = new ExpressionEvaluatorImpl(false);

    public StandaloneXpContextImpl() {
    }

    public void initialize(StandaloneXpAppContext appCtx) {
        this.appCtx = appCtx;
        this.variableResolver = new StandaloneVariableResolverImpl(this);
    }

    public void release() {
        this.appCtx = null;
        this.variableResolver = null;
    }

    // XpContext methods

    public Object getAttribute(String name) {
        return pageScopeMap.get(name);
    }

    public Object getAttribute(String name, int scope) {
        switch (scope) {
            case PAGE_SCOPE:
                return pageScopeMap.get(name);
            case APPLICATION_SCOPE:
                return appCtx.getAttribute(name);
            default:
                throw new IllegalArgumentException("invalid scope: " + scope);
        }
    }

    public void removeAttribute(String name) {
        pageScopeMap.remove(name);
    }

    public void removeAttribute(String name, int scope) {
        switch (scope) {
            case PAGE_SCOPE:
                pageScopeMap.remove(name);
                break;
            case APPLICATION_SCOPE:
                appCtx.removeAttribute(name);
                break;
            default:
                throw new IllegalArgumentException("invalid scope: " + scope);
        }
    }

    public void setAttribute(String name, Object obj) {
        pageScopeMap.put(name, obj);
    }

    public void setAttribute(String name, Object obj, int scope) {
        if (null == obj) {
            removeAttribute(name, scope);
        } else {
            switch (scope) {
                case PAGE_SCOPE:
                    pageScopeMap.put(name,obj);
                    break;
                case APPLICATION_SCOPE:
                    appCtx.setAttribute(name, obj);
                    break;
                default:
                    throw new IllegalArgumentException("invalid scope: " + scope);
            }
        }
    }

    public Enumeration getAttributeNamesInScope(int scope){
        Set keys;
        switch (scope) {
            case PAGE_SCOPE:
                keys = pageScopeMap.keySet();
                break;
            case APPLICATION_SCOPE:
                return appCtx.getAttributeNames();
            default:
                throw new IllegalArgumentException("invalid scope: " + scope);
        }
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
    }

    public Object findAttribute(String name) {
        Object o;
        o = getAttribute(name, PAGE_SCOPE);
        if(null == o) { o = getAttribute(name, APPLICATION_SCOPE); }
        return o;
    }

    public int getAttributesScope(String name) {
        if(null != getAttribute(name, PAGE_SCOPE)) { return PAGE_SCOPE; }
        if(null != getAttribute(name, APPLICATION_SCOPE)) { return APPLICATION_SCOPE; }
        return 0;
    }

    public VariableResolver getVariableResolver() {
        return variableResolver;
    }

    public ExpressionEvaluator getExpressionEvaluator() {
        return expEval;
    }

    public int resolveScope(String scope) {
        if (APPLICATION_SCOPE_STRING.equals(scope)) {
           return APPLICATION_SCOPE;
        } else if (PAGE_SCOPE_STRING.equals(scope)) {
            return PAGE_SCOPE;
        } else {
            throw new IllegalArgumentException("invalid scope string: " + scope);
        }
    }

    public String resolveScope(int scope) {
        switch (scope) {
        case PAGE_SCOPE:
            return PAGE_SCOPE_STRING;
        case APPLICATION_SCOPE:
            return APPLICATION_SCOPE_STRING;
        default:
            throw new IllegalArgumentException("Illegal Scope: " + scope);
        }
    }

    public int[] getScopes() {
        return new int[] { PAGE_SCOPE, APPLICATION_SCOPE };
    }
}
