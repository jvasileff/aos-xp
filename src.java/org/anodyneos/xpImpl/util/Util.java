package org.anodyneos.xpImpl.util;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.xml.sax.SAXException;

public class Util {

    public static String escapeStringQuoted(String string) {
        if (null != string) {
            return "\"" + escapeString(string) + "\"";
        } else {
            return "null";
        }
    }

    /**
     * Escapes '"' and '\' characters in a String (add a '\' before them) so that it can
     * be inserted in java source.
     */
    public static String escapeString(String string) {
        char chr[] = string.toCharArray();
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < chr.length; i++) {
            switch (chr[i]) {
                case '\t':
                    buffer.append("\\t");
                    break;
                case '\r':
                    buffer.append("\\r");
                    break;
                case '\n':
                    buffer.append("\\n");
                    break;
                case '\b':
                    buffer.append("\\b");
                    break;
                case '\f':
                    buffer.append("\\f");
                    break;
                case '"':
                case '\\':
                    buffer.append('\\');
                    buffer.append(chr[i]);
                    break;
                default:
                    if (' ' <= chr[i] && chr[i] < 0x7F) {
                        buffer.append(chr[i]);
                    } else {
                        buffer.append("\\u");
                        buffer.append(int2digit(chr[i] >> 12));
                        buffer.append(int2digit(chr[i] >> 8));
                        buffer.append(int2digit(chr[i] >> 4));
                        buffer.append(int2digit(chr[i]));
                    }
                    break;
            }
        }

