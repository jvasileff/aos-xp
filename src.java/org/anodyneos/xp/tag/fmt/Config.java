/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.anodyneos.xp.tag.fmt;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

import org.anodyneos.xp.XpContext;
import org.anodyneos.xp.http.HttpXpContext;

/**
 * Class supporting access to configuration settings.
 */
public class Config {

    /*
     * I18N/Formatting actions related configuration data
     */

    /**
     * Name of configuration setting for application- (as opposed to browser-)
     * based preferred locale
     */
    public static final String FMT_LOCALE
        = "org.anodyneos.xp.tag.fmt.locale";

    /**
     * Name of configuration setting for fallback locale
     */
    public static final String FMT_FALLBACK_LOCALE
        = "org.anodyneos.xp.tag.fmt.fallbackLocale";

    /**
     * Name of configuration setting for i18n localization context
     */
    public static final String FMT_LOCALIZATION_CONTEXT
        = "org.anodyneos.xp.tag.fmt.localizationContext";

    /**
     * Name of localization setting for time zone
     */
    public static final String FMT_TIME_ZONE
        = "org.anodyneos.xp.tag.fmt.timeZone";

    /*
     * SQL actions related configuration data
     */

    /**
     * Name of configuration setting for SQL data source
     */
    public static final String SQL_DATA_SOURCE
        = "org.anodyneos.xp.tag.sql.dataSource";

    /**
     * Name of configuration setting for maximum number of rows to be included
     * in SQL query result
     */
    public static final String SQL_MAX_ROWS
        = "org.anodyneos.xp.tag.sql.maxRows";

    /*
     * Private constants
     */
    private static final String PAGE_SCOPE_SUFFIX = ".page";
    private static final String REQUEST_SCOPE_SUFFIX = ".request";
    private static final String SESSION_SCOPE_SUFFIX = ".session";
    private static final String APPLICATION_SCOPE_SUFFIX = ".application";

    /**
     * Looks up a configuration variable in the given scope.
     *
     * <p> The lookup of configuration variables is performed as if each scope
     * had its own name space, that is, the same configuration variable name
     * in one scope does not replace one stored in a different scope.
     *
     * @param pc Page context in which the configuration variable is to be
     * looked up
     * @param name Configuration variable name
     * @param scope Scope in which the configuration variable is to be looked
     * up
     *
     * @return The <tt>java.lang.Object</tt> associated with the configuration
     * variable, or null if it is not defined.
     */
    public static Object get(XpContext xpc, String name, int scope) {
        return xpc.getAttribute(name + "." + xpc.resolveScope(scope), scope);
    }

    /**
     * Looks up a configuration variable in the "request" scope.
     *
     * <p> The lookup of configuration variables is performed as if each scope
     * had its own name space, that is, the same configuration variable name
     * in one scope does not replace one stored in a different scope.
     *
     * @param request Request object in which the configuration variable is to
     * be looked up
     * @param name Configuration variable name
     *
     * @return The <tt>java.lang.Object</tt> associated with the configuration
     * variable, or null if it is not defined.
     */
    public static Object get(ServletRequest request, String name) {
        return request.getAttribute(name + "." + HttpXpContext.REQUEST_SCOPE_STRING);
    }

    /**
     * Looks up a configuration variable in the "session" scope.
     *
     * <p> The lookup of configuration variables is performed as if each scope
     * had its own name space, that is, the same configuration variable name
     * in one scope does not replace one stored in a different scope.</p>
     *
     * @param session Session object in which the configuration variable is to
     * be looked up
     * @param name Configuration variable name
     *
     * @return The <tt>java.lang.Object</tt> associated with the configuration
     * variable, or null if it is not defined, if session is null, or if the session
     * is invalidated.
     */
    public static Object get(HttpSession session, String name) {
        Object ret = null;
        if (session != null) {
            try {
                ret = session.getAttribute(name + "." + HttpXpContext.SESSION_SCOPE_STRING);
            } catch (IllegalStateException ex) {} // when session is invalidated
        }
        return ret;
    }

    /**
     * Looks up a configuration variable in the "application" scope.
     *
     * <p> The lookup of configuration variables is performed as if each scope
     * had its own name space, that is, the same configuration variable name
     * in one scope does not replace one stored in a different scope.
     *
     * @param context Servlet context in which the configuration variable is
     * to be looked up
     * @param name Configuration variable name
     *
     * @return The <tt>java.lang.Object</tt> associated with the configuration
     * variable, or null if it is not defined.
     */
    public static Object get(ServletContext context, String name) {
        return context.getAttribute(name + "." + XpContext.APPLICATION_SCOPE_STRING);
    }

    /**
     * Sets the value of a configuration variable in the given scope.
     *
     * <p> Setting the value of a configuration variable is performed as if
     * each scope had its own namespace, that is, the same configuration
     * variable name in one scope does not replace one stored in a different
     * scope.
     *
     * @param pc Page context in which the configuration variable is to be set
     * @param name Configuration variable name
     * @param value Configuration variable value
     * @param scope Scope in which the configuration variable is to be set
     */
    public static void set(XpContext pc, String name, Object value, int scope) {
        pc.setAttribute(name, name + "." + pc.resolveScope(scope), scope);
    }

