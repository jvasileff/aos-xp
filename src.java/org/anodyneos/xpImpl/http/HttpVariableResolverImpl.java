package org.anodyneos.xpImpl.http;

import java.util.Enumeration;

import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.VariableResolver;

import org.anodyneos.xp.http.HttpXpContext;
import org.anodyneos.xp.http.HttpXpContext.Scope;
import org.apache.commons.el.EnumeratedMap;


public class HttpVariableResolverImpl implements VariableResolver {

    HttpXpContext ctx;

    // needed so that it can be treated as plain old bean
    public HttpVariableResolverImpl(){}

    public HttpVariableResolverImpl(HttpXpContext ctx) {
        this.ctx = ctx;
    }

    public Object resolveVariable(String pName) throws ELException {

        Scope scope = ctx.resolveToScope(pName);
        if (scope != null){
            return new ScopeMap(ctx,scope);
        }else{
            return ctx.getAttribute(pName);
        }
    }
    public void setHttpXpContext(HttpXpContext httpXpContext){
        this.ctx = httpXpContext;
    }

    private class ScopeMap extends EnumeratedMap {
        HttpXpContext ctx;
        Scope scope;

        public ScopeMap(HttpXpContext ctx, Scope scope) {
            this.ctx = ctx;
            this.scope = scope;
        }

        public Enumeration enumerateKeys() {
            return ctx.getAttributeNamesInScope(scope.toInt());
        }


        public boolean isMutable() {
            return true;
        }

        public Object getValue(Object pKey) {
            if (pKey instanceof String) {
                return ctx.getAttribute((String) pKey, scope.toInt());
            } else {
                return null;
            }
        }

    }
}
