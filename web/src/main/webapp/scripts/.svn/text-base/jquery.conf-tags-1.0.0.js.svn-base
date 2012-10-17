(function($){
	var defaults = {
		css: {
			tagsTrack: "conf-tags-track",
			tagsTrackHdr: "conf-tags-track-hdr",
			tagsTracks: "conf-tags-tracks",
			tagsHour: "conf-tags-hour",
			tagsHourHdr: "conf-tags-hour-hdr",
			tagsHours: "conf-tags-hours",
			tagsPresenter: "conf-tags-presenter",
			tagsPresenterHdr: "conf-tags-presenter-hdr",
			tagsPresenters: "conf-tags-presenters",
			tagsSec: "conf-tags-sec",
			tagsTag: "conf-tags-tag",
			tagsTagHdr: "conf-tags-tag-hdr",
			tagsTags: "conf-tags-tags"
		}
	}

	var logic = function() {
		return {
			buildSec: function(ts) {
				var logic = this;

				var tHdrDomE = $.eGen.div(this.opt.css.tagsTrackHdr);
				tHdrDomE.append("Tracks");

				tHdrDomE.bind("click", function(){
					$.modal(logic.buildModal("Tracks", logic.loadTracks), {
						minHeight: 388,
						minWidth: 494,
						overlay:80,
						overlayCss: {backgroundColor:"#000"}
					});
				});

				ts.append(tHdrDomE);
				ts.append($.eGen.div(null, this.opt.css.tagsTracks));

				var tHdrDomE = $.eGen.div(this.opt.css.tagsTagHdr);
				tHdrDomE.append("Tags (more...)");
				tHdrDomE.bind("click", function() {
					$.modal(logic.buildModal("Tags", logic.loadTags), {
						minHeight: 388,
						minWidth: 494,
						overlay:80,
						overlayCss: {backgroundColor:"#000"}
					});
				});
				ts.append(tHdrDomE);
				ts.append($.eGen.div(null, this.opt.css.tagsTags));

				var aHdrDomE = $.eGen.div(this.opt.css.tagsPresenterHdr);
				aHdrDomE.append("Presenters (more...)");
				aHdrDomE.bind("click", function() {
					$.modal(logic.buildModal("Presenters", logic.loadAuthors), {
						minHeight: 388,
						minWidth: 494,
						overlay:80,
						overlayCss: {backgroundColor:"#000"}
					});
				});
				ts.append(aHdrDomE);
				ts.append($.eGen.div(null, this.opt.css.tagsPresenters));

				var hHdrDomE = $.eGen.div(this.opt.css.tagsHourHdr);
				hHdrDomE.append("Hours");
				ts.append(hHdrDomE);
				ts.append($.eGen.div(null, this.opt.css.tagsHours));
			},
			buildModal: function(label, loader) {
				var modal = $.eGen.div(null, this.opt.dom.id.modal);

				var head = $.eGen.div(null, this.opt.dom.id.modalHdr);
				var headText = $.eGen.span();
				headText.text(label);
				head.append(headText);
				modal.append(head);

				var body = $.eGen.div(null, this.opt.dom.id.modalBody);
				loader.call(this, body);
				modal.append(body);

				var foot = $.eGen.div(null, this.opt.dom.id.modalFtr);
				modal.append(foot);

				if (!$.browser.msie) {
					head.corner("5px top");
					foot.corner("5px bottom");
				}

				return modal;
			},
			load: function() {
				this.loadTracks($("#" + this.opt.css.tagsTracks), this.tracks.length);
				this.loadTags($("#" + this.opt.css.tagsTags), 5);
				this.loadAuthors($("#" + this.opt.css.tagsPresenters), 7);
				this.loadHours($("#" + this.opt.css.tagsHours));
			},
			loadAuthors: function(authorsDomE, count) {
				var search = this.opt.handle.search;
				if (count) {
					if (count > this.authO.length) {
						count = this.authO.length;
					}
					var list = [];
					for (var a in this.authors) {
						list.push(htmlEscape(a));
					}
					for (var i = 0; i < count; i++) {
						var indx = Math.round(Math.random() * list.length);
						load(list[indx], this);
					}
				} else {
					for (var a in this.authO) {
						load(this.authO[a], this);
					}
				}
				function load(author, thiz) {
					var authorDomE = $.eGen.div(thiz.opt.css.tagsPresenter);
					authorDomE.append(author+"<span>("+thiz.authors[author]+")</span>");
					authorDomE.bind("click", function() {
						var sText = $(this).text();
						$.modal.close();
						var pIndx = sText.indexOf("(");
						var sText = sText.substr(0, pIndx);
						search(sText, "Authors");
					});
					authorsDomE.append(authorDomE);
				}
			},
			loadHours: function(hoursDomE) {
				var search = this.opt.handle.search;
				for (var d in this.times) {
					for (var i = this.opt.time.start; i < this.opt.time.end; i++) {
						var h = (i <= 12)? i : ((i % 13) + 1);
						var _h;
						for(var j = 0; j<2; j++){
							if(j==0){
								_h = h + ":00";
							}else{
								_h = h + ":30";
							}
							if (this.times[d][_h] >= 1) {
								var hourDomE = $.eGen.div(this.opt.css.tagsHour);
								hourDomE.append(this.opt.text.day3[d]+" "+_h+"<span>("+this.times[d][_h]+")</span>");
								hourDomE.bind("click", function() {
									var sText = $(this).text();
									$.modal.close();
									var pIndx = sText.indexOf("(");
									var sText = sText.substr(0, pIndx);
									search(sText, "Times");
								});
								hoursDomE.append(hourDomE);
							}
						}
					}
				}
			},
			loadTags: function(tagsDomE, count) {
				var search = this.opt.handle.search;
				if (count) {
					if (count > this.tagO.length) {
						count = this.tagO.length;
					}
					var list = [];
					for (var t in this.tags) {
						//Hardcoding Presentations and Projects tags
						//if(t=="Projects" || t=="Presentations"){
							//load(t,this);
						//}else{
							list.push(htmlEscape(t));
						//}
					}
					for (var i = 0; i < count; i++) {
						var indx = Math.round(Math.random() * list.length);
						load(list[indx], this);
					}
				} else {
					for (var t in this.tagO) {
						load(this.tagO[t], this);
					}
				}
				function load(tag, thiz) {
					var tagDomE = $.eGen.div(thiz.opt.css.tagsTag);
					tagDomE.append(tag+"<span>("+thiz.tags[tag]+")</span>");
					tagDomE.bind("click", function() {
						var sText = $(this).text();
						$.modal.close();
						var pIndx = sText.indexOf("(");
						var sText = sText.substr(0, pIndx);
						search(sText, "Tags");
					});
					tagsDomE.append(tagDomE);
				}
			},
			loadTracks: function(tracksDomE, count) {
				var search = this.opt.handle.search;
				if (count) {
					var list = [];
					for (var t in this.tracks) {
						list.push(t);
					}

					for (var i = 0; i < count; i++) {
						//var indx = Math.round(Math.random() * list.length);
						load(list[i], this);
					}
				} else {
					for (var t in this.trackO) {
						load(this.trackO[t], this);
					}
				}
				function load(track, thiz) {
					var trackDomE = $.eGen.div(thiz.opt.css.tagsTrack);
					trackDomE.append(track+"<span>("+thiz.tracks[track]+")</span>");
					trackDomE.bind("click", function() {
						var sText = $(this).text();
						$.modal.close();
						var pIndx = sText.indexOf("(");
						var sText = sText.substr(0, pIndx);
						search(sText, "Tracks");
					});
					tracksDomE.append(trackDomE);
				}
			},
			conf: function(data) {
				this.trackO = [];
				this.tracks = {};
				this.tagO = [];
				this.tags = {};
				this.authO = [];
				this.authors = {};
				this.times = {};
				for (var c in data) {
					for (var t in data[c].tags) {
						if (this.tags[data[c].tags[t]]) {
							this.tags[data[c].tags[t]]++;
						} else {
							this.tags[data[c].tags[t]] = 1;
							this.tagO.push(data[c].tags[t]);
						}
					}
					if (data[c].track != undefined) {
						if (this.tracks[data[c].track]) {
							this.tracks[data[c].track]++;
						} else {
							this.tracks[data[c].track] = 1;
							this.trackO.push(data[c].track);
						}
					}
					for (var a in data[c].authors) {
						if (this.authors[data[c].authors[a]]) {
							this.authors[data[c].authors[a]]++;
						} else {
							this.authors[data[c].authors[a]] = 1;
							this.authO.push(data[c].authors[a]);
						}
					}
					//var date = new Date(data[c].start - this.opt.time.zoneOffset);
					var date = new Date(data[c].start);
					var day = date.getDay();
					if (!this.times[day])
						this.times[day] = {};

					var start = this.opt.time.format(data[c].start);
					if (this.times[day][start]) {
						this.times[day][start]++;
					} else {
						this.times[day][start] = 1;
					}
				}
				this.tagO.sort();
				this.trackO.sort();
				this.authO.sort();
			}
		}
	}

	$.fn.confTagsUi = function(options) {
		return this.each(function() {
			var ts = $(this);

			var opt = defaults = $.extend(defaults, options);
			ts.data("opt", opt);

			var func = logic();
			func.opt = opt;
			ts.data("func", func);

			func.buildSec(ts);
		});
	}

	$.fn.confTagsData = function(data) {
		return this.each(function() {
			var ts = $(this);
			var func = ts.data("func");
			func.conf(data);
			func.load();
		});
	}
})(jQuery);
