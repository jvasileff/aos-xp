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

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.anodyneos.xp.XpContentHandler;
import org.anodyneos.xp.XpContext;
import org.anodyneos.xp.XpException;
import org.anodyneos.xp.http.HttpXpContext;
import org.anodyneos.xp.tag.Util;
import org.anodyneos.xp.tagext.XpTag;
import org.anodyneos.xp.tagext.XpTagSupport;

/**
 * Support for tag handlers for &lt;setLocale&gt;, the locale setting tag in
 * JSTL 1.0.
 *
 * @author jvas
 */
public class SetLocaleTag extends XpTagSupport {

    // *********************************************************************
    // Private constants

    private static final char HYPHEN = '-';

    private static final char UNDERSCORE = '_';

    // *********************************************************************
    // Protected state

    private Object value; // 'value' attribute

    private String variant; // 'variant' attribute

    // *********************************************************************
    // Private state

    private int scope; // 'scope' attribute

    // *********************************************************************
    // Constructor and initialization

    public SetLocaleTag() {
        value = variant = null;
        scope = XpContext.PAGE_SCOPE;
    }

    // *********************************************************************
    // Accessor methods

    // for tag attribute
    public void setValue(Object value) {
        this.value = value;
    }

    // for tag attribute
    public void setVariant(String variant) {
        this.variant = variant;
    }

    // *********************************************************************
    // Tag attributes known at translation time

    public void setScope(String scope) {
        this.scope = getXpContext().resolveScope(scope);
    }

    // *********************************************************************
    // Tag logic

    public void doTag(XpContentHandler out) throws XpException {
        Locale locale = null;

        if (value == null) {
            locale = Locale.getDefault();
        } else if (value instanceof String) {
            if (((String) value).trim().equals("")) {
                locale = Locale.getDefault();
            } else {
                locale = parseLocale((String) value, variant);
            }
        } else {
            locale = (Locale) value;
        }

        Config.set(getXpContext(), Config.FMT_LOCALE, locale, scope);
        // setResponseLocale(pageContext, locale);
    }

    // *********************************************************************
    // Public utility methods

    /**
     * See parseLocale(String, String) for details.
     */
    public static Locale parseLocale(String locale) {
        return parseLocale(locale, null);
    }

    /**
     * Parses the given locale string into its language and (optionally) country
     * components, and returns the corresponding <tt>java.util.Locale</tt>
     * object.
     *
     * If the given locale string is null or empty, the runtime's default locale
     * is returned.
     *
     * @param locale
     *            the locale string to parse
     * @param variant
     *            the variant
     *
     * @return <tt>java.util.Locale</tt> object corresponding to the given
     *         locale string, or the runtime's default locale if the locale
     *         string is null or empty
     *
     * @throws IllegalArgumentException
     *             if the given locale does not have a language component or has
     *             an empty country component
     */
    public static Locale parseLocale(String locale, String variant) {

        Locale ret = null;
        String language = locale;
        String country = null;
        int index = -1;

        if (((index = locale.indexOf(HYPHEN)) > -1) || ((index = locale.indexOf(UNDERSCORE)) > -1)) {
            language = locale.substring(0, index);
            country = locale.substring(index + 1);
        }

        if ((language == null) || (language.length() == 0)) {
            throw new IllegalArgumentException("Missing language");
        }

        if (country == null) {
            if (variant != null)
                ret = new Locale(language, "", variant);
            else
                ret = new Locale(language, "");
        } else if (country.length() > 0) {
            if (variant != null)
                ret = new Locale(language, country, variant);
            else
                ret = new Locale(language, country);
        } else {
            throw new IllegalArgumentException("Missing country");
        }

        return ret;
    }

    // *********************************************************************
    // Package-scoped utility methods

    /*
     * Returns the formatting locale to use with the given formatting action in
     * the given page.
     *
     * @param pc The page context containing the formatting action @param
     * fromTag The formatting action @param format <tt> true </tt> if the
     * formatting action is of type <formatXXX> (as opposed to <parseXXX>), and
     * <tt> false </tt> otherwise (if set to <tt> true </tt> , the formatting
     * locale that is returned by this method is used to set the response
     * locale).
     *
     * @param avail the array of available locales
     *
     * @return the formatting locale to use
     */
    static Locale getFormattingLocale(XpContext pc, XpTag fromTag, boolean format, Locale[] avail) {

        LocalizationContext locCtxt = null;

        // Get formatting locale from enclosing <fmt:bundle>
        XpTag parent = findAncestorWithClass(fromTag, BundleTag.class);
        if (parent != null) {
            /*
             * use locale from localization context established by parent
             * <fmt:bundle> action, unless that locale is null
             */
            locCtxt = ((BundleTag) parent).getLocalizationContext();
            if (locCtxt.getLocale() != null) {
                if (format) {
                    // setResponseLocale(pc, locCtxt.getLocale());
                }
                return locCtxt.getLocale();
            }
        }

        // Use locale from default I18N localization context, unless it is null
        if ((locCtxt = BundleTag.getLocalizationContext(pc)) != null) {
            if (locCtxt.getLocale() != null) {
                if (format) {
                    // setResponseLocale(pc, locCtxt.getLocale());
                }
                return locCtxt.getLocale();
            }
        }

        /*
         * Establish formatting locale by comparing the preferred locales (in
         * order of preference) against the available formatting locales, and
         * determining the best matching locale.
         */
        Locale match = null;
        Locale pref = getLocale(pc, Config.FMT_LOCALE);
        if (pref != null) {
            // Preferred locale is application-based
            match = findFormattingMatch(pref, avail);
        } else {
            // Preferred locales are browser-based
            match = findFormattingMatch(pc, avail);
        }
        if (match == null) {
            // Use fallback locale.
            pref = getLocale(pc, Config.FMT_FALLBACK_LOCALE);
            if (pref != null) {
                match = findFormattingMatch(pref, avail);
            }
        }
        if (format && (match != null)) {
            // setResponseLocale(pc, match);
        }

        return match;
    }

