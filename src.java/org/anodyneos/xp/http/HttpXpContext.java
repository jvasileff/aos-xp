/*
 * Created on May 9, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.anodyneos.xp.http;

import org.anodyneos.xp.XpContext;
import java.util.HashMap;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

/**
 * @author jvas
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public abstract class HttpXpContext implements XpContext {

    private ExpressionEvaluator expressionEvaluator;
    private VariableResolver  variableResolver;

    public static enum Scope {
            PAGE_SCOPE(0,"page"),
            REQUEST_SCOPE(1,"request"),
            SESSION_SCOPE(2,"session"),
            APPLICATION_SCOPE(3,"application");

            private int intValue;
            private String strValue;

            Scope (int intValue, String strValue){
                this.intValue = intValue;
                this.strValue = strValue;
            }
            public int toInt() {return intValue;}
            public String toString() {return strValue;}
        };

    public static HashMap<String,Scope> scopeMap = new HashMap<String,Scope>();
    static{
        scopeMap.put(Scope.PAGE_SCOPE.toString(),Scope.PAGE_SCOPE);
        scopeMap.put(Scope.REQUEST_SCOPE.toString(),Scope.REQUEST_SCOPE);
        scopeMap.put(Scope.SESSION_SCOPE.toString(),Scope.SESSION_SCOPE);
        scopeMap.put(Scope.APPLICATION_SCOPE.toString(),Scope.APPLICATION_SCOPE);
    }

    public int resolveScope(String scope){
        if (scopeMap.containsKey(scope)){
            Scope scopeInstance = scopeMap.get(scope);
            return scopeInstance.toInt();
        }else{
            return getDefaultScope();
        }
    }

    public Scope resolveToScope(String scope){
        return scopeMap.get(scope);
    }

    public int getDefaultScope(){
        return Scope.PAGE_SCOPE.toInt();
    }
    public void setExpressionEvaluator(ExpressionEvaluator expressionEvaluator){
        this.expressionEvaluator = expressionEvaluator;
    }
    public void setVariableResolver(VariableResolver  variableResolver ){
        this.variableResolver = variableResolver;
    }
    public ExpressionEvaluator getExpressionEvaluator(){
        return expressionEvaluator;
    }
    public VariableResolver getVariableResolver(){
        return variableResolver;
    }

    public Object getAttribute(String name){return getAttribute(name,getDefaultScope());}
    public void setAttribute(String name, Object obj){setAttribute(name, obj, getDefaultScope());}
    public void removeAttribute(String name){removeAttribute(name,getDefaultScope());}
}
