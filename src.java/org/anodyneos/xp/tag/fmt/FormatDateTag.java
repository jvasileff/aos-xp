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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.jsp.el.ELException;

import org.anodyneos.xp.XpContentHandler;
import org.anodyneos.xp.XpContext;
import org.anodyneos.xp.XpException;
import org.anodyneos.xp.tag.Util;
import org.anodyneos.xp.tagext.XpTagSupport;
import org.xml.sax.SAXException;

/**
 * Support for tag handlers for &lt;formatDate&gt;, the date and time formatting
 * tag in JSTL 1.0.
 *
 * @author Jan Luehe
 */

public abstract class FormatDateTag extends XpTagSupport {

    // *********************************************************************
    // Private constants

    private static final String DATE = "date";

    private static final String TIME = "time";

    private static final String DATETIME = "both";

    // *********************************************************************
    // Protected state

    protected Date value; // 'value' attribute

    protected String type; // 'type' attribute

    protected String pattern; // 'pattern' attribute

    protected Object timeZone; // 'timeZone' attribute

    protected String dateStyle; // 'dateStyle' attribute

    protected String timeStyle; // 'timeStyle' attribute

    // *********************************************************************
    // Private state

    private String var; // 'var' attribute

    private int scope; // 'scope' attribute

    // *********************************************************************
    // Constructor and initialization

    public FormatDateTag() {
        type = dateStyle = timeStyle = null;
        pattern = var = null;
        value = null;
        timeZone = null;
        scope = XpContext.PAGE_SCOPE;
    }

    // 'value' attribute
    public void setValue(Date value) {
        this.value = value;
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

    /*
     * Formats the given date and time.
     */
    public void doTag(XpContentHandler out) throws XpException, SAXException, ELException {

        String formatted = null;

        if (value == null) {
            if (var != null) {
                getXpContext().removeAttribute(var, scope);
            }
            return; // do not invoke body for this tag.
        }

        // Create formatter
        Locale locale = SetLocaleTag.getFormattingLocale(getXpContext(), this, true, DateFormat.getAvailableLocales());

        if (locale != null) {
            DateFormat formatter = createFormatter(locale);

            // Apply pattern, if present
            if (pattern != null) {
                try {
                    ((SimpleDateFormat) formatter).applyPattern(pattern);
                } catch (ClassCastException cce) {
                    formatter = new SimpleDateFormat(pattern, locale);
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
                    throw new XpException("bad timezone");
                }
            } else {
                tz = TimeZoneTag.getTimeZone(getXpContext(), this);
            }
            if (tz != null) {
                formatter.setTimeZone(tz);
            }
            formatted = formatter.format(value);
        } else {
            // no formatting locale available, use Date.toString()
            formatted = value.toString();
        }

        if (var != null) {
            getXpContext().setAttribute(var, formatted, scope);
        } else {
            out.characters(formatted);
        }

        // do not invoke body for this tag
    }

    // *********************************************************************
    // Private utility methods

    private DateFormat createFormatter(Locale loc) throws XpException {
        DateFormat formatter = null;

        if ((type == null) || DATE.equalsIgnoreCase(type)) {
            formatter = DateFormat.getDateInstance(Util.getStyle(dateStyle, "FORMAT_DATE_INVALID_DATE_STYLE"), loc);
        } else if (TIME.equalsIgnoreCase(type)) {
            formatter = DateFormat.getTimeInstance(Util.getStyle(timeStyle, "FORMAT_DATE_INVALID_TIME_STYLE"), loc);
        } else if (DATETIME.equalsIgnoreCase(type)) {
            formatter = DateFormat.getDateTimeInstance(Util.getStyle(dateStyle, "FORMAT_DATE_INVALID_DATE_STYLE"), Util
                    .getStyle(timeStyle, "FORMAT_DATE_INVALID_TIME_STYLE"), loc);
        } else {
            throw new XpException("invalid date type: " + type);
        }

        return formatter;
    }
}
