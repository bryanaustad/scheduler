(function($){
	var defaults = {
		css: {
			searchCenter: "conf-search-center",
			searchHdr: "conf-search-hdr",
			searchLeft: "conf-search-left",
			searchRight: "conf-search-right",
			searchSec: "conf-search-sec",
			resultSec: "conf-result-sec"
		},
		id: {
			searchInp: "conf-search-inp"
		}
	}

	var courses = {};

	$.fn.sortSearchData = function(data) {
		return this.each(function() {
			var results = $(this).children("." + defaults.css.resultSec);
			results.data("data", data);
		});
	}

	$.fn.sortSearchShowAll = function() {
		var results = $(this).children("." + defaults.css.resultSec);
		var data = results.data("data");
		var opt = results.data("opt");

		for (var i in data) {
			if (!data[i]) alert("Server Fail - Check database connection");
			results.append(search.result.create(data[i], opt));
		}
	}

	$.fn.sortSearchUi = function(options) {
		var opt = defaults = $.extend(defaults, options);
		search.opt = opt;

		function newSearch(ss) {
			var s = $.eGen.div(opt.css.searchSec);
			s.append($.eGen.span(opt.css.searchLeft));

			var input = $.eGen.input.text(opt.id.searchInp, "Search...");
			var close = $.eGen.span(opt.css.searchRight);
			s.append($.eGen.span(opt.css.searchCenter).append(input));
			s.append(close);

			input.bind("keyup", function() {
				if (search.timer) clearTimeout(search.timer);
				if($(this).val()==""){
					showAll();
				}else{
				search.timer = setTimeout(search.go, 250);
				search.elements = {
					input: $(this),
					ss: ss,
					type: "All"
				}
				}
			});
			close.bind("click", function() {
				input.val('');
				showAll();
			});

			return s;
		}

		function showAll() {
			search.result.showAll();
		}

		function newResults() {
			var r = $.eGen.div(opt.css.resultSec);
			r.data("opt", opt);
			return r;
		}

		function newHeader() {
			var hdr = $.eGen.div(opt.css.searchHdr);
			hdr.text("Sessions");
			return hdr;
		}

		return this.each(function() {
			var ss = $(this);
			ss.append(newHeader());
			ss.append(newSearch(ss));
			ss.append(newResults(ss));
		});
	}

	$.fn.sortSearchUpdate = function(course) {
		return this.each(function() {
			var results = $(this).children("." + defaults.css.resultSec);
			var data = results.data("data");
			var opt = results.data("opt");

			search.result.update(course, opt);
		});
	}

	$.fn.sortSearchFor = function(sText, type) {
		return this.each(function() {
			var results = $(this).children("." + defaults.css.resultSec);
			var data = results.data("data");
			var opt = results.data("opt");

			var input = $("#" + opt.id.searchInp);
			input.val("\"" + sText + "\"");

			search.elements = {
				input: input,
				ss: $(this),
				type: type
			}
			search.go();
		});
	}
	var search = {
		data: null,
		fix: function(data) {
			var dataCopy = $.extend(true, [], data);  //Deep copy using JQuery
			for (var i in data) {
				var x = dataCopy[i];  //Shallow copy
				if (!x) continue;
				for(var a in x.authors) {
					x.authors[a] = x.authors[a].toLowerCase();
				}
				if (x.desc) x.desc = x.desc.toLowerCase();
				for(var t in x.tags) {
					x.tags[t] = x.tags[t].toLowerCase();
				}
				if (x.name) x.name = x.name.toLowerCase();
				if(x.room){
					if (x.room) x.room = x.room.toLowerCase();
				}
				if (x.start) {
					var o = this.opt;
					var date = new Date(x.start + o.time.zoneOffset);
					x.date = (o.date.format(date) + " " + o.time.format(x.start)).toLowerCase();
				}
			}
			return dataCopy;
		},
		getTerms: function(sText) {
			sText = sText.toLowerCase();  //sText == search text
			var terms = [];
			var splits = sText.split("\"");
			if (splits.length % 2 == 0) {  //Check to make sure quotes around terms are matched
				return false;
			}
			sText = "";
			for (var i in splits) {  //Separate the search terms from the quotes and store the terms
				if (i % 2 == 1) {
					terms.push(splits[i]);
				}
				else {
					var nonQ = splits[i].split(" ");
					terms = terms.concat(nonQ);
				}
			}
			for (var i = 0; i < terms.length; i++) {
				if (terms[i].length < 2) {
					terms.splice(i, 1);
					i--;
				}
			}
			return terms;
		},
		go: function() {
			var ss = search.elements.ss;
			var input = search.elements.input;
			var str = input.val();
			var filtered = input.data("filtered");
			var data = ss.children("." + defaults.css.resultSec).data("data");

			if (str.length < 3) {  // Wait until third character is entered before searching
				if (filtered) {
					search.result.showAll(data);
					filtered = false;
				}
				else return;
			}

			var searchEngine;// = new Worker("scripts/search.engine-1.0.0.js");
			if (!searchEngine) searchEngine = search;
			searchEngine.onmessage = function(msg) {
				var results = ss.children("." + defaults.css.resultSec);
				if (!msg) return;
				var rData = eval(msg.data);
				if (rData) {
					rData.sort(function(a, b) {
						return b.score - a.score;
					});
					for (var c in courses) {
						courses[c].hide();
					}
					for (var r in rData) {
						var c = courses[rData[r].id];
						if (c) {
							results.append(c);
							c.show();
						}
					}
				}
			}
			searchEngine.postMessage(data);
			searchEngine.postMessage({type: search.elements.type})
			searchEngine.postMessage(str);
			input.data("filtered", true);
		},
		postMessage: function(msg) {
			if (typeof(msg) == "string") {
				var ret = {data: this["search" + this.type](msg)};
				this.onmessage(ret);
			}
			else {
				if (msg.type) {
					this.type = msg.type;
				}
				else if (!this.data) {
					this.data = this.fix(msg);
				}
			}
		},
		result: {
			action: function(course, opt) {
				var actions = $.eGen.div("actions");
				if (!course.general && (course.start != undefined)){
					if (course.registered) {
						actions.bind("click", function() {
							opt.handle.unselect(course, function() {});
						});
					} else if (course.tentative) {
						actions.bind("click", function() {
							opt.handle.confirm(course, function() {});
						});
					} else {
						actions.bind("click", function() {
							opt.handle.select(course, function() {});
						});
					}
				}
				return actions;
			},
			build: function(course, opt, courseDomE) {
				courseDomE.append(this.action(course, opt));
				courseDomE.append(this.title(course, opt));
				courseDomE.append(this.description(course, opt));
				courseDomE.append(this.tags(course));

				if (course.general){
					courseDomE.addClass("general");
				} else {
					if (course.registered) {
						courseDomE.addClass("registered");
					}
					else if (course.tentative) {
						courseDomE.addClass("tentative");
					}
				}
			},
			create: function(course, opt) {
				var courseDomE = $.eGen.div("course");
				courses[course.id] = courseDomE;
				this.build(course, opt, courseDomE);
				return courseDomE;
			},
			description: function(course, opt) {
				var desc = $.eGen.div("desc");
				desc.text(course.desc.substring(0, 200) + "...");
				desc.bind("click", function() {
					opt.handle.zoom(course, null);
				});
				return desc;
			},
			showAll: function() {
				for (var c in courses) {
					courses[c].show();
				}
			},
			tags: function(course) {
				var tags = $.eGen.div("tags");
				for (var t in course.tags) {
					var tag = $.eGen.span("tag");
					tag.text(course.tags[t]);
					tag.bind("click", function() {
						var sText = $(this).text();
						hooks.handle.search(sText, "Tags");
					});
					tags.append(tag);
					tags.append(" &nbsp; ");
				}
				return tags;
			},
			title: function(course, opt) {
				var title = $.eGen.div("title");
				title.text((course.name));
				if (course.start != undefined && course.room != undefined)
					title.append("<span>" + opt.text.day[new Date(course.start).getDay()] + " " + opt.time.format(course.start) + " - " + htmlEscape(course.room) + "</span>")
				title.bind("click", function() {
					opt.handle.zoom(course, null);
				});
				return title;
			},
			update: function(course, opt) {
				var courseDomE = courses[course.id];
				courseDomE.empty();
				courseDomE.removeClass("registered");
				courseDomE.removeClass("tentative");
				this.build(course, opt, courseDomE);
				return courseDomE;
			}
		},
		searchAll: function(msg) {
			var term = this.getTerms(msg);
			if (!term) return false;
			var rVal = "{[";
			for (var i in this.data) {
				var score = 0;
				var x = this.data[i];
				for (var t in term) {
					for(var a in x.authors) {
						if (x.authors[a].indexOf(term[t]) != -1) score=+2;
					}
					if (x.desc.indexOf(term[t]) != -1) score++;
					for(var tg in x.tags) {
						if (x.tags[tg].indexOf(term[t]) != -1) score+=3;
					}
					if(x.room != undefined)
						if (x.room.indexOf(term[t]) != -1) score+=3;
					if (x.name.indexOf(term[t]) != -1) score+=2;
					if(x.date != undefined)
						if (x.date.indexOf(term[t]) != -1) score+=3;
				}
				if (score > 0) rVal += "{'score':" + score + ",'id':'" + x.id + "'},";
			}
			if (rVal.length > 4)
				rVal = rVal.substring(0, rVal.length - 1);
			return rVal + "]}";
		},
		searchAuthors: function(msg) {
			msg = this.getTerms(msg)[0];
			var rVal = "{[";
			for (var i in this.data) {
				for(var a in this.data[i].authors) {
					if (this.data[i].authors[a] == msg) {
						rVal += "{'score':1 ,'id':'" + this.data[i].id + "'},";
						break;
					}
				}
			}
			if (rVal.length > 4)
				rVal = rVal.substring(0, rVal.length - 1);
			return rVal + "]}";
		},
		searchTimes: function(msg) {
			msg = this.getTerms(msg)[0];
			var rVal = "{[";
			for (var i in this.data) {
				if(this.opt.text.day3[new Date(this.data[i].start - this.opt.time.zoneOffset).getDay()] == undefined){
					continue;
				}
				var day = this.opt.text.day3[new Date(this.data[i].start - this.opt.time.zoneOffset).getDay()].toLowerCase();
				var time = this.opt.time.format(this.data[i].start);
				var date = day + " " + time;
				if (msg == date) {
					rVal += "{'score':1,'id':'" + this.data[i].id + "'},";
				}
			}
			if (rVal.length > 4)
				rVal = rVal.substring(0, rVal.length - 1);
			return rVal + "]}";
		},
		searchTags: function(msg) {
			msg = this.getTerms(msg)[0];
			var rVal = "{[";
			for (var i in this.data) {
				for(var tg in this.data[i].tags) {
					if (this.data[i].tags[tg] == msg) {
						rVal += "{'score':1,'id':'" + this.data[i].id + "'},";
						break;
					}
				}
			}
			if (rVal.length > 4)
				rVal = rVal.substring(0, rVal.length - 1);
			return rVal + "]}";
		},
		searchTracks: function(msg) {
			msg = this.getTerms(msg)[0];
			var rVal = "{[";
			for (var i in this.data) {
					var tr;
					if(this.data[i].track != undefined){
						tr = this.data[i].track.toLowerCase();
					}
					if (tr == msg) {
						rVal += "{'score':1,'id':'" + this.data[i].id + "'},";
					}
			}
			if (rVal.length > 4)
				rVal = rVal.substring(0, rVal.length - 1);
			return rVal + "]}";
		}
	}
})(jQuery);

$.eGen = {
	div: function(cls, id) {
		var attrs = (id)? {id: id} : null;
		return this.e("<div>", attrs, cls);
	},
	e: function(tag, attrs, cls) {
		var e = $(tag);
		if (attrs) {
			for (var i in attrs) {
				e.attr(i, attrs[i]);
			}
		}
		if (cls) e.addClass(cls);
		return e;
	},
	input: {
		text: function(id, placeholder) {
			return $.eGen.e("<input>", {id: id, placeholder: placeholder}, null);
		}
	},
	span: function(cls,id) {
		var attrs = (id)? {id: id} : null;
		return this.e("<span>", attrs, cls);
	}
}
