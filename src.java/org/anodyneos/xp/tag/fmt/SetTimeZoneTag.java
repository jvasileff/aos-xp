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
import org.anodyneos.xp.tagext.XpTagSupport;
import org.xml.sax.SAXException;

/**
 * Support for tag handlers for &lt;setTimeZone&gt;, the time zone setting tag
 * in JSTL 1.0.
 *
 * @author Jan Luehe
 */

public abstract class SetTimeZoneTag extends XpTagSupport {

    // *********************************************************************
    // Protected state

    protected Object value; // 'value' attribute

    // *********************************************************************
    // Private state

    private int scope; // 'scope' attribute

    private String var; // 'var' attribute

    // *********************************************************************
    // Constructor and initialization

    public SetTimeZoneTag() {
        value = var = null;
        scope = XpContext.PAGE_SCOPE;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    // *********************************************************************
    // Tag attributes known at translation time

    public void setScope(String scope) {
        this.scope = getXpContext().resolveScope(scope);
    }

    public void setVar(String var) {
        this.var = var;
    }

    // *********************************************************************
    // Tag logic

    public void doTag(XpContentHandler out) throws XpException, SAXException, ELException {
        TimeZone timeZone = null;

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

        if (var != null) {
            getXpContext().setAttribute(var, timeZone, scope);
        } else {
            Config.set(getXpContext(), Config.FMT_TIME_ZONE, timeZone, scope);
        }

        // no contents, do not invoke body.
    }
}
