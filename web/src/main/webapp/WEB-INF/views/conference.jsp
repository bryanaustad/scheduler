<!DOCTYPE html>
<%@include file="/WEB-INF/views/includes/init.jsp" %>
<html lang="en">
<head>
	<title>Conference Configuration</title>
	<jsp:include page="/WEB-INF/views/includes/brains.jsp"/>
	<script type="text/javascript" src="${pageContext['request'].contextPath}/scripts/ajaxfileupload.js"></script>
	<script type="text/javascript">
	var conference;
	$(function() {
		$.ajaxSetup({
			// Disable caching of AJAX responses */
			cache: false
		});
		var dates = $("#datefrom, #dateto").datepicker({
			changeYear: true,
			dateFormat:"yy-mm-dd",
			defaultDate: "+1w",
			changeYear: true,
			changeMonth: true,
			numberOfMonths: 1,
			minDate: new Date(),
			onSelect: function(selectedDate) {
				var option = this.id == "datefrom" ? "minDate" : "maxDate",
					instance = $(this).data("datepicker"),
					date = $.datepicker.parseDate(
						instance.settings.dateFormat ||
						$.datepicker._defaults.dateFormat,
						selectedDate, instance.settings);
				dates.not(this).datepicker("option", option, date);
			},
			onClose: function(dateText, inst) {
				generateDateTimeTable();
			}
		});

		$("#datelast").datepicker({
			changeYear: true,
			dateFormat:"yy-mm-dd",
			defaultDate: "+1w",
			changeMonth: true,
			numberOfMonths: 1,
			minDate: new Date()
		});

		$(document).ready(function() {
			$("#removeImage").bind("click",function(){
				$("#imageRemoveStatus").val(true);
				var imgtext = "<img id='logo' name='logo' alt='na' src='${pageContext['request'].contextPath}/images/nophoto.jpg' width='100' height='100'></img>";
				$("#loadimage").html(imgtext);
				$("#imagefilestatus").val("");
				$("#removeImage").hide();
			});
			$.getJSON("${pageContext['request'].contextPath}/attendee/getattendeesCount", null, function(data) {
				$("#attendeesCount").val(data.attendeesCount);
				$.getJSON("${pageContext['request'].contextPath}/conference/info.json", null, function(data) {
					conference=data;
					if (conference.datefrom!=undefined) {
						$("#name").val(conference.name);
						$("#datefrom").val(conference.datefrom);
						$("#dateto").val(conference.dateto);
						$("#datelast").val(conference.datelast);
						$("#timezone").val(conference.timezonename+";"+conference.timezone.toFixed(2));
						$("#maxattendees").val(conference.maxattendees);
						$("#tracks").val(conference.tracks);
						if (conference.fileext!=undefined && conference.fileext!="") {
							var imgtext = "<img id='logo' name='logo' alt='na' src='data:image/"+conference.fileext+";base64,"+conference.image+"' width='100' height='100'></img>";
							$("#removeImage").show();
							$("#loadimage").html(imgtext);
						} else {
							var imgtext = "<img id='logo' name='logo' alt='na' src='${pageContext['request'].contextPath}/images/nophoto.jpg' width='100' height='100'></img>";
							$("#loadimage").html(imgtext);
							$("#imagefilestatus").val("");
							$("#removeImage").hide();
						}
						generateDateTimeTable();
					} else {
						$("#maxattendees").val(data.attendeesCount);
					}
				});
			});
		});
	});

	function generateDateTimeTable() {
		$('.sessionbody').html("");
		if ($('#dateto').val()) {
			var startdatesplit = ($("#datefrom").val()).split("-");
			var startdate = new Date(startdatesplit[0],(startdatesplit[1]-1),startdatesplit[2]);
			var enddatesplit = ($('#dateto').val()).split("-");
			var enddate = new Date(enddatesplit[0],(enddatesplit[1]-1),enddatesplit[2]);
			while (startdate<=enddate){
				generaterow(startdate);
				startdate.setDate(startdate.getDate()+1);
			}
		}
		for (i=0;i<conference.times.length;i++) {
			var timeObj = conference.times[i];
			var startdatetime = convertGMTtoConferenceTimezone(timeObj.start,conference.timezone);
			linedate=startdatetime.getFullYear()+'-'+("0"+(startdatetime.getMonth()+1)).slice(-2)+'-'+("0"+startdatetime.getDate()).slice(-2);
			$("#start_"+linedate).val(convertAM_PM(startdatetime.getHours(),startdatetime.getMinutes()));
			var enddatetime = convertGMTtoConferenceTimezone(timeObj.end,conference.timezone);
			$("#end_"+linedate).val(convertAM_PM(enddatetime.getHours(),enddatetime.getMinutes()));
		}
	}

	function generaterow(startdate) {
		var gentr = $(document.createElement('tr'));
		var dateto = startdate.getFullYear()+'-'+("0"+(startdate.getMonth()+1)).slice(-2)+'-'+("0"+startdate.getDate()).slice(-2);
		var datelabel = startdate.toString().substring(0,3)+", "+startdate.toString().substring(4,10)+", "+startdate.getFullYear();
		gentr.after().html("<td><label>"+datelabel+"</label><input id='day_"+dateto+"' type='hidden' name='day_"+dateto+"' value='"+dateto+"'></input></td>");
		var sestimetd = $(document.createElement('td'));
		var start = $(document.createElement('input')).attr("id", 'start_'+dateto);
		start.attr("name","start_"+dateto);
		start.attr("type","text");
		start.attr("size","10");
		start.val("");
		sestimetd.append(start);
		gentr.append(sestimetd)
		var endsestimetd = $(document.createElement('td'));
		var end = $(document.createElement('input')).attr("id", 'end_'+dateto);
		end.attr("name","end_"+dateto);
		end.attr("type","text");
		end.attr("size","10");
		end.val("");
		endsestimetd.append(end);
		gentr.append(endsestimetd);
		$('.sessionbody').append(gentr);

		start.bind("blur",function() {
			timedifflength(start,end,null,true);
		});
		start.ptTimeSelect({
			onClose: function(i) {
				i.blur();
			}
		});
		end.bind("blur",function() {
			timedifflength(start,end,null,false);
		});
		end.ptTimeSelect({
			onClose: function(i) {
				i.blur();
			}
		});
	}

	function validateinputbox(fieldname,fieldext) {
		if ($.trim($('#'+fieldname).val())=="")
			return " <li>"+fieldext+" is required.</li>";
		return "";
	}

	function validateform() {
		//TODO: put in validation code for tracks
		var errorMsg = "";
		var checkedstatus = false;
		var timestatus = false;
		errorMsg += validateinputbox("name","Name of Conference");
		errorMsg += validateinputbox("datefrom","From Date");
		errorMsg += validateinputbox("dateto","To Date");
		errorMsg += validateinputbox("datelast","Last Date Submission");
		var integerOnly=/^\d+$/;
		if ($.trim($('#maxattendees').val())=="" || (!integerOnly.test($.trim($('#maxattendees').val())) && ($('#maxattendees').val()!=0))) {
			errorMsg += "<li>Please enter valid number for Max Attendees</li>";
		} else if (parseInt($('#maxattendees').val())<parseInt($("#attendeesCount").val())) {
			errorMsg += "<li>There are already "+$("#attendeesCount").val()+" attendees. Please enter a larger value for Max Attendees.</li>";
		}
		$("#formerrormsg").html(errorMsg);
		if (errorMsg=="" && $("#imageerrormsg").val()=="") {
			return true;
		} else {
			return false;
		}
	}

	function ajaxFileUpload() {
		var errorMsg = "";
		var imageupload = $('#imagefile');
		if (imageupload.val()!="") {
			var ext = (imageupload.val()).split(".");
			var imgformat = ['jpg','png','jpeg','gif','JPG','PNG','JPEG','GIF'];
			var found = false;
			for (var i=0;i<imgformat.length;i++) {
				if (ext[ext.length-1]==imgformat[i]) {
					found = true;
				}
			}
			if(!found) {
				errorMsg +=('<li>Please upload a valid image file(gif/png/jpg/jpeg).</li>');
			}
		}
		if (errorMsg!="") {
			$("#imageerrormsg").html(errorMsg);
			return false;
		} else {
			$("#imageerrormsg").html("");
			$("#loadimage").html("");
			$("#loading")
			.ajaxStart(function() {
				$(this).show();
			})

			.ajaxComplete(function() {
				$(this).hide();
			});

			$.ajaxSetup({
				// Disable caching of AJAX responses */
				cache: false
			});

			$.ajaxFileUpload({
				url:'conference/loadlogo.json',
				secureuri:false,
				fileElementId:'imagefile',
				dataType: "json",
				success: function (data, status) {},
				error: function (data, status, e) {
					//alert(e);
				}
			});
			return false;
		}
	}
	</script>
	<style>
	#my-box{
		width:800px;
		overflow: auto;
	}
	#my-box input {
		width: auto;
	}
	dl{
		display: inline-block;
		width: 100%;
		padding-left: 0px;
		margin-left: 0px;
	}
	dt{
		width: 170px;
		text-align: left;
		font-weight: normal;
		padding-left: 5px;
		white-space: nowrap;
	}
	dd{
		width : 55%;
	}
	.buttontd{
		text-align: right;
	}
	</style>
