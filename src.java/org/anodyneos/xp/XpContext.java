package org.anodyneos.xp;

import java.util.Enumeration;

import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

public interface XpContext {

    void initialize();
    void release();

    Object getAttribute(String name);
    Object getAttribute(String name, int scope);

    void removeAttribute(String name);
    void removeAttribute(String name, int scope);

    void setAttribute(String name, Object obj);
    void setAttribute(String name, Object obj, int scope);

    Enumeration getAttributeNamesInScope(int scope);

    ExpressionEvaluator getExpressionEvaluator();
    VariableResolver getVariableResolver();

    int resolveScope(String scope);
}
