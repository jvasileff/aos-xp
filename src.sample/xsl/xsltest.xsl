<?xml version="1.0"?>

<!DOCTYPE xsl:stylesheet [
<!ENTITY copy   "&#169;">
<!ENTITY nbsp   "&#160;">
]>

<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml"/>

    <!--
        Including an xsl file in order to test a runtime issue.

        For some older combinations of Xerces/Xalan, included files are parsed
        with validation enabled when there is a DOCTYPE in the included file.
        This causes validation error messages for each element of the included
        file.

        A runtime workaround is to include recent versions of Xerces and Xalan
        in the endorsed directory.
    -->
    <xsl:include href="xsltest-include.xsl"/>

    <xsl:template match="/">
        <xsl:copy-of select="."/>
    </xsl:template>

</xsl:stylesheet>

