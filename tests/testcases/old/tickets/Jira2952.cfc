<cfcomponent extends="org.railo.cfml.test.RailoTestCase">
	<!---
	<cffunction name="beforeTests"></cffunction>
	<cffunction name="afterTests"></cffunction>
	<cffunction name="setUp"></cffunction>
	--->
	<cffunction name="test">
		<cfxml variable="local.MyDoc">
		<MyDoc>this is a test<test>test 2</test>
		<myTag>
		<tagTest>tag</tagTest>
		</myTag>
		</MyDoc>
		</cfxml>

		<cfset assertEquals(5,arrayLen(MyDoc.Mydoc.xmlNodes))>

		<cfset assertEquals('x?xml version="1.0" encoding="utf-8"?>this is a test',replace(trim(MyDoc.Mydoc.xmlNodes[1]&" "),'<','x','all'))>
		<cfset assertEquals("this is a test",MyDoc.Mydoc.xmlNodes[1].xmlText)>

	</cffunction>
</cfcomponent>