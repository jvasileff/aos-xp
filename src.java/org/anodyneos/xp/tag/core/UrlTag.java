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

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.el.ELException;

import org.anodyneos.xp.XpContext;
import org.anodyneos.xp.XpException;
import org.anodyneos.xp.XpOutput;
import org.anodyneos.xp.http.HttpXpContext;
import org.anodyneos.xp.tag.fmt.Config;
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

    public static final String DEFAULT_URI_ENCODING = "UTF-8";
    public static final String VALID_SCHEME_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789+.-";

    private String value; // 'value' attribute
    private String context; // 'context' attribute
    private String uriEncoding; // 'uriEncoding' attribute
    private String attributeName; // 'attributeName' attribute
    private String attributeNamespace; // 'attributeNamespace' attribute
    private String var; // 'var' attribute
    private int scope; // processed 'scope' attr

    private ParamTag.ParamManager params; // added parameters


    public UrlTag() {
        value = var = null;
        params = null;
        context = null;
        scope = XpContext.PAGE_SCOPE;
    }


    // attribute setters & getters
    public void setVar(String var) { this.var = var; }
    public void setValue(String value) { this.value = value; }
    public void setAttributeName(String attributeName) { this.attributeName = attributeName; }
    public void setAttributeNamespace(String attributeNamespace) { this.attributeNamespace = attributeNamespace; }
    public void setScope(String scope) { this.scope = getXpContext().resolveScope(scope); }
    public void setContext(String context) { this.context = context; }
    public void setUriEncoding(String uriEncoding) { this.uriEncoding = uriEncoding; }

    public String getContext() {
        String ctx = null;

        // try explicit setting via "context" tag attribute
        if (null != this.context) {
            ctx = this.context;
        }

        // try config
        if (null == ctx) {
            ctx = (String) Config.find(getXpContext(), Config.CORE_CONTEXT_URI);
        }

        // try http servlet
        if (null == ctx && getXpContext() instanceof HttpXpContext) {
            HttpServletRequest request = (HttpServletRequest) ((HttpXpContext)getXpContext()).getRequest();
            ctx = request.getContextPath();
        }

        return ctx;
    }


    // *********************************************************************
    // Collaboration with subtags

    public void addParameter(String name, String value) {
        params.addParameter(name, value);
    }

    // *********************************************************************
    // Tag logic

    public void doTag(XpOutput out) throws XpException, SAXException, ELException {
        params = new ParamTag.ParamManager();

        if (null != getXpBody()) {
            getXpBody().invokeToString();
        }

        String result; // the eventual result

        String contextUri = getContext();
        String baseUrl = resolveUri(value, contextUri);
        String encoding = getUriEncodingFor(baseUrl);
        try {
            result = params.aggregateParams(baseUrl, encoding);
        } catch (UnsupportedEncodingException ex) {
            throw new XpException(ex);
        }

        // if the URL is relative, rewrite it to include non-cookie based session ID
        /*
        if (!isAbsoluteUri(result) && getXpContext() instanceof HttpXpContext) {
            // JIRA ZP-6
            // The result looks like: /zeus-proto/;jsessionid=AAECA83C53178F746DAFB5CE56CE9EA3
            // which causes some pre-login problems (pre session cookie).  Disabling until solution worked out for
            // using the output of <c:url> to produce a baseURL template for post-process skinning.
            HttpServletResponse response = ((HttpServletResponse) ((HttpXpContext) getXpContext()).getResponse());
            result = response.encodeURL(result);
        }
        */

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

    /**
     * @param uri
     *                The absolute or relative URI to resolve;
     * @param contextUri
     *                The context URI used to resolve relative URIs with
     *                absolute paths; may be absolute or relative with an
     *                absolute path component.
     *
     * @return The resolved URI; may be relative, fully qualified, or absolute.
     */
    public static String resolveUri(String uri, String contextUri) throws XpException {

        if (null == contextUri) {
            return uri;
        }

        if (! isAbsoluteUri(contextUri) && ! contextUri.startsWith("/")) {
            throw new XpException("context URI must be absolute or relative with an absolute path");
        }

        if (isAbsoluteUri(uri)) {
            // don't touch absolute URIs
            return uri;
        } else if (! uri.startsWith("/")) {
            // don't touch relative URIs
            return uri;
        } else {
            // uri is an absolute path
            // avoid "//" in path part
            if(contextUri.endsWith("/")) {
                return (contextUri + uri.substring(1));
            } else {
                return (contextUri + uri);
            }
        }

    }


    /**
     * Returns <tt>true</tt> if our current URL is absolute, <tt>false</tt>
     * otherwise.
     */
    public static boolean isAbsoluteUri(String url) {
        // a null URL is not absolute, by our definition
        if (url == null) {
            return false;
        }

        // do a fast, simple check first
        int colonPos;
        if ((colonPos = url.indexOf(":")) == -1) {
            return false;
        }

        // if we DO have a colon, make sure that every character
        // leading up to it is a valid scheme character
        for (int i = 0; i < colonPos; i++) {
            if (VALID_SCHEME_CHARS.indexOf(url.charAt(i)) == -1) {
                return false;
            }
        }

        // if so, we've got an absolute url
        return true;
    }

    protected String getUriEncodingFor(String uri) {
        String encoding;

        encoding = this.uriEncoding;

        if (null == encoding) {
            encoding =  (String) Config.find(getXpContext(), Config.CORE_URI_ENCODING);
        }

        if (null == encoding && !isAbsoluteUri(uri) && (getXpContext() instanceof HttpXpContext)) {
            encoding = ((HttpXpContext) getXpContext()).getResponse().getCharacterEncoding();
        }

        if (null == encoding) {
            encoding =  (String) Config.find(getXpContext(), Config.CORE_FALLBACK_URI_ENCODING);
        }

        if (null == encoding) {
            encoding = DEFAULT_URI_ENCODING;
        }

        return encoding;
    }


}
