/*
 * Created on May 8, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package test.org.anodyneos.xpImpl.util;

import junit.framework.TestCase;

import org.anodyneos.xpImpl.util.CoerceUtil;


/**
 * @author jvas
 */
public class CoerceUtilTest extends TestCase {

    public void testSimplifyType() {
        assertEquals(CoerceUtil.simplifyType("xx.yy.zz"), "xx.yy.zz");
        assertEquals(CoerceUtil.simplifyType("java.lang.String"), "String");
        assertEquals(CoerceUtil.simplifyType("java.lang.Boolean"), "Boolean");
        assertEquals(CoerceUtil.simplifyType("java.lang.Byte"), "Byte");
        assertEquals(CoerceUtil.simplifyType("java.lang.Character"), "Character");
        assertEquals(CoerceUtil.simplifyType("java.lang.Double"), "Double");
        assertEquals(CoerceUtil.simplifyType("java.lang.Integer"), "Integer");
        assertEquals(CoerceUtil.simplifyType("java.lang.Float"), "Float");
        assertEquals(CoerceUtil.simplifyType("java.lang.Long"), "Long");
        assertEquals(CoerceUtil.simplifyType("java.lang.Short"), "Short");
        assertEquals(CoerceUtil.simplifyType("java.lang.Object"), "Object");
    }

