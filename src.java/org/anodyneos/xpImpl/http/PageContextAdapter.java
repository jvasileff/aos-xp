package org.anodyneos.xpImpl.http;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

import org.anodyneos.xp.http.HttpXpContext;

/**
 * This class partially adapts an HttpXpContext to a JSP PageContext that may be
 * used with org.apache.commons.el classes.
 */
public class PageContextAdapter extends PageContext {

    private HttpXpContext httpXpContext;

    public PageContextAdapter(HttpXpContext httpXpContext) {
        this.httpXpContext = httpXpContext;
    }

    public void initialize(Servlet arg0, ServletRequest arg1,
            ServletResponse arg2, String arg3, boolean arg4, int arg5,
            boolean arg6) throws IOException, IllegalStateException,
            IllegalArgumentException {
    }

    public void release() {
    }

    public HttpSession getSession() {
        return httpXpContext.getHttpSession();
    }

    public Object getPage() {
        return null;
    }

    public ServletRequest getRequest() {
        return httpXpContext.getServletRequest();
    }

    public ServletResponse getResponse() {
        return httpXpContext.getServletResponse();
    }

    public Exception getException() {
        return null;
    }

    public ServletConfig getServletConfig() {
        return httpXpContext.getServletConfig();
    }

    public ServletContext getServletContext() {
        return httpXpContext.getServletContext();
    }

    public void forward(String arg0) throws ServletException, IOException {
    }

    public void include(String arg0) throws ServletException, IOException {
    }

    public void include(String arg0, boolean arg1) throws ServletException,
            IOException {
    }

    public void handlePageException(Exception arg0) throws ServletException,
            IOException {
    }

    public void handlePageException(Throwable arg0) throws ServletException,
            IOException {
    }

    public void setAttribute(String arg0, Object arg1) {
        httpXpContext.setAttribute(arg0, arg1);
    }

    public void setAttribute(String arg0, Object arg1, int arg2) {
        httpXpContext.setAttribute(arg0, arg1, arg2);
    }

    public Object getAttribute(String arg0) {
        return httpXpContext.getAttribute(arg0);
    }

    public Object getAttribute(String arg0, int arg1) {
        return httpXpContext.getAttribute(arg0, arg1);
    }

    public Object findAttribute(String arg0) {
        return httpXpContext.findAttribute(arg0);
    }

    public void removeAttribute(String arg0) {
        httpXpContext.removeAttribute(arg0);
    }

    public void removeAttribute(String arg0, int arg1) {
        httpXpContext.removeAttribute(arg0, arg1);
    }

    public int getAttributesScope(String arg0) {
        return httpXpContext.getAttributesScope(arg0);
    }

    public Enumeration getAttributeNamesInScope(int arg0) {
        return httpXpContext.getAttributeNamesInScope(arg0);
    }

    public JspWriter getOut() {
        return null;
    }

    public ExpressionEvaluator getExpressionEvaluator() {
        return httpXpContext.getExpressionEvaluator();
    }

    public VariableResolver getVariableResolver() {
        return httpXpContext.getVariableResolver();
    }

}
