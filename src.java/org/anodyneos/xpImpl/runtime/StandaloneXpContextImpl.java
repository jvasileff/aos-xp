package org.anodyneos.xpImpl.runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

import org.anodyneos.xp.XpContext;
import org.anodyneos.xp.standalone.StandaloneXpContext;
import org.apache.commons.el.ExpressionEvaluatorImpl;

public class StandaloneXpContextImpl extends XpContextA implements StandaloneXpContext, VariableResolver {

    private ExpressionEvaluator expressionEvaluator = new ExpressionEvaluatorImpl(false);

    private Map<String, Object> pageScopeMap = new HashMap<String, Object>();
    private Map<String, Object> applicationScopeMap = new HashMap<String, Object>();

    public XpContext wrap(List<String> nestedVars, List<String> atBeginVars,
            List<String> atEndVar, Map<String, String> aliases) {
        return new StandaloneXpContextWrapper(this, nestedVars, atBeginVars, atEndVar, aliases);
    }

    @Override
    public Map<String, Object> getScopeAsMap(int scope) {
        if (PAGE_SCOPE == scope) {
            return pageScopeMap;
        } else if (APPLICATION_SCOPE == scope) {
            return applicationScopeMap;
        } else {
            throw new IllegalArgumentException("Invalid scope <" + scope + ">");
        }
    }

    @Override
    public final int[] getScopes() {
        return new int[] { PAGE_SCOPE, APPLICATION_SCOPE };
    }

    @Override
    public final int resolveScope(String scope) {
        if (APPLICATION_SCOPE_STRING.equals(scope)) {
           return APPLICATION_SCOPE;
        } else if (PAGE_SCOPE_STRING.equals(scope)) {
            return PAGE_SCOPE;
        } else {
            throw new IllegalArgumentException("invalid scope string <" + scope + ">");
        }
    }

    @Override
    public final String resolveScope(int scope) {
        switch (scope) {
        case PAGE_SCOPE:
            return PAGE_SCOPE_STRING;
        case APPLICATION_SCOPE:
            return APPLICATION_SCOPE_STRING;
        default:
            throw new IllegalArgumentException("Illegal Scope <" + scope + ">");
        }
    }

    @Override
    public final VariableResolver getVariableResolver() {
        return this;
    }

    // from VariableResolver interface
    public final Object resolveVariable(String pName) throws ELException {
        if (PAGE_SCOPE_STRING.equals(pName)) {
            return getScopeAsMap(PAGE_SCOPE);
        } else if (APPLICATION_SCOPE_STRING.equals(pName)) {
            return getScopeAsMap(APPLICATION_SCOPE);
        } else {
            return getAttribute(pName);
        }
    }

    @Override
    public ExpressionEvaluator getExpressionEvaluator() {
        return expressionEvaluator;
    }

}
