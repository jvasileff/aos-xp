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
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.jsp.el.ELException;

import org.anodyneos.xp.XpException;
import org.anodyneos.xp.XpOutput;
import org.anodyneos.xp.tagext.XpTag;
import org.anodyneos.xp.tagext.XpTagSupport;
import org.xml.sax.SAXException;

/**
 * <p>
 * Support for tag handlers for &lt;param&gt;, the URL parameter subtag for
 * &lt;import&gt; in JSTL 1.0.
 * </p>
 *
 * @see ParamParent, ImportSupport, URLEncodeSupport
 * @author Shawn Bayern
 */

public class ParamTag extends XpTagSupport {

    // *********************************************************************
    // Protected state

    protected String name; // 'name' attribute

    protected String value; // 'value' attribute

    // *********************************************************************
    // Constructor and initialization

    public ParamTag() {
        name = value = null;
    }

    // *********************************************************************
    // Tag logic

    // simply send our name and value to our appropriate ancestor
    public void doTag(XpOutput out) throws XpException, SAXException,
            ELException {
        XpTag t = findAncestorWithClass(this, ParamParent.class);
        if (t == null) {
            throw new XpException("No ParamParent found.");
        }

        // take no action for null or empty names
        if (name == null || name.equals("")) {
            return;
        }

        // send the parameter to the appropriate ancestor
        ParamParent parent = (ParamParent) t;
        String value = this.value;
        if (value == null) {
            if (getXpBody() == null) {
                value = "";
            } else {
                value = getXpBody().invokeToString().trim();
            }
        }

        parent.addParameter(name, value);

    }

    // for tag attribute
    public void setName(String name) {
        this.name = name;
    }

    // for tag attribute
    public void setValue(String value) {
        this.value = value;
    }

    // *********************************************************************
    // Support for parameter management

    /**
     * Provides support for aggregating query parameters in URLs. Specifically,
     * accepts a series of parameters, ensuring that - newer parameters will
     * precede older ones in the output URL - all supplied parameters precede
     * those in the input URL
     */
    public static class ParamManager {

        // *********************************
        // Private state

        private List names = new LinkedList();

        private List values = new LinkedList();

        private boolean done = false;

        // *********************************
        // Public interface

        /** Adds a new parameter to the list. */
        public void addParameter(String name, String value) {
            if (done)
                throw new IllegalStateException();
            if (name != null) {
                names.add(name);
                if (value != null)
                    values.add(value);
                else
                    values.add("");
            }
        }

        /**
         * Produces a new URL with the stored parameters, in the appropriate
         * order.
         */
        public String aggregateParams(String url, String encoding) throws UnsupportedEncodingException {
            if (done) {
                throw new IllegalStateException();
            }

            done = true;

            // build a string from the parameter list
            StringBuffer newParams = new StringBuffer();
            for (int i = 0; i < names.size(); i++) {
                newParams.append(URLEncoder.encode((String) names.get(i), encoding));
                newParams.append('=');
                newParams.append(URLEncoder.encode((String) values.get(i), encoding));
                if (i < (names.size() - 1)) {
                    newParams.append("&");
                }
            }

            // insert these parameters into the URL as appropriate
            if (newParams.length() > 0) {
                int questionMark = url.indexOf('?');
                if (questionMark == -1) {
                    return (url + "?" + newParams);
                } else {
                    StringBuffer workingUrl = new StringBuffer(url);
                    workingUrl.insert(questionMark + 1, (newParams + "&"));
                    return workingUrl.toString();
                }
            } else {
                return url;
            }
        }
    }
}
