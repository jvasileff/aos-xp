/*
 * Created on Jan 27, 2005
 */
package org.anodyneos.xpImpl.util;

import java.util.ArrayList;

import org.xml.sax.SAXException;

/**
 * @author jvas
 */
public class FunctionUtil {

    /**
     * Get the parameters types from the function signature.
     * @return An array of parameter class names
     */
    public static String[] getParameters(String signature) throws SAXException {
        ArrayList<String> params = new ArrayList<String>();
        // Signature is of the form
        // <return-type> S <method-name S? '('
        // < <arg-type> ( ',' <arg-type> )* )? ')'
        int start = signature.indexOf('(') + 1;
        boolean lastArg = false;
        while (true) {
            int p = signature.indexOf(',', start);
            if (p < 0) {
                p = signature.indexOf(')', start);
                if (p < 0) {
                    throw new SAXException("Invalid signature: " + signature);
                }
                lastArg = true;
            }
            String arg = signature.substring(start, p).trim();
            if (!"".equals(arg)) {
                params.add(arg);
            }
            if (lastArg) {
                break;
            }
            start = p+1;
        }
        return (String[]) params.toArray(new String[params.size()]);
    }

    public static String getParameterCode(String[] params) {
        StringBuffer ds = new StringBuffer();
        ds.append("new Class[] {");
        for (int k = 0; k < params.length; k++) {
            if (k != 0) {
                ds.append(", ");
            }
            int iArray = params[k].indexOf('[');
            if (iArray < 0) {
                ds.append(params[k] + ".class");
            }
            else {
                String baseType = params[k].substring(0, iArray);
                ds.append("java.lang.reflect.Array.newInstance(");
                ds.append(baseType);
                ds.append(".class,");

                // Count the number of array dimension
                int aCount = 0;
                for (int jj = iArray; jj < params[k].length(); jj++ ) {
                    if (params[k].charAt(jj) == '[') {
                        aCount++;
                    }
                }
                if (aCount == 1) {
                    ds.append("0).getClass()");
                } else {
                    ds.append("new int[" + aCount + "]).getClass()");
                }
            }
        }
        ds.append("}");
        return ds.toString();
    }

    public static String getParameterCode(String signature) throws SAXException {
        return getParameterCode(getParameters(signature));
    }

    /**
     * Get the method name from the signature.
     */
    public static String getMethod(String signature) throws SAXException {
        int start = signature.indexOf(' ');
        if (start < 0) {
            throw new SAXException("Invalid signature: " + signature);
        }
        int end = signature.indexOf('(');
        if (end < 0) {
            throw new SAXException("Invalid signature: " + signature);
        }
        return signature.substring(start+1, end).trim();
    }

}
