package org.anodyneos.xpImpl.standalone;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

import org.anodyneos.xp.standalone.StandaloneXpContext;
import org.apache.commons.el.ExpressionEvaluatorImpl;

public class StandaloneXpContextImpl extends StandaloneXpContext {

    /**
     * Page Context
     */
    private Map[] scopeMaps = new Map[] { new HashMap(), new HashMap() };

    private Map pageScopeMap = new HashMap();
    private Map globalScopeMap = new HashMap();
    private VariableResolver variableResolver;
    private ExpressionEvaluator expEval = new ExpressionEvaluatorImpl(false);

    public StandaloneXpContextImpl() {
    }

    public void initialize() {
        this.variableResolver = new StandaloneVariableResolverImpl(this);
    }

    public void release() {
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
            case GLOBAL_SCOPE:
                return globalScopeMap.get(name);
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
            case GLOBAL_SCOPE:
                globalScopeMap.remove(name);
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
                case GLOBAL_SCOPE:
                    globalScopeMap.put(name,obj);
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
            case GLOBAL_SCOPE:
                keys = globalScopeMap.keySet();
                break;
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
        if(null == o) { o = getAttribute(name, GLOBAL_SCOPE); }
        return o;
    }

    public int getAttributesScope(String name) {
        if(null != getAttribute(name, PAGE_SCOPE)) { return PAGE_SCOPE; }
        if(null != getAttribute(name, GLOBAL_SCOPE)) { return GLOBAL_SCOPE; }
        return 0;
    }

    public VariableResolver getVariableResolver() {
        return variableResolver;
    }

    public ExpressionEvaluator getExpressionEvaluator() {
        return expEval;
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
