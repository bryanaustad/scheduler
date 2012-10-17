<!DOCTYPE html>
<%@include file="/WEB-INF/views/includes/init.jsp" %>
<html lang="en">
<head>
	<title>Dashboard</title>
	<jsp:include page="/WEB-INF/views/includes/brains.jsp"/>
	<script type="text/javascript" charset="utf-8">
	var countryName="";
	var organizationName="";
	var courseId =0;
	$(function(){
		$.ajaxSetup({
			// Disable caching of AJAX responses */
			cache: false
		});
		$.getJSON("${pageContext['request'].contextPath}/dashboard/optionlist.json", null, function(data) {
			for (var i in data.countryList) {
				if (!data.countryList[i]=='') {
					var optn = document.createElement("OPTION");
					optn.text = data.countryList[i];
					optn.value = data.countryList[i];
					document.getElementById("country").options.add(optn);
				}
			}
			for (var i in data.organizationList) {
				if (!data.organizationList[i]=='') {
					var optn = document.createElement("OPTION");
					optn.text = data.organizationList[i];
					optn.value = data.organizationList[i];
					document.getElementById("org").options.add(optn);
				}
			}
			for (var i in data.courseList) {
				if (!data.courseList[i]=='') {
					var optn = document.createElement("OPTION");
					if (data.courseList[i].postname==undefined) {
						optn.text = data.courseList[i].name;
					} else {
						optn.text = data.courseList[i].name+data.courseList[i].postname;
					}
					optn.value = data.courseList[i].courseid;
					document.getElementById("course").options.add(optn);
				}
			}

			$.getJSON("${pageContext['request'].contextPath}/dashboard/getAttendeesCount.json", null, function(data) {
				var genheadingorgtr = $(document.createElement('tr'));
				var orgth = $(document.createElement("th")).attr("colspan","2").attr("style","padding: 8px;text-align:center;color: #666666;");
				orgth.append("Organization");
				genheadingorgtr.append(orgth);
				$('#countryorgattendeecountbody').append(genheadingorgtr);
				var maintr = $(document.createElement('tr'));
				var mainth = $(document.createElement("td")).attr("class","rotateimg");
				var genspan = $(document.createElement("span")).attr("class","rotatetext");
				genspan.append("Country");
				mainth.append(genspan);
				maintr.append(mainth);
				var attendeescount = new Array();
				var totalattendeescount = 0;
				var maintd = $(document.createElement("td"));
				var gendiv = $(document.createElement('div')).attr("style","overflow:auto;");
				var innertable = $(document.createElement('table')).attr("id","courseListAttendees1");
				innertable.attr("class","attendeesStyle");
				var genorgtr = $(document.createElement('tr'));
				for (var i in data) {
					var gentr = $(document.createElement('tr'));
					var orgList = data[i].organizationList;
					var countryattendeescount = 0;
					if (i==0){
						var genth = $(document.createElement("th"));
						genth.append("&nbsp;");
						genorgtr.append(genth);
					}
					for(var j in orgList){
						if (i==0){
							var genth = $(document.createElement("th")).attr("style","text-align:center;");
							genth.append(orgList[j].organization);
							genorgtr.append(genth);
						}
						if (j==0){
							var genth = $(document.createElement("th"));
							genth.append(data[i].country);
							gentr.append(genth);
						}
						var gentd = $(document.createElement("td")).attr("style","color:blue;text-decoration:underline;");
						gentd.append(orgList[j].totalAttendees);
						attendeescount[j] = parseInt(attendeescount[j]==undefined?0:attendeescount[j])+ parseInt(orgList[j].totalAttendees);
						countryattendeescount=parseInt(countryattendeescount==undefined?0:countryattendeescount)+ parseInt(orgList[j].totalAttendees);
						gentd.bind("click", {orgItem: orgList[j].courseList,country:data[i].country,organization:orgList[j].organization}, function(event) {
							generateResult(event.data.orgItem,event.data.country,event.data.organization,"0");
						});
						gentr.append(gentd);
					}
					var gentd = $(document.createElement("td")).attr("style","color:blue;text-decoration:underline;");
					gentd.append(countryattendeescount);
					totalattendeescount+=countryattendeescount;
					gentd.bind("click", {country:data[i].country}, function(event) {
						ajaxfunction(event.data.country,"all","all");
					});
					gentr.append(gentd);
					if(i==0){
						var genth = $(document.createElement("th")).attr("style","text-align:center;");
						genth.append("Total");
						genorgtr.append(genth);
						innertable.append(genorgtr);
					}
					innertable.append(gentr);
				}
				var gentr = $(document.createElement('tr'));
				var genth = $(document.createElement("th"));
				genth.append("Total");
				gentr.append(genth);
				for(var j in attendeescount){
					var gentd = $(document.createElement("td")).attr("style","color:blue;text-decoration:underline;");
					gentd.append(""+attendeescount[j]+"");
					gentr.append(gentd);
					gentd.bind("click", {organization:data[0].organizationList[j].organization}, function(event) {
						ajaxfunction("all",event.data.organization,"all");
					});
				}
				var gentd = $(document.createElement("td")).attr("style","color:blue;text-decoration:underline;");
				gentd.append(""+totalattendeescount+"");
				gentr.append(gentd);
				gentd.bind("click", null, function(event) {
					ajaxfunction("all","all","all");
				});
				innertable.append(gentr);
				gendiv.append(innertable);
				maintd.append(gendiv);
				maintr.append(maintd);
				$('#countryorgattendeecountbody').append(maintr);
			});
		});
	});
	function generateResult(courses,country,organization,courseid) {
		$('#courseListAttendees').html("");
		var param = "?course="+courseid+"&"+"country="+country+"&organization="+organization;
		$('#downloadlink').html("<b><a href='${pageContext['request'].contextPath}/dashboard/downloadcourseregistrant.json"+param+"'> <img src='${pageContext['request'].contextPath}/images/csvicon.gif' alt='No Icon'/>Download</a></b>");
		$('#resultmsg').html("");
		$('#resultmsg').html("<b> <span style='font-size: 16px;padding-bottom:10px;'>Course Registration count </span><br/><br/>Country = '"+country+"' & Organization = '"+organization+"'</b>");

		if(courseid=="0"){
			courseid = ((courses.length==1)?courses[0].id:'all');
		}
		var gen_thead = $(document.createElement('thead'));
		var gen_tr = $(document.createElement('tr'));
		var gen_th = $(document.createElement("th"));
		gen_th.append("Course Name");
		gen_tr.append(gen_th);
		gen_th = $(document.createElement("th"));
		gen_th.append("Register");
		gen_tr.append(gen_th);
		gen_th = $(document.createElement("th"));
		gen_th.append("Tentative");
		gen_tr.append(gen_th);
		$('#courseListAttendees').append(gen_tr);
		gen_th = $(document.createElement("th"));
		gen_th.append("Total Attendees");
		gen_tr.append(gen_th);
		gen_thead.append(gen_tr);
		$('#courseListAttendees').append(gen_thead);
		//var registivecount = 0;
		//var tentativecount = 0;
		for (var k in courses){
			var gen_tr = $(document.createElement('tr'));
			var gen_td = $(document.createElement("td")).attr("style","text-align:left;white-space:nowrap;");
			gen_td.append(htmlEscape(courses[k].name)+courses[k].postname);
			gen_tr.append(gen_td);
			gen_td = $(document.createElement("td"));
			gen_td.append(courses[k].registiveCount);
			//registivecount+=courses[k].registiveCount;
			gen_tr.append(gen_td);
			gen_td = $(document.createElement("td"));
			gen_td.append(courses[k].tentativeCount);
			gen_tr.append(gen_td);
			//tentativecount+=courses[k].tentativeCount;
			gen_td = $(document.createElement("td"));
			gen_td.append(courses[k].tentativeCount+courses[k].registiveCount);
			gen_tr.append(gen_td);
			$('#courseListAttendees').append(gen_tr);
		}
		var gen_tr = $(document.createElement('tr'));
		var gen_td = $(document.createElement("th"));
		gen_td.append("");
		gen_tr.append(gen_td);
		gen_td = $(document.createElement("th")).attr("style","text-align:center");
		gen_td.append("");
		gen_tr.append(gen_td);
		gen_td = $(document.createElement("th")).attr("style","text-align:center");
		gen_td.append("");
		gen_tr.append(gen_td);
		gen_td = $(document.createElement("th")).attr("style","text-align:center");
		gen_td.append("");
		gen_tr.append(gen_td);
		//$('#courseListAttendees').append(gen_tr);
		document.location.href = '#courseresult';
		$('#courseListAttendees').dataTable({"bDestroy":true,"bPaginate":false});
	}

	function ajaxfunction(countryname,organizationamae,courseid){
		var obj = {};
		obj.country = countryname;
		obj.organization = organizationamae;
		obj.courseid = courseid;
		$.getJSON("${pageContext['request'].contextPath}/dashboard/search.json", obj, function(data) {
			generateResult(data,obj.country,obj.organization,courseid);
		});
	}

	function search(){
		ajaxfunction($("#country").val(),$("#org").val(),$("#course").val());
	}
	</script>
	</head>
	<body>
		<jsp:include page="/WEB-INF/views/includes/header.jsp"/>
		<h2 class="pageheading">Dashboard</h2>
		<form class="ixf-form form-v1" >
			<label>Country</label>
			<select name="country" id="country">
				<option value="all">All</option>
			</select>
			<label>Organization</label>
			<select name="org" id="org">
				<option value="all">All</option>
			</select>
			<label>Course</label>
			<select id="course" name="course">
				<option value="all">All</option>
			</select>
			<a href="#namae"> <input type="button" id=register value="Search" class="ixf-button primary" onclick="search()"></a>
		</form>
		<label id="resultmsg"></label>
		<br/>
		<label id="downloadlink"></label>
		</div>
		<table id='countryorgattendeecountbody'>
		</table>
		<table id='courseListAttendees' class="attendeesStyle">
		</table>
		<!-- <div class="clearfix church-logo" >The Church of Jesus Christ of Latter-day Saints</div> -->
	</body>
</html>
