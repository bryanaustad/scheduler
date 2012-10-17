<!DOCTYPE html>
<%@include file="/WEB-INF/views/includes/init.jsp" %>
<html lang="en">
	<head>
		<title>Update Profile</title>
		<jsp:include page="/WEB-INF/views/includes/brains.jsp"/>
		<script type="text/javascript" charset="utf-8">
		var timezone;
		function setGMTDateObject(sdate,localtime) {
			var s_date = sdate.split("-");
			var hr = localtime.split(":");
			var minutes = (hr[1].split(" "))[0];
			var hours = parseFloat(hr[0]);
			if ((hr[1].split(" "))[1]=="pm"){
				if (hours!=12){
					hours +=12;
				}
			}else{
				//am
				if (hours==12){
					hours = 0;
				}
			}
			var d = new Date();

			d.setUTCFullYear(s_date[0]);
			d.setUTCMonth((s_date[1]-1));
			d.setUTCDate(s_date[2]);
			d.setUTCHours(hours);
			d.setUTCMinutes(minutes);
			d.setUTCSeconds("0");
			return d;
			//return  new Date.UTC(s_date[2],(s_date[0]-1),s_date[1], hours, minutes);
		}

		function convertAM_PM(hours,minutes){
			hours = parseInt(hours);
			minutes = parseInt(minutes);
			if (minutes<10){
				minutes = "0"+minutes;
			}
			if (hours>=12){
				hours = (hours-12);
				if (hours==0){hours=12;}
				return ((hours<10)?("0"+hours):hours)+":"+minutes+" pm";
			} else {
				if (hours==0)hours=12;
				return ((hours<10)?("0"+hours):hours)+":"+minutes+" am";
			}
		}

		var i = 0;
		function generaterow(startdate,enddate) {
			var gendl = $(document.createElement('dl'));
			var stdate = (startdate.toString().split(":00 "))[0];
			var eddate = (enddate.toString().split(":00 "))[0];
			var startdaygen = $(document.createElement('input')).attr("id", 'day_'+i);
			startdaygen.attr("name","day_"+i);
			startdaygen.attr("type","checkbox");
			startdaygen.attr("checked",false);
			startdaygen.attr("value",startdate.getFullYear()+'-'+("0"+(startdate.getMonth()+1)).slice(-2)+"-"+("0"+startdate.getDate()).slice(-2));
			var gendt = $(document.createElement('dt'));
			gendt.append(startdaygen);
			var gmtminutes = ((timezone*60)%60);
			if (((timezone*60)%60)==0){
				gmtminutes +="0";
			}
			var gmthours = parseInt(timezone);
			if (parseInt(timezone)<10 && parseInt(timezone)>=0) {
				gmthours = "+0"+gmthours;
			} else if (parseInt(timezone)<0 && parseInt(timezone)>-10) {
				gmthours = "-0"+(-1*gmthours);
			} else if (parseInt(timezone)>=10) {
				gmthours = "+"+gmthours;
			}
			var genlabel = $(document.createElement('label')).attr('for','day_'+i);
			genlabel.append('&nbsp;'+(stdate.substring(0,15))+" "+convertAM_PM(startdate.getHours(),startdate.getMinutes())+" - "+convertAM_PM(enddate.getHours(),enddate.getMinutes())+" (GMT"+gmthours+":"+gmtminutes+")");
			gendt.append(genlabel);
			gendl.append(gendt);
			$(".dynamicdaygen").append(gendl);
			i++;
		}

		$(function() {
			$.ajaxSetup({
				cache: false
			});
			$("#dob").datepicker({
				maxDate: new Date(),
				changeYear: true,
				dateFormat:"yy-mm-dd",
				//showOn: "button",
				//buttonImage: "${pageContext['request'].contextPath}/images/calendar.gif",
				//buttonImageOnly: true,
				//buttonText: "Date of Birth",
				changeMonth: true
			});

			$("a.primary").click(function() {
				$("#signin-box form").hide();
				$("#signin-loading").fadeIn();
				window.pct = 0;
				setTimeout("showAssets()", 2000);
			});
			$("#daysattendingconferenceId").hide();

			$.getJSON("${pageContext['request'].contextPath}/conference/info.json", null, function(data) {
				if (data.timezone!=undefined){
					timezone = data.timezone;
					$("#daysattendingconferenceId").show();
					$(data.times).each( function() {
						var startdatetime = convertGMTtoConferenceTimezone((this.start),timezone+24);
						var enddatetime = convertGMTtoConferenceTimezone((this.end),timezone+24);
						generaterow(startdatetime,enddatetime);
					});
				}

				$("#removeImage").bind("click",function(){
					$("#imageRemoveStatus").val(true);
						$("#img32").attr('src',"${pageContext['request'].contextPath}/images/nophoto.jpg");
						$("#removeImage").hide();
				});

				$.getJSON("${pageContext['request'].contextPath}/register/loadProfile", null, function(editdata) {
					$("#dietneeds").val(editdata.dietneeds);
					//$("#name").val(editdata.name);
					//$("#username").val(editdata.username);
					//$("#accountid").val(editdata.accountid);
					$("#address").val(editdata.address);
					$("#city").val(editdata.city);
					$("#province").val(editdata.province);
					//alert(editdata.country);
					$("#country").val(editdata.country);
					$("#postal").val(editdata.postal);
					$("#phone").val(editdata.phone);
					$("#gender").val(editdata.gender);
					$("#shirtsize").val(editdata.shirtsize);
					$("#org").val(editdata.organization);
					$("#email").val(editdata.email);
					for (var j=0;j<editdata.days.length;j++) {
						// check all day_* inputs that match this day
						$('input[name^="day_"][value="'+editdata.days[j]+'"]').attr("checked", "checked");
					}
					var presenter= ""+editdata.presenter;
					$("#presenter").val(editdata.presenter);
					if (presenter == 'true') {
						$('#bios').show();
						function binary(d) {
							var o = '';
							for (var i=0; i<d.length; i=i+2){
								o+=String.fromCharCode(eval('0x'+(d.substring(i,i+2)).toString(16)));
							}
							return o;
						}
						if (editdata.fileext+'' !='null' && $.trim(editdata.fileext)!='' && editdata.fileext !=undefined){
							$("#img32").attr('src',"data:image/"+editdata.fileext+";base64,"+(editdata.photo));
							$("#removeImage").attr('src',"${pageContext['request'].contextPath}/images/delete.png");
							$("#removeImage").show();
							//$("#img32").attr('src',"${pageContext['request'].contextPath}/images/upload/"+editdata.id+'.'+editdata.fileext);
						} else {
							$("#img32").attr('src',"${pageContext['request'].contextPath}/images/nophoto.jpg");
							$("#removeImage").hide();
						}
						$("#dob").val(editdata.dob);
						$("#qualification").val(editdata.qualification);
						$("#biography").val(editdata.biography);
					} else {
						$('#bios').hide();
						$("#img32").hide();
						$("#removeImage").hide();
					}
					$("#shirtsize").val(editdata.shirtsize)
				});
			});

			// Update user information from WAM in case it has changed
			// One can hope at least gender has not. :)
			$.getJSON("${pageContext['request'].contextPath}/register/userinfo.json", null, function(data){
				$("#email").val(data.email);
				$("#name").val(data.name);
				$("#username").val(data.username);
				$("#accountid").val(data.accountid);
				$("#gender").val(data.gender);
			});
		});

		function showAssets(){
			window.int = setInterval("loaderMania()", 500);
		}

		function goIn(){
			window.location = "register";
		}

		function loaderMania(){
			var increment = Math.floor(Math.random()*50);
			window.pct += increment;
			if (window.pct > 100){
				clearInterval(window.int);
				window.location = "register";
			} else {
				$("#signin-loading p").text("Loading "+pct+"%");
			}
		}

		function submitenter(myfield,e) {
			var keycode;
			if (window.event) keycode = window.event.keyCode;
			else if (e) keycode = e.which;
			else return true;
			if (keycode == 13) {
				document.f.submit()
				return false;
			} else
				return true;
		}

		function validateinputbox(fieldname,fieldext){
			if ($.trim($('#'+fieldname).val())=="")
				return " <li>"+fieldext+" is required.</li><br/>";
			return "";
		}

		function findSize() {
			if ( $.browser.msie ) {
				var a = document.getElementById('imagefile').value;
				$('#myImage').attr('style','display:block;');
				$('#myImage').attr('src',a);

				var imgbytes = document.getElementById('myImage').fileSize;
				var imgkbytes = Math.round(parseInt(imgbytes)/1024);
				alert(imgkbytes+' KB');
				$('#myImage').attr('style','display:none;');
			}else {
				// var fileInput = $("#imagefile")[0];
				// var imgbytes = fileInput.files[0].fileSize; // Size returned in bytes.
				var files = document.getElementsByName('imagefile'); // FileList object
				for (var i=0;i<5;i++)
					alert(files[i].fileSize)
				var imgkbytes = Math.round(parseInt(imgbytes)/1024);
				alert(imgkbytes+' KB');
			}
		}

		function validateform() {
			var errorMsg = "";
			errorMsg += validateinputbox("name","Name");
			errorMsg += validateinputbox("address","Address");
			errorMsg += validateinputbox("city","City");
			errorMsg += validateinputbox("province","State/Province");
			errorMsg += validateinputbox("postal","ZIP/Postal Code");
			errorMsg += validateinputbox("phone","Phone Number");
			errorMsg += validateinputbox("email","Email ID");
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
				if (!found) {
					errorMsg +=('Invalid file type.\nPlease attach a valid image file(gif/png/jpg/jpeg)');
				}
			}

			$(".errormsg").html(errorMsg);
			if (errorMsg=="") {
				return true;
			} else {
				return false;
			}
		}
	</script>
	<style>
		.errormsg{
			padding-left: 20px;
		}
	</style>
	</head>
	<body class="signin" onload="document.f.email.focus();">
		<jsp:include page="/WEB-INF/views/includes/header.jsp"/>
		<h2 class="pageheading">Edit Your Profile</h2>
		<div id="wide-box">
			<div class="errormsg" ></div>
			<form name="f" class="ixf-form form-v1" method="POST"  enctype="multipart/form-data" action="${pageContext['request'].contextPath}/register/update">
				<input type="hidden" name="username" id="username"/>
				<input type="hidden" name="accountid" id="accountid"/>
				<input type="hidden" name="presenter" id="presenter"/>
				<input type='hidden' id='imageRemoveStatus' name="imageRemoveStatus" value='false'/>
				<input type='hidden' id='filelength' value='0'/>
				<dl>
					<dt><label for="name">Name<span class="colorRed">*</span></label></dt>
					<dd><input type="text" name="name" id="name" readonly="readonly"/></dd>
				</dl>
				<dl>
					<dt><label for="email">Email<span class="colorRed">*</span></label></dt>
					<dd><input type="text" name="email" id="email" /></dd>
				</dl>
				<dl>
					<dt><label for="address">Address<span class="colorRed">*</span></label></dt>
					<dd><input type="text" name="address" id="address"/></dd>
				</dl>
				<dl>
					<dt><label for="city">City<span class="colorRed">*</span></label></dt>
					<dd><input type="text" name="city" id="city"/></dd>
				</dl>
				<dl>
					<dt><label for="province">State/Province<span class="colorRed">*</span></label></dt>
					<dd><input type="text" name="province" id="province"/></dd>
				</dl>
				<dl>
					<dt><label for="country">Country<span class="colorRed">*</span></label></dt>
					<dd><%@ include file="includes/country.jsp" %></dd>
				</dl>
				<dl>
					<dt><label for="postal">ZIP/Postal Code<span class="colorRed">*</span></label></dt>
					<dd><input type="text" name="postal" id="postal"/></dd>
				</dl>
				<dl>
					<dt><label for="phone">Phone<span class="colorRed">*</span></label></dt>
					<dd><input type="text" name="phone" id="phone"/></dd>
				</dl>
				<dl>
					<dt><label for="gender">Gender<span class="colorRed">*</span></label></dt>
					<dd><select name="gender" id="gender" aria-required="true" onKeyPress="return submitenter(this,event)">
							<option value="M">Male</option>
							<option value="F">Female</option>
						</select>
					</dd>
				</dl>
				<dl>
					<dt><label for="shirtsize">Shirt Size<span class="colorRed">*</span></label></dt>
					<dd><select name="shirtsize" id="shirtsize" aria-required="true" onKeyPress="return submitenter(this,event)">
							<option value="None">None</option>
							<option value="XS">XS</option>
							<option value="S">S</option>
							<option value="M">M</option>
							<option value="L">L</option>
							<option value="XL">XL</option>
							<option value="XXL">XXL</option>
							<option value="XXXL">XXXL</option>
						</select>
					</dd>
				</dl>
				<dl>
					<dt><label for="org">Organization<span class="colorRed">*</span></label></dt>
					<dd><select name="org" id="org" aria-required="true" onKeyPress="return submitenter(this,event)">
						<option value="Other">Other</option>
						<option value="ICS">ICS</option>
						<option value="Family History">Family History</option>
						<option value="BYU">BYU</option>
						<option value="BYU-Idaho">BYU-Idaho</option>
						<option value="BYU-Hawaii">BYU-Hawaii</option>
						<option value="LDS Business College">LDS Business College</option>
						<option value="Utah State University">Utah State University</option>
						<option value="University of Utah">University of Utah</option>
						<option value="Weber State University">Weber State University</option>
						<option value="Vendor">Vendor</option>
					</select></dd>
				</dl>
				<dl  id="daysattendingconferenceId">
					<dt>Days attending conference<span class="colorRed">*</span></dt>
					<dd><div class="dynamicdaygen"></div></dd>
				</dl>
				<dl>
					<dt><label for="dietneeds">Special Dietary Needs</label></dt>
					<dd><textarea name="dietneeds" id="dietneeds"></textarea></dd>
				</dl>
				<div id="bios">
				<dl>
					<dt><label for="dob">Date of Birth</label></dt>
					<dd><input type="text" name="dob" id="dob" readonly="readonly" style="width: 100px;"/></dd>
				</dl>
				<dl>
					<dt><label for="qualification">Qualification</label></dt>
					<dd><input type="text" name="qualification" id="qualification"/></dd>
				</dl>
				<dl>
					<dt><label for="biography">Biography</label></dt>
					<dd><textarea name="biography" id="biography" rows="6"></textarea></dd>
				</dl>
				<dl>
					<dd>
					<div style="position:relative;">
					<img id="img32" alt="Profile Photo" width="100" height="100" src="${pageContext['request'].contextPath}/images/nophoto.jpg">
					<img id="removeImage" alt="remove" src="${pageContext['request'].contextPath}/images/delete.png" style="display:none;vertical-align: top;cursor: pointer;position: absolute;left: 80px;top: 0px;">
					</div>
					</dd>
				</dl>
				<dl>
					<dt><label for="imagefile">Upload Picture:</label></dt>
					<dd><input type="file" name="imagefile" id="imagefile" accept="image/*"/></dd>
				</dl>
				</div>
				<dl class="nopad">
					<dt>&nbsp;</dt>
					<dd><input type="submit" value="Update" class="ixf-button primary" onclick="return validateform();"/>
						<a href = "${pageContext['request'].contextPath}/unregister" class = "ixf-button tertiary">Unregister</a>
					</dd>
				</dl>
			</form>
			<div id="signin-loading" style="display:none;">
				<p>Updating in...<strong></strong></p>
				<span></span>
			</div>
		</div><!-- /signin-container -->
		<!-- <div class="church-logo">The Church of Jesus Christ of Latter-day Saints</div> -->
	</body>
</html>
