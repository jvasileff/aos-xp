package org.anodyneos.xpImpl.standalone;

import java.util.Enumeration;

import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.VariableResolver;

import org.anodyneos.xp.standalone.StandaloneXpContext;
import org.apache.commons.el.EnumeratedMap;


/**
 * @author jvas
 */
public class StandaloneVariableResolverImpl implements VariableResolver {

    StandaloneXpContext ctx;

    public StandaloneVariableResolverImpl(StandaloneXpContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Implicit variables take precidence over defined variables.
     */
    public Object resolveVariable(String pName) throws ELException {
        if (StandaloneXpContext.PAGE_SCOPE_STRING.equals(pName)) {
            return new PageScopeMap(ctx);
        } else if (StandaloneXpContext.APPLICATION_SCOPE_STRING.equals(pName)) {
            return new ApplicationScopeMap(ctx);
        } else {
            return ctx.getAttribute(pName);
        }
    }

    private class PageScopeMap extends EnumeratedMap {
        StandaloneXpContext ctx;
        public PageScopeMap(StandaloneXpContext ctx) {
            this.ctx = ctx;
        }

        public Enumeration enumerateKeys() {
            return ctx.getAttributeNamesInScope(StandaloneXpContext.PAGE_SCOPE);
        }


        public boolean isMutable() {
            return true;
        }

        public Object getValue(Object pKey) {
            if (pKey instanceof String) {
                return ctx.getAttribute((String) pKey, StandaloneXpContext.PAGE_SCOPE);
            } else {
                return null;
            }
        }

    }
    private class ApplicationScopeMap extends EnumeratedMap {
        StandaloneXpContext ctx;
        public ApplicationScopeMap(StandaloneXpContext ctx) {
            this.ctx = ctx;
        }

        public Enumeration enumerateKeys() {
            return ctx.getAttributeNamesInScope(StandaloneXpContext.APPLICATION_SCOPE);
        }

        public boolean isMutable() {
            return true;
        }

        public Object getValue(Object pKey) {
            if (pKey instanceof String) {
                return ctx.getAttribute((String) pKey, StandaloneXpContext.APPLICATION_SCOPE);
            } else {
                return null;
            }
        }
    }

}
