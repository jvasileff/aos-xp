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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.jsp.el.ELException;

import org.anodyneos.xp.XpContext;
import org.anodyneos.xp.XpException;
import org.anodyneos.xp.XpOutput;
import org.anodyneos.xp.tagext.XpTag;
import org.anodyneos.xp.tagext.XpTagSupport;
import org.xml.sax.SAXException;

/**
 * Support for tag handlers for &lt;message&gt;, the message formatting tag in
 * JSTL 1.0.
 *
 * @author Jan Luehe
 */

public class MessageTag extends XpTagSupport {

    // *********************************************************************
    // Public constants

    public static final String UNDEFINED_KEY = "???";

    // *********************************************************************
    // Protected state

    protected String keyAttrValue; // 'key' attribute value

    protected boolean keySpecified; // 'key' attribute specified

    protected LocalizationContext bundleAttrValue; // 'bundle' attribute value

    protected boolean bundleSpecified; // 'bundle' attribute specified?

    // *********************************************************************
    // Private state

    private String var; // 'var' attribute

    private int scope; // 'scope' attribute

    private List<Object> params;

    // *********************************************************************
    // Constructor and initialization

    public MessageTag() {
        params = new ArrayList<Object>();
        var = null;
        scope = XpContext.PAGE_SCOPE;
        keyAttrValue = null;
        keySpecified = false;
        bundleAttrValue = null;
        bundleSpecified = false;
    }

    // for tag attribute
    public void setKey(String key) {
        this.keyAttrValue = key;
        this.keySpecified = true;
    }

    // for tag attribute
    public void setBundle(LocalizationContext locCtxt) {
        this.bundleAttrValue = locCtxt;
        this.bundleSpecified = true;
    }

    // *********************************************************************
    // Tag attributes known at translation time

    public void setVar(String var) {
        this.var = var;
    }

    public void setScope(String scope) {
        this.scope = getXpContext().resolveScope(scope);
    }

    // *********************************************************************
    // Collaboration with subtags

    /**
     * Adds an argument (for parametric replacement) to this tag's message.
     *
     * @see ParamSupport
     */
    public void addParam(Object arg) {
        params.add(arg);
    }

    // *********************************************************************
    // Tag logic

    public void doTag(XpOutput out) throws XpException, SAXException,
            ELException {

        String key = null;
        LocalizationContext locCtxt = null;

        // determine the message key by...
        if (keySpecified) {
            // ... reading 'key' attribute
            key = keyAttrValue;
            // this check is required as getXpBody returns null if the tag looks like: <fmt:message key="x" />
            if (getXpBody() != null) {
                getXpBody().invokeToString(out); // use this to avoid output - body should only be param tags.
            }
        } else {
            // ... retrieving and trimming our body
            if (getXpBody() != null) {
                key = getXpBody().invokeToString(out).trim();
            }
        }

        if ((key == null) || key.equals("")) {
            out.write("??????");
        }

        String prefix = null;
        if (!bundleSpecified) {
            XpTag t = findAncestorWithClass(this, BundleTag.class);
            if (t != null) {
                // use resource bundle from parent <bundle> tag
                BundleTag parent = (BundleTag) t;
                locCtxt = parent.getLocalizationContext();
                prefix = parent.getPrefix();
            } else {
                locCtxt = BundleTag
                        .getLocalizationContext(getXpContext());
            }
        } else {
            // localization context taken from 'bundle' attribute
            locCtxt = bundleAttrValue;
            //if (locCtxt.getLocale() != null) {
            //    SetLocaleSupport.setResponseLocale(pageContext, locCtxt
            //            .getLocale());
            //}
        }

        String message = UNDEFINED_KEY + key + UNDEFINED_KEY;
        if (locCtxt != null) {
            ResourceBundle bundle = locCtxt.getResourceBundle();
            if (bundle != null) {
                try {
                    // prepend 'prefix' attribute from parent bundle
                    if (prefix != null)
                        key = prefix + key;
                    message = bundle.getString(key);
                    // Perform parametric replacement if required
                    if (!params.isEmpty()) {
                        Object[] messageArgs = params.toArray();
                        MessageFormat formatter = new MessageFormat(""); // empty
                        // pattern,
                        // default
                        // Locale
                        if (locCtxt.getLocale() != null) {
                            formatter.setLocale(locCtxt.getLocale());
                        } else {
                            // For consistency with the <fmt:formatXXX> actions,
                            // we try to get a locale that matches the user's
                            // preferences
                            // as well as the locales supported by 'date' and
                            // 'number'.
                            // System.out.println("LOCALE-LESS LOCCTXT: GETTING
                            // FORMATTING LOCALE");
                            Locale locale = SetLocaleTag
                                    .getFormattingLocale(getXpContext());
                            // System.out.println("LOCALE: " + locale);
                            if (locale != null) {
                                formatter.setLocale(locale);
                            }
                        }
                        formatter.applyPattern(message);
                        message = formatter.format(messageArgs);
                    }
                } catch (MissingResourceException mre) {
                    message = UNDEFINED_KEY + key + UNDEFINED_KEY;
                }
            }
        }

        if (var != null) {
            getXpContext().setAttribute(var, message, scope);
        } else {
            out.write(message);
        }
    }

}
