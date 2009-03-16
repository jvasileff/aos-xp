/*
 * Copyright 2002-2007 the original author or authors.
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

package org.anodyneos.xp.spring;

import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.anodyneos.xp.tag.Config;

import org.springframework.web.servlet.support.RequestContext;

public class XpRequestContext extends RequestContext {

    public XpRequestContext(HttpServletRequest request, Map model) {
        super(request, model);
    }

    public XpRequestContext(HttpServletRequest request, ServletContext servletContext, Map model) {
        super(request, servletContext, model);
    }

    public XpRequestContext(HttpServletRequest request, ServletContext servletContext) {
        super(request, servletContext);
    }

    public XpRequestContext(HttpServletRequest request) {
        super(request);
    }

    protected final HttpServletRequest getRequest2() {
        return super.getRequest();
    }

    protected final ServletContext getServletContext2() {
        return super.getServletContext();
    }

    /**
     * Determine the fallback locale for this context.
     * <p>The default implementation checks for a JSTL locale attribute
     * in request, session or application scope; if not found,
     * returns the <code>HttpServletRequest.getLocale()</code>.
     * @return the fallback locale (never <code>null</code>)
     * @see javax.servlet.http.HttpServletRequest#getLocale()
     */
    protected Locale getFallbackLocale() {
        Locale locale = XpLocaleResolver.getXpLocale(getRequest(), getServletContext());
        if (locale != null) {
                return locale;
        }
        return getRequest().getLocale();
    }

    /**
     * Inner class that isolates the JSTL dependency.
     * Just called to resolve the fallback locale if the JSTL API is present.
     */
    private static class XpLocaleResolver {
        public static Locale getXpLocale(HttpServletRequest request, ServletContext servletContext) {
            Object localeObject = Config.get(request, Config.FMT_LOCALE);
            if (localeObject == null) {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    localeObject = Config.get(session, Config.FMT_LOCALE);
                }
                if (localeObject == null && servletContext != null) {
                    localeObject = Config.get(servletContext, Config.FMT_LOCALE);
                }
            }
            return (localeObject instanceof Locale ? (Locale) localeObject : null);
        }
    }


}