        return buffer.toString();
    }

    private static char int2digit(int x) {
        x &= 0xF;
        if (x <= 9) return (char)(x + '0');
        else return (char)(x - 10 + 'A');
    }


    public static String toSetMethod(String name) {
        if (name.length() == 1) {
            return "set" + name.toUpperCase();
        } else {
            return "set" + name.substring(0,1).toUpperCase() + name.substring(1);
        }
    }

    /**
     * Returns true if arg contains an EL. The backslash char '\\' escapes only $.
     * It does not escape itself. This is as defined in JSP 2.0 for XML
     * content.
     *
     * @param s
     * @return
     */
    public static boolean hasEL(String s) {
        for(int start = s.indexOf("${"); start != -1; start = s.indexOf("${", start+1)) {
            if (0 == start || s.charAt(start-1) != '\\') {
                // if not escaped, return true
                return true;
            }
        }
        return false;
    }

    /**
     * Unescapes a regular string using EL escaping conventions and return the
     * result. This method uses elSplit to do the dirty work.
     *
     * @param s
     *            Must be regular text without an EL expression.
     * @return The unescaped String or "" if s is null.
     * @throws SAXException if the String contains an unescaped EL.
     */
    public static String unescapeEL(String s) throws SAXException {
        if (null == s || "".equals(s)) {
            return "";
        }

        TextPart[] parts = elSplit(s);
        if (parts != null && parts[0].isEL || parts.length > 1) {
            throw new SAXException("EL found where not expected.");
        }
        return parts[0].part;
    }

    /**
     * Like escapeStringQuoted() except that the string is first run through
     * unescapeEL to remove extra backslashes.
     *
     * @param s
     *            must satisfy null != s && hasEL(s) == false
     * @return the quoted String
     * @throws SAXException if thrown by unescapeEL
     */
    public static String escapeStringQuotedEL(String s) throws SAXException {
        return escapeStringQuoted(unescapeEL(s));
    }

    /**
     * Utility to call elExpressionCode(Util.TextPart[]...) with a String
     * instead of TextPart[].
     *
     * @param inExpr
     *            A String containing plain text and/or EL expressions
     * @param xpContextVar
     * @param targetClass
     * @return @throws
     *         SAXException if thrown by elSplit(inExpr) or
     *         elExpressionCode(Util.TextPart[]...)
     */
    public static String elExpressionCode(String inExpr, String targetClass)
            throws SAXException {
        return elExpressionCode(elSplit(inExpr), targetClass);
    }

    public static String elExpressionCode(Util.TextPart[] parts, String type)
            throws SAXException {

        // normalize type
        type = CoerceUtil.simplifyType(type);

        if(parts.length == 1 && ! parts[0].isEL) {
            // one part, non-EL.  Convert to an expression representing the given type.
            try {
                return CoerceUtil.javaExpression(parts[0].part, type);
            } catch (NumberFormatException e) {
                throw new SAXException("Number out of range or cannot parse.", e);
            } catch (IllegalArgumentException e) {
                throw new SAXException("Invalid type for operation: " + type, e);
            }
        } else if(parts.length == 1 && parts[0].isEL) {
            if(! CoerceUtil.isNativeType(type)) {
                return "(" + type + ") "
                  + "elEvaluator.evaluate("
                  + escapeStringQuoted(parts[0].part)
                  + ", " + type + ".class"
                  + ", varResolver"
                  + ", null)";
            } else {
                // need to box/unbox
                String boxClass = CoerceUtil.boxClass(type);
                StringBuffer expr = new StringBuffer(
                          "((" + boxClass + ") "
                        + "elEvaluator.evaluate("
                        + escapeStringQuoted(parts[0].part)
                        + ", " + boxClass + ".class"
                        + ", varResolver"
                        + ", null))");
                // for boolean, byte, char, double, int, float, long, short
                expr.append("." + type + "Value()");
                return expr.toString();
            }
        } else { // multiple parts
            StringBuffer expr = new StringBuffer();
            expr.append ("(String) ");
            for (int i = 0; i < parts.length; i++) {
                if(i > 0) {
                    expr.append(" + ");
                }
                if (! parts[i].isEL) {
                    expr.append(escapeStringQuoted(parts[i].part));
                } else { // this part is an EL
                    expr.append(
                        "elEvaluator.evaluate("
                        + escapeStringQuoted(parts[i].part)
                        + ", String.class"
                        + ", varResolver"
                        + ", null)");
                }
            }
            if("String".equals(type) || "Object".equals(type)) { // multiple parts, String
                return expr.toString();
            } else { // multiple parts, not String
                // type must be native type, boxClass, or Object

                // Same as string, but run-time pass through Coerce utility
                if (CoerceUtil.isNativeType(type)) {
                    String type2 = type.substring(0,1).toUpperCase() + type.substring(1);
                    expr.insert(0, "org.anodyneos.xp.util.XpCoerce.coerceTo" + type2 + "Type(");
                    expr.append(")");
                } else if (CoerceUtil.isBoxClass(type)) {
                    expr.insert(0, "org.anodyneos.xp.util.XpCoerce.coerceTo" + type + "(");
                    expr.append(")");
                } else {
                    throw new SAXException("Expression invalid for type: " + type);
                }
                return expr.toString();
            }
        }
    }


    /**
     * Returns a String containing Java code that can be used to represent the
     * result of processing the given expression.
     *
     * @param parts
     * @param xpContextVar
     *            The name of a variable that contains the XpContext
     * @param targetClass
     *            The Java class name for the result. If not java.lang.String,
     *            inExpr must contain only an EL expression.
     * @return Java expression
     */
    public static String elExpressionCodeOld(Util.TextPart[] parts, String targetClass)
            throws SAXException {

        // @TODO: what about FunctionMapper() - probably needs to be passed in since
        // the page defines mappings using namespaces.

        if("java.lang.String".equals(targetClass)) {
            targetClass = "String";
        }

        // not returning a String
        if (! "String".equals(targetClass)) {
            if (parts.length != 1 || ! parts[0].isEL) {
                // if not a String, entire inExpr must be an EL expression
                throw new SAXException("expression is not a java.lang.String, must only have EL");
            } else {
                return "(" + targetClass + ") "
                        + "elEvaluator.evaluate("
                        + escapeStringQuoted(parts[0].part)
                        + ", " + targetClass + ".class"
                        + ", varResolver"
                        + ", null)";
            }
        } else {
            if(parts.length == 1 && ! parts[0].isEL) {
                // dispose of simple cases
                return escapeStringQuoted(parts[0].part);
            }

            StringBuffer code = new StringBuffer();
            code.append ("(String) ");
            for (int i = 0; i < parts.length; i++) {
                if(i > 0) {
                    code.append(" + ");
                }
                if (! parts[i].isEL) {
                    code.append(escapeStringQuoted(parts[i].part));
                } else { // this part is an EL
                    code.append(
                        "elEvaluator.evaluate("
                        + escapeStringQuoted(parts[i].part)
                        + ", String.class"
                        + ", varResolver"
                        + ", null)");
                }
            }
            return code.toString();
        }
    }

    public static void outputCharactersCode(String raw, CodeWriter out) throws SAXException {
        // this will output on multiple lines to make generated code easier to read.
        // TODO split up the regular text lines at line breaks

        // NOTE: we should really only expect "\n" from the input because of:
        // http://www.w3.org/TR/1998/REC-xml-19980210#sec-line-ends which
        // requires \r\n and \r to be converted to a single \n. We can safely
        // replace \r crap with just \n although this doesn't necesarily apply
        // to text mode if that will be supported.

        // TODO: What about normalizing the result of EL expressions at
        // runtime???

        // note: cannot use part.part.split("\n|\r\n|\r"); because split()
        // throws away
        // adjacent matches so the result will not have the right # of
        // linebreaks.

        Util.TextPart[] parts = Util.elSplit(raw);
        for(int i = 0; i < parts.length; i++) {
            Util.TextPart part = parts[i];
            if (part.isEL) {
                String codeValue = elExpressionCode(new TextPart[] { part },
                        "String");
                out.printIndent().println("xpOut.characters(" + codeValue + ");");
            } else {
                // since we are getting the String after elSplit, it does not need further
                // unescaping, so we will use the regular "escapeStringQuoted"
                String codeValue = escapeStringQuoted(part.part);
                out.printIndent().println("xpOut.characters(" + codeValue + ");");
            }
        }
    }

    /**
     * Splits a string that contains regular text and EL expressions into an
     * array of TextParts where each part holds either literal text or a
     * complete EL expression. Literal text never occurs as adjacent Strings
     * within the array. Literal text returned by this function has been
     * unescaped.
     *
     * @param str
     * @return the array of <code>TextPart</code> or null if <code>str==null</code>.
     */
    public static TextPart[] elSplit(String str) throws SAXException {
        // @TODO: determine null or empty array convention, adjust code, and fix all
        // code that uses this method.

        // Handle easy cases first
        if (null == str) {
            return null;
        }
        if (str.indexOf("$") == -1) {
            // if no "$", then there are no EL and no backslashed to remove.
            return new TextPart[] { new TextPart(str, false) };
        }
        StringBuffer sb = new StringBuffer();
        List list = new ArrayList();
        StringCharacterIterator it = new StringCharacterIterator(str);
        char buf = ' ';  // may be '\\', '$', or ' '
        for(char cur = it.first(); cur != CharacterIterator.DONE; cur = it.next()) {
            switch (cur) {
            case '$':
                if('\\' == buf) {
                    // escaped, output
                    sb.append('$');
                    buf = ' ';
                } else {
                    // not escaped, may be start of EL
                    buf = '$';
                }
                break;
            case '\\':
                 if('\\' == buf || '$' == buf) {
                     // output buf
                     sb.append(buf);
                 }
                 // we may be escaping $, lets wait and see
                 buf = '\\';
                 break;
            case '{':
                if('\\' == buf) {
                    // output buf and this
                    sb.append(buf);
                    sb.append(cur);
                    buf = ' ';
                } else if (' ' == buf) {
                    // output this
                    sb.append(cur);
                    // buf = ' '; // buf is already ' '
                } else { // buf == '$'
                    // we are starting an EL
                    buf = ' ';
                    if(sb.length() != 0) {
                        list.add(new TextPart(sb.toString(), false));
                        sb.setLength(0);
                    }
                    sb.append("${");
                    // read in rest of EL including trailing }
                    char elBuf = ' '; // equals ' ' or '\'
                    boolean done = false;
                    char quoteType = ' '; // equals '\'' or '"'
                    for(cur = it.next(); cur != CharacterIterator.DONE; cur = it.next()) {
                        sb.append(cur); // we don't mangle inside EL
                        if (' ' == quoteType) {
                            // enter quote, end EL, or do nothing
                            if (cur == '\'' || cur == '"') {
                                quoteType = cur;
                                elBuf = ' ';
                            } else if (cur == '}') {
                                // end EL
                                done = true;
                                break;
                            }
                        } else if (quoteType == '\'' || quoteType == '"') {
                            if ('\\' == elBuf) {
                                // cur is being escaped
                                elBuf = ' ';
                            } else if ('\\' == cur) {
                                // escape next
                                elBuf = '\\';
                            } else if (quoteType == cur) {
                                // out of quote now
                                quoteType = ' ';
                            }
                        }
                    }
                    if (done) {
                        list.add(new TextPart(sb.toString(), true));
                        sb.setLength(0);
                    } else {
                        throw new SAXException("EL did not end in }");
                    }
                }
                break;
            default:
                if('\\' == buf || '$' == buf) {
                    sb.append(buf);
                    buf = ' ';
                }
                sb.append(cur);
                break;
            }
        }
        if(sb.length() != 0) {
            list.add(new TextPart(sb.toString(), false));
        }
        return (TextPart[]) list.toArray(new TextPart[list.size()]);
    }

    public static void main(String[] args) throws Exception {
        // test splitEl
        String[] stringArray;

        // test only text
        System.out.println(
                Arrays.asList(elSplit("some_text_slash\\_slashslash\\\\_dollar$_dollar\\$_literal\\${")).toString()
        );
        // text only expr
        System.out.println(
                Arrays.asList(elSplit("${myexpr_slash\\_quote_'${}{}\\''_quote\"${}{}\\\"\"}")).toString()
        );

        // test start and end text
        System.out.println(
                Arrays.asList(elSplit("xxx${yyy}xxx")).toString()
        );

        // test start and end expr
        System.out.println(
                Arrays.asList(elSplit("${yyy}xxx${yyy}")).toString()
        );

        // more testing would include exceptions for non-ending EL due to missing } or endless quote
    }

    public static class TextPart {
        public String part;
        public boolean isEL;

        public TextPart(String part, boolean isEL) {
            this.part = part;
            this.isEL = isEL;
        }

        public boolean equals(Object thatObj) {
            if(thatObj == null || ! (thatObj instanceof TextPart)) {
                return false;
            }
            TextPart that = (TextPart) thatObj;
            if(this.isEL != that.isEL) {
                return false;
            }
            if(this.part == null && that.part != null) {
                return false;
            }
            if(this.part == null && that.part == null) {
                return true;
            }
            return(this.part.equals(that.part));
        }
    }
}
