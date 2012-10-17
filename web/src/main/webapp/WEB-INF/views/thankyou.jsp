<!DOCTYPE html>
<%@include file="/WEB-INF/views/includes/init.jsp" %>
<html lang="en">
	<head>
		<title>Thank You</title>
		<jsp:include page="/WEB-INF/views/includes/brains.jsp"/>
	</head>
	<body class="signin" >
		<jsp:include page="/WEB-INF/views/includes/header.jsp"/>
		<h2 class="pageheading">Thank You</h2>
		<div id="signin-box">
			<h2>Thank you for your feedback.</h2>
			<center><a href="${pageContext['request'].contextPath}/feedback">return to feedback</a></center>
			<br/>
			<center><a href="${pageContext['request'].contextPath}/feedback?signmeout">logout</a></center>
		</div><!-- /signin-box -->
		<!-- <div class="church-logo">The Church of Jesus Christ of Latter-day Saints</div> -->
	</body>
</html>
