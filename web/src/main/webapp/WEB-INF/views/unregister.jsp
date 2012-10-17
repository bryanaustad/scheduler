<!DOCTYPE html>
<%@include file="/WEB-INF/views/includes/init.jsp" %>
<html lang="en">
	<head>
		<title>Unregister from Conference</title>
		<jsp:include page="/WEB-INF/views/includes/brains.jsp"/>
		<script type="text/javascript" charset="utf-8">
			$(function(){
				$.deleteJSON("${pageContext['request'].contextPath}/quit", null, function(data) {
					$.getJSON("${pageContext['request'].contextPath}/register/menu", null, function(data) {
						var divid = $('#menugen');
						var premenutext = "<div class='menu-d'><a href='${pageContext['request'].contextPath}/";
						var middlemenutext = "' class='menulink'>";
						var postmenutext = "</a></div>";
						divid.html('');
						for (var i in data) {
							var menu = data[i].split(":");
							divid.append(premenutext+menu[1]+middlemenutext+menu[0]+postmenutext);
						}
					});
				});
			});
		</script>
	</head>
	<body class="signin" >
		<jsp:include page="/WEB-INF/views/includes/header.jsp"/>
		<h2 class="pageheading">Unregister</h2>
		<div id="signin-box">
			<h2>You've successfully unregistered.</h2>
		</div>
		<!-- <div class="church-logo">The Church of Jesus Christ of Latter-day Saints</div> -->
	</body>
</html>