</head>
<body class="signin" onload="document.f.name.focus();">
	<jsp:include page="/WEB-INF/views/includes/header.jsp"/>
	<h2 class="pageheading">Conference Configuration</h2>
	<div id="my-box" >
		<div class="errormsg">
			<span id="formerrormsg"></span>
			<span id="imageerrormsg"></span>
		</div>
		<form name="f" class="form-v1" method="POST" enctype="multipart/form-data" action="${pageContext['request'].contextPath}/conference/modify.json">
			<input type="hidden" id="attendeesCount" />
			<input type='hidden' id='imageRemoveStatus' name="imageRemoveStatus" value='false'/>
			<input type="hidden" id="imagefilestatus" value=""/>
			<dl>
				<dt><label for="name">Name of Conference:<span class="colorRed">*</span></label></dt>
				<dd><input type="text" name="name" id="name" size="50" maxlength="50"/></dd>
			</dl>
			<dl>
				<dt><label for="datefrom">From Date:<span class="colorRed">*</span></label></dt>
				<dd><input type="text" name="datefrom" id="datefrom" size="50" readonly="readonly"/></dd>
			</dl>
			<dl>
				<dt><label for="dateto">To Date:<span class="colorRed">*</span></label></dt>
				<dd><input type="text" name="dateto" id="dateto" size="50" readonly="readonly"/></dd>
			</dl>
			 <dl>
				<dt><label for="datelast">Last Date Submission:<span class="colorRed">*</span></label></dt>
				<dd><input type="text" name="datelast" id="datelast" size="50" readonly="readonly"/></dd>
			</dl>
			<dl>
				<dt><label for="maxattendees">Max Attendees:<span class="colorRed">*</span></label></dt>
				<dd><input type="text" name="maxattendees" id="maxattendees" size="50"/>
				</dd>
			</dl>
			<dl>
				<dt><label for="tracks">Tracks:<span class="colorRed">*</span></label></dt>
				<dd><input type="text" name="tracks" id="tracks" size="50"/>
				</dd>
			</dl>
			<dl>
				<dt><label for="timezone">Time Zone:</label></dt>
				<dd><select name="timezone" id="timezone">
					<option value="Etc/GMT-12;-12.00">International Date Line West (GMT-12:00)</option>
					<option value="Pacific/Midway;-11.00">Midway Island, Samoa (GMT-11:00)</option>
					<option value="US/Hawaii;-10.00">Hawaii (GMT-10:00)</option>
					<option value="America/Anchorage;-9.00">Alaska (GMT-09:00)</option>
					<option value="America/Tijuana;-8.00">Tijuana, Baja California (GMT-08:00)</option>
					<option value="US/Pacific-New;-8.00">Pacific Time (US &amp; Canada) (GMT-08:00)</option>
					<option value="US/Mountain;-7.00">Mountain Time (US &amp; Canada) (GMT-07:00)</option>
					<option value="America/Chihuahua;-7.00">Chihuahua, La Paz, Mazatlan - Old (GMT-07:00)</option>
					<option value="US/Arizona;-7.00">Arizona (GMT-07:00)</option>
					<option value="Canada/Saskatchewan;-6.00">Saskatchewan (GMT-06:00)</option>
					<option value="America/Mexico_City;-6.00">Guadalajara, Mexico City, Monterrey - Old (GMT-06:00)</option>
					<option value="US/Central;-6.00">Central Time (US &amp; Canada) (GMT-06:00)</option>
					<option value="US/East-Indiana;-5.00">Indiana (East) (GMT-05:00)</option>
					<option value="US/Eastern;-5.00">Eastern Time (US &amp; Canada) (GMT-05:00)</option>
					<option value="America/Lima;-5.00">Bogota, Lima, Quito, Rio Branco (GMT-05:00)</option>
					<option value="America/Caracas;-4.50">Caracas (GMT-04:30)</option>
					<option value="America/Santiago;-4.00">Santiago (GMT-04:00)</option>
					<option value="America/Manaus;-4.00">Manaus (GMT-04:00)</option>
					<option value="America/La_Paz;-4.00">La Paz (GMT-04:00)</option>
					<option value="Canada/Atlantic;-4.00">Atlantic Time (Canada) (GMT-04:00)</option>
					<option value="Canada/Newfoundland;-3.50">Newfoundland (GMT-03:30)</option>
					<option value="America/Montevideo;-3.00">Montevideo (GMT-03:00)</option>
					<option value="America/Danmarkshavn;-3.00">Greenland (GMT-03:00)</option>
					<option value="America/Guyana;-3.00">Georgetown (GMT-03:00)</option>
					<option value="America/Buenos_Aires;-3.00">Buenos Aires (GMT-03:00)</option>
					<option value="Etc/GMT-2;-2.00">Mid-Atlantic (GMT-02:00)</option>
					<option value="Atlantic/Cape_Verde;-1.00">Cape Verde Is. (GMT-01:00)</option>
					<option value="Atlantic/Azores;-1.00">Azores (GMT-01:00)</option>
					<option value="Africa/Casablanca;0.00">Casablanca (GMT)</option>
					<option value="Europe/London;0.00">Dublin, Edinburgh, Lisbon, London (GMT)</option>
					<option value="Atlantic/Reykjavik;0.00">Monrovia, Reykjavik (GMT)</option>
					<option value="Europe/Berlin;1.00">Amsterdam, Berlin, Bern, Rome, Stockholm, Vienna (GMT+01:00)</option>
					<option value="Europe/Belgrade;1.00">Belgrade, Bratislava, Budapest, Ljubljana, Prague (GMT+01:00)</option>
					<option value="Europe/Paris;1.00">Brussels, Copenhagen, Madrid, Paris (GMT+01:00)</option>
					<option value="Europe/Warsaw;1.00">Sarajevo, Skopje, Warsaw, Zagreb (GMT+01:00)</option>
					<option value="Africa/Algiers;1.00">West Central Africa (GMT+01:00)</option>
					<option value="Asia/Amman;2.00">Amman (GMT+02:00)</option>
					<option value="Europe/Athens;2.00">Athens, Bucharest, Istanbul (GMT+02:00)</option>
					<option value="Asia/Beirut;2.00">Beirut (GMT+02:00)</option>
					<option value="Africa/Cairo;2.00">Cairo (GMT+02:00)</option>
					<option value="Africa/Harare;2.00">Harare, Pretoria (GMT+02:00)</option>
					<option value="Europe/Kiev;2.00">Helsinki, Kyiv, Riga, Sofia, Tallinn, Vilnius (GMT+02:00)</option>
					<option value="Asia/Jerusalem;2.00">Jerusalem (GMT+02:00)</option>
					<option value="Europe/Minsk;3.00">Minsk (GMT+03:00)</option>
					<option value="Africa/Windhoek;2.00">Windhoek (GMT+02:00)</option>
					<option value="Asia/Baghdad;3.00">Baghdad (GMT+03:00)</option>
					<option value="Asia/Kuwait;3.00">Kuwait, Riyadh (GMT+03:00)</option>
					<option value="Europe/Moscow;3.00">Moscow, St. Petersburg, Volgograd (GMT+03:00)</option>
					<option value="Africa/Nairobi;3.00">Nairobi (GMT+03:00)</option>
					<option value="Asia/Tbilisi;3.00">Tbilisi (GMT+03:00)</option>
					<option value="Asia/Tehran;3.50">Tehran (GMT+03:30)</option>
					<option value="Asia/Muscat;4.00">Abu Dhabi, Muscat (GMT+04:00)</option>
					<option value="Asia/Baku;4.00">Baku (GMT+04:00)</option>
					<option value="Europe/Samara;4.00">Caucasus Standard Time (GMT+04:00)</option>
					<option value="Indian/Mauritius;4.00">Port Louis (GMT+04:00)</option>
					<option value="Asia/Yerevan;4.00">Yerevan (GMT+04:00)</option>
					<option value="Asia/Kabul;4.50">Kabul (GMT+04:30)</option>
					<option value="Asia/Yekaterinburg;5.00">Yekaterinburg (GMT+05:00)</option>
					<option value="Asia/Karachi;5.00">Islamabad, Karachi (GMT+05:00)</option>
					<option value="Asia/Tashkent;5.00">Tashkent (GMT+05:00)</option>
					<option value="Asia/Calcutta;5.50">Chennai, Kolkata, Mumbai, New Delhi (GMT+05:30)</option>
					<option value="Asia/Colombo;5.50">Sri Jayawardenapura (GMT+05:30)</option>
					<option value="Asia/Kathmandu;5.75">Kathmandu (GMT+05:45)</option>
					<option value="Asia/Almaty;6.00">Almaty, Novosibirsk (GMT+06:00)</option>
					<option value="Asia/Dhaka;6.00">Astana, Dhaka (GMT+06:00)</option>
					<option value="Asia/Rangoon;6.50">Yangon (Rangoon) (GMT+06:30)</option>
					<option value="Asia/Bangkok;7.00">Bangkok, Hanoi, Jakarta (GMT+07:00)</option>
					<option value="Asia/Krasnoyarsk;7.00">Krasnoyarsk (GMT+07:00)</option>
					<option value="Asia/Hong_Kong;8.00">Beijing, Chongqing, Hong Kong, Urumqi (GMT+08:00)</option>
					<option value="Asia/Irkutsk;8.00">Irkutsk, Ulaan Bataar (GMT+08:00)</option>
					<option value="Asia/Kuala_Lumpur(GMT+08:00);8.00">Kuala Lumpur, Singapore (GMT+08:00)</option>
					<option value="Austrailia/Perth;8.00">Perth (GMT+08:00)</option>
					<option value="Asia/Taipei;8.00">Taipei (GMT+08:00)</option>
					<option value="Asia/Tokyo;9.00">Osaka, Sapporo, Tokyo (GMT+09:00)</option>
					<option value="Asia/Seoul;9.00">Seoul (GMT+09:00)</option>
					<option value="Asia/Yakutsk;9.00">Yakutsk (GMT+09:00)</option>
					<option value="Australia/Adelaide;9.50">Adelaide (GMT+09:30)</option>
					<option value="Australia/Darwin;9.50">Darwin (GMT+09:30)</option>
					<option value="Australia/Brisbane;10.00">Brisbane (GMT+10:00)</option>
					<option value="Australia/Sydney;10.00">Canberra, Melbourne, Sydney (GMT+10:00)</option>
					<option value="Pacific/Guam;10.00">Guam, Port Moresby (GMT+10:00)</option>
					<option value="Australia/Hobart;10.00">Hobart (GMT+10:00)</option>
					<option value="Asia/Vladivostok;10.00">Vladivostok (GMT+10:00)</option>
					<option value="Asia/Magadan;11.00">Magadan, Solomon Is., New Caledonia (GMT+11:00)</option>
					<option value="Pacific/Auckland;12.00">Auckland, Wellington (GMT+12:00)</option>
					<option value="Pacific/Fiji;12.00">Fiji, Kamchatka, Marshall Is. (GMT+12:00)</option>
				</select></dd>
			</dl>
			<table class="sessiontable" >
				<caption>Conference Date &amp; Time</caption>
				<thead>
					<tr>
						<th>Conference Day</th>
						<th>Start Time</th>
						<th>End Time</th>
					</tr>
				</thead>
				<tbody class="sessionbody" >
				</tbody>
			</table>
			<dl>
				<dt>Logo</dt>
				<dd>
					<img id="loading" alt="loading" src="${pageContext['request'].contextPath}/images/anim_loading2.gif" style="display:none;">
					<div style="position:relative;">
						<span id="loadimage"></span>
						<img id="removeImage" alt="Remove Image" src="${pageContext['request'].contextPath}/images/delete.png" style="display:none;vertical-align: top;cursor: pointer;position: absolute;left: 80px;top: 0px;">
					</div>
				</dd>
			</dl>
			<dl>
				<dt><label for="imagefile">Upload new Logo:</label></dt>
				<dd>
					<input type="file" name="imagefile" id="imagefile" onchange="return ajaxFileUpload();"/>
				</dd>
			</dl>
			<dl>
				<dt>&nbsp;</dt>
				<dd><input class="ixf-button primary" type="submit" id="save" value="Save" onclick="return validateform();" /></dd>
			</dl>
		</form>
	</div>
	<!-- <div class="church-logo">The Church of Jesus Christ of Latter-day Saints</div> -->
</body>
</html>
