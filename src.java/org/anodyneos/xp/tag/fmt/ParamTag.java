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

import javax.servlet.jsp.el.ELException;

import org.anodyneos.xp.XpException;
import org.anodyneos.xp.XpOutput;
import org.anodyneos.xp.tagext.XpTag;
import org.anodyneos.xp.tagext.XpTagSupport;
import org.xml.sax.SAXException;

/**
 * Support for tag handlers for &lt;param&gt;, the message argument subtag in
 * JSTL 1.0 which supplies an argument for parametric replacement to its parent
 * &lt;message&gt; tag.
 *
 * @see MessageSupport
 * @author Jan Luehe
 */

public class ParamTag extends XpTagSupport {

    // *********************************************************************
    // Protected state

    protected Object value; // 'value' attribute

    protected boolean valueSpecified; // status

    // *********************************************************************
    // Constructor and initialization

    public ParamTag() {
        value = null;
        valueSpecified = false;
    }

    public void setValue(Object value) {
        this.value = value;
        this.valueSpecified = true;
    }

    // *********************************************************************
    // Tag logic

    // Supply our value to our parent <fmt:message> tag
    public void doTag(XpOutput out) throws XpException, SAXException, ELException {
        XpTag t = findAncestorWithClass(this, MessageTag.class);
        if (t == null) {
            throw new XpException("param outside message");
        }
        MessageTag parent = (MessageTag) t;

        /*
         * Get argument from 'value' attribute or body, as appropriate, and add
         * it to enclosing <fmt:message> tag, even if it is null or equal to "".
         */
        Object input = null;
        // determine the input by...
        if (valueSpecified) {
            // ... reading 'value' attribute
            input = value;
        } else {
            // ... retrieving and trimming our body (TLV has ensured that it's
            // non-empty)
            input = getXpBody().invokeToString().trim();
        }
        parent.addParam(input);

    }

}
