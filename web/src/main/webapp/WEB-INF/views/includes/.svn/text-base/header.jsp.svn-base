<%@include file="/WEB-INF/views/includes/init.jsp" %>

<div id="session-d">
	<div id='menugen'></div>
	<script type="text/javascript">
	$(function() {
		$.ajaxSetup({
			// Disable caching of AJAX responses */
			cache: false
		});
		$.getJSON("${pageContext['request'].contextPath}/register/menu", null, function(data) {
			var divid = $('#menugen');
			var premenutext = "<div class='menu-d'><a href='"
			var context = "${pageContext['request'].contextPath}/";
			var middlemenutext = "' class='menulink'>";
			var postmenutext = "</a></div>";
			for (var i in data) {
				var menu = data[i].split(":");
				if (menu[1].charAt(0) == '/')
					divid.append(premenutext+menu[1]+middlemenutext+menu[0]+postmenutext);
				else
					divid.append(premenutext+context+menu[1]+middlemenutext+menu[0]+postmenutext);
			}
		});
	});
	</script>
	<div id="user-d">
		<sec:authorize access="isAuthenticated()">
			Welcome, <sec:authentication property="principal"/>
		</sec:authorize>
	</div>
</div>
<div id="header-d">
	<h1 class="headerfont"><span class="conferenceName"><%=org.apache.commons.lang.StringEscapeUtils.escapeHtml(String.valueOf(application.getAttribute("conferencename")))%></span></h1>
</div>