    /**
     * Sets the value of a configuration variable in the "request" scope.
     *
     * <p> Setting the value of a configuration variable is performed as if
     * each scope had its own namespace, that is, the same configuration
     * variable name in one scope does not replace one stored in a different
     * scope.
     *
     * @param request Request object in which the configuration variable is to
     * be set
     * @param name Configuration variable name
     * @param value Configuration variable value
     */
    public static void set(ServletRequest request, String name, Object value) {
        request.setAttribute(name + "." + HttpXpContext.REQUEST_SCOPE_STRING, value);
    }

    /**
     * Sets the value of a configuration variable in the "session" scope.
     *
     * <p> Setting the value of a configuration variable is performed as if
     * each scope had its own namespace, that is, the same configuration
     * variable name in one scope does not replace one stored in a different
     * scope.
     *
     * @param session Session object in which the configuration variable is to
     * be set
     * @param name Configuration variable name
     * @param value Configuration variable value
     */
    public static void set(HttpSession session, String name, Object value) {
        session.setAttribute(name + "." + HttpXpContext.SESSION_SCOPE_STRING, value);
    }

    /**
     * Sets the value of a configuration variable in the "application" scope.
     *
     * <p> Setting the value of a configuration variable is performed as if
     * each scope had its own namespace, that is, the same configuration
     * variable name in one scope does not replace one stored in a different
     * scope.
     *
     * @param context Servlet context in which the configuration variable is to
     * be set
     * @param name Configuration variable name
     * @param value Configuration variable value
     */
    public static void set(ServletContext context, String name, Object value) {
        context.setAttribute(name + "." + XpContext.APPLICATION_SCOPE_STRING, value);
    }

    /**
     * Removes a configuration variable from the given scope.
     *
     * <p> Removing a configuration variable is performed as if each scope had
     * its own namespace, that is, the same configuration variable name in one
     * scope does not impact one stored in a different scope.
     *
     * @param pc Page context from which the configuration variable is to be
     * removed
     * @param name Configuration variable name
     * @param scope Scope from which the configuration variable is to be
     * removed
     */
    public static void remove(XpContext pc, String name, int scope) {
        pc.removeAttribute(name + "." + pc.resolveScope(scope), scope);
    }

    /**
     * Removes a configuration variable from the "request" scope.
     *
     * <p> Removing a configuration variable is performed as if each scope had
     * its own namespace, that is, the same configuration variable name in one
     * scope does not impact one stored in a different scope.
     *
     * @param request Request object from which the configuration variable is
     * to be removed
     * @param name Configuration variable name
     */
    public static void remove(ServletRequest request, String name) {
        request.removeAttribute(name + "." + HttpXpContext.REQUEST_SCOPE_STRING);
    }

    /**
     * Removes a configuration variable from the "session" scope.
     *
     * <p> Removing a configuration variable is performed as if each scope had
     * its own namespace, that is, the same configuration variable name in one
     * scope does not impact one stored in a different scope.
     *
     * @param session Session object from which the configuration variable is
     * to be removed
     * @param name Configuration variable name
     */
    public static void remove(HttpSession session, String name) {
        session.removeAttribute(name + "." + HttpXpContext.SESSION_SCOPE_STRING);
    }

    /**
     * Removes a configuration variable from the "application" scope.
     *
     * <p> Removing a configuration variable is performed as if each scope had
     * its own namespace, that is, the same configuration variable name in one
     * scope does not impact one stored in a different scope.
     *
     * @param context Servlet context from which the configuration variable is
     * to be removed
     * @param name Configuration variable name
     */
    public static void remove(ServletContext context, String name) {
        context.removeAttribute(name + "." + XpContext.APPLICATION_SCOPE_STRING);
    }

    /**
     * Finds the value associated with a specific configuration setting
     * identified by its context initialization parameter name.
     *
     * <p> For each of the JSP scopes (page, request, session, application),
     * get the value of the configuration variable identified by <tt>name</tt>
     * using method <tt>get()</tt>. Return as soon as a non-null value is
     * found. If no value is found, get the value of the context initialization
     * parameter identified by <tt>name</tt>.
     *
     * @param pc Page context in which the configuration setting is to be
     * searched
     * @param name Context initialization parameter name of the configuration
     * setting
     *
     * @return The <tt>java.lang.Object</tt> associated with the configuration
     * setting identified by <tt>name</tt>, or null if it is not defined.
     */
    public static Object find(XpContext pc, String name) {
        int[] scopes = pc.getScopes();
        Object ret = null;
        for (int i = 0; i < scopes.length; i++) {
            ret = get(pc, name, scopes[i]);
            if (null != ret) {
                break;
            }
        }
        return ret;
    }
}
