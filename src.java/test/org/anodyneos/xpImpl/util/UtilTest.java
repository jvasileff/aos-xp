/*
 * Created on May 4, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package test.org.anodyneos.xpImpl.util;

import java.util.Arrays;

import junit.framework.TestCase;

import org.anodyneos.xpImpl.util.Util;
import org.xml.sax.SAXException;


/**
 * @author jvas
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class UtilTest extends TestCase {

    /**
     * Tests \t, \n, \b, \f, ", \\, and unicode chars before SPACE and after ~
     * (0x20 through 0x7E)
     *
     */
    public void testEscapeString() {
        // normal string
        assertEquals(Util.escapeString(
                "abcd"),
                "abcd");
        // all special chars
        assertEquals(Util.escapeString(
                "\t\n\b\f\"\\"),
                "\\t\\n\\b\\f\\\"\\\\");
        // all special chars with x in between them
        assertEquals(Util.escapeString(
                "x\tx\nx\bx\fx\"x\\x"),
                "x\\tx\\nx\\bx\\fx\\\"x\\\\x");
        // unicode border conditions
        assertEquals(Util.escapeString(
                "\u001f\u0020\u0021_\u007d\u007e\u007f"),
                "\\u001F !_}~\\u007F");
        // more border conditions
        assertEquals(Util.escapeString(
                "x\u0000\u0001_\ufffe\uffff"),
                "x\\u0000\\u0001_\\uFFFE\\uFFFF");
    }

    public void testHasEL() {
        // single EL
        assertEquals(Util.hasEL("${expr}"), true);
        // non-EL simple
        assertEquals(Util.hasEL("expr"), false);
        // tough non-EL (note: backslashes only escape $, nothing else)
        assertEquals(Util.hasEL("$ $} \\$ \\\\${expr}xxx\\${expr}xxx"), false);
        // tough EL
        assertEquals(Util.hasEL("\\ ${expr}"), true);
        // method doesn't care if EL is actually valid
        assertEquals(Util.hasEL("${expr"), true);
        assertEquals(Util.hasEL("x${expr"), true);
        assertEquals(Util.hasEL("x${"), true);
    }

    /**
     * EL Split creates an array for each normal-text and EL part. Normal-text
     * parts must be un-escaped such that \$ yields $. An exception is thrown
     * if there is an unterminated EL.
     *
     * @throws SAXException
     */
    public void testElSplit() throws SAXException {
        // simple non-EL
        assertTrue(Arrays.equals(
                Util.elSplit("expr"),
                new Util.TextPart[] {
                        new Util.TextPart("expr", false)
                }));
        // simple EL
        assertTrue(Arrays.equals(
                Util.elSplit("${expr}"),
                new Util.TextPart[] {
                        new Util.TextPart("${expr}", true)
                }));
        // non-EL with escaped $
        assertTrue(Arrays.equals(
                Util.elSplit("\\${expr} \\\\${expr}"),
                new Util.TextPart[] {
                        new Util.TextPart("${expr} \\${expr}", false)
                }));
        // multiple simple
        assertTrue(Arrays.equals(
                Util.elSplit("${expr}xxxx${expr}xxxx"),
                new Util.TextPart[] {
                        new Util.TextPart("${expr}", true),
                        new Util.TextPart("xxxx", false),
                        new Util.TextPart("${expr}", true),
                        new Util.TextPart("xxxx", false)
                }));
        // multiple simple #2
        assertTrue(Arrays.equals(
                Util.elSplit("xxxx${expr}xxxx${expr}"),
                new Util.TextPart[] {
                        new Util.TextPart("xxxx", false),
                        new Util.TextPart("${expr}", true),
                        new Util.TextPart("xxxx", false),
                        new Util.TextPart("${expr}", true)
                }));
        // tough EL with closing brace in single and double quotes and quotes being escaped.
        assertTrue(Arrays.equals(
                // yy${xx'}${xx}\'}"${xx}'__"}${xx}\"}'${xx}"}yy
                Util.elSplit("yy${xx'}${xx}\\'}\"${xx}'__\"}${xx}\\\"}'${xx}\"}yy"),
                new Util.TextPart[] {
                        new Util.TextPart("yy", false),
                        new Util.TextPart("${xx'}${xx}\\'}\"${xx}'__\"}${xx}\\\"}'${xx}\"}", true),
                        new Util.TextPart("yy", false)
                }));
        // properly unescape non-EL
        assertTrue(Arrays.equals(
                // \$xx$\xx\\$xx --> $xx$\xx\$xx
                Util.elSplit("\\$xx$\\xx\\\\$xx"),
                new Util.TextPart[] {
                        new Util.TextPart("$xx$\\xx\\$xx", false)
                }));
        // never-ending EL
        try {
            Util.elSplit("xx${yy}xx${yy");
            fail("EL never ends, should throw Exception");
        } catch (SAXException success) {
        }
        // never-ending EL due to non-ending single quote
        try {
            Util.elSplit("xx${yy}xx${yy'yy\\'y}yyy}");
            fail("EL never ends due to single quote, should throw Exception");
        } catch (SAXException success) {
        }
        // never-ending EL due to non-ending single quote
        try {
            Util.elSplit("xx${yy}xx${yy\"yy\\\"y}yyy}");
            fail("EL never ends due to double quote, should throw Exception");
        } catch (SAXException success) {
        }
    }

}