    public void testJavaExpression() {

        // String
        assertEquals("\"\"", CoerceUtil.javaExpression(null, "String"));
        assertEquals("\"\"", CoerceUtil.javaExpression("", "String"));
        assertEquals("\"x\\\"y\\\"z\"", CoerceUtil.javaExpression("x\"y\"z", "String"));
        assertEquals("\"x\\\"y\\\"z\"", CoerceUtil.javaExpression("x\"y\"z", "java.lang.String"));

        // boolean
        assertEquals("false", CoerceUtil.javaExpression(null, "boolean"));
        assertEquals("false", CoerceUtil.javaExpression("", "boolean"));
        assertEquals("true", CoerceUtil.javaExpression("true", "boolean"));
        assertEquals("true", CoerceUtil.javaExpression("TrUe", "boolean"));
        assertEquals("false", CoerceUtil.javaExpression("false", "boolean"));
        assertEquals("false", CoerceUtil.javaExpression("faxse", "boolean"));

        // Boolean
        assertEquals("Boolean.FALSE", CoerceUtil.javaExpression(null, "Boolean"));
        assertEquals("Boolean.FALSE", CoerceUtil.javaExpression("", "Boolean"));
        assertEquals("Boolean.TRUE", CoerceUtil.javaExpression("true", "Boolean"));
        assertEquals("Boolean.TRUE", CoerceUtil.javaExpression("TrUe", "Boolean"));
        assertEquals("Boolean.FALSE", CoerceUtil.javaExpression("false", "Boolean"));
        assertEquals("Boolean.FALSE", CoerceUtil.javaExpression("faxse", "Boolean"));

        // byte
        assertEquals("(byte)0", CoerceUtil.javaExpression(null, "byte"));
        assertEquals("(byte)0", CoerceUtil.javaExpression("", "byte"));
        assertEquals("(byte)10", CoerceUtil.javaExpression("10", "byte"));
        assertEquals("(byte)127", CoerceUtil.javaExpression("127", "byte"));
        assertEquals("(byte)-128", CoerceUtil.javaExpression("-128", "byte"));
        try {
            CoerceUtil.javaExpression("-129", "byte");
            fail("Should throw NumberFormatException for byte -129");
        } catch (NumberFormatException e) {
            // success
        }

        // Byte
        assertEquals("new Byte((byte)0)", CoerceUtil.javaExpression(null, "Byte"));
        assertEquals("new Byte((byte)0)", CoerceUtil.javaExpression("", "Byte"));
        assertEquals("new Byte((byte)10)", CoerceUtil.javaExpression("10", "Byte"));
        assertEquals("new Byte((byte)127)", CoerceUtil.javaExpression("127", "Byte"));
        assertEquals("new Byte((byte)-128)", CoerceUtil.javaExpression("-128", "Byte"));
        try {
            CoerceUtil.javaExpression("-129", "Byte");
            fail("Should throw NumberFormatException for Byte -129");
        } catch (NumberFormatException e) {
            // success
        }

        // char
        assertEquals("(char)0", CoerceUtil.javaExpression(null, "char"));
        assertEquals("(char)0", CoerceUtil.javaExpression("", "char"));
        assertEquals("(char)65", CoerceUtil.javaExpression("ABC", "char"));
        assertEquals("(char)66", CoerceUtil.javaExpression("B", "char"));

        // Character
        assertEquals("new Character((char)0)", CoerceUtil.javaExpression(null, "Character"));
        assertEquals("new Character((char)0)", CoerceUtil.javaExpression("", "Character"));
        assertEquals("new Character((char)65)", CoerceUtil.javaExpression("ABC", "Character"));
        assertEquals("new Character((char)66)", CoerceUtil.javaExpression("B", "Character"));

        // double
        assertEquals("0.0", CoerceUtil.javaExpression(null, "double"));
        assertEquals("0.0", CoerceUtil.javaExpression("", "double"));
        assertEquals("4.9E-324", CoerceUtil.javaExpression("4.9E-324", "double"));
        assertEquals("0.0", CoerceUtil.javaExpression("4.9E-325", "double"));
        assertEquals("-0.0", CoerceUtil.javaExpression("-4.9E-325", "double"));
        assertEquals("1.7976931348623157E308", CoerceUtil.javaExpression("1.7976931348623157E308", "double"));
        assertEquals("Double.POSITIVE_INFINITY", CoerceUtil.javaExpression("1.7976931348623159E308", "double"));
        assertEquals("Double.NEGATIVE_INFINITY", CoerceUtil.javaExpression("-1.7976931348623159E308", "double"));
        try {
            CoerceUtil.javaExpression("x10.0", "double");
            fail("Should throw NumberFormatException");
        } catch (NumberFormatException e) {
            // success
        }

        // Double
        assertEquals("new Double(0.0)", CoerceUtil.javaExpression(null, "Double"));
        assertEquals("new Double(0.0)", CoerceUtil.javaExpression("", "Double"));
        assertEquals("new Double(4.9E-324)", CoerceUtil.javaExpression("4.9E-324", "Double"));
        assertEquals("new Double(0.0)", CoerceUtil.javaExpression("4.9E-325", "Double"));
        assertEquals("new Double(-0.0)", CoerceUtil.javaExpression("-4.9E-325", "Double"));
        assertEquals("new Double(1.7976931348623157E308)", CoerceUtil.javaExpression("1.7976931348623157E308", "Double"));
        assertEquals("new Double(Double.POSITIVE_INFINITY)", CoerceUtil.javaExpression("1.7976931348623159E308", "Double"));
        assertEquals("new Double(Double.NEGATIVE_INFINITY)", CoerceUtil.javaExpression("-1.7976931348623159E308", "Double"));
        try {
            CoerceUtil.javaExpression("x10.0", "Double");
            fail("Should throw NumberFormatException");
        } catch (NumberFormatException e) {
            // success
        }

        // float
        assertEquals("0.0f", CoerceUtil.javaExpression(null, "float"));
        assertEquals("0.0f", CoerceUtil.javaExpression("", "float"));
        assertEquals("1.4E-45f", CoerceUtil.javaExpression("1.4E-45", "float"));
        assertEquals("0.0f", CoerceUtil.javaExpression("1.4E-46", "float"));
        assertEquals("-0.0f", CoerceUtil.javaExpression("-1.4E-46", "float"));
        assertEquals("3.4028235E38f", CoerceUtil.javaExpression("3.4028235E38", "float"));
        assertEquals("Float.POSITIVE_INFINITY", CoerceUtil.javaExpression("3.4028236E38", "float"));
        assertEquals("Float.NEGATIVE_INFINITY", CoerceUtil.javaExpression("-3.4028236E38", "float"));
        try {
            CoerceUtil.javaExpression("x10.0", "float");
            fail("Should throw NumberFormatException");
        } catch (NumberFormatException e) {
            // success
        }

        // Float
        assertEquals("new Float(0.0f)", CoerceUtil.javaExpression(null, "Float"));
        assertEquals("new Float(0.0f)", CoerceUtil.javaExpression("", "Float"));
        assertEquals("new Float(1.4E-45f)", CoerceUtil.javaExpression("1.4E-45", "Float"));
        assertEquals("new Float(0.0f)", CoerceUtil.javaExpression("1.4E-46", "Float"));
        assertEquals("new Float(-0.0f)", CoerceUtil.javaExpression("-1.4E-46", "Float"));
        assertEquals("new Float(3.4028235E38f)", CoerceUtil.javaExpression("3.4028235E38", "Float"));
        assertEquals("new Float(Float.POSITIVE_INFINITY)", CoerceUtil.javaExpression("3.4028236E38", "Float"));
        assertEquals("new Float(Float.NEGATIVE_INFINITY)", CoerceUtil.javaExpression("-3.4028236E38", "Float"));
        try {
            CoerceUtil.javaExpression("x10.0", "Float");
            fail("Should throw NumberFormatException");
        } catch (NumberFormatException e) {
            // success
        }

        // int
        assertEquals("0", CoerceUtil.javaExpression(null, "int"));
        assertEquals("0", CoerceUtil.javaExpression("", "int"));
        assertEquals("10", CoerceUtil.javaExpression("10", "int"));
        assertEquals("2147483647", CoerceUtil.javaExpression("2147483647", "int"));
        assertEquals("-2147483648", CoerceUtil.javaExpression("-2147483648", "int"));
        try {
            CoerceUtil.javaExpression("2147483648", "int");
            fail("Should throw NumberFormatException");
        } catch (NumberFormatException e) {
            // success
        }

        // Integer
        assertEquals("new Integer(0)", CoerceUtil.javaExpression(null, "Integer"));
        assertEquals("new Integer(0)", CoerceUtil.javaExpression("", "Integer"));
        assertEquals("new Integer(10)", CoerceUtil.javaExpression("10", "Integer"));
        assertEquals("new Integer(2147483647)", CoerceUtil.javaExpression("2147483647", "Integer"));
        assertEquals("new Integer(-2147483648)", CoerceUtil.javaExpression("-2147483648", "Integer"));
        try {
            CoerceUtil.javaExpression("2147483648", "Integer");
            fail("Should throw NumberFormatException");
        } catch (NumberFormatException e) {
            // success
        }

        // long
        assertEquals("0L", CoerceUtil.javaExpression(null, "long"));
        assertEquals("0L", CoerceUtil.javaExpression("", "long"));
        assertEquals("10L", CoerceUtil.javaExpression("10", "long"));
        assertEquals("9223372036854775807L", CoerceUtil.javaExpression("9223372036854775807", "long"));
        assertEquals("-9223372036854775808L", CoerceUtil.javaExpression("-9223372036854775808", "long"));
        try {
            CoerceUtil.javaExpression("-9223372036854775809", "long");
            fail("Should throw NumberFormatException");
        } catch (NumberFormatException e) {
            // success
        }

        // Long
        assertEquals("new Long(0L)", CoerceUtil.javaExpression(null, "Long"));
        assertEquals("new Long(0L)", CoerceUtil.javaExpression("", "Long"));
        assertEquals("new Long(10L)", CoerceUtil.javaExpression("10", "Long"));
        assertEquals("new Long(9223372036854775807L)", CoerceUtil.javaExpression("9223372036854775807", "Long"));
        assertEquals("new Long(-9223372036854775808L)", CoerceUtil.javaExpression("-9223372036854775808", "Long"));
        try {
            CoerceUtil.javaExpression("-9223372036854775809", "Long");
            fail("Should throw NumberFormatException");
        } catch (NumberFormatException e) {
            // success
        }

        // short
        assertEquals("(short)0", CoerceUtil.javaExpression(null, "short"));
        assertEquals("(short)0", CoerceUtil.javaExpression("", "short"));
        assertEquals("(short)10", CoerceUtil.javaExpression("10", "short"));
        assertEquals("(short)32767", CoerceUtil.javaExpression("32767", "short"));
        assertEquals("(short)-32768", CoerceUtil.javaExpression("-32768", "short"));
        try {
            CoerceUtil.javaExpression("32768", "short");
            fail("Should throw NumberFormatException");
        } catch (NumberFormatException e) {
            // success
        }
        try {
            CoerceUtil.javaExpression("x32768", "short");
            fail("Should throw NumberFormatException");
        } catch (NumberFormatException e) {
            // success
        }

        // Short
        assertEquals("new Short((short)0)", CoerceUtil.javaExpression(null, "Short"));
        assertEquals("new Short((short)0)", CoerceUtil.javaExpression("", "Short"));
        assertEquals("new Short((short)10)", CoerceUtil.javaExpression("10", "Short"));
        assertEquals("new Short((short)32767)", CoerceUtil.javaExpression("32767", "Short"));
        assertEquals("new Short((short)-32768)", CoerceUtil.javaExpression("-32768", "Short"));
        try {
            CoerceUtil.javaExpression("32768", "Short");
            fail("Should throw NumberFormatException");
        } catch (NumberFormatException e) {
            // success
        }

        // Object
        assertEquals("\"\"", CoerceUtil.javaExpression(null, "Object"));
        assertEquals("\"\"", CoerceUtil.javaExpression("", "Object"));
        assertEquals("\"x\\\"y\\\"z\"", CoerceUtil.javaExpression("x\"y\"z", "Object"));
        assertEquals("\"x\\\"y\\\"z\"", CoerceUtil.javaExpression("x\"y\"z", "java.lang.Object"));

        // unsupported type
        try {
            CoerceUtil.javaExpression("10", "badType");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

}
