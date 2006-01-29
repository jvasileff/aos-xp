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

package org.anodyneos.xp.tag.core;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.el.ELException;

import org.anodyneos.xp.XpContext;
import org.anodyneos.xp.XpException;
import org.anodyneos.xp.XpOutput;
import org.anodyneos.xp.http.HttpXpContext;
import org.anodyneos.xp.tagext.XpTagSupport;
import org.xml.sax.SAXException;

/**
 * <p>
 * Support for tag handlers for &lt;url&gt;, the URL creation and rewriting tag
 * in JSTL 1.0.
 * </p>
 *
 * @author Shawn Bayern
 */

public class UrlTag extends XpTagSupport implements ParamParent {

    private String value; // 'value' attribute

    private String context; // 'context' attribute

    private String attributeName;
    private String attributeNamespace;

    // *********************************************************************
    // Private state

    private String var; // 'var' attribute

    private int scope; // processed 'scope' attr

    private ParamTag.ParamManager params; // added parameters

    // *********************************************************************
    // Constructor and initialization

    public UrlTag() {
        value = var = null;
        params = null;
        context = null;
        scope = XpContext.PAGE_SCOPE;
    }

    // *********************************************************************
    // Tag attributes known at translation time

    public void setVar(String var) {
        this.var = var;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public void setAttributeNamespace(String attributeNamespace) {
        this.attributeNamespace = attributeNamespace;
    }

    public void setScope(String scope) {
        this.scope = getXpContext().resolveScope(scope);
    }

    // *********************************************************************
    // Collaboration with subtags

    // inherit Javadoc
    public void addParameter(String name, String value) {
        params.addParameter(name, value);
    }

    // *********************************************************************
    // Tag logic

    public void doTag(XpOutput out) throws XpException, SAXException,
            ELException {
        params = new ParamTag.ParamManager();

        if (null != getXpBody()) {
            getXpBody().invokeToString();
        }

        String result; // the eventual result

        // add (already encoded) parameters
        String baseUrl = resolveUrl(value, context,
                (HttpXpContext) getXpContext());
        result = params.aggregateParams(baseUrl);

        // if the URL is relative, rewrite it
        if (!isAbsoluteUrl(result)) {
            HttpServletResponse response = ((HttpServletResponse) ((HttpXpContext) getXpContext())
                    .getResponse());
            result = response.encodeURL(result);
        }

        // JIRA ZP-6
        // for some containers, "result" looks like: /zeus-proto/;jsessionid=AAECA83C53178F746DAFB5CE56CE9EA3
        // when the initial (pre session cookie) page is requested.
        // so we need to strip out the stuff after the semicolon
        int semicolonIndex = result.indexOf(';');
        result = semicolonIndex != -1 ? result.substring(0, semicolonIndex) : result;

        // store or print the output
        if (var != null) {
            getXpContext().setAttribute(var, result, scope);
        } else if (attributeName == null){
            out.write(result);
        }

        // always set an attribute if asked to do so
        if (attributeName != null) {

            out.addAttribute(attributeNamespace, attributeName, result);
        }

    }

    // *********************************************************************
    // Utility methods

    public static String resolveUrl(String url, String context,
            HttpXpContext pageContext) throws XpException {
        // don't touch absolute URLs
        if (isAbsoluteUrl(url))
            return url;

        // normalize relative URLs against a context root
        HttpServletRequest request = (HttpServletRequest) pageContext
                .getRequest();
        if (context == null) {
            if (url.startsWith("/"))
                return (request.getContextPath() + url);
            else
                return url;
        } else {
            if (!context.startsWith("/") || !url.startsWith("/")) {
                throw new XpException("IMPORT_BAD_RELATIVE");
            }
            if (context.equals("/")) {
                // Don't produce string starting with '//', many
                // browsers interpret this as host name, not as
                // path on same host.
                return url;
            } else {
                return (context + url);
            }
        }
    }

    // *********************************************************************
    // Public utility methods

    public static final String VALID_SCHEME_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789+.-";

    /**
     * Returns <tt>true</tt> if our current URL is absolute, <tt>false</tt>
     * otherwise.
     */
    public static boolean isAbsoluteUrl(String url) {
        // a null URL is not absolute, by our definition
        if (url == null)
            return false;

        // do a fast, simple check first
        int colonPos;
        if ((colonPos = url.indexOf(":")) == -1)
            return false;

        // if we DO have a colon, make sure that every character
        // leading up to it is a valid scheme character
        for (int i = 0; i < colonPos; i++)
            if (VALID_SCHEME_CHARS.indexOf(url.charAt(i)) == -1)
                return false;

        // if so, we've got an absolute url
        return true;
    }

}
