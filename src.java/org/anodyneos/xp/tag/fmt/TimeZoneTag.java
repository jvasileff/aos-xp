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

import java.util.TimeZone;

import javax.servlet.jsp.el.ELException;

import org.anodyneos.xp.XpContentHandler;
import org.anodyneos.xp.XpContext;
import org.anodyneos.xp.XpException;
import org.anodyneos.xp.tagext.XpTag;
import org.anodyneos.xp.tagext.XpTagSupport;
import org.xml.sax.SAXException;

/**
 * Support for tag handlers for &lt;timeZone&gt;, the time zone tag in JSTL 1.0.
 *
 * @author Jan Luehe
 */

public abstract class TimeZoneTag extends XpTagSupport {

    // *********************************************************************
    // Protected state

    protected Object value; // 'value' attribute

    // *********************************************************************
    // Private state

    private TimeZone timeZone;

    // *********************************************************************
    // Constructor and initialization

    public TimeZoneTag() {
        value = null;
    }

    // for tag attribute
    public void setValue(Object value) {
        this.value = value;
    }

    // *********************************************************************
    // Collaboration with subtags

    public TimeZone getTimeZone() {
        return timeZone;
    }

    // *********************************************************************
    // Tag logic

    public void doTag(XpContentHandler out) throws XpException, SAXException, ELException {

        if (value == null) {
            timeZone = TimeZone.getTimeZone("GMT");
        } else if (value instanceof String) {
            if (((String) value).trim().equals("")) {
                timeZone = TimeZone.getTimeZone("GMT");
            } else {
                timeZone = TimeZone.getTimeZone((String) value);
            }
        } else {
            timeZone = (TimeZone) value;
        }

        getXpBody().invoke(out);
    }

    // *********************************************************************
    // Package-scoped utility methods

    /*
     * Determines and returns the time zone to be used by the given action.
     *
     * <p> If the given action is nested inside a &lt;timeZone&gt; action, the
     * time zone is taken from the enclosing &lt;timeZone&gt; action.
     *
     * <p> Otherwise, the time zone configuration setting <tt>
     * javax.servlet.jsp.jstl.core.Config.FMT_TIME_ZONE </tt> is used.
     *
     * @param pageContext the page containing the action for which the time zone
     * needs to be determined @param fromTag the action for which the time zone
     * needs to be determined
     *
     * @return the time zone, or <tt> null </tt> if the given action is not
     * nested inside a &lt;timeZone&gt; action and no time zone configuration
     * setting exists
     */
    static TimeZone getTimeZone(XpContext pc, XpTag fromTag) {
        TimeZone tz = null;

        XpTag t = findAncestorWithClass(fromTag, TimeZoneTag.class);
        if (t != null) {
            // use time zone from parent <timeZone> tag
            TimeZoneTag parent = (TimeZoneTag) t;
            tz = parent.getTimeZone();
        } else {
            // get time zone from configuration setting
            Object obj = Config.find(pc, Config.FMT_TIME_ZONE);
            if (obj != null) {
                if (obj instanceof TimeZone) {
                    tz = (TimeZone) obj;
                } else {
                    tz = TimeZone.getTimeZone((String) obj);
                }
            }
        }

        return tz;
    }
}
