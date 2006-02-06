/*
 * Created on May 8, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.anodyneos.xpImpl.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jvas
 *
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public final class CoerceUtil {

    private static final Map<String, String> typesMap;
    private static final Map<String, String> nativeTypesMap;

    static {
        typesMap = new HashMap<String, String>();
        typesMap.put("java.lang.String", "String");
        typesMap.put("java.lang.Boolean", "Boolean");
        typesMap.put("java.lang.Byte", "Byte");
        typesMap.put("java.lang.Character", "Character");
        typesMap.put("java.lang.Double", "Double");
        typesMap.put("java.lang.Integer", "Integer");
        typesMap.put("java.lang.Float", "Float");
        typesMap.put("java.lang.Long", "Long");
        typesMap.put("java.lang.Short", "Short");
        typesMap.put("java.lang.Object", "Object");

        nativeTypesMap = new HashMap<String, String>();
        nativeTypesMap.put("boolean", "Boolean");
        nativeTypesMap.put("byte", "Byte");
        nativeTypesMap.put("char", "Character");
        nativeTypesMap.put("double", "Double");
        nativeTypesMap.put("int", "Integer");
        nativeTypesMap.put("float", "Float");
        nativeTypesMap.put("long", "Long");
        nativeTypesMap.put("short", "Short");
    }

    private CoerceUtil() {
        // no instances
    }

    public static boolean isNativeType(String type) {
        return nativeTypesMap.containsKey(type);
    }

    public static boolean isBoxClass(String type) {
        return nativeTypesMap.containsValue(simplifyType(type));
    }

    public static String boxClass(String type) {
        return nativeTypesMap.get(type);
    }

    public static String simplifyType(String type) {
        if (typesMap.containsKey(type)) {
            type = typesMap.get(type);
        }
        return type;
    }

    public static String javaExpression(String raw, String type) {
        // normalize type
        if (typesMap.containsKey(type)) {
            type = typesMap.get(type);
        }

        raw = raw == null ? "" : raw;

        if ("String".equals(type)) {
            return Util.escapeStringQuoted(raw);
        } else if ("boolean".equals(type) || "Boolean".equals(type)) {
            Boolean val = Boolean.valueOf(raw);
            if ("boolean".equals(type)) {
                return val.toString();
            } else {
                return val.booleanValue() ? "Boolean.TRUE" : "Boolean.FALSE";
            }
        } else if ("byte".equals(type) || "Byte".equals(type)) {
            String primitive;
            if (raw.length() == 0) {
                primitive = "(byte)0";
            } else {
                primitive = "(byte)" + Byte.valueOf(raw).toString();
            }
            if ("byte".equals(type)) {
                return primitive;
            } else {
                return "new Byte(" + primitive + ")";
            }
        } else if ("char".equals(type) || "Character".equals(type)) {
            String primitive;
            if (raw.length() == 0) {
                primitive = "(char)0";
            } else {
                primitive = "(char)" + new Integer(raw.charAt(0)).toString();
            }
            if ("char".equals(type)) {
                return primitive;
            } else {
                return "new Character(" + primitive + ")";
            }
        } else if ("double".equals(type) || "Double".equals(type)) {
            String primitive;
            if (raw.length() == 0) {
                primitive = "0.0";
            } else {
                Double val = Double.valueOf(raw);
                if (val.isNaN()) {
                    primitive = "Double.NaN";
                } else if (val.doubleValue() == Double.POSITIVE_INFINITY) {
                    primitive = "Double.POSITIVE_INFINITY";
                } else if (val.doubleValue() == Double.NEGATIVE_INFINITY) {
                    primitive = "Double.NEGATIVE_INFINITY";
                } else {
                    primitive = Double.valueOf(raw).toString();
                }
            }
            if ("double".equals(type)) {
                return primitive;
            } else {
                return "new Double(" + primitive + ")";
            }
        } else if ("int".equals(type) || "Integer".equals(type)) {
            String primitive;
            if (raw.length() == 0) {
                primitive = "0";
            } else {
                primitive = Integer.valueOf(raw).toString();
            }
            if ("int".equals(type)) {
                return primitive;
            } else {
                return "new Integer(" + primitive + ")";
            }
        } else if ("float".equals(type) || "Float".equals(type)) {
            String primitive;
            if (raw.length() == 0) {
                primitive = "0.0f";
            } else {
                Float val = Float.valueOf(raw);
                if (val.isNaN()) {
                    primitive = "Float.NaN";
                } else if (val.floatValue() == Float.POSITIVE_INFINITY) {
                    primitive = "Float.POSITIVE_INFINITY";
                } else if (val.floatValue() == Float.NEGATIVE_INFINITY) {
                    primitive = "Float.NEGATIVE_INFINITY";
                } else {
                    primitive = Float.valueOf(raw).toString() + "f";
                }
            }
            if ("float".equals(type)) {
                return primitive;
            } else {
                return "new Float(" + primitive + ")";
            }
        } else if ("long".equals(type) || "Long".equals(type)) {
            String primitive;
            if (raw.length() == 0) {
                primitive = "0L";
            } else {
                primitive = Long.valueOf(raw).toString() + "L";
            }
            if ("long".equals(type)) {
                return primitive;
            } else {
                return "new Long(" + primitive + ")";
            }
        } else if ("short".equals(type) || "Short".equals(type)) {
            String primitive;
            if (raw.length() == 0) {
                primitive = "(short)0";
            } else {
                primitive = "(short)" + Short.valueOf(raw).toString();
            }
            if ("short".equals(type)) {
                return primitive;
            } else {
                return "new Short(" + primitive + ")";
            }
        } else if ("Object".equals(type)) {
            // same as "String"
            return Util.escapeStringQuoted(raw);
        } else {
            throw new IllegalArgumentException("type not supported");
        }
    }
}
