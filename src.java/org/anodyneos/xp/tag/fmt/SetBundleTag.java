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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.anodyneos.xp.http.HttpXpContext;
import org.anodyneos.xp.tagext.XpTagSupport;

/**
 * Support for tag handlers for &lt;setBundle&gt;, the JSTL 1.0 tag that loads a
 * resource bundle and stores it in a scoped variable.
 *
 * @author Jan Luehe
 */

public abstract class SetBundleTag extends XpTagSupport {

    // *********************************************************************
    // Protected state

    protected String basename; // 'basename' attribute

    // *********************************************************************
    // Private state

    private int scope; // 'scope' attribute

    private String var; // 'var' attribute

    // *********************************************************************
    // Constructor and initialization

    public SetBundleTag() {
        basename = null;
        scope = PageContext.PAGE_SCOPE;
    }

    // for tag attribute
    public void setBasename(String basename) {
        this.basename = basename;
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

    public void doTag() throws JspException {
        LocalizationContext locCtxt = BundleTag.getLocalizationContext(
                (HttpXpContext) getXpContext(), basename);

        if (var != null) {
            getXpContext().setAttribute(var, locCtxt, scope);
        } else {
            Config.set((HttpXpContext) getXpContext(),
                    Config.FMT_LOCALIZATION_CONTEXT, locCtxt, scope);
        }

    }

}
