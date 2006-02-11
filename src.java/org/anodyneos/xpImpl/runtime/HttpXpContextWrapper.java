package org.anodyneos.xpImpl.runtime;

import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;

import org.anodyneos.xp.XpContext;
import org.anodyneos.xp.http.HttpXpContext;

public class HttpXpContextWrapper extends XpContextWrapperA implements HttpXpContext {

    HttpXpContext wrappedContext;

    public HttpXpContextWrapper(HttpXpContext wrappedContext,
            List<String> nestedVars, List<String> atBeginVars, List<String> atEndVar,
            Map<String, String> aliases) {
        super(wrappedContext, nestedVars, atBeginVars, atEndVar, aliases);
        this.wrappedContext = wrappedContext;
    }

    @Override
    public XpContext wrap(List<String> nestedVars, List<String> atBeginVars, List<String> atEndVars,
            Map<String, String> aliases) {
        return new HttpXpContextWrapper(this, nestedVars, atBeginVars, atEndVars, aliases);
    }

    public ServletRequest getRequest() {
        return wrappedContext.getRequest();
    }

    public ServletResponse getResponse() {
        return wrappedContext.getResponse();
    }

    public ServletConfig getServletConfig() {
        return wrappedContext.getServletConfig();
    }

    public ServletContext getServletContext() {
        return wrappedContext.getServletContext();
    }

    public HttpSession getSession() {
        return wrappedContext.getSession();
    }

    public void initialize(Servlet servlet, ServletRequest servletRequest, ServletResponse servletResponse) {
        // noop
    }

}
