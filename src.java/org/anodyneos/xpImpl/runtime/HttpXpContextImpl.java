package org.anodyneos.xpImpl.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

import org.anodyneos.xp.XpContext;
import org.anodyneos.xp.http.HttpXpContext;
import org.apache.commons.el.ExpressionEvaluatorImpl;

public class HttpXpContextImpl extends XpContextA implements HttpXpContext, VariableResolver {

    private ExpressionEvaluator expressionEvaluator = new ExpressionEvaluatorImpl(false);

    private Servlet servlet;
    private ServletRequest servletRequest;
    private ServletResponse servletResponse;

    private Map<String, Object> pageScopeMap = new HashMap<String, Object>();
    private Map<String, Object> requestScopeMap;
    private Map<String, Object> sessionScopeMap;
    private Map<String, Object> applicationScopeMap;

    private Map<String, Object> paramMap;
    private Map<String, String[]> paramValuesMap;
    private Map<String, String> headerMap;
    private Map<String, String[]> headerValuesMap;
    private Map<String, String> initParamMap;
    private Map<String, Cookie> cookieMap;

    @Override
    public Map<String, Object> getScopeAsMap(int scope) {
        switch (scope) {
        case PAGE_SCOPE:
            return pageScopeMap;
        case REQUEST_SCOPE:
            return getRequestScopeMap();
        case SESSION_SCOPE:
            return getSessionScopeMap();
        case APPLICATION_SCOPE:
            return getApplicationScopeMap();
        default:
            throw new IllegalArgumentException("Invalid scope <" + scope + ">");
        }
    }

    @Override
    public VariableResolver getVariableResolver() {
        return this;
    }

    @Override
    public int[] getScopes() {
        return new int[] { PAGE_SCOPE, REQUEST_SCOPE, SESSION_SCOPE, APPLICATION_SCOPE };
    }

    @Override
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

    @Override
    public String resolveScope(int scope) {
        switch (scope) {
        case PAGE_SCOPE:
            return PAGE_SCOPE_STRING;
        case REQUEST_SCOPE:
            return REQUEST_SCOPE_STRING;
        case SESSION_SCOPE:
            return SESSION_SCOPE_STRING;
        case APPLICATION_SCOPE:
            return APPLICATION_SCOPE_STRING;
        default:
            throw new IllegalArgumentException("Illegal Scope: " + scope);
        }
    }

    @Override
    public XpContext wrap(List<String> nestedVars, List<String> atBeginVars, List<String> atEndVar,
            Map<String, String> aliases) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ExpressionEvaluator getExpressionEvaluator() {
        return expressionEvaluator;
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
    }

    private Map<String, Object> getRequestScopeMap() {
        if(null == requestScopeMap) {
            requestScopeMap = new EnumeratedMap<String, Object>() {
                ServletRequest target = servletRequest;
                @Override
                @SuppressWarnings({"unchecked"})
                public Enumeration<String> enumerateKeys() {
                    return target.getAttributeNames();
                }
                @Override
                public Object getValue(Object key) {
                    return target.getAttribute((String) key);
                }
                @Override
                public Object put(String key, Object value) {
                    target.setAttribute(key, value);
                    return value;
                }
                @Override
                public Object remove(Object key) {
                    Object ret = getValue(key);
                    target.removeAttribute((String) key);
                    return ret;
                }
                public boolean isMutable() {
                    return true;
                }
            };
        }
        return requestScopeMap;
    }

    private Map<String, Object> getSessionScopeMap() {
        if(null == sessionScopeMap) {
            sessionScopeMap = new EnumeratedMap<String, Object>() {
                HttpSession target = getSession();
                @Override
                @SuppressWarnings({"unchecked"})
                public Enumeration<String> enumerateKeys() {
                    return target.getAttributeNames();
                }
                @Override
                public Object getValue(Object key) {
                    return target.getAttribute((String) key);
                }
                @Override
                public Object put(String key, Object value) {
                    target.setAttribute(key, value);
                    return value;
                }
                @Override
                public Object remove(Object key) {
                    Object ret = getValue(key);
                    target.removeAttribute((String) key);
                    return ret;
                }
                public boolean isMutable() {
                    return true;
                }
            };
        }
        return sessionScopeMap;
    }

    private Map<String, Object> getApplicationScopeMap() {
        if(null == applicationScopeMap) {
            applicationScopeMap = new EnumeratedMap<String, Object>() {
                ServletContext target = getServletContext();
                @Override
                @SuppressWarnings({"unchecked"})
                public Enumeration<String> enumerateKeys() {
                    return target.getAttributeNames();
                }
                @Override
                public Object getValue(Object key) {
                    if (key instanceof String) {
                        return target.getAttribute((String) key);
                    } else {
                        return null;
                    }
                }
                @Override
                public Object put(String key, Object value) {
                    target.setAttribute(key, value);
                    return value;
                }
                @Override
                public Object remove(Object key) {
                    Object ret;
                    if (key instanceof String) {
                        ret = getValue(key);
                        target.removeAttribute((String) key);
                    } else {
                        ret = null;
                    }
                    return ret;
                }
                public boolean isMutable() {
                    return true;
                }
            };
        }
        return applicationScopeMap;
    }

