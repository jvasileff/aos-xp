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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.jsp.el.ELException;

import org.anodyneos.xp.XpContext;
import org.anodyneos.xp.XpException;
import org.anodyneos.xp.XpOutput;
import org.anodyneos.xp.tag.Util;
import org.anodyneos.xp.tagext.XpTagSupport;
import org.xml.sax.SAXException;

/**
 * Support for tag handlers for &lt;parseDate&gt;, the date and time parsing tag
 * in JSTL 1.0.
 *
 * @author Jan Luehe
 */

public class ParseDateTag extends XpTagSupport {

    // *********************************************************************
    // Private constants

    private static final String DATE = "date";

    private static final String TIME = "time";

    private static final String DATETIME = "both";

    // *********************************************************************
    // Protected state

    protected String value; // 'value' attribute

    protected boolean valueSpecified; // status

    protected String type; // 'type' attribute

    protected String pattern; // 'pattern' attribute

    protected Object timeZone; // 'timeZone' attribute

    protected Locale parseLocale; // 'parseLocale' attribute

    protected String dateStyle; // 'dateStyle' attribute

    protected String timeStyle; // 'timeStyle' attribute

    // *********************************************************************
    // Private state

    private String var; // 'var' attribute

    private int scope; // 'scope' attribute

    // *********************************************************************
    // Constructor and initialization

    public ParseDateTag() {
        type = dateStyle = timeStyle = null;
        value = pattern = var = null;
        valueSpecified = false;
        timeZone = null;
        scope = XpContext.PAGE_SCOPE;
        parseLocale = null;
    }

    // 'value' attribute
    public void setValue(String value) {
        this.value = value;
        this.valueSpecified = true;
    }

    // 'type' attribute
    public void setType(String type) {
        this.type = type;
    }

    // 'dateStyle' attribute
    public void setDateStyle(String dateStyle) {
        this.dateStyle = dateStyle;
    }

    // 'timeStyle' attribute
    public void setTimeStyle(String timeStyle) {
        this.timeStyle = timeStyle;
    }

    // 'pattern' attribute
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    // 'timeZone' attribute
    public void setTimeZone(Object timeZone) {
        this.timeZone = timeZone;
    }

    // 'parseLocale' attribute
    public void setParseLocale(Object loc) {
        if (loc != null) {
            if (loc instanceof Locale) {
                this.parseLocale = (Locale) loc;
            } else {
                if (!"".equals((String) loc)) {
                    this.parseLocale = SetLocaleTag.parseLocale((String) loc);
                }
            }
        }
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
    // Tag logic

    public void doTag(XpOutput out) throws XpException, SAXException, ELException {

        String input = null;

        // determine the input by...
        if (valueSpecified) {
            // ... reading 'value' attribute
            input = value;
        } else {
            // ... retrieving and trimming our body
            input = getXpBody().invokeToString().trim();
        }

        if ((input == null) || input.equals("")) {
            if (var != null) {
                getXpContext().removeAttribute(var, scope);
            }
            return; // do not invoke body for this tag;
        }

        /*
         * Set up parsing locale: Use locale specified via the 'parseLocale'
         * attribute (if present), or else determine page's locale.
         */
        Locale locale = parseLocale;
        if (locale == null)
            locale = SetLocaleTag.getFormattingLocale(getXpContext(), this, false, DateFormat.getAvailableLocales());
        if (locale == null) {
            throw new XpException("parse date: no parse locale");
        }

        // Create parser
        DateFormat parser = createParser(locale);

        // Apply pattern, if present
        if (pattern != null) {
            try {
                ((SimpleDateFormat) parser).applyPattern(pattern);
            } catch (ClassCastException cce) {
                parser = new SimpleDateFormat(pattern, locale);
            }
        }

        // Set time zone
        TimeZone tz = null;
        if ((timeZone instanceof String) && ((String) timeZone).equals("")) {
            timeZone = null;
        }
        if (timeZone != null) {
            if (timeZone instanceof String) {
                tz = TimeZone.getTimeZone((String) timeZone);
            } else if (timeZone instanceof TimeZone) {
                tz = (TimeZone) timeZone;
            } else {
                throw new XpException("parse date: bad timezone.");
            }
        } else {
            tz = TimeZoneTag.getTimeZone(getXpContext(), this);
        }
        if (tz != null) {
            parser.setTimeZone(tz);
        }

        // Parse date
        Date parsed = null;
        try {
            parsed = parser.parse(input);
        } catch (ParseException pe) {
            throw new XpException("parse date: parse error: " + input, pe);
        }

        if (var != null) {
            getXpContext().setAttribute(var, parsed, scope);
        } else {
            out.characters(parsed);
        }

        // do not invoke body for this tag.
    }

    // *********************************************************************
    // Private utility methods

    private DateFormat createParser(Locale loc) throws XpException {
        DateFormat parser = null;

        if ((type == null) || DATE.equalsIgnoreCase(type)) {
            parser = DateFormat.getDateInstance(Util.getStyle(dateStyle, "PARSE_DATE_INVALID_DATE_STYLE"), loc);
        } else if (TIME.equalsIgnoreCase(type)) {
            parser = DateFormat.getTimeInstance(Util.getStyle(timeStyle, "PARSE_DATE_INVALID_TIME_STYLE"), loc);
        } else if (DATETIME.equalsIgnoreCase(type)) {
            parser = DateFormat.getDateTimeInstance(Util.getStyle(dateStyle, "PARSE_DATE_INVALID_DATE_STYLE"), Util
                    .getStyle(timeStyle, "PARSE_DATE_INVALID_TIME_STYLE"), loc);
        } else {
            throw new XpException("parse date: invalid type: " + type);
        }

        parser.setLenient(false);

        return parser;
    }
}
