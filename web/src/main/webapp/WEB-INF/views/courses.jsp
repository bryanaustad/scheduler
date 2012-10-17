<!DOCTYPE html>
<%@include file="/WEB-INF/views/includes/init.jsp" %>
<html lang="en">
	<head>
		<title>Courses</title>
		<jsp:include page="/WEB-INF/views/includes/brains.jsp"/>
		<script type="text/javascript" src="${pageContext['request'].contextPath}/scripts/dateformat.js"></script>
		<script type="text/javascript" charset="utf-8">
			var papers;
			var timezone;
			function approval(test, approvetype) {
				var ischkd = $('#'+approvetype+'_'+test).attr("checked");
				var course;
				for (var i in papers) {
					if (papers[i].id == test) {
						course = papers[i];
						break;
					}
				}
				if (ischkd == true) {
					$.putJSON("${pageContext['request'].contextPath}/course/" + test + "/" + approvetype + "/approvecourse.json", null, function(data){});
					if (approvetype == 'approve_tech') {
						course.approve_tech = true;
					} else if (approvetype == 'approve_ip') {
						course.approve_ip = true;
					} else if (approvetype == 'approve_cor') {
						course.approve_cor = true;
					} else if (approvetype == 'general') {
						course.general = true;
					}
				} else {
					$.putJSON("${pageContext['request'].contextPath}/course/" + test + "/" + approvetype + "/unapprovecourse.json", null, function(data){});
					if (approvetype == 'approve_tech') {
						course.approve_tech = false;
					} else if (approvetype == 'approve_ip') {
						course.approve_ip = false;
					} else if (approvetype == 'approve_cor') {
						course.approve_cor = false;
					} else if (approvetype == 'general') {
						course.general = false;
					}
				}
			};

			function view(pg) {
				var sessions = $('#sessions').attr('checked');
				if (pg.length < 1)
					first = 0;
				$("#thetable").empty();
				$("#thetable").append("<thead><tr><th>Presentation Name</th><th>Submitter</th><th>Presenters</th><th>Track</th><th>Tags</th><th>Audience</th><th>Slide Deck</th><th>Gen</th><th>Tech</th><th>IP</th><th>Cor</th><th>Reg</th><th>Tent</th><th>Start</th><th>End</th><th>Room</th></tr></thead>");
				$("#thetable").append("<tbody>");
				for (var j in pg) {
					var entries=1;
					if (sessions) {
						if (pg[j].sessions === undefined || pg[j].sessions.length === 0)
							pg[j].sessions= new Array(new Object());
						else
							entries=pg[j].sessions.length;
					}
					for (var i=0;i<entries;i++) {
						var attachment = "";
						if (pg[j].filename === undefined)
							attachment = "";
						else {
							attachment = "<a target=\"_blank\" href=\"${pageContext['request'].contextPath}/course/paper/"+
								htmlEscape(pg[j].filename)+"\">"+htmlEscape(pg[j].filedisplayname)+"</a>";
						}
						var general = "";
						if (pg[j].general != undefined && pg[j].general === true)
							general = "checked=\"true\"";
						var approve_tech = "";
						if (pg[j].approve_tech != undefined && pg[j].approve_tech === true)
							approve_tech = "checked=\"true\"";
						var approve_ip = "";
						if (pg[j].approve_ip != undefined && pg[j].approve_ip === true)
							approve_ip = "checked=\"true\"";
						var approve_cor = "";
						if (pg[j].approve_cor != undefined && pg[j].approve_cor === true)
							approve_cor = "checked=\"true\"";
						var sessionsbody = "";
						var name = pg[j].name;
						if (sessions) {
							var startimetoString = "";
							if (pg[j].sessions[i].start != undefined) {
								var start = convertGMTtoConferenceTimezone(pg[j].sessions[i].start,timezone);
								startimetoString = start.format("yyyy.mm.dd-hh:MMTT");
							}
							var endtimetoString = "";
							if (pg[j].sessions[i].end != undefined) {
								var end = convertGMTtoConferenceTimezone(pg[j].sessions[i].end,timezone);
								endtimetoString = end.format("yyyy.mm.dd-hh:MMTT");
							}
							var room = "";
							if (pg[j].sessions[i].room != undefined)
								room=pg[j].sessions[i].room;
							sessionsbody = "<td>" + startimetoString + "</td><td>" + endtimetoString + "</td><td>" + htmlEscape(room) + "</td>";
							if (pg[j].sessions[i].postname != undefined)
								name = name+pg[j].sessions[i].postname;
						} else {
							sessionsbody="<td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td>";
						}
						var row = $("<tr id=\"" + pg[j].id + "\"><td>"+
								"<a href=\"${pageContext['request'].contextPath}/course?id=" + pg[j].id + "\">" + htmlEscape(name) + "</a>" +
								"</td><td>" +
								htmlEscape(pg[j].email) +
								"</td><td>" +
								htmlEscape(convertArraytoStringBySemicolon(pg[j].authors)) +
								"</td><td>" +
								htmlEscape(pg[j].track) +
								"</td><td>" +
								htmlEscape(convertArraytoStringBySemicolon(pg[j].tags)) +
								"</td><td>" +
								htmlEscape(convertArraytoStringBySemicolon(pg[j].audience)) +
								"</td><td>" +
								attachment +
								"</td><td>" +
								"<input type=\"checkbox\" id=\"general_" + pg[j].id + "\" value=\"" + pg[j].id + "\" " + general + " onclick=\"approval('" + pg[j].id + "','general');\"\>" +
								"</td><td>" +
								"<input type=\"checkbox\" id=\"approve_tech_" + pg[j].id + "\" value=\"" + pg[j].id + "\" " + approve_tech + " onclick=\"approval('" + pg[j].id + "','approve_tech');\"\>" +
								"</td><td>" +
								"<input type=\"checkbox\" id=\"approve_ip_" + pg[j].id + "\" value=\"" + pg[j].id + "\" " + approve_ip + " onclick=\"approval('" + pg[j].id + "','approve_ip');\"\>" +
								"</td><td>" +
								"<input type=\"checkbox\" id=\"approve_cor_" + pg[j].id + "\" value=\"" + pg[j].id + "\" " + approve_cor + " onclick=\"approval('" + pg[j].id + "','approve_cor');\"\>" +
								"</td><td>" +
								pg[j].registered +
								"</td><td>" +
								pg[j].tentative +
								"</td>"+sessionsbody+
								"</tr>");
						$("#thetable").append(row);
					}
				}
				$("#thetable").append("</tbody>");
				$('#thetable').dataTable({
					"sScrollX": "100%",
					"sScrollY": "400px",
					"bDestroy":true,
					"bPaginate":false,
					"aoColumnDefs": [
						{"sSortDataType":"dom-checkbox", "bSearchable":false, "aTargets":[7,8,9,10]}
					]
				});
			}

			$(function() {
				$.ajaxSetup({
					// Disable caching of AJAX responses */
					cache: false
				});
				$.getJSON("${pageContext['request'].contextPath}/conference/info.json", null, function(data) {
					timezone = data.timezone;
					$.getJSON("${pageContext['request'].contextPath}/course/courses.json", null, function(data) {
						papers=data;
						view(papers);
					});
				});
			});
		</script>
	</head>
	<body class="signin">
		<jsp:include page="/WEB-INF/views/includes/header.jsp"/>
		<h2 class="pageheading">Courses</h2>
			<input type="checkbox" name="sessions" id="sessions" value="Show Sessions" onclick="view(papers);" />
			<label for="sessions">Show sessions, times, and rooms</label>
			<div style="overflow: auto;">
				<table class="ixf-table ixf-table-default ixf-fixed" id="thetable" data-dt-rows="5">
				</table>
			</div>
		<!-- <div class="church-logo">The Church of Jesus Christ of Latter-day Saints</div> -->
	</body>
</html>
