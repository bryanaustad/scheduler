<!DOCTYPE html>
<%@include file="/WEB-INF/views/includes/init.jsp" %>
<html lang="en">
	<head>
		<meta http-equiv="X-UA-Compatible" content="chrome=1">
		<title>Schedule</title>
		<jsp:include page="/WEB-INF/views/includes/brains.jsp"/>
		<link rel="stylesheet" href="${pageContext['request'].contextPath}/styles/jquery.conf-search.css" type="text/css" media="screen, projection" />
		<link rel="stylesheet" href="${pageContext['request'].contextPath}/styles/jquery.conf-schedule.css" type="text/css" media="print, screen, projection" />
		<link rel="stylesheet" href="${pageContext['request'].contextPath}/styles/jquery.conf-tags.css" type="text/css" media="screen, projection" />
		<script type="text/javascript" src="${pageContext['request'].contextPath}/scripts/jquery.corner.js"></script>
		<script type="text/javascript" src="${pageContext['request'].contextPath}/scripts/jquery.simplemodal-1.3.min.js"></script>
		<script type="text/javascript" src="${pageContext['request'].contextPath}/scripts/jquery.conf-search-1.0.0.js"></script>
		<script type="text/javascript" src="${pageContext['request'].contextPath}/scripts/jquery.conf-schedule-1.0.0.js"></script>
		<script type="text/javascript" src="${pageContext['request'].contextPath}/scripts/jquery.conf-tags-1.0.0.js"></script>
		<script type="text/javascript" src="${pageContext['request'].contextPath}/scripts/jquery.jeditable.js"></script>
		<script type="text/javascript" src="${pageContext['request'].contextPath}/scripts/splitter.js"></script>
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
				dateArray: ["0000-00-00"],
				dom: {
					id: {
						modal: "modal",
						modalHdr: "modal-hdr",
						modalBody: "modal-body",
						modalBody2: "modal-body2",
						modalFtr: "modal-ftr",
						modalTiny: "modal-tiny",
						modelAuthorlink: "model-author-link",
						modelauthors: "authors",
						coursebacklink : "course-back-link",
						courseback : "courseback",
						coursebutton : "coursebutton",
						courseeditlink : "courseeditlink"
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
									$("#results").sortSearchUpdate(conflicts[0]);
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
					undefinedvalid:function(value){
						return (value==undefined || value=="undefined")?"":value;
					},
					authorzoom:function(author,course){
						var modal = $.eGen.div(null, hooks.dom.id.modal);
						var trimmed = (htmlEscape(author.name).length > 52) ? htmlEscape(author.name).substr(0, 52) + "..." : htmlEscape(author.name);

						var head = $.eGen.div(null, hooks.dom.id.modalHdr);
						var headText = $.eGen.span();
						headText.text("Author Profile : "+trimmed);
						head.append(headText);
						modal.append(head);

						var body = $.eGen.div("authordtls", hooks.dom.id.modalBody2);
						var authorinfo = $.eGen.div("authorinfo", "authorinfo");
						authorinfo.append("<div><span class='model-label'>Name</span>: <span id='author-name'>"+hooks.handle.undefinedvalid(htmlEscape(author.name))+"</span></div>");
						authorinfo.append("<div><span class='model-label'>Gender</span>: <span id='author-gen'>"+((author.gender=="M")?"Male":"Female")+"</span></div>");
						authorinfo.append("<div><span class='model-label'>Organization</span>: <span id='author-org'>"+hooks.handle.undefinedvalid(htmlEscape(author.organization))+"</span></div>");
						authorinfo.append("<div><span class='model-label'>Qualification</span>: <span id='author-dtnds'>"+hooks.handle.undefinedvalid(htmlEscape(author.qualification))+"</span></div>");
						authorinfo.append("<div><span class='model-label'>Email</span>: <span id='author-email'><a style='color:#FEFEFC;' href='mailto:"+hooks.handle.undefinedvalid(htmlEscape(author.email))+"'>"+htmlEscape(author.email)+"</a></span></div>");
						authorinfo.append("<div><span class='model-label'>Username</span>: <span id='author-username'>"+hooks.handle.undefinedvalid(htmlEscape(author.username))+"</span></div>");
						authorinfo.append("<div><span class='model-label' id='biographystyle'>Biography</span>: <span id='author-dtnds' class='desc_style'>"+hooks.handle.undefinedvalid(htmlEscape(author.biography))+"</span>");
						if (hooks.roleconfig.admin==true || hooks.roleconfig.approver==true || hooks.roleconfig.accountid==author.accountid) {
							authorinfo.append("<div><span class='model-label'>Phone</span>: <span id='author-phone'>"+hooks.handle.undefinedvalid(htmlEscape(author.phone))+"</span></div>");
							authorinfo.append("<div><span class='model-label'>Address</span>: <span id='author-addr'>"+hooks.handle.undefinedvalid(htmlEscape(author.address))+"</span></div>");
							authorinfo.append("<div><span class='model-label'>City</span>: <span id='author-prov'>"+hooks.handle.undefinedvalid(htmlEscape(author.city))+"</span></div>");
							authorinfo.append("<div><span class='model-label'>State/Province</span>: <span id='author-prov'>"+hooks.handle.undefinedvalid(htmlEscape(author.province))+"</span></div>");
							authorinfo.append("<div><span class='model-label'>Country</span>: <span id='author-cnty'>"+hooks.handle.undefinedvalid(htmlEscape(author.country))+"</span></div>");
							authorinfo.append("<div><span class='model-label'>Postal</span>: <span id='author-pstl'>"+hooks.handle.undefinedvalid(htmlEscape(author.postal))+"</span></div>");
							authorinfo.append("<div><span class='model-label'>Shirtsize</span>: <span id='author-shsz'>"+author.shirtsize+"</span></div>");
							authorinfo.append("<div><span class='model-label'>Dietneeds</span>: <span id='author-dtnds'>"+author.dietneeds+"</span></div>");
							authorinfo.append("<div><span class='model-label'>Date of Birth</span>: <span id='author-dtnds'>"+author.dob+"</span></div>");
						}

						var authorimg = $.eGen.div("authorimg", "authorimg");

						if (author.fileext+'' !='null' && $.trim(author.fileext)!='' && author.fileext !=undefined){
							//oImg.setAttribute('src',"${pageContext['request'].contextPath}/images/upload/"+author.id+"."+author.fileext);
							var oImg= "<img id='img32' alt='Profile Picture' width='100' height='100' src='data:image/"+author.fileext+";base64,"+author.photo+"'/>";
							//var oImg= "<img id='img32' name='img32' alt='na' width='100' height='100' src='"+"${pageContext['request'].contextPath}/images/upload/"+author.id+"."+author.fileext+"'/>";
							// authorimg.append(oImg);
							/* var oImg=document.createElement('img');
							oImg.setAttribute('height', '100px');
							oImg.setAttribute('width', '100px');
							oImg.setAttribute('src',);
							oImg.setAttribute('alt', 'na');
							*/
							authorimg.append(oImg);
						} else {
							var oImg= "<img id='img32' alt='Profile Picture' width='100' height='100' src='${pageContext['request'].contextPath}/images/nophoto.jpg'/>";
							authorimg.append(oImg);
						}

						var courseback = $.eGen.div("courseback", hooks.dom.id.courseback);
						var courselink = $.eGen.span(hooks.dom.id.coursebacklink, hooks.dom.id.coursebacklink);
						courselink.append("Back");
						courselink.bind("click", function() {
							$.modal.close();
							$.modal(hooks.handle.zoom(course,function() {}), {
								minHeight: 388,
								minWidth: 494,
								overlay:80,
								overlayCss: {backgroundColor:"#000"}
							});
						});
						courseback.append(courselink);
						authorinfo.append(courseback);
						authorinfo.append("</div>");
						body.append(authorinfo);

						body.append(authorimg);
						modal.append(body);

						var foot = $.eGen.div(null, hooks.dom.id.modalFtr);
						modal.append(foot);

						if (!$.browser.msie) {
							head.corner("5px top");
							foot.corner("5px bottom");
						}
						return modal;
					},
					textinlinebind:function(spanclass,spanid,value,type,ajaxurl,arrayflag,roleconfiginfo,courseinfo) {
						var gendiv = $.eGen.span(spanclass,spanid);
						gendiv.append(""+value+"");
						if (roleconfiginfo.accountid==courseinfo.accountid || roleconfiginfo.admin ==true) {
							gendiv.bind("mouseover", function() {
								if (type=="autocomplete") {
									$.editable.addInputType('autocomplete', {
										element : $.editable.types.text.element,
										plugin : function(settings, original) {
											function split(val) {
												return val.split(/;\s*/);
											}
											function extractLast(term) {
												return split(term).pop();
											}
											$('input', this).autocomplete({
												source: function(request, response) {
													$.getJSON("${pageContext['request'].contextPath}/"+settings.autocomplete.url, {
														term: extractLast(request.term)
													}, response);
												},
												search: function() {
													// custom minLength
													var term = extractLast(this.value);
													if (term.length < 2) {
														return false;
													}
												},
												focus: function() {
													// prevent value inserted on focus
													return false;
												},
												select: function(event, ui) {
													var terms = split( this.value );
													// remove the current input
													terms.pop();
													// add the selected item
													terms.push( ui.item.value );
													// add placeholder to get the comma-and-space at the end
													terms.push("");
													this.value = terms.join("; ");
													return false;
												}
											});
										}
									});
								}
							});
						}
						return gendiv;
					},
					autosuggest:function(id,url,value,pretext) {
						var gendiv =  $(document.createElement('div')).attr("class","model-div");
						var genspan =  $(document.createElement('span')).attr("class","model-label").html(pretext);
						gendiv.append(genspan);
						gendiv.append(": ");
						var inputtag =  $(document.createElement('input')).attr("type","text").attr("id",id).attr("value",value).attr("size","40");
						function split(val) {
							return val.split(/;\s*/);
						}
						function extractLast(term) {
							return split(term).pop();
						}

						inputtag.autocomplete({
							source: function(request, response) {
								$.getJSON("${pageContext['request'].contextPath}/"+url, {
									term: extractLast(request.term)
								}, response);
							},
							search: function() {
								var term = extractLast(this.value);
								if (term.length < 2) {
									return false;
								}
							},
							focus: function() {
								return false;
							},
							select: function(event, ui) {
								var terms = split( this.value );
								terms.pop();
								terms.push( ui.item.value );
								terms.push("");
								this.value = terms.join("; ");
								return false;
							}
						});
						gendiv.append(inputtag);
						return gendiv;
					},
						zoom: function(course, callback) {
							function buildModal(course) {
								var modal = $.eGen.div(null, hooks.dom.id.modal);
								var trimmed = ((course.name).length > 52) ? (course.name).substr(0, 52) + "..." : (course.name);
								var head = $.eGen.div(null, hooks.dom.id.modalHdr);
								var headText = $.eGen.span();
								headText.text(trimmed);
								head.append(headText);
								modal.append(head);

								var body = $.eGen.div(course.id, hooks.dom.id.modalBody2);
								if (hooks.roleconfig.admin==true){
									var edit = $.eGen.div(hooks.dom.id.courseeditlink, hooks.dom.id.courseeditlink);
									edit.append("<a href='${pageContext['request'].contextPath}/course?id="+course.id.replace(/_[0-9]*/,'')
											+"' width='16' height='16' border='0'>"
											+"<img src='${pageContext['request'].contextPath}/images/edit.png' width='16' height='16'border='0' />"
											+"Edit</a>");
									body.append(edit);
								} else if (hooks.roleconfig.accountid==course.accountid){
									var edit = $.eGen.div(hooks.dom.id.courseeditlink, hooks.dom.id.courseeditlink);
									edit.append("<a href='${pageContext['request'].contextPath}/paper?id="+course.id.replace(/_[0-9]*/,'')
											+"' width='16' height='16' border='0'>"
											+"<img src='${pageContext['request'].contextPath}/images/edit.png' width='16' height='16'border='0' />"
											+"Edit</a>");
									body.append(edit);
								}
								body.append("<div class='model-dtls-line'> <span class='model-label'>Course</span>: ");
								if (course.url!=undefined)
									body.append('<a style="color:#ccc;" target="_blank" href="'+course.url+'">'+htmlEscape(course.name)+'</a>');
								else
									body.append(htmlEscape(course.name));
								body.append("</div>");
								body.append("<br/><div class='model-dtls-line'><span  class='model-label'>Description </span>: ");
								body.append(hooks.handle.textinlinebind('spdesc','desc',hooks.handle.undefinedvalid(htmlEscape(course.desc)),'textarea',null,false,hooks.roleconfig,course));
								var authors = $.eGen.div(null, hooks.dom.id.modelauthors);
								authors.append("<span class='model-label'>Presented By </span>: ");
								var authortext = $.eGen.span('editauthors', 'editauthors');
								try{
								var comma = " ";
								for (var authorid in course.authors) {
									var authorlink = $.eGen.span(hooks.dom.id.modelAuthorlink, hooks.dom.id.modelAuthorlink);
									authorlink.append(htmlEscape(course.authors[authorid]));
									authorlink.bind("click", function() {
										var obj = {};
										// Unescape html to handle <> in usernames, etc.
										obj.name = $.trim($("<div/>").html(this.innerHTML).text());
										$.getJSON("${pageContext['request'].contextPath}/author/details.json", obj, function(data) {
											if (data.name!= undefined && data.email != undefined){
												$.modal.close();
												$.modal(hooks.handle.authorzoom(data,course), {
													minHeight: 388,
													minWidth: 494,
													overlay:80,
													overlayCss: {backgroundColor:"#000"}
												});
											}else{
												alert("No Data found");
											}
										});
									});
									authortext.append(comma);
									authortext.append(authorlink);
									comma = " ; ";
								}
							} catch(error) {}
							authors.append(authortext);

							//var authoredit = $.eGen.span("spauthors", "spauthors");
							//authoredit.append("&nbsp;&nbsp;&nbsp;");
							//authoredit.bind("click", function() {
							//	alert(""+course.authors);
							//	alert($('#editauthors').html('course.authors'));
							//});

							//authors.append(authoredit);
							authors.append("&nbsp;&nbsp;")
							body.append(authors);
							body.append("<div class='model-dtls-line'><span  class='model-label'>Tags </span>: ");
							body.append(hooks.handle.textinlinebind('sptags','tags',hooks.handle.undefinedvalid((htmlEscape(convertArraytoStringBySemicolon(course.tags)))),'autocomplete',"tag/list.json",true,hooks.roleconfig,course));
							body.append("</div>");
							if (course.room) {
								body.append("<br/><div class='model-dtls-line'><span  class='model-label'>Room </span>: ");
								body.append(hooks.handle.textinlinebind('sproom','room',hooks.handle.undefinedvalid(htmlEscape(course.room)),'text',null,false,hooks.roleconfig,course));
								body.append("</div>");
							}
							if (course.start != undefined && course.end != undefined) {
								body.append("<br/><div class='model-dtls-line'><span  class='model-label'>Date </span>: ");
								var coursestartdate = new Date(course.start);
								var courseenddate = new Date(course.end);
								//var conferencedate = (((coursestartdate.toString()).split(":00 GMT"))[0])+ " - "+courseenddate.toString().substring(16,21)+ " GMT "+(parseInt(hooks.timezone)+":"+((hooks.timezone*60)%60));

								var gmtminutes = ((hooks.timezone*60)%60);
								if (((hooks.timezone*60)%60)==0){
									gmtminutes +="0";
								}
								var gmthours = parseInt(hooks.timezone);
								if (parseInt(hooks.timezone)<10 && parseInt(hooks.timezone)>=0){
									gmthours = "+0"+gmthours;
								}else if (parseInt(hooks.timezone)<0 && parseInt(hooks.timezone)>-10){
									gmthours = "-0"+(-1*gmthours);
								}else if (parseInt(hooks.timezone)>=10){
									gmthours = "+"+gmthours;
								}
								var conferencedate = coursestartdate.toDateString()+" "+
										convertAM_PM(coursestartdate.getHours(),coursestartdate.getMinutes())+
										" - "+
										convertAM_PM(courseenddate.getHours(),courseenddate.getMinutes())+
										" (GMT "+gmthours+":"+gmtminutes+")";
								body.append(conferencedate);
								body.append("</div>");
								var coursestartdate = new Date(course.start);
								coursestartdate.setHours(coursestartdate.getHours()-parseInt(hooks.timezone));
								coursestartdate.setMinutes(coursestartdate.getMinutes()-((hooks.timezone*60)%60));
								coursestartdate.setMinutes (coursestartdate.getMinutes()-coursestartdate.getTimezoneOffset());
								var courseenddate = new Date(course.end);
								courseenddate.setHours(courseenddate.getHours()-parseInt(hooks.timezone));
								courseenddate.setMinutes(courseenddate.getMinutes()-((hooks.timezone*60)%60));
								courseenddate.setMinutes (courseenddate.getMinutes()-courseenddate.getTimezoneOffset());
								//body.append("<br/><div class='model-dtls-line'><span  class='model-label'>Local Time </span>: ");
								//var localconferencedate = coursestartdate.toDateString()+" "+
								//		convertAM_PM(coursestartdate.getHours(),coursestartdate.getMinutes())+
								//		" - "+
								//		convertAM_PM(courseenddate.getHours(),courseenddate.getMinutes())+
								//		" (GMT "+(coursestartdate.toString().split("GMT"))[1]+")";
								//body.append(localconferencedate);
								//body.append("</div>");
							}
							body.append("</div>");
							if (course.filename != undefined){
								body.append("<br/><div class='model-dtls-line'><span  class='model-label'>Slide Deck </span>: ");
								body.append(" <a style='color:#ccc;' target='_blank' href=\"${pageContext['request'].contextPath}/course/paper/"+htmlEscape(course.filename)+"\">"+htmlEscape(course.filedisplayname)+"</a>");
								body.append("</div>");
							}
							if (!course.registered && !course.general && (course.start != undefined)) {
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
							//foot.append("<span class='courseeditimg'>&nbsp;</span>");
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
					end: 18,
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
					start: 7
				}

			}
			$(function() {
				$.ajaxSetup({
					// Disable caching of AJAX responses */
					cache: false
				});
				var _data;
				function buildModal() {
					var modal = $.eGen.div(null, hooks.dom.id.modal);
					var body = $.eGen.div(null, hooks.dom.id.modalTiny);
					body.text("Please wait...");
					modal.append(body);
					var img = $("<img src='${pageContext['request'].contextPath}/images/anim_loading2.gif'/>");
					body.append(img);
					if (!$.browser.msie) {
						body.corner("6px");
					}
					return modal;
				}
				$.getJSON("${pageContext['request'].contextPath}/roles/get", null, function(data) {
					hooks.roleconfig = data;
					var selectiondate = data.days.length;
					var dateArray = data.days;
				$.getJSON("${pageContext['request'].contextPath}/conference/info.json", null, function(data) {
				if (data.timezone!=undefined) {
					var timezone = data.timezone;
					hooks.timezone = data.timezone;
					var timeArrayObj = data.times;
					var conferenceGMTstartdate = new Date();
					var conferenceGMTenddate = new Date();
					for (var i=0;i<timeArrayObj.length;i++) {
						var timeObj = timeArrayObj[i];
						var startdate = convertGMTtoConferenceTimezone(timeObj.start,hooks.timezone);
						var endtimedate = convertGMTtoConferenceTimezone(timeObj.end,hooks.timezone);
						if (i==0) {
							conferenceGMTstartdate = startdate;
							conferenceGMTenddate = endtimedate;
						} else {
							if (conferenceGMTstartdate.getHours() > startdate.getHours()){
									conferenceGMTstartdate = startdate;
							} else if (conferenceGMTstartdate.getHours() == startdate.getHours()){
								if (conferenceGMTstartdate.getMinutes() > startdate.getMinutes()){
									conferenceGMTstartdate = startdate;
								}
							}
							if (conferenceGMTenddate.getHours() < endtimedate.getHours()){
								conferenceGMTenddate = endtimedate;
							} else if (conferenceGMTenddate.getHours() == endtimedate.getHours()){
								if (conferenceGMTenddate.getMinutes() < endtimedate.getMinutes()){
									conferenceGMTenddate = endtimedate;
								}
							}
						}
					}
					hooks.time.start = conferenceGMTstartdate.getHours();
					hooks.time.end = conferenceGMTenddate.getHours() + (conferenceGMTenddate.getMinutes()==0?0:1);
					//hooks.days = i;//no of days
					hooks.days = selectiondate;
					hooks.dateArray = dateArray;

					$("#calendar").sortScheduleUi(hooks);
					$("#filters").confTagsUi(hooks);
					$.modal(buildModal(), {
						containerId: "simplemodal-container2",
						minHeight: 120,
						minWidth: 160,
						overlay:80,
						overlayCss: {backgroundColor:"#000"}
					});

					$.getJSON("${pageContext['request'].contextPath}/course/listapproved.json", null, function(data) {
						_data = convertGMTtoBrowserGMT(data);
						hooks.data = _data;
						$.modal.close();
						$("#calendar").sortScheduleData(data);
						$("#results").sortSearchData(data);
						$("#results").sortSearchShowAll();
						$("#filters").confTagsData(data);
						if (hooks.days>3) {
							var widthcalendar = 150*hooks.days+80;
							$("#content-d").attr("style","width:"+(widthcalendar)+"px;");
							$("#MySplitter").css("min-width",(widthcalendar+650)+"px");
							//$("#content-d").attr("style","width:auto;");
							//$("#MySplitter").css("min-width","auto");
							$("#RightPane").css("background-color","#E9E8E8");
							// Main vertical splitter, anchored to the browser window
							$("#MySplitter").splitter({
								splitVertical: true,
								outline: true,
								sizeLeft: true,
								minLeft: 190,
								maxLeft:widthcalendar,
								anchorToWindow: true,
								resizeToWidth: true,
								dock: "right",
								dockKey: 'Z',
								accessKey: "I"
							});
							$("#calendar").attr("style","width:"+widthcalendar+"px;");
						} else {
							$("#content-d").attr("style","width:1160px;");
							$("#MySplitter").attr("style","width:1160px;");
							$("#MySplitter").splitter({
								splitVertical: false,
								outline: false,
								sizeLeft: true,
								minLeft: 500,
								minRight: 650,
								resizeToWidth: true,
								dock: "right",
								dockKey: 'Z',
								accessKey: "I"
							});
							$(".vsplitbar").removeClass();
							$("#calendar").attr("style","width:500px;");
							$("#RightPane").css("min-width",(650)+"px");
						}
					});
				} else {
					$("#content-d").hide();
				}
			});
		});
				function convertGMTtoBrowserGMT(data){
					for (var i=0;i<data.length;i++){
						if (data[i].start != undefined) {
							var startdate = convertGMTtoConferenceTimezone(data[i].start,hooks.timezone);
							data[i].start = startdate.getTime();
						}
						if (data[i].end != undefined) {
							var endtimedate = convertGMTtoConferenceTimezone(data[i].end,hooks.timezone);
							data[i].end = endtimedate.getTime();
						}
					}
					return data;
				}

				$("#results").sortSearchUi(hooks);

				$("#unreg-all").bind("click", function() {
					$.deleteJSON("${pageContext['request'].contextPath}/register/unregisterall/", null, null);
					for (var c in _data) {
						_data[c].registered = false;
						_data[c].tentative = false;
						$("#results").sortSearchUpdate(_data[c]);
					}
					$("#calendar").sortScheduleUpdate();
				});
				$("#unreg-tent").bind("click", function() {
					$.deleteJSON("${pageContext['request'].contextPath}/register/unregisteralltentative/", null, null);
					for (var c in _data) {
						_data[c].tentative = false;
						$("#results").sortSearchUpdate(_data[c]);
					}
					$("#calendar").sortScheduleUpdate();
				});
			});

			//]]>
		</script>

	</head>
	<body style="overflow-x:auto;" class="schedulebody">
		<jsp:include page="/WEB-INF/views/includes/header.jsp"/>
		<h2 class="pageheading">Schedule</h2>
		<div id="content-d">
			<div id="action-d">
				<div>&nbsp;</div>
				<div>&nbsp;</div>
				<div id="unreg-all"><img src="${pageContext['request'].contextPath}/images/ico-check-red-x.png" alt="red x" /><span>Unregister All</span></div>
				<div id="unreg-tent"><img src="${pageContext['request'].contextPath}/images/ico-check-red-x.png" alt="red x" /><span>Remove Tentative</span></div>
			</div>
			<div id="MySplitter">
				<div id="LeftPane" class="calendar_scroll">
					<div id="calendar-d">
						<div id="calendar">
						</div>
					</div>
				</div>
				<div id="RightPane" >
					<div id="results-d">
						<div id="results"></div>
					</div>
					<div id="filters-d">
						<div id="filters"></div>
					</div>
				</div>
			</div>
		</div>
		<!-- <div class="clearfix church-logo">The Church of Jesus Christ of Latter-day Saints</div> -->
	<script>
		var isIE6 = (navigator.userAgent.indexOf("MSIE 6.") != -1);
		if (isIE6) {
			CFInstall.check({
				preventPrompt: true,
				onmissing: function() {
					alert("A plugin is required.  You will be redirected to the plugin site.  Restart your browser after installation is complete.");
					window.location = "https://www.google.com/chromeframe/eula.html";
				}
			});
		}
	</script>
	</body>
</html>
