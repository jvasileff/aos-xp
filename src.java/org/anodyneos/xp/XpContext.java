package org.anodyneos.xp;

import java.util.Enumeration;

import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

public interface XpContext {

    public static final int PAGE_SCOPE = 1;
    public static final int APPLICATION_SCOPE = 2;

    public static final String PAGE_SCOPE_STRING = "page";
    public static final String APPLICATION_SCOPE_STRING = "application";

    //void initialize();
    void release();

    Object getAttribute(String name);
    Object getAttribute(String name, int scope);

    void removeAttribute(String name);
    void removeAttribute(String name, int scope);

    void setAttribute(String name, Object obj);
    void setAttribute(String name, Object obj, int scope);

    Enumeration getAttributeNamesInScope(int scope);

    Object findAttribute(String name);
    int getAttributesScope(String name);

    ExpressionEvaluator getExpressionEvaluator();
    VariableResolver getVariableResolver();

    int resolveScope(String scope);
}
