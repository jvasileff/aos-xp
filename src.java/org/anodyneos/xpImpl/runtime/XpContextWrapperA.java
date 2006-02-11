package org.anodyneos.xpImpl.runtime;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;
import javax.servlet.jsp.tagext.VariableInfo;

import org.anodyneos.xp.XpContext;

/**
 * Portions adapted from Apache Jakarta Commons EL "JspContextWrapper.java"
 *
 * @author jvas
 */
public abstract class XpContextWrapperA extends XpContextA implements XpContext, VariableResolver {

    private Map<String, Object> pageScopeMap = new HashMap<String, Object>();
    private XpContext wrappedContext;

    private List<String> nestedVars;
    private List<String> atBeginVars;
    private List<String> atEndVars;
    private Map<String, String> aliases;

    private HashMap<String, Object> originalNestedVars;

    protected XpContextWrapperA(XpContext wrappedContext,
            List<String> nestedVars, List<String> atBeginVars, List<String> atEndVar,
            Map<String, String> aliases) {
        this.wrappedContext = wrappedContext;
        this.nestedVars = nestedVars;
        this.atBeginVars = atBeginVars;
        this.atEndVars = atEndVar;
        this.aliases = aliases;

        this.originalNestedVars = new HashMap<String, Object>(nestedVars.size());

        syncBeginTagFile();
    }

    protected final XpContext getWrappedContext() {
        return wrappedContext;
    }

    @Override
    public final Map<String, Object> getScopeAsMap(int scope) {
        if (PAGE_SCOPE == scope) {
            return pageScopeMap;
        } else {
            return wrappedContext.getScopeAsMap(scope);
        }
    }

    private void syncBeginTagFile() {
        saveNestedVariables();
    }

    /**
     * Synchronize variables before fragment invokation
     */
    public final void syncBeforeInvoke() {
        copyTagToPageScope(VariableInfo.NESTED);
        copyTagToPageScope(VariableInfo.AT_BEGIN);
    }

    /**
     * Synchronize variables at end of tag file
     */
    public final void syncEndTagFile() {
        copyTagToPageScope(VariableInfo.AT_BEGIN);
        copyTagToPageScope(VariableInfo.AT_END);
        restoreNestedVariables();
    }

    /**
     * Copies the variables of the given scope from the virtual page scope of
     * this JSP context wrapper to the page scope of the invoking JSP context.
     *
     * @param scope variable scope (one of NESTED, AT_BEGIN, or AT_END)
     */
    private void copyTagToPageScope(int scope) {
        Iterator iter = null;

        switch (scope) {
        case VariableInfo.NESTED:
            if (nestedVars != null) {
                iter = nestedVars.iterator();
            }
            break;
        case VariableInfo.AT_BEGIN:
            if (atBeginVars != null) {
                iter = atBeginVars.iterator();
            }
            break;
        case VariableInfo.AT_END:
            if (atEndVars != null) {
                iter = atEndVars.iterator();
            }
            break;
        }

        while ((iter != null) && iter.hasNext()) {
            String varName = (String) iter.next();
            Object obj = getAttribute(varName);
            varName = findAlias(varName);
            if (obj != null) {
                wrappedContext.setAttribute(varName, obj);
            } else {
                wrappedContext.removeAttribute(varName, PAGE_SCOPE);
            }
        }
    }

    /**
     * Saves the values of any NESTED variables that are present in
     * the invoking JSP context, so they can later be restored.
     */
    private void saveNestedVariables() {
        if (nestedVars != null) {
            Iterator<String> iter = nestedVars.iterator();
            while (iter.hasNext()) {
                String varName = iter.next();
                varName = findAlias(varName);
                Object obj = wrappedContext.getAttribute(varName);
                if (obj != null) {
                    originalNestedVars.put(varName, obj);
                }
            }
        }
    }

    /**
     * Restores the values of any NESTED variables in the invoking JSP
     * context.
     */
    private void restoreNestedVariables() {
        if (nestedVars != null) {
            Iterator iter = nestedVars.iterator();
            while (iter.hasNext()) {
                String varName = (String) iter.next();
                varName = findAlias(varName);
                Object obj = originalNestedVars.get(varName);
                if (obj != null) {
                    wrappedContext.setAttribute(varName, obj);
                } else {
                    wrappedContext.removeAttribute(varName, PAGE_SCOPE);
                }
            }
        }
    }

    /**
     * Checks to see if the given variable name is used as an alias, and if so,
     * returns the variable name for which it is used as an alias.
     *
     * @param varName The variable name to check
     * @return The variable name for which varName is used as an alias, or
     * varName if it is not being used as an alias
     */
    private String findAlias(String varName) {
        if (aliases == null)
            return varName;

        String alias = aliases.get(varName);
        if (alias == null) {
            return varName;
        }
        return alias;
    }

    @Override
    public abstract XpContext wrap(List<String> nestedVars, List<String> atBeginVars,
            List<String> atEndVar, Map<String, String> aliases);

    @Override
    public final int[] getScopes() {
        return wrappedContext.getScopes();
    }

    @Override
    public final int resolveScope(String scope) {
        return wrappedContext.resolveScope(scope);
    }

    @Override
    public final String resolveScope(int scope) {
        return wrappedContext.resolveScope(scope);
    }

    @Override
    public final ExpressionEvaluator getExpressionEvaluator() {
        return wrappedContext.getExpressionEvaluator();
    }

    // from VariableResolver interface
    public final Object resolveVariable(String name) throws ELException {
        if (PAGE_SCOPE_STRING.equals(name)) {
            return getScopeAsMap(PAGE_SCOPE);
        } else {
            return wrappedContext.getVariableResolver().resolveVariable(name);
        }
    }

    public final VariableResolver getVariableResolver() {
        return this;
    }

}
