package org.anodyneos.xpImpl.runtime;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

import org.anodyneos.xp.XpContext;
import org.anodyneos.xp.XpException;


/**
 * @author jvas
 */
public class XPTagSetHelper {

    private XPTagSetHelper() {
        super();
    }

    public static void set(XpContext xpContext, String var, String scope, Object target, String property, Object value) throws XpException, ELException {

        if (null != var) {
            // simple case, setting a scripting variable
            if (null == scope) {
                xpContext.setAttribute(var, value);
            } else {
                xpContext.setAttribute(var, value, xpContext.resolveScope(scope));
            }
        } else {
            // must have target and property
            if (target instanceof Map) {
                if(value == null) {
                    ((Map) target).remove(property);
                } else {
                    ((Map) target).put(property, value);
                }
            } else if (target != null) {
                // must be a javaBean
                try {
                    PropertyDescriptor pd[] =
                        Introspector.getBeanInfo(target.getClass()).getPropertyDescriptors();
                    boolean succeeded = false;
                    for (int i=0; i < pd.length; i++) {
                        if(pd[i].getName().equals(property)) {
                            Method m = pd[i].getWriteMethod();
                            if (m == null) {
                                throw new XpException("No setter method");
                            }
                            if (value != null) {
                                try {
                                    m.invoke(target, new Object[] { convertToExpectedType(xpContext, value, m.getParameterTypes()[0])});
                                } catch (ELException ex) {
                                    throw new XpException(ex.getMessage());
                                }
                            } else {
                                m.invoke(target, new Object[] {null});
                            }
                            succeeded = true;
                        }
                    }
                    if (!succeeded) {
                        throw new XpException("No setter method");
                    }
                } catch (IllegalAccessException ex) {
                    throw new XpException(ex.getMessage());
                } catch (IntrospectionException ex) {
                    throw new XpException(ex.getMessage());
                } catch (InvocationTargetException ex) {
                    throw new XpException(ex.getMessage());
                }
            } else {
                throw new XpException("Must provide target or var attribute");
            }
        }
    }

    /**
     * Convert an object to an expected type according to the conversion
     * rules of the Expression Language.
     */
    public static Object convertToExpectedType(XpContext xpContext, final Object value, Class expectedType )
           throws ELException {
        ExpressionEvaluator evaluator = xpContext.getExpressionEvaluator();
        return evaluator.evaluate( "${result}", expectedType,
            new VariableResolver() {
                public Object resolveVariable( String pName ) {
                    return value;
                }
            }, null );
    }
}
