function htmlEscape(str) {
	   return String(str)
	           .replace(/&/g, '&amp;')
	           .replace(/"/g, '&quot;')
	           .replace(/'/g, '&#39;')
	           .replace(/</g, '&lt;')
	           .replace(/>/g, '&gt;');
}

function htmlUnEscape(str){
	var htmlNode = document.createElement("DIV");
	   htmlNode.innerHTML = str;
	   if (htmlNode.innerText !== undefined)
	      return htmlNode.innerText; // IE
	   return htmlNode.textContent; // FF
}

function getGMTTime(dd) {
	return new Date(Date.UTC(dd.getFullYear(), dd.getMonth(), dd.getDate(), dd.getHours(), dd.getMinutes(), 0,0));
}

function getDateObject(sdate,localtime) {
	var s_date = sdate.split('-');
	var hr = localtime.split(':');
	var minutes = (hr[1].split(" "))[0];
	var hours = parseFloat(hr[0]);
	if ((hr[1].split(' '))[1]=='pm') {
		if(hours!=12){
			hours +=12;
		}
	} else {
		//am
		if (hours==12) {
			hours = 0;
		}
	}
	return new Date(s_date[0],(s_date[1]-1),s_date[2], hours, minutes);
}

function convertAM_PM(hours,minutes){
	if( minutes<10){
		minutes = "0"+minutes;
	}
	if (hours>=12){
		hours = (hours-12);
		if (hours==0)
			hours=12;
		return((hours<10)?("0"+hours):hours)+":"+minutes+" pm";
	}else{
		if(hours==0)
			hours=12;
		return ((hours<10)?("0"+hours):hours)+":"+minutes+" am";
	}
}
function convertConferenceTimezonetoGMT(dateobj,conftimezone)
{
	return getGMTTime(dateobj)-parseInt(conftimezone*60*60*1000);
}
function convertGMTtoConferenceTimezone(time,conftimezone){
	var datetime = new Date();
	datetime.setTime(parseInt(time)+parseInt(conftimezone*60*60*1000));
	datetime.setMinutes(datetime.getMinutes()+datetime.getTimezoneOffset());
	if(datetime.getMinutes()==59 || datetime.getMinutes()==44 || datetime.getMinutes()==29 || datetime.getMinutes()==14 ){
		datetime.setMinutes(datetime.getMinutes()+1);
	}
	return datetime;
}

function setGMTDateObject(sdate,localtime) {
	var s_date = sdate.split("-");
	var hr = localtime.split(":");
	var minutes = (hr[1].split(" "))[0];
	var hours = parseFloat(hr[0]);
	if ((hr[1].split(" "))[1].toLowerCase()=="pm") {
		if (hours!=12) {
			hours +=12;
		}
	} else {
		//am
		if(hours==12) {
			hours = 0;
		}
	}
	var d = new Date();

	d.setUTCFullYear(s_date[0]);
	d.setUTCMonth((s_date[1]-1));
	d.setUTCDate(s_date[2]);
	d.setUTCHours(hours);
	d.setUTCMinutes(minutes);
	return d;
	//return  new Date.UTC(s_date[2],(s_date[0]-1),s_date[1], hours, minutes);
}

function timedifflength(start,end,track_len,timestartflag) {
	if (start.val()!="" && end.val()!="") {
		var time = (setGMTDateObject("1-1-1",end.val())-setGMTDateObject("1-1-1",start.val()))/(1000*60);
		if (time<0) {
			if (timestartflag) {
				var stime = start.val();
				end.val(stime);
			} else {
				var etime = end.val();
				start.val(etime);
			}
			time = 0;
		}
		if (track_len != null)
			track_len.val(time.toFixed()+"min");
	}
}

function convertArraytoStringBySemicolon(valueArray){
	var str= "";
	for (var i=0;i<valueArray.length;i++){
		str+=valueArray[i]+"; ";
	}
	return str.substring(0,str.length-2);
}

function formatTime(x){
	var date = new Date(x);
	var hours = date.getHours();
	var ampm;
	if(hours >= 12){
		ampm = "pm";
		hours = hours - 12;
	} else {
		ampm = "am";
	}
	if (hours == 0) {
		hours = 12;
	}
	var min = date.getMinutes();
	if (min == 0)
		min = min + "0";
	var f_d = hours + ":" + min + " " + ampm;
	return f_d;
}
