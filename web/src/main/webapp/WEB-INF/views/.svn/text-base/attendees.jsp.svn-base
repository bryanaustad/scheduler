<!DOCTYPE html>
<%@include file="/WEB-INF/views/includes/init.jsp" %>
<html lang="en">
	<head>
		<title>Attendees</title>
		<jsp:include page="/WEB-INF/views/includes/brains.jsp"/>
		<script type="text/javascript" src="${pageContext['request'].contextPath}/scripts/jquery.corner.js"></script>
		<script type="text/javascript" src="${pageContext['request'].contextPath}/scripts/jquery.simplemodal-1.3.min.js"></script>
		<script type="text/javascript" src="${pageContext['request'].contextPath}/scripts/jquery.conf-search-1.0.0.js"></script>
		<!--[if lte IE 6]>
			<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/chrome-frame/1/CFInstall.min.js"> </script>
		<![endif]-->
		<script type="text/javascript">
		$(function(){
			$.ajaxSetup({
				// Disable caching of AJAX responses */
				cache: false
				});
			$.getJSON("${pageContext['request'].contextPath}/attendee/attendees.json", null, function(data) {
				var attendees = $('#attendees');

				function authorzoom(author){
					var modal = $.eGen.div(null, "modal");
					var trimmed = (author.name.length > 52) ? author.name.substr(0, 52) + "..." : author.name;

					var head = $(document.createElement('div')).attr("id", 'modal-hdr');
					var headText = $.eGen.span();
					headText.text(trimmed + " Author Profile");
					head.append(headText);
					modal.append(head);

					var body = $(document.createElement('div')).attr("id", 'modal-body2').attr('class','authordtls');
					var authorinfo = $(document.createElement('div')).attr("id", 'authorinfo').attr("class","authorinfo");
					authorinfo.append("<div><span>Name		 : </span> <span id='author-name'>"+htmlEscape(author.name)+"</span></div>");
					authorinfo.append("<div><span>Username		 : </span> <span id='author-name'>"+htmlEscape(author.username)+"</span></div>");
					authorinfo.append("<div><span>Email		: </span> <span id='author-email'>"+htmlEscape(author.email)+"</span></div>");
					authorinfo.append("<div><span>Organization : </span> <span id='author-org'>"+htmlEscape(author.organization)+"</span></div>");
					authorinfo.append("<div><span>Address	  : </span> <span id='author-addr'>"+htmlEscape(author.address)+"</span></div>");
					authorinfo.append("<div><span>City	 : </span> <span id='author-prov'>"+htmlEscape(author.city)+"</span></div>");
					authorinfo.append("<div><span>State/Province	 : </span> <span id='author-prov'>"+htmlEscape(author.province)+"</span></div>");
					authorinfo.append("<div><span>Country	 : </span> <span id='author-cnty'>"+htmlEscape(author.country)+"</span></div>");
					authorinfo.append("<div><span>Postal		 : </span> <span id='author-pstl'>"+htmlEscape(author.postal)+"</span></div>");
					authorinfo.append("<div><span>Phone		 : </span> <span id='author-phone'>"+htmlEscape(author.phone)+"</span></div>");
					authorinfo.append("<div><span>Gender		 : </span> <span id='author-gen'>"+((author.gender=='M')?"Male":"Female")+"</span></div>");
					authorinfo.append("<div><span>Shirtsize	 : </span> <span id='author-shsz'>"+author.shirtsize+"</span></div>");
					authorinfo.append("<div><span>Role	 : </span> <span id='author-shsz'>"+author.role+"</span></div>");
					authorinfo.append("<div><span>Dietneeds	 : </span> <span id='author-dtnds'>"+htmlEscape(author.dietneeds)+"</span></div>");
					authorinfo.append("<div><span>Date of Birth	 : </span> <span id='author-dtnds'>"+author.dob+"</span></div>");
					authorinfo.append("<div><span>Qualification	 : </span> <span id='author-dtnds'>"+htmlEscape(author.qualification)+"</span></div>");
					var authorimg = $(document.createElement('div')).attr("id", 'authorimg').attr("class","authorimg");

					if(author.fileext+'' !='null' && $.trim(author.fileext)!='' && author.fileext !=undefined){
						 /*var oImg=document.createElement('img');
						oImg.setAttribute('src',"${pageContext['request'].contextPath}/images/upload/"+author.id+"."+author.fileext);
						oImg.setAttribute('alt', 'na');
						 oImg.setAttribute('height', '100px');
						 oImg.setAttribute('width', '100px');	*/
						var oImg= "<img id='img32' name='img32' alt='na' width='100' height='100' src='data:image/"+author.fileext+";base64,"+author.photo+"'/>";
							authorimg.append(oImg);
						 //authorimg.append(oImg);
					 }else{
						 var oImg=document.createElement('img');
						 oImg.setAttribute('src',"${pageContext['request'].contextPath}/images/nophoto.jpg");
						 oImg.setAttribute('alt', 'na');
						 oImg.setAttribute('height', '100px');
						 oImg.setAttribute('width', '100px');
						 authorimg.append(oImg);
					 }

					body.append(authorinfo);
					body.append(authorimg);
					modal.append(body);

					var foot = $(document.createElement('div')).attr("id", 'modal-ftr');
					modal.append(foot);

					if (!$.browser.msie) {
						head.corner("5px top");
						foot.corner("5px bottom");
					}
					return modal;
				}
				var theadtag = $(document.createElement('thead'))
				var trtag = $(document.createElement('tr'));
				trtag.append("<th>Username</th><th>Name</th><th>Email ID</th><th>Phone No</th><th>Gender</th><th>City</th><th>State/Province</th><th>Country</th><th>Shirt Size</th><th>Role</th>");
				theadtag.append(trtag);
				attendees.append(theadtag);
				for(var index in data){
					var trtag = $(document.createElement('tr'));
					trtag.append("<td><a href=\"${pageContext['request'].contextPath}/roles?accountid="+data[index].accountID+"\">"+htmlEscape(data[index].username)+"</a></td>");
					var tdtag = $(document.createElement('td')).attr('class','model-author-links');
					tdtag.append(""+htmlEscape(data[index].name)+"");
					tdtag.bind("click", function() {
						var obj = {};
						obj.name = htmlUnEscape($.trim(this.innerHTML));
						$.getJSON("${pageContext['request'].contextPath}/author/details.json", obj, function(data) {
							if(data.name!= undefined && data.email != undefined){
								$.modal(authorzoom(data), {
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
					trtag.append(tdtag);
					trtag.append("<td><a href='mailto:"+htmlEscape(data[index].email)+"'>"+htmlEscape(data[index].email)+"</a></td>");
					trtag.append("<td>"+htmlEscape(data[index].phone)+"</td>");
					trtag.append("<td>"+((data[index].gender=='M')?"Male":"Female")+"</td>");
					trtag.append("<td>"+htmlEscape(data[index].city)+"</td>");
					trtag.append("<td>"+htmlEscape(data[index].province)+"</td>");
					trtag.append("<td>"+data[index].country+"</td>");
					trtag.append("<td>"+data[index].shirtsize+"</td>");
					trtag.append("<td>"+data[index].role+"</td>");
					attendees.append(trtag);
				 }
				$('#attendees').dataTable({
					"sScrollX": "100%",
					"sScrollY": "400px",
					"bPaginate":false
				});
			});
			});
		</script>
	</head>
	<body>
		<jsp:include page="/WEB-INF/views/includes/header.jsp"/>
		<h2 class="pageheading">Attendees</h2>
		<div id="content-d">
			<div id="downloadlink">
				<b><a href="${pageContext['request'].contextPath}/attendee/attendees.csv"> <img src="${pageContext['request'].contextPath}/images/csvicon.gif" alt="No Icon"/>Download</a></b>
			</div>
			<table id="attendees">
			</table>
			<div class="clearfix church-logo">The Church of Jesus Christ of Latter-day Saints</div>
		</div>
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
