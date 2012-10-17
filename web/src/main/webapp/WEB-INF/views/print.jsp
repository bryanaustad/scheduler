<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
		<meta http-equiv="X-UA-Compatible" content="chrome=1">
		<title>Print</title>
		<link rel="stylesheet" href="${pageContext['request'].contextPath}/styles/jquery.conf-schedule.css" type="text/css" media="print,screen, projection" />
		<link rel="stylesheet" href="${pageContext['request'].contextPath}/styles/print.css" type="text/css" media="print, screen, projection" />
		<link rel="stylesheet" href="${pageContext['request'].contextPath}/styles/conf.css" type="text/css" media="screen, projection" />
		<script type="text/javascript" src="${pageContext['request'].contextPath}/scripts/jquery-1.3.2.js"></script>
		<script type="text/javascript" src="${pageContext['request'].contextPath}/scripts/jquery.rest-1.0.0.js"></script>
		<script type="text/javascript" src="${pageContext['request'].contextPath}/scripts/jquery.corner.js"></script>
		<script type="text/javascript" src="${pageContext['request'].contextPath}/scripts/jquery.simplemodal-1.3.min.js"></script>
		<script type="text/javascript" src="${pageContext['request'].contextPath}/scripts/jquery.conf-search-1.0.0.js"></script>
		<script type="text/javascript" src="${pageContext['request'].contextPath}/scripts/jquery.conf-schedule-1.0.0.js"></script>
		<script type="text/javascript" src="${pageContext['request'].contextPath}/scripts/jquery.conf-tags-1.0.0.js"></script>
		<!--[if lte IE 6]>
			<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/chrome-frame/1/CFInstall.min.js"> </script>
		<![endif]-->

		<script type="text/javascript">
			//<![CDATA[
			var hooks = {
				date: {
					format: function(date) {
						return hooks.text.day[date.getDay()] + ", " +
							date.getDate() + " " + hooks.text.month[date.getMonth()];
					}
				},
				days: 2,
				dom: {
					id: {
						modal: "modal",
						modalHdr: "modal-hdr",
						modalBody: "modal-body",
						modalBody2: "modal-body2",
						modalFtr: "modal-ftr",
						modalTiny: "modal-tiny"
					}
				},
				handle: {
					confirm: function(course, callback) {
						$("#calendar").sortScheduleConflicts(course, function(conflicts) {
							for (var c in conflicts) {
								if (conflicts[c].registered) {
									conflicts[c].tentative = true;
									conflicts[c].registered = false;
									$.putJSON("${pageContext['request'].contextPath}/register/" + conflicts[c].id + "/" + true + "/", null, function(data) {});
								}
								$("#results").sortSearchUpdate(conflicts[c]);
							}
						});
						course.tentative = false;
						course.registered = true;
						$.putJSON("${pageContext['request'].contextPath}/register/" + course.id + "/" + false + "/", null, function(data) {});
						$("#calendar").sortScheduleUpdate();
						$("#results").sortSearchUpdate(course);
						callback();
					},
					search: function(crit, type) {
						$("#results").sortSearchFor(crit, type);
					},
					select: function(course, callback) {
						$("#calendar").sortScheduleConflicts(course, function(conflicts) {
							if (conflicts.length > 0) {
								course.tentative = true;
								course.registered = false;
							}
							else {
								course.tentative = false;
								course.registered = true;
							}
						});
						$.putJSON("${pageContext['request'].contextPath}/register/" + course.id + "/" + course.tentative + "/", null, function(data) {});
						$("#calendar").sortScheduleUpdate();
						$("#results").sortSearchUpdate(course);
						callback();
					},
					unselect: function(course, callback) {
						if (course.registered) {
							$("#calendar").sortScheduleConflicts(course, function(conflicts) {
								if (conflicts.length > 0) {
									conflicts[0].tentative = false;
									conflicts[0].registered = true;
									$.putJSON("${pageContext['request'].contextPath}/register/" + conflicts[0].id + "/" + false + "/", null, function(data) {});
								}
							});
						}
						course.tentative = false;
						course.registered = false;
						$.deleteJSON("${pageContext['request'].contextPath}/register/" + course.id + "/", null, function(data) {});
						$("#calendar").sortScheduleUpdate();
						$("#results").sortSearchUpdate(course);
						callback();
					},
					zoom: function(course, callback) {
						function buildModal(course) {
							var modal = $.eGen.div(null, hooks.dom.id.modal);

							var trimmed = (course.name.length > 52) ? course.name.substr(0, 52) + "..." : course.name;

							var head = $.eGen.div(null, hooks.dom.id.modalHdr);
							var headText = $.eGen.span();
							headText.text(trimmed);
							head.append(headText);
							modal.append(head);

							var body = $.eGen.div(null, hooks.dom.id.modalBody2);
							body.append("<div class='name'>" + course.name + "</div>");
							body.append("<div class='authors'><span>Presented By: </span>" + course.authors + "</div>");
							body.append("<div class='tags'><span>Tags: </span>" + course.tags + "</div>");
							if (course.room) {
								body.append("<div class='room'><span>Room: </span>" + course.room + "</div>");
							}
							body.append("<div class='desc'>" + course.desc + "</div>");

							if (!course.registered) {
								var reg = $.eGen.div("register");
								reg.text("[register]");
								reg.bind("click", function() {
									//hooks.handle.select(course, function(){});
									hooks.handle.confirm(course, function(){});
									$.modal.close();
								});
								body.append(reg);
							}

							modal.append(body);

							var foot = $.eGen.div(null, hooks.dom.id.modalFtr);
							modal.append(foot);

							if (!$.browser.msie) {
								head.corner("5px top");
								foot.corner("5px bottom");
							}
							return modal;
						}
						$.modal(buildModal(course), {
							minHeight: 388,
							minWidth: 494,
							overlay:80,
							overlayCss: {backgroundColor:"#000"}
						});
						if (callback) callback();
					}
				},
				text: {
					day: ["Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"],
					day3: ["Sun","Mon","Tue","Wed","Thu","Fri","Sat"],
					month: ["January","February","March","April","May","June","July","August","September","October","November","December"]
				},
				time: {
					end: 19,
					format: function(ticks) {
						//var date = new Date(ticks + this.zoneOffset);
						var date = new Date(ticks);
						var hour = date.getHours();
						var minute = date.getMinutes();
						//var ap = "am";
						if (hour > 12) { hour = hour - 12; }
						if (hour == 0) { hour = 12; }

						return hour + ((minute==0)? ":00" : ":" + minute);
					},
					zoneOffset: (new Date()).getTimezoneOffset() * 60000,
					start: 8
				}
			}
			$(function() {
				var _data;

				function buildModal() {
					var modal = $.eGen.div(null, hooks.dom.id.modal);

					var body = $.eGen.div(null, hooks.dom.id.modalTiny);
					body.text("Please wait...");
					modal.append(body);

					var img = $("<img src='images/anim_loading2.gif'/>");
					body.append(img);

					if (!$.browser.msie) {
						body.corner("6px");
					}
					return modal;
				}

				$("#calendar").sortScheduleUi(hooks);
				$("#filters").confTagsUi(hooks);
				$.modal(buildModal(), {
					containerId: "simplemodal-container2",
					minHeight: 120,
					minWidth: 160,
					overlay:80,
					overlayCss: {backgroundColor:"#000"}
				});
				$.getJSON("${pageContext['request'].contextPath}/course/list.json", null, function(data) {
					_data = data;
					$.modal.close();
					$("#calendar").sortScheduleData(data);
					$("#results").sortSearchData(data);
					$("#results").sortSearchShowAll();
					$("#filters").confTagsData(data);
				});
				$("#results").sortSearchUi(hooks);

				$("#unreg-all").bind("click", function() {
					$.deleteJSON("${pageContext['request'].contextPath}/register/unregisterall/", null, null);
					for (var c in _data) {
						_data[c].registered = false;
						_data[c].tentative = false;
					}
					$("#calendar").sortScheduleUpdate();
					$("#results").sortSearchUpdate(course);
				});
				$("#unreg-tent").bind("click", function() {
					$.deleteJSON("${pageContext['request'].contextPath}/register/unregisteralltentative/", null, null);
					for (var c in _data) {
						_data[c].tentative = false;
					}
					$("#calendar").sortScheduleUpdate();
					$("#results").sortSearchUpdate(course);
				});
			});


			//]]>
		</script>
	</head>
	<body>
		<div id="header-d">
			<div id="search-d">
			</div>
			<h1>Session Scheduler</h1>
		</div>
		<div id="content-d">
			<div id="calendar-d">
				<div id="calendar"></div>
			</div>
			<div class="clearfix"></div>
		</div>

		<script type="text/javascript">
			var browserName = navigator.appName;
			if(browserName == "Microsoft Internet Explorer"){
				//window.location = "${pageContext['request'].contextPath}/printie";
			}
		</script>
	</body>
</html>
