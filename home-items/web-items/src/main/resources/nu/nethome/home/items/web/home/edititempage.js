/*
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome  (http://www.nethome.nu)
 *
 * OpenNetHome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenNetHome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * A couple of plugins for selecting home items and their actions
 * Peter Lagerhem, 2010-11-09
 */
$(document).ready(function() {
 	$('.attributecmd-item').homeItemSimpleSelector({});
 	$(".refsel").click(onItemRefClick);
 	$(".refselsingle").click(onItemSingleRefClick);
});

function onItemSingleRefClick(event) {
    var rows = event.target.parentNode.parentNode.children;
    var result = "";
    var i, itemsField, currentInput;
    for (i = 0; i < rows.length; i += 1) {
        currentInput = rows[i].childNodes[rows[i].childNodes.length - 2];
        if (currentInput.checked && currentInput === event.target) {
            result = rows[i].lastChild.data;
        } else {
            currentInput.checked = false;
        }
    }
    $("#" + event.target.value).get(0).value = result;
}

function onItemRefClick(event) {
    var rows = event.target.parentNode.parentNode.children;
    var result = "";
    var i, isFirst, itemsField, currentInput;
    isFirst = true;
    for (i = 0; i < rows.length; i += 1) {
        currentInput = rows[i].childNodes[rows[i].childNodes.length - 2];
        if (currentInput.checked) {
            result += isFirst ? "" : ",";
            result += rows[i].lastChild.data;
            isFirst = false;
        }
    }
    $("#" + event.target.value).get(0).value = result;
}

function refreshTheActionList(sel,el,url) {
	$.post(url+sel, function(data, textStatus) {
		$(el).empty();
		var obj = $.parseJSON(data);
		if(obj==null)return;
		if(obj.results.length==0) {
			$("<optgroup label='No actions available'></optgroup>")
				.appendTo(el);
		} else {
			$("<optgroup label='Select one action'></optgroup>")
				.appendTo(el);
			$.each(obj.results, function() {
				s="";
				$("<option>"+this.name+"</option>")
					.appendTo(el);
			});
		}
		$(el).change( function(){
		});
	});	
}

(function($) {
	// A simple home item selector
	$.fn.homeItemSimpleSelector = function(options) {
		var container = this;
		var settings = jQuery.extend({
			color : 'transparent'
		// put more defaults here
		}, options);
		return this.each(function() {
			var sec = $(this).next('select');
			$(this).change(function(e){
			// e.stopPropagation();
				refreshTheActionList($(this).val(),sec,"home?a=ajax&r=json&f=getActions&itemid=");
			});
		});		
	};
	
	// A litte more esteatic home item selector
	// To be finalized, or removed eventually
	$.fn.homeItemSelector = function(options) {
		var container = this;
		var settings = jQuery.extend({
			color : 'transparent'
		// put more defaults here
		}, options);

		var cnt = 1;

		return this.each(function() {
					// For every found item, wrap children in span, and make the
					// two known ones clickable
					var _el = this, _ea = $(this).next(), org = $(this);
					_el.id = (typeof _el.id == "undefined" || "string" ? "his_"
							+ cnt++ : _el.id);
					_ea.id = (typeof _ea.id == "undefined" || "string" ? "hia_"
							+ cnt++ : _ea.id);
					$(this)
							.data('original-color',
									$(this).css('background-color'))
							.css('background-color',
									settings.color || '#fff47f')
							.bind(
									'click',
									function() {
										var hiid = $(org).children(
												'.attributecmd-item');
										var hiac = $(org).children(
												'.attributecmd-action');
										refreshList(
												org,
												hiid,
												hiac,
												_el.id,
												"home?a=ajax&r=json&f=getHomeItems",
												"home?a=ajax&r=json&f=getActions&itemid=");
									});
				});
	};
	function hideMe() {
	}
  
})(jQuery);

function refreshList(org,hiid,hiac,id,url,url_action) {
	$.post(url, function(data) {
		var el='#'+id, 
			sb=id+'sb', 
			sba=id+'sba',
			elp=$('<span class="attributecmd-box"></span>').appendTo($(el).parent()); 
//		$(el).empty();
		var obj = $.parseJSON(data);
		if(obj==null)return;
		$(org).fadeTo('slow', 0);
		var oldHeight = $(org).height();
		$(org).height(0);
		var his=$('<select id="'+sb+'"></select>')
			.hide()
			.appendTo(elp)
			.change(function(e){
//				e.stopPropagation();
				var ea=$(this);
				refreshActionList($(this).val(),hiac,sba,url_action)
			});
		if(obj.results.length==0) {
			$("<optgroup label='No items available'></optgroup>")
				.appendTo(his);
		} else {
			$("<optgroup label='Select one home item'></optgroup>")
				.appendTo(his);
			var hiid_text=hiid.html()
			$.each(obj.results, function() {
				s="";
				if(hiid_text==this.name)
					s=" selected='selected'";
				$("<option value='"+this.id+"'"+s+">"+this.name+"</option>")
					.appendTo(his);
			});
		}
		$(his).appendTo(elp).fadeIn();
		var hia=$('<select id="'+sba+'"></select>').hide();
		$(hia).appendTo(elp).fadeIn();
		$('.table_action').height(oldHeight);
		$(his).change();
		$("<img src='web/home/ok.png' />")
			.hide()
			.appendTo(elp)
			.fadeIn()
			.one('click', function() {
				var cmd = $('#'+sb+' :selected').text()
				hiid.html(cmd);
				$(this).parent().parent("input[name$='_c']").html(cmd);
				hiac.html($('#'+sba+' :selected').text());
				// $(elp).empty();
				$(elp).remove();
				$(org).height(oldHeight);
				$(org).fadeTo('slow',1.0);
		});
		$("<img src='web/home/cancel.png' />")
			.hide()
			.appendTo(elp)
			.fadeIn()
			.one('click', function() {
				$(elp).remove();
				$(org).height(oldHeight);
				$(org).fadeTo('slow',1.0);
			});
	});	
}

function refreshActionList(sel,hiac,id,url) {
	$.post(url+sel, function(data) {
		var el='#'+id; 
		$(el).empty();
		var obj = $.parseJSON(data);
		if(obj==null)return;
		var his=$(id).hide();
		if(obj.results.length==0) {
			$("<optgroup label='No actions available'></optgroup>")
				.appendTo(el);
		} else {
			$("<optgroup label='Select one action'></optgroup>")
				.appendTo(el);
			var hiac_text=hiac.html();
			$.each(obj.results, function() {
				s="";
				if(hiac_text==this.name)
					s=" selected='selected'";
				$("<option value='"+this.id+"'"+s+">"+this.name+"</option>")
					.appendTo(el);
			});
		}
		$(his).fadeIn();
		$(el).change( function(){
		});
	});	
}

