package org.anodyneos.xpImpl.runtime;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

import org.anodyneos.xp.XpContext;

public abstract class XpContextA implements XpContext {

    public abstract Map<String, Object> getScopeAsMap(int scope);
    public abstract VariableResolver getVariableResolver();
    public abstract int[] getScopes();
    public abstract int resolveScope(String scope);
    public abstract String resolveScope(int scope);

    public abstract XpContext wrap(List<String> nestedVars, List<String> atBeginVars,
            List<String> atEndVar, Map<String, String> aliases);

    public final Object findAttribute(String name) {
        int[] scopes = getScopes();
        Object ret = null;

        for(int i=0 ; i < scopes.length; i++) {
            ret = getScopeAsMap(scopes[i]);
            if (null != ret) {
                break;
            }
        }
        return ret;
    }

    public final Object getAttribute(String name, int scope) {
        return getScopeAsMap(scope).get(name);
    }

    public final Object getAttribute(String name) {
        return getAttribute(name, PAGE_SCOPE);
    }

    public final Enumeration getAttributeNamesInScope(int scope) {
        Map m = getScopeAsMap(scope);
        if (m instanceof EnumeratedMap) {
            return ((EnumeratedMap)m).enumerateKeys();
        } else {
            return Collections.enumeration(getScopeAsMap(scope).keySet());
        }
    }

    public final int getAttributesScope(String name) {
        int[] scopes = getScopes();
        int ret = 0;

        for(int i=0; i < scopes.length; i++) {
            if(null != getAttribute(name, scopes[i])) {
                ret = i;
                break;
            }
        }
        return ret;
    }

    public final void removeAttribute(String name, int scope) {
        getScopeAsMap(scope).remove(name);
    }

    public final void removeAttribute(String name) {
        int[] scopes = getScopes();

        for(int i=0; i < scopes.length; i++) {
            removeAttribute(name, scopes[i]);
        }
    }

    public final void setAttribute(String name, Object obj, int scope) {
        if (null == name) {
            removeAttribute(name, scope);
        } else {
            getScopeAsMap(scope).put(name, obj);
        }
    }

    public final void setAttribute(String name, Object obj) {
        setAttribute(name, obj, PAGE_SCOPE);
    }

    public abstract ExpressionEvaluator getExpressionEvaluator();
}

