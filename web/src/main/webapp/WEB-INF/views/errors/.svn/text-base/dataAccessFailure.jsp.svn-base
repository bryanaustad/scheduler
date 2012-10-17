<!DOCTYPE html>
<%@ page isErrorPage="true" import="java.io.*" %>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="web" uri="http://code.lds.org/web" %>
<%@ include file="/WEB-INF/views/includes/init.jsp" %>
<tags:template>
	<jsp:body>
		<div class="ixf-panel ui-layout-center padding-md">
			<h2>${messages['dataAccessFailure.accessfailure']}</h2>
			<p/>
				${messages['dataAccessFailure.problem']}
			<p/>
			<c:if test="<%=Boolean.valueOf(System.getProperty(\"view.showExceptions\"))%>">
				<web:display-exception />
			</c:if>
			<a href="${pageContext['request'].contextPath}/">main site</a>
		</div>
	</jsp:body>
</tags:template>
