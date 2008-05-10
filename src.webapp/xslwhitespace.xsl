<?xml version="1.0"?>

<!DOCTYPE xsl:stylesheet [
<!ENTITY copy   "&#169;">
<!ENTITY nbsp   "&#160;">
]>

<xsl:stylesheet
    version="1.0"
    xmlns="http://www.w3.org/1999/xhtml"
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
    xmlns:aostest="http://www.anodyneos.org/aostest"
    xmlns:xalan="http://xml.apache.org/xalan"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output
            method="xml"
            media-type="application/xhtml+xml"
            doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
            doctype-system="http://www.w3.org/2002/08/xhtml/xhtml1-strict.dtd"

            indent="no"
            xalan:indent-amount="2"
            omit-xml-declaration="no"
            encoding="UTF-8"/>

           <!--
            method="html"
            media-type="text/html"
            doctype-public="-//W3C//DTD HTML 4.01 Frameset//EN"

            method="xml"
            media-type="application/xhtml+xml"
            doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
            doctype-system="http://www.w3.org/2002/08/xhtml/xhtml1-strict.dtd"

            xalan:indent-amount="2"
            -->

    <!--
    <xsl:strip-space elements="*"/>
    <xsl:preserve-space elements="xhtml:p"/>
    -->

    <xsl:template match="aostest:somecontent">
        <p>
            This is a test with a <b>bold</b> word.
        </p>
        <p>
            This is a test with a <xsl:if test="true()"> word </xsl:if> word.
        </p>
        <p>
            This is a test with a  <xsl:if test="true()"> <b> <xsl:value-of select="'word'"/> </b> </xsl:if> word.
        </p>
        <p>
            This is a test with <b>bold</b> <i>italic</i> words.
        </p>
        <p>
            <b>This is all bold.</b>
        </p>
        <p>
            <b>
                <i>Bold and italic.</i>
            </b>
        </p>

        <p>
            Test whitespace in attributes: <a href="." title="  whitespace  ">some link</a>
        </p>
        <p>
            Test whitespace in attributes: <a href="." title="nowhitespace">some link</a>
        </p>
        <p>
            Test whitespace in xsl attributes2: <a href="."><xsl:attribute name="title">  whitespace  </xsl:attribute>some link</a>
        </p>
        <p>
            Test whitespace in xsl attributes2: <a href="."><xsl:attribute name="title">nowhitespace</xsl:attribute>some link</a>
        </p>
        <p>
            Test whitespace in xsl attributes3cr: <a href="."><xsl:attribute name="title">  whitespace
            </xsl:attribute>some link</a>
        </p>
        <p>
            Test whitespace in xsl attributes4spaces: <a href="."><xsl:attribute name="title">     </xsl:attribute>some link</a>
        </p>
        <p>
            Test whitespace in xsl attributes5tag: <a href="."><xsl:attribute name="title"><xsl:value-of select="'  spaces  '"/></xsl:attribute>some link</a>
        </p>
    </xsl:template>

    <!--~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    ~~
    ~~  By default, copy source
    ~~
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~-->

    <xsl:template match="/xhtml:html | /html">
        <html   xmlns="http://www.w3.org/1999/xhtml"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://www.w3.org/1999/xhtml http://www.w3.org/2002/08/xhtml/xhtml1-strict.xsd">
            <xsl:apply-templates select="@* | node()"/>
        </html>
    </xsl:template>

    <xsl:template match="*">
        <!--
            If we were to use xsl:copy on elements, all namespaces defined in
            the source would appear in the result (we don't want that.)
        -->
        <xsl:choose>
            <xsl:when test="namespace-uri() != ''">
                <!--
                    NOTE: xsl:element is preferred in order to cut down on
                    source document namespace declarations showing up in the
                    output, BUT, XSLTC cannot handle it well as it defines a
                    brand new prefix for every element.  See Jira bug ZP-3.

                    <xsl:element name="{local-name()}" namespace="{namespace-uri()}">
                        <xsl:apply-templates select="@* | node()"/>
                    </xsl:element>
                -->
                <xsl:copy>
                    <xsl:apply-templates select="@* | node()"/>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="{local-name()}" namespace="http://www.w3.org/1999/xhtml">
                    <xsl:apply-templates select="@* | node()"/>
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Copy all attributes and text nodes -->
    <xsl:template match="@* | text()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>

