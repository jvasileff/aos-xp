package org.anodyneos.xpImpl.http;

import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.VariableResolver;

import org.anodyneos.xp.http.HttpXpContext;
import org.apache.commons.el.VariableResolverImpl;

public class HttpVariableResolverImpl implements VariableResolver {

    HttpXpContext ctx;
    VariableResolver variableResolver;

    public HttpVariableResolverImpl(HttpXpContext ctx) {
        this.ctx = ctx;
        this.variableResolver = new VariableResolverImpl(new PageContextAdapter(ctx));
    }

    public Object resolveVariable(String pName) throws ELException {
        return variableResolver.resolveVariable(pName);
    }

}
