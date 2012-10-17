(function($){
	var defaults = {
		css: {
			courseAction: "course-action",
			courseAuthors: "course-authors",
			courseConfirm: "course-confirm",
			courseRemove: "course-remove",
			courseTime: "course-time",
			courseTitle: "course-title",
			courseZoom: "course-zoom",
			scheduleSec: "conf-sched-sec",
			schedCol: "conf-sched-col",
			schedDay: "conf-sched-day",
			schedDayHdr: "conf-sched-day-hdr",
			schedCourse: "conf-sched-course",
			schedCourseDet: "conf-sched-course-detail",
			schedCourseGen: "conf-sched-course-general",
			schedCourseTen: "conf-sched-course-tentative",
			schedHdr: "conf-sched-hdr",
			schedHour: "conf-sched-hour",
			schedQtr: "conf-sched-qtr",
			schedTimeHdr: "conf-sched-time-hdr",
			schedTimeSec: "conf-sched-time-sec",
			schedTime: "conf-sched-time"
		}
	}

	var sched = {
		event: {
			clump: function(course) {
				//Debug start
				// alert(32);
				//Debug end
				var clumped = 0;
				var cS = course.start;
				var cE = course.end;
				for (var i in this.clumps) {
					//Debug start
					// alert(39 + ": [" + i + "]");
					//Debug end
					var cl = this.clumps[i];
					var clS = cl.start;
					var clE = cl.end;
					if ((cS < clS && cE < clS) ||			// starts and ends before clump starts
							(cS > clE && cE > clE)) {		// starts and ends after clump ends
						// Do nothing
					}
					else {
						course = this.merge(course, cl, i);
						clumped++;
					}
				}
				if (!clumped && !course.isClump) {
					this.clumps.push(this.newClump(course));
				}
				else {
					for (var i in this.clumps) {
						if (this.clumps[i].kill) this.clumps.splice(i, 1);
					}
				}
			},
			days: [],
			daysLabel: [],
			display: function(clump) {
				//var date = new Date(clump.start - sched.opt.time.zoneOffset);
				var date = new Date(clump.start);
				//Debug start
					// alert(68 + ":\ndate == " + date);
				//Debug end
				var day = this.getDay(date);
				var hour = date.getHours();
				var dm = date.getMinutes();
				var min = Math.round(dm / 15) * 15;

				var length = Math.round(clump.end - clump.start) / 900000;

				var slotDomE = $("#" + day + "-" + hour + "-" + min);
				for (var c in clump.columns) {
					var col = clump.columns[c]
					var colDomE = $.eGen.div(sched.opt.css.schedCol);
					var full = ($.browser.msie && clump.columns.length == 2)? 99 : 100;
					colDomE.css("width", Math.floor(full / clump.columns.length) + "%");
					colDomE.css("height", (length * 20) + "px");
					slotDomE.append(colDomE);

					var pushed = 0;

					for (var cc in col.courses) {
						var course = col.courses[cc];
						//Debug start
						// alert(88);
						//Debug end
						var cLength = Math.round(course.end - course.start) / 900000;
						var offset = Math.round(course.start - clump.start) /900000;

						var cls
						if (course.registered) cls = sched.opt.css.schedCourse;
						if (course.tentative) cls = sched.opt.css.schedCourseTen;
						if (course.general) cls = sched.opt.css.schedCourseGen;

						var courseDomE = $.eGen.div(cls, course.id);
						if (cc != "0") courseDomE.addClass("stacked");
						courseDomE.css("top", ((offset - pushed) * 20) + "px");
						courseDomE.css("height", ((cLength * 20) - 3) + "px");
						courseDomE.data("course", course);
						if (!$.browser.msie) courseDomE.corner("6px");
						colDomE.append(courseDomE);

						var cActionDomE = $.eGen.div(sched.opt.css.courseAction);

						var cActRemDomE = $.eGen.div(sched.opt.css.courseRemove);
						if (!course.general){
							cActRemDomE.bind("click", function() {
								var course = $(this).parent().parent().data("course");
								sched.opt.handle.unselect(course, function() {});
							});
						}else{
							cActRemDomE.css("cursor","default");
						}
						cActionDomE.append(cActRemDomE);

						var cActZoomDomE = $.eGen.div(sched.opt.css.courseZoom);
						cActZoomDomE.bind("click", function() {
							var course = $(this).parent().parent().data("course");
							sched.opt.handle.zoom(course, function() {});
						});
						cActionDomE.append(cActZoomDomE);

						var cActConfDomE = $.eGen.div(sched.opt.css.courseConfirm);
						if (course.tentative){
							cActConfDomE.bind("click", function() {
								var course = $(this).parent().parent().data("course");
								sched.opt.handle.confirm(course, function() {});
							});
						}else{
							cActConfDomE.css("cursor","default");
						}
						cActionDomE.append(cActConfDomE);
						courseDomE.append(cActionDomE);

						var cDetailDomE = $.eGen.div(sched.opt.css.schedCourseDet);

						var cTimeDomE = $.eGen.div(sched.opt.css.courseTime);
						cTimeDomE.text(sched.opt.time.format(course.start) + " - " + course.room);
						//cTimeDomE.text(sched.opt.time.format(course.start - sched.opt.time.zoneOffset));
						cDetailDomE.append(cTimeDomE);

						var cTitleDomE = $.eGen.div(sched.opt.css.courseTitle);
						cTitleDomE.text(course.name);
						cDetailDomE.append(cTitleDomE);

						var cAuthorsDomE = $.eGen.div(sched.opt.css.courseAuthors);
						if(course.authors == null){
							cAuthorsDomE.text("");
						}else{
							var strAuthors = "";
							for(var i=0;i<course.authors.length;i++){
								//cAuthorsDomE.text(course.authors[i]);
								if(i!=0){
									strAuthors = strAuthors + ", ";
								}
								strAuthors = strAuthors + course.authors[i];
							}
							cAuthorsDomE.text(strAuthors);
						}
						cDetailDomE.append(cAuthorsDomE);

						courseDomE.append(cDetailDomE);
						pushed += cLength;
					}
				}
			},
			formatDate: function(date) {
				//Debug start
				// alert(175 + ":\ndate == " + date +"\nAs a String: " + String(date));
				//Debug end
				//Formats the date parameter into a string of 8 digits
				return "" + date.getFullYear() + ("0" + (date.getMonth() + 1)).slice(-2) + ("0" + date.getDate()).slice(-2);
			},	
			getDay: function(date) {
				var dayId = this.formatDate(date);
				//Debug start
				// alert(164 + ":\n" + "date == " + date + "\n" + "dayId == " + dayId);
				//Debug end
				for (var i in this.days) {
					if (this.days[i] == dayId) {
						//Debug start
						// alert(169 + ":\n" + "this.days[" + i + "] == " + this.days[i] + "\n" 
							// + "dayId == " + dayId);
						//Debug end
						return i;
					}
				}
				//Debug start
				// alert(176 + ":\n" + "this.days.length == " + this.days.length);
				//Debug end
				this.days.push(dayId);
				this.daysLabel[dayId] = sched.opt.date.format(date);
			},
			loadAll: function(ss, data) {
				//Format the dates in the dateArray to match dayIds
				var formattedDateArray = new Array();
				for (var i in sched.opt.dateArray) {
					var dateParts = sched.opt.dateArray[i].split("-");
					dateI = new Date(dateParts[0], dateParts[1] - 1, dateParts[2]);
					formattedDateArray.push(this.formatDate(dateI));
				}
				
				this.clumps = new Array();
				for (var i in data) {
					var course = data[i];
					if (course.registered || course.tentative || course.general) {
						startDate = new Date(course.start);
						//Debug start
						// alert("formattedDateArray == " + formattedDateArray + "\n"
							// + "formatDate(startDate) == " + this.formatDate(startDate));
						// alert("$.inArray(this.formatDate(startDate), formattedDateArray) == " + $.inArray(this.formatDate(startDate), formattedDateArray));
						//Debug end
						if ($.inArray(this.formatDate(startDate), formattedDateArray) >= 0) {
							this.getDay(startDate);
							course.end = course.end - 1;
							this.clump(course);
						}
					}
				}
				this.days.sort();
				for (var d = 0; d < sched.opt.days; d++) {
					//Debug start
					// alert('"#" + sched.opt.css.schedDayHdr + "_" + d == ' + "#" + sched.opt.css.schedDayHdr + "_" + d + "\n"
						// + "this.daysLabel[this.days[d]] == " + this.daysLabel[this.days[d]]);
					//Debug end
					$("#" + sched.opt.css.schedDayHdr + "_" + d).text(this.daysLabel[this.days[d]]);
					//Debug start
					// alert(189 + "[" + d + "]");
					//Debug end
					if(sched.opt.days==3){
						$("#" + sched.opt.css.schedDayHdr + "_" + d).width(($("#calendar").width()*30/100));
					}else if(sched.opt.days>3){
						$("#" + sched.opt.css.schedDayHdr + "_" + d).width("150px");
					}else if(sched.opt.days==1){
						$("#" + sched.opt.css.schedDayHdr + "_" + d).width("300px");
					}
					
				}
				for (var c in this.clumps) {
					this.sortClump(this.clumps[c]);
					this.display(this.clumps[c]);
				}
			},
			merge: function(c1, c2, i) {
				if (c1.start < c2.start) {
					c2.start = c1.start;
				}
				if (c1.end > c2.end) {
					c2.end = c1.end;
				}
				if (c1.isClump) {
					for (var c in c1.courses) {
						c2.courses.push(c1.courses[c]);
					}
				}
				else {
					c2.courses.push(c1);
				}
				c1.kill = true;
				return c2;
			},
			newClump: function(course) {
				return {
					isClump: true,
					courses: [course],
					end: course.end,
					start: course.start
				}
			},
			newColumn: function(course) {
				return {
					courses: [course],
					fits: function(c) {
						for (var i in this.courses) {
							if (!(c.end < this.courses[i].start ||
									c.start > this.courses[i].end)) {
								return false;
							}
						}
						return true;
					}
				}
			},
			sortClump: function(clump) {
				clump.columns = [];
				for (var i in clump.courses) {
					var done = false;
					for (var c in clump.columns) {
						if (clump.columns[c].fits(clump.courses[i])) {
							clump.columns[c].courses.push(clump.courses[i]);
							done = true;
							break;
						}
					}
					if (!done) {
						clump.columns.push(this.newColumn(clump.courses[i]));
					}
				}
			}
		},
		getId: function(opt, day, fifteens) {
			var hr = opt.time.start + Math.floor(fifteens / 4);
			var min = (fifteens % 4) * 15;
			return day + "-" + hr + "-" + min;
		},
		ui: {
			header: function() {
				var o = sched.opt;
				var hdr = $.eGen.div(o.css.schedHdr);
				hdr.append($.eGen.div(o.css.schedTimeHdr));
				for (var d = 0; d < o.days; d++) {
					hdr.append($.eGen.div(o.css.schedDayHdr, o.css.schedDayHdr + "_" + d));
					if(o.days==3){
						$("#"+o.css.schedDayHdr).width(($("#calendar").width()*30/100));
					}else if(o.days>3){
						$("#"+o.css.schedDayHdr).width("150px");
						$("#calendar").width((160*o.days+100)+"px");
					}else if(o.days==3){
						$("#"+o.css.schedDayHdr).width("400px");
					}
				}
				return hdr;
			},
			legend: function(hours) {
				var o = sched.opt;
				var timeSec = $.eGen.div(o.css.schedTimeSec);

				var time = new Date();
				time.setHours(o.time.start);
				time.setMinutes(0);
				var ticks = time.getTime();

				for (var h = 0; h < hours; h++) {
					var hourSec = $.eGen.div(o.css.schedTime);
					var hourSpan = $.eGen.span(o.css.schedTime);
					hourSpan.text(sched.opt.time.format(ticks + (h * 3600000)));
					hourSec.append(hourSpan);
					timeSec.append(hourSec);
				}
				return timeSec;
			}
		}
	}

	$.fn.sortScheduleUi = function(options) {
		var opt = defaults = $.extend(defaults, options);

		function newSchedule(ss) {
			var hours = (opt.time.end - opt.time.start);
			//Debug start
			// alert("opt.time.start == " + opt.time.start + "\n" +
			      // "opt.time.end == " + opt.time.end + "\n" +
				  // "hours == " + hours + "\n" + 
				  // "opt.days == " + opt.days + "\n" +
				  // "opt.dateArray == " + opt.dateArray);
			//Debug end
			ss.append(sched.ui.header());
			ss.append(sched.ui.legend(hours));
			for (var d = 0; d < opt.days; d++) {
				var daySec;
				daySec = $.eGen.div(opt.css.schedDay);
				if (opt.days == 3) {
					daySec.width($("#calendar").width()*30/100);
				} else if (opt.days > 3) {
					daySec.width("150px" );
				} else if (opt.days == 1) {
					daySec.width("400px");
				}
				var fifteens = hours * 4;
				for (var h = 0; h < fifteens; h++) {
					var id = sched.getId(opt, d, h);
					var cls = (h % 4 == 3)
						?opt.css.schedHour
						:opt.css.schedQtr;
					var hourSec = $.eGen.div(cls, id);
					daySec.append(hourSec);
				}
				ss.append(daySec);
			}
			//Debug start
			// alert(331);
			//Debug end
		}

		return this.each(function() {
			var ss = $(this);
			ss.data("opt", opt);
			sched.opt = ss.data("opt");
			newSchedule(ss);
		});
	}

	$.fn.sortScheduleData = function(data) {
		return this.each(function() {
			var ss = $(this);
			ss.data("data", data);
			sched.data = data;
			sched.opt = ss.data("opt");
			sched.event.loadAll(ss, data);
		});
	}

	$.fn.sortScheduleUpdate = function() {
		return this.each(function() {
			var ss = $(this);
			$("." + defaults.css.schedCol).remove();
			var data = ss.data("data");
			sched.event.loadAll(ss, data);
		});
	}

	$.fn.sortScheduleConflicts = function(course, callback) {
		return this.each(function() {
			var ss = $(this);
			var conflicts = [];
			for (var c in sched.event.clumps) {
				var clump = sched.event.clumps[c];
				if (course.start >= clump.start && course.start <= clump.end) {
					column_loop:
					for (var cc in clump.columns) {
						var cols = clump.columns[cc];
						for (var ccc in cols.courses) {
							var c2 = cols.courses[ccc];
							if (c2.id == course.id) {
								continue column_loop;
							}
							else if (c2.general) {
								continue;
							}
							else if ((c2.start < course.start && c2.end < course.start) ||
									(c2.start > course.end && c2.end > course.end)) {
								continue;
							}
							else {
								conflicts.push(c2);
							}
						}
					}
				}
			}
			callback(conflicts);
		});
	}

})(jQuery);
