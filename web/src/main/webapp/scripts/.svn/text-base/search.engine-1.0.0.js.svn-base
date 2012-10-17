var data = null;

var onmessage = function(msg){
	if (typeof(msg.data) == "string") postMessage(search(msg.data));
	else if (!data) data = fix(msg.data);
};

var fix = function(data) {
	for (var i in data) {
		var x = data[i];
		if (!x) continue;
		for(var a in x.authors) {
			x.authors[a] = x.authors[a].toLowerCase();
		}
		if (x.desc) x.desc = x.desc.toLowerCase();
		for(var t in x.tags) {
			x.tags[t] = x.tags[t].toLowerCase();
		}
		if (x.name) x.name = x.name.toLowerCase();
	}
	return data;
}

var search = function(msg) {
	var rVal = "{[";
	var term = msg.toLowerCase().split(" ");
	for (var i in data) {
		var score = 0;
		var x = data[i];
		for (var t in term) {
			for(var a in x.authors) {
				if (x.authors[a].indexOf(term[t]) != -1) score=+2;
			}
			if (x.desc.indexOf(term[t]) != -1) score++;
			for(var tg in x.tags) {
				if (x.tags[tg].indexOf(term[t]) != -1) score+=3;
			}
			if (x.name.indexOf(term[t]) != -1) score+=2;
		}
		if (score > 0) rVal += "{'score':" + score + ",'id':" + x.id + "},";
	}
	rVal = rVal.substring(0, rVal.length - 1);
	return rVal + "]}";
}