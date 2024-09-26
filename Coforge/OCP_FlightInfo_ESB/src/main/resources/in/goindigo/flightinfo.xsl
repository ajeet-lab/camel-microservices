<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 <xsl:output method="text" indent="no" />
  <xsl:template match="/">
    <xsl:apply-templates />
  </xsl:template>

<xsl:param name="quote">'</xsl:param>
<xsl:param name="dquote">"</xsl:param>

<xsl:template match="/service">
        <xsl:for-each select="/service/response">
        <xsl:text>select </xsl:text>
        <xsl:for-each select="./Column">
        <xsl:value-of select="@ID"/>
        <xsl:if test="position() &lt; last()">
                <xsl:text>, </xsl:text>
        </xsl:if>
        <xsl:if test="position()=last()">
                <xsl:text></xsl:text>
        </xsl:if>
        </xsl:for-each>
        </xsl:for-each>

        <xsl:for-each select="/service/request">
        <xsl:text> ,Case  when  (dep='CMB' or arr='CMB')  then 'INTL' when (DepUTCDiff=330 and ArrUTCDiff=330 )  then 'DOM' else 'INTL' end as FlightType  from FLIGHT_INFO where </xsl:text>
        <!--<xsl:text> ,(case when  (deputcdiff=330 and ArrUTCDiff=330) then 'DOM' else 'INTL' end) as FlightType  from FLIGHT_INFO where </xsl:text> -->
        <!--<xsl:text> from ESB_EmployeeData25 where </xsl:text>-->
        <xsl:for-each select="./Column">
        <xsl:value-of select="@ID"/><xsl:text>=</xsl:text><xsl:value-of select="$quote"/><xsl:value-of select="@Value"/><xsl:value-of select="$quote"/>
        <xsl:if test="position() &lt; last()">
                <xsl:text> and </xsl:text>
        </xsl:if>
        <xsl:if test="position()=last()">
                <xsl:text></xsl:text>
        </xsl:if>
        </xsl:for-each>
        </xsl:for-each>
       	<xsl:text> order by </xsl:text><xsl:value-of select="$dquote"/><xsl:text>StartTime</xsl:text><xsl:value-of select="$dquote"/>
</xsl:template>
</xsl:stylesheet>
