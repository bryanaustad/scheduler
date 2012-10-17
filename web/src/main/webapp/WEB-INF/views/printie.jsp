<!DOCTYPE html>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Print IE page</title>
	<script type="text/javascript" src="${pageContext['request'].contextPath}/scripts/jquery-1.3.2.js"></script>
	<script type="text/javascript" src="${pageContext['request'].contextPath}/scripts/jquery.rest-1.0.0.js"></script>
	<script type="text/javascript" src="${pageContext['request'].contextPath}/scripts/jquery.conf-search-1.0.0.js"></script>
	<script type="text/javascript" src="${pageContext['request'].contextPath}/scripts/jquery.conf-schedule-1.0.0.js"></script>
	<script type="text/javascript" src="${pageContext['request'].contextPath}/scripts/jquery.conf-tags-1.0.0.js"></script>
<script type="text/javascript">
$.getJSON("${pageContext['request'].contextPath}/course/list.json", null, function(data) {
	for (i in data) {
		var course = data[i];
		if (course.registered || course.tentative || course.general) {
			var courseDomE = $.eGen.div(null, "course");
			var cDetailDomE = $.eGen.div("course-detail");

			var cTimeDomE = $.eGen.div("course-time");
			cTimeDomE.text(util.formatTime(course.start) + " - " + course.room);
			//cTimeDomE.text(sched.opt.time.format(course.start - sched.opt.time.zoneOffset));
			cDetailDomE.append(cTimeDomE);

			var cTitleDomE = $.eGen.div("course-title");
			cTitleDomE.text(course.name);
			cDetailDomE.append(cTitleDomE);

			var cAuthorsDomE = $.eGen.div("course-authors");
			if (course.authors == null) {
				cAuthorsDomE.text("");
			} else {
				cAuthorsDomE.text(course.authors[0]);
			}
			cDetailDomE.append(cAuthorsDomE);

			courseDomE.append(cDetailDomE);
			$("#calendar").append(courseDomE);
		}
	}
});

var util = {
	formatTime: function(ticks){
		var date = new Date(ticks);
		var hour = date.getHours();
		var minute = date.getMinutes();
		var day = date.getDay();
		switch(day){
		case 2:
			day = "Tuesday";
			break;
		case 3:
			day = "Wednesday";
			break;
		}
		//var ap = "am";
		if (hour > 12) { hour = hour - 12; }
		if (hour == 0) { hour = 12; }

		return day + " " + hour + ((minute==0)? ":00" : ":" + minute);
	}
}

</script>
</head>
<body>
<div id="header-d">
			<div id="search-d">
			</div>
			<h1>Session Scheduler</h1>
			<h2>Temporary fix for IE print bug.  Please use FireFox for better results.</h2>
		</div>
		<div id="content-d">
			<div id="calendar-d">
				<div id="calendar"></div>
			</div>
			<div class="clearfix"></div>
		</div>
</body>
</html>
