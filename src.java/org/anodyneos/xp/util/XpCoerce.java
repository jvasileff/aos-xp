package org.anodyneos.xp.util;

public class XpCoerce {

    public static final Object coerceToObject(char x)       { return new Character(x); }
    public static final Object coerceToObject(byte x)       { return new Byte(x); }
    public static final Object coerceToObject(boolean x)    { return new Boolean(x); }
    public static final Object coerceToObject(int x)        { return new Integer(x); }
    public static final Object coerceToObject(long x)       { return new Long(x); }
    public static final Object coerceToObject(float x)      { return new Float(x); }
    public static final Object coerceToObject(double x)     { return new Double(x); }
    public static final Object coerceToObject(Object x)     { return x; }

    public static final String coerceToString(char x)       { return String.valueOf(x); }
    public static final String coerceToString(char[] x)     { return String.valueOf(x); }
    public static final String coerceToString(byte x)       { return String.valueOf(x); }
    public static final String coerceToString(byte[] x)     { return new String(x); }
    public static final String coerceToString(boolean x)    { return String.valueOf(x); }
    public static final String coerceToString(int x)        { return String.valueOf(x); }
    public static final String coerceToString(long x)       { return String.valueOf(x); }
    public static final String coerceToString(float x)      { return String.valueOf(x); }
    public static final String coerceToString(double x)     { return String.valueOf(x); }
    public static final String coerceToString(Object x)     { if (null == x) return null; else return x.toString(); }

    public static final boolean coerceToBooleanType(String x) {
        return "true".equalsIgnoreCase(x);
    }

    public static final byte coerceToByteType(String x) {
        if(null == x || x.length()==0) {
            return (byte)0;
        } else {
            return Byte.parseByte(x);
        }
    }
    public static final char coerceToCharType(String x) {
        if(null == x || x.length()==0) {
            return (char)0;
        } else {
            return x.charAt(0);
        }
    }
    public static final double coerceToDoubleType(String x) {
        if(null == x || x.length()==0) {
            return 0.0;
        } else {
            return Double.parseDouble(x);
        }
    }
    public static final int coerceToIntType(String x) {
        if(null == x || x.length()==0) {
            return 0;
        } else {
            return Integer.parseInt(x);
        }
    }
    public static final float coerceToFloatType(String x) {
        if(null == x || x.length()==0) {
            return 0f;
        } else {
            return Float.parseFloat(x);
        }
    }
    public static final long coerceToLongType(String x) {
        if(null == x || x.length()==0) {
            return 0L;
        } else {
            return Long.parseLong(x);
        }
    }
    public static final short coerceToShortType(String x) {
        if(null == x || x.length()==0) {
            return (short)0;
        } else {
            return Short.parseShort(x);
        }
    }

    public static final Boolean coerceToBoolean(String x) {     return Boolean.valueOf(x); }
    public static final Byte coerceToByte(String x) {           return new Byte(coerceToByteType(x)); }
    public static final Character coerceToCharacter(String x) { return new Character(coerceToCharType(x)); }
    public static final Double coerceToDouble(String x) {       return new Double(coerceToDoubleType(x)); }
    public static final Integer coerceToInteger(String x) {     return new Integer(coerceToIntType(x)); }
    public static final Float coerceToFloat(String x) {         return new Float(coerceToFloatType(x)); }
    public static final Long coerceToLong(String x) {           return new Long(coerceToLongType(x)); }
    public static final Short coerceToShort(String x) {         return new Short(coerceToShortType(x)); }

    public static final char CR = '\r';
    public static final char LF = '\n';
    public static String normalizeCRLF(String inStr) {
        // From XML 1.0, 2.11: To simplify the tasks of applications, the characters passed to
        // an application by the XML processor must be as if the XML processor normalized all line
        // breaks in external parsed entities (including the document entity) on input, before
        // parsing, by translating both the two-character sequence #xD #xA and any #xD that is not
        // followed by #xA to a single #xA character.

        if(inStr == null) {
            return null;
        }

        String retStr;
        int firstCR = inStr.indexOf(CR);

        // first, find a CR - if none exist, return inStr, else, generate new string
        if (firstCR == -1) {
            retStr = inStr;
        } else {
            StringBuffer sb = new StringBuffer(inStr.length());
            boolean lastWasCR = false;
            char current;
            for (int i = 0; i < inStr.length(); i++) {
                current = inStr.charAt(i);
                if (lastWasCR && (current != LF)) {
                    sb.append(LF);
                }
                if (current == CR) {
                    lastWasCR = true;
                } else {
                    lastWasCR = false;
                    sb.append(current);
                }
            }
            if (lastWasCR) {
                sb.append(LF);
            }
            retStr = sb.toString();
        }
        return retStr;
    }

}
