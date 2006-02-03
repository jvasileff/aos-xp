/*
 * Created on May 8, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.anodyneos.xp.tag.core;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

import org.anodyneos.xp.XpException;
import org.anodyneos.xp.XpOutput;
import org.anodyneos.xp.tagext.XpTagSupport;
import org.xml.sax.SAXException;

/**
 * SetTag supports setting a value of a scripting variable or a property of a
 * target that may be a java bean or Map.
 *
 * <xp:set @value @var [@scope]/>
 *
 * <xp:set @var [@scope]>bodyContent </xp:set>
 *
 * <xp:set @value @target @property/>
 *
 * <xp:set @target @property>bodyContent </xp:set>
 *
 * @author jvas
 */
public class SetTag extends XpTagSupport {

    private Object value;
    private Object target;
    private String property;
    private String scope;
    private String var;

    /**
     *
     */
    public SetTag() {
        super();
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void doTag(XpOutput out) throws XpException, ELException, SAXException {
        if (null == value) {
            if (getXpBody() != null) {
                // get value from body
                value = getXpBody().invokeToString(out);
            } else {
                value = null;
            }
        }
        if (null != var) {
            // simple case, setting a scripting variable
            if (null == scope) {
                getXpContext().setAttribute(var, value);
            } else {
                getXpContext().setAttribute(var, value, getXpContext().resolveScope(scope));
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
                                    m.invoke(target, new Object[] { convertToExpectedType(value, m.getParameterTypes()[0])});
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
    private Object convertToExpectedType( final Object val, Class expectedType )
           throws ELException {
        ExpressionEvaluator evaluator = getXpContext().getExpressionEvaluator();
        return evaluator.evaluate( "${result}", expectedType,
            new VariableResolver() {
                public Object resolveVariable( String pName ) {
                    return val;
                }
            }, null );
    }

}
