<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : XMLVMClassStubber.xsl
    Created on : March 10, 2014, 10:21 AM
    Author     : shannah
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:vm="http://xmlvm.org" xmlns:dex="http://xmlvm.org/dex">
    

    <!-- TODO customize transformation rules 
         syntax recommendation http://www.w3.org/TR/xslt 
    -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="dex:code">
        <!-- do something different -->
        <dex:code>
       
        </dex:code>
    </xsl:template>

</xsl:stylesheet>