    private Map<String, Object> getParamMap() {
        if(null == paramMap) {
            paramMap = new EnumeratedMap<String, Object>() {
                ServletRequest req = getRequest();
                @Override
                @SuppressWarnings({"unchecked"})
                public Enumeration<String> enumerateKeys() {
                    return req.getParameterNames();
                }
                @Override
                public Object getValue(Object key) {
                    if (key instanceof String) {
                        return req.getParameter((String) key);
                    } else {
                        return null;
                    }
                }
                public boolean isMutable() {
                    return false;
                }
            };


        }
        return paramMap;
    }

    private Map<String, String[]> getParamValuesMap() {
        if(null == paramValuesMap) {
            paramValuesMap = new EnumeratedMap<String, String[]>() {
                ServletRequest req = getRequest();
                @Override
                @SuppressWarnings({"unchecked"})
                public Enumeration<String> enumerateKeys() {
                    return req.getParameterNames();
                }
                @Override
                public String[] getValue(Object key) {
                    if (key instanceof String) {
                        return req.getParameterValues((String) key);
                    } else {
                        return null;
                    }
                }
                public boolean isMutable() {
                    return false;
                }
            };
        }
        return paramValuesMap;
    }

    private Map<String, String> getHeaderMap() {
        if(null == headerMap) {
            headerMap = new EnumeratedMap<String, String>() {
                HttpServletRequest req = (HttpServletRequest) getRequest();
                @Override
                @SuppressWarnings({"unchecked"})
                public Enumeration<String> enumerateKeys() {
                    return req.getHeaderNames();
                }
                @Override
                public String getValue(Object key) {
                    if (key instanceof String) {
                        return req.getHeader((String) key);
                    } else {
                        return null;
                    }
                }
                public boolean isMutable() {
                    return false;
                }
            };
        }
        return headerMap;
    }

    private Map<String, String[]> getHeaderValuesMap() {
        if(null == headerValuesMap) {
            headerValuesMap = new EnumeratedMap<String, String[]>() {
                HttpServletRequest req = (HttpServletRequest) getRequest();
                @Override
                @SuppressWarnings({"unchecked"})
                public Enumeration<String> enumerateKeys() {
                    return req.getHeaderNames();
                }
                @Override
                @SuppressWarnings({"unchecked"})
                public String[] getValue(Object key) {
                    if (key instanceof String) {
                        List<String> l = new ArrayList<String>();
                        for (Enumeration<String> e = req.getHeaders((String) key); e.hasMoreElements() ;) {
                            l.add(e.nextElement());
                        }
                        return l.toArray(new String[l.size()]);
                    } else {
                        return null;
                    }
                }
                public boolean isMutable() {
                    return false;
                }
            };
        }
        return headerValuesMap;
    }

    private Map<String, String> getInitParamMap() {
        if(null == initParamMap) {
            initParamMap = new EnumeratedMap<String, String>() {
                ServletContext ctx = getServletContext();
                @Override
                @SuppressWarnings({"unchecked"})
                public Enumeration<String> enumerateKeys() {
                    return ctx.getInitParameterNames();
                }
                @Override
                public String getValue(Object key) {
                    if (key instanceof String) {
                        return ctx.getInitParameter((String) key);
                    } else {
                        return null;
                    }
                }
                public boolean isMutable() {
                    return false;
                }
            };
        }
        return initParamMap;
    }

    private Map<String, Cookie> getCookieMap() {
        if(null == cookieMap) {
            HttpServletRequest request = (HttpServletRequest) getRequest ();
            Cookie [] cookies = request.getCookies ();
            Map<String, Cookie> ret = new HashMap<String, Cookie>();
            for (int i = 0; cookies != null && i < cookies.length; i++) {
              Cookie cookie = cookies [i];
              if (cookie != null) {
                String name = cookie.getName ();
                if (!ret.containsKey (name)) {
                  ret.put (name, cookie);
                }
              }
            }
            cookieMap = Collections.unmodifiableMap(ret);
        }
        return cookieMap;
    }

    // from VariableResolver interface
    public final Object resolveVariable(String pName) throws ELException {
        if (PAGE_SCOPE_STRING.equals(pName)) {
            return getScopeAsMap(PAGE_SCOPE);
        } else if (REQUEST_SCOPE_STRING.equals(pName)) {
            return getScopeAsMap(REQUEST_SCOPE);
        } else if (SESSION_SCOPE_STRING.equals(pName)) {
            return getScopeAsMap(SESSION_SCOPE);
        } else if (APPLICATION_SCOPE_STRING.equals(pName)) {
            return getScopeAsMap(APPLICATION_SCOPE);
        } else if ("param".equals (pName)) {
            return getParamMap();
        } else if ("paramValues".equals (pName)) {
            return getParamValuesMap();
        } else if ("header".equals (pName)) {
            return getHeaderMap();
        } else if ("headerValues".equals (pName)) {
            return getHeaderValuesMap();
        } else if ("initParam".equals (pName)) {
            return getInitParamMap();
        } else if ("cookie".equals (pName)) {
            return getCookieMap();
        } else if ("xpContext".equals(pName)) {
            return this;
        } else {
            return getAttribute(pName);
        }
    }

}