    /**
     * Setup the available formatting locales that will be used by
     * getFormattingLocale(XpContext).
     */
    static Locale[] availableFormattingLocales;
    static {
        Locale[] dateLocales = DateFormat.getAvailableLocales();
        Locale[] numberLocales = NumberFormat.getAvailableLocales();
        Vector vec = new Vector(dateLocales.length);
        for (int i = 0; i < dateLocales.length; i++) {
            for (int j = 0; j < numberLocales.length; j++) {
                if (dateLocales[i].equals(numberLocales[j])) {
                    vec.add(dateLocales[i]);
                    break;
                }
            }
        }
        availableFormattingLocales = new Locale[vec.size()];
        availableFormattingLocales = (Locale[]) vec.toArray(availableFormattingLocales);
        /*
         * for (int i=0; i <availableFormattingLocales.length; i++) {
         * System.out.println("AvailableLocale[" + i + "] " +
         * availableFormattingLocales[i]); }
         */
    }

    /*
     * Returns the formatting locale to use when <fmt:message> is used with a
     * locale-less localization context.
     *
     * @param pc The page context containing the formatting action @return the
     * formatting locale to use
     */
    static Locale getFormattingLocale(XpContext pc) {
        /*
         * Establish formatting locale by comparing the preferred locales (in
         * order of preference) against the available formatting locales, and
         * determining the best matching locale.
         */
        Locale match = null;
        Locale pref = getLocale(pc, Config.FMT_LOCALE);
        if (pref != null) {
            // Preferred locale is application-based
            match = findFormattingMatch(pref, availableFormattingLocales);
        } else {
            // Preferred locales are browser-based
            match = findFormattingMatch(pc, availableFormattingLocales);
        }
        if (match == null) {
            // Use fallback locale.
            pref = getLocale(pc, Config.FMT_FALLBACK_LOCALE);
            if (pref != null) {
                match = findFormattingMatch(pref, availableFormattingLocales);
            }
        }
        if (match != null) {
            // setResponseLocale(pc, match);
        }

        return match;
    }

    /*
     * Returns the locale specified by the named scoped attribute or context
     * configuration parameter.
     *
     * <p> The named scoped attribute is searched in the page, request, session
     * (if valid), and application scope(s) (in this order). If no such
     * attribute exists in any of the scopes, the locale is taken from the named
     * context configuration parameter.
     *
     * @param pageContext the page in which to search for the named scoped
     * attribute or context configuration parameter @param name the name of the
     * scoped attribute or context configuration parameter
     *
     * @return the locale specified by the named scoped attribute or context
     * configuration parameter, or <tt> null </tt> if no scoped attribute or
     * configuration parameter with the given name exists
     */
    static Locale getLocale(XpContext pageContext, String name) {
        Locale loc = null;

        Object obj = Config.find(pageContext, name);
        if (obj != null) {
            if (obj instanceof Locale) {
                loc = (Locale) obj;
            } else {
                loc = parseLocale((String) obj);
            }
        }

        return loc;
    }

    // *********************************************************************
    // Private utility methods

    /*
     * Determines the client's preferred locales from the request, and compares
     * each of the locales (in order of preference) against the available
     * locales in order to determine the best matching locale.
     *
     * @param pageContext Page containing the formatting action @param avail
     * Available formatting locales
     *
     * @return Best matching locale, or <tt> null </tt> if no match was found
     */
    private static Locale findFormattingMatch(XpContext xpc, Locale[] avail) {
        if (!(xpc instanceof HttpXpContext)) {
            // We don't know what the client wants, return system locale.
            // TODO: should we do this?
            return Locale.getDefault();
        } else {
            // Determine from client's browser settings.
            HttpXpContext hxpc = (HttpXpContext) xpc;
            Locale match = null;
            for (Enumeration enum_ = Util.getRequestLocales((HttpServletRequest) hxpc.getRequest()); enum_
                    .hasMoreElements();) {
                Locale locale = (Locale) enum_.nextElement();
                match = findFormattingMatch(locale, avail);
                if (match != null) {
                    break;
                }
            }
            return match;
        }
    }

    /*
     * Returns the best match between the given preferred locale and the given
     * available locales.
     *
     * The best match is given as the first available locale that exactly
     * matches the given preferred locale ("exact match"). If no exact match
     * exists, the best match is given to an available locale that meets the
     * following criteria (in order of priority): - available locale's variant
     * is empty and exact match for both language and country - available
     * locale's variant and country are empty, and exact match for language.
     *
     * @param pref the preferred locale @param avail the available formatting
     * locales
     *
     * @return Available locale that best matches the given preferred locale, or
     * <tt> null </tt> if no match exists
     */
    private static Locale findFormattingMatch(Locale pref, Locale[] avail) {
        Locale match = null;
        boolean langAndCountryMatch = false;
        for (int i = 0; i < avail.length; i++) {
            if (pref.equals(avail[i])) {
                // Exact match
                match = avail[i];
                break;
            } else if (!"".equals(pref.getVariant()) && "".equals(avail[i].getVariant())
                    && pref.getLanguage().equals(avail[i].getLanguage())
                    && pref.getCountry().equals(avail[i].getCountry())) {
                // Language and country match; different variant
                match = avail[i];
                langAndCountryMatch = true;
            } else if (!langAndCountryMatch && pref.getLanguage().equals(avail[i].getLanguage())
                    && ("".equals(avail[i].getCountry()))) {
                // Language match
                if (match == null) {
                    match = avail[i];
                }
            }
        }
        return match;
    }

}
