package org.anodyneos.xpImpl.http;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

import org.anodyneos.xp.http.HttpXpContext;
import org.apache.commons.el.ExpressionEvaluatorImpl;

public class HttpXpContextImpl extends HttpXpContext {

    private Servlet servlet;
    private ServletRequest servletRequest;
    private ServletResponse servletResponse;

    private VariableResolver variableResolver;
    private ExpressionEvaluator expVal = new ExpressionEvaluatorImpl(false);

    private Hashtable pageScopeMap = new Hashtable();

    public HttpXpContextImpl(Servlet servlet, ServletRequest servletRequest,
            ServletResponse servletResponse) {
        this.servlet = servlet;
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
    }

    public ServletRequest getRequest() {
        return servletRequest;
    }

    public ServletResponse getResponse() {
        return servletResponse;
    }

    public ServletConfig getServletConfig() {
        return servlet.getServletConfig();
    }

    public ServletContext getServletContext() {
        return servlet.getServletConfig().getServletContext();
    }

    public HttpSession getSession() {
        return ((HttpServletRequest)servletRequest).getSession();
    }

    public void initialize(Servlet iServlet, ServletRequest iServletRequest,
            ServletResponse iServletResponse) {
        this.servlet = iServlet;
        this.servletRequest = iServletRequest;
        this.servletResponse = iServletResponse;
        this.variableResolver = new HttpVariableResolverImpl(this);
    }

    public void release() {
        this.servlet = null;
        this.servletRequest = null;
        this.servletResponse = null;
        this.variableResolver = null;
        pageScopeMap.clear();
    }

    public Object getAttribute(String name) {
        return getAttribute(name, PAGE_SCOPE);
    }

    public Object getAttribute(String name, int scope) {
        if (null == name) {
            throw new NullPointerException("attribute name is null");
        } else {
            switch (scope) {
            case PAGE_SCOPE:
                return pageScopeMap.get(name);
            case REQUEST_SCOPE:
                return servletRequest.getAttribute(name);
            case SESSION_SCOPE:
                return getSession().getAttribute(name);
            case APPLICATION_SCOPE:
                return getServletContext().getAttribute(name);
            default:
                throw new IllegalArgumentException("Illegal Scope: " + scope);
            }
        }
    }

    public void removeAttribute(String name) {
        removeAttribute(name, PAGE_SCOPE);
    }

    public void removeAttribute(String name, int scope) {
        if (null == name) {
            throw new NullPointerException("attribute name is null");
        } else {
            switch (scope) {
            case PAGE_SCOPE:
                pageScopeMap.remove(name);
                break;
            case REQUEST_SCOPE:
                servletRequest.removeAttribute(name);
                break;
            case SESSION_SCOPE:
                getSession().removeAttribute(name);
                break;
            case APPLICATION_SCOPE:
                getServletContext().removeAttribute(name);
                break;
            default:
                throw new IllegalArgumentException("Illegal Scope: " + scope);
            }
        }
    }

    public void setAttribute(String name, Object obj) {
        setAttribute(name, obj, PAGE_SCOPE);
    }

    public void setAttribute(String name, Object obj, int scope) {
        if (null == name) {
            throw new NullPointerException("attribute name is null");
        } else if (null == obj) {
            removeAttribute(name, scope);
        } else {
            switch (scope) {
            case PAGE_SCOPE:
                pageScopeMap.put(name, obj);
                break;
            case REQUEST_SCOPE:
                servletRequest.setAttribute(name, obj);
                break;
            case SESSION_SCOPE:
                getSession().setAttribute(name, obj);
                break;
            case APPLICATION_SCOPE:
                getServletContext().setAttribute(name, obj);
                break;
            default:
                throw new IllegalArgumentException("Illegal Scope: " + scope);
            }
        }
    }

    public Enumeration getAttributeNamesInScope(int scope) {
        switch (scope) {
        case PAGE_SCOPE:
            return pageScopeMap.keys();
        case REQUEST_SCOPE:
            return servletRequest.getAttributeNames();
        case SESSION_SCOPE:
            return getSession().getAttributeNames();
        case APPLICATION_SCOPE:
            return getServletContext().getAttributeNames();
        default:
            throw new IllegalArgumentException("Illegal Scope: " + scope);
        }
    }

    public ExpressionEvaluator getExpressionEvaluator() {
        return expVal;
    }

    public VariableResolver getVariableResolver() {
        return variableResolver;
    }

    public int resolveScope(String scope) {
        if (PAGE_SCOPE_STRING.equals(scope)) {
            return PAGE_SCOPE;
        } else if (REQUEST_SCOPE_STRING.equals(scope)) {
            return REQUEST_SCOPE;
        } else if (SESSION_SCOPE_STRING.equals(scope)) {
            return SESSION_SCOPE;
        } else if (APPLICATION_SCOPE_STRING.equals(scope)) {
            return APPLICATION_SCOPE;
        } else {
            throw new IllegalArgumentException("Illegal Scope: " + scope);
        }
    }

    public Object findAttribute(String name) {
        Object o;
        o = getAttribute(name, PAGE_SCOPE);
        if (null == o) { o = getAttribute(name, REQUEST_SCOPE); }
        if (null == o) { o = getAttribute(name, SESSION_SCOPE); }
        if (null == o) { o = getAttribute(name, APPLICATION_SCOPE); }
        return o;
    }

    public int getAttributesScope(String name) {
        if (null != getAttribute(name, PAGE_SCOPE)) { return PAGE_SCOPE; }
        if (null != getAttribute(name, REQUEST_SCOPE)) { return REQUEST_SCOPE; }
        if (null != getAttribute(name, SESSION_SCOPE)) { return SESSION_SCOPE; }
        if (null != getAttribute(name, APPLICATION_SCOPE)) { return APPLICATION_SCOPE; }
        return 0;
    }
}
