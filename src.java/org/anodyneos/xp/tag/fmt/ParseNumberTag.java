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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import javax.servlet.jsp.el.ELException;

import org.anodyneos.xp.XpContext;
import org.anodyneos.xp.XpException;
import org.anodyneos.xp.XpOutput;
import org.anodyneos.xp.tagext.XpTagSupport;
import org.xml.sax.SAXException;

/**
 * Support for tag handlers for &lt;parseNumber&gt;, the number parsing tag in
 * JSTL 1.0.
 *
 * @author Jan Luehe
 */

public class ParseNumberTag extends XpTagSupport {

    // *********************************************************************
    // Private constants

    private static final String NUMBER = "number";

    private static final String CURRENCY = "currency";

    private static final String PERCENT = "percent";

    // *********************************************************************
    // Protected state

    protected String value; // 'value' attribute

    protected boolean valueSpecified; // status

    protected String type; // 'type' attribute

    protected String pattern; // 'pattern' attribute

    protected Locale parseLocale; // 'parseLocale' attribute

    protected boolean isIntegerOnly; // 'integerOnly' attribute

    protected boolean integerOnlySpecified;

    // *********************************************************************
    // Private state

    private String var; // 'var' attribute

    private int scope; // 'scope' attribute

    // *********************************************************************
    // Constructor and initialization

    public ParseNumberTag() {
        value = type = pattern = var = null;
        valueSpecified = false;
        parseLocale = null;
        integerOnlySpecified = false;
        scope = XpContext.PAGE_SCOPE;
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

    // 'pattern' attribute
    public void setPattern(String pattern) {
        this.pattern = pattern;
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

    // 'integerOnly' attribute
    public void setIntegerOnly(boolean isIntegerOnly) {
        this.isIntegerOnly = isIntegerOnly;
        this.integerOnlySpecified = true;
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

    public void doTag(XpOutput out) throws XpException, ELException, SAXException {
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
            return; // do not invoke body for this tag
        }

        /*
         * Set up parsing locale: Use locale specified via the 'parseLocale'
         * attribute (if present), or else determine page's locale.
         */
        Locale loc = parseLocale;
        if (loc == null)
            loc = SetLocaleTag.getFormattingLocale(getXpContext(), this, false, NumberFormat.getAvailableLocales());
        if (loc == null) {
            throw new XpException("parse number, no parse locale");
        }

        // Create parser
        NumberFormat parser = null;
        if ((pattern != null) && !pattern.equals("")) {
            // if 'pattern' is specified, 'type' is ignored
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(loc);
            parser = new DecimalFormat(pattern, symbols);
        } else {
            parser = createParser(loc);
        }

        // Configure parser
        if (integerOnlySpecified)
            parser.setParseIntegerOnly(isIntegerOnly);

        // Parse number
        Number parsed = null;
        try {
            parsed = parser.parse(input);
        } catch (ParseException pe) {
            throw new XpException("parse number: parse error: " + input, pe);
        }

        if (var != null) {
            getXpContext().setAttribute(var, parsed, scope);
        } else {
            out.write(parsed);
        }

        // do not invoke body for this tag;
    }

    // *********************************************************************
    // Private utility methods

    private NumberFormat createParser(Locale loc) throws XpException {
        NumberFormat parser = null;

        if ((type == null) || NUMBER.equalsIgnoreCase(type)) {
            parser = NumberFormat.getNumberInstance(loc);
        } else if (CURRENCY.equalsIgnoreCase(type)) {
            parser = NumberFormat.getCurrencyInstance(loc);
        } else if (PERCENT.equalsIgnoreCase(type)) {
            parser = NumberFormat.getPercentInstance(loc);
        } else {
            throw new XpException("parse number: invalid type: " + type);
        }

        return parser;
    }
}
