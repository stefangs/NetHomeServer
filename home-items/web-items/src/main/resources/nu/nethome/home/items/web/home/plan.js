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


function callAjaxAction(item, action, attribute)
{
if (window.XMLHttpRequest)
  {// code for IE7+, Firefox, Chrome, Opera, Safari
  xmlhttp=new XMLHttpRequest();
  }
else
  {// code for IE6, IE5
  xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
  }
xmlhttp.onreadystatechange=function()
  {
  if (xmlhttp.readyState==4 && xmlhttp.status==200)
    {
	var res = new Array();
	res = xmlhttp.responseText.split(',');
    document.getElementById(res[0]).innerHTML=res[1];
    }
  }
  var url = "/home?a=ajax&name=" + escape(item) + "&action=" + escape(action) + "&attribute=" + escape(attribute);
  xmlhttp.open("GET",url,true);
  xmlhttp.send();
}
var mapMaker = {
	offsetX: -16, // tooltip X offset
	offsetY: 16,  // tooltip Y offset
	element: null,
	DLs:     false,
	DTs:     false,
	DDs:     false,
	Closes:   false,
	on:      false,
	/* constructor - sets events */
	init: function(){
		var i=0;
		var ii=0;
		var currentLocation = 0;
		mapMaker.DLs = document.getElementsByTagName('dl');
		mapMaker.DTs = document.getElementsByTagName('dt');
		mapMaker.DDs = document.getElementsByTagName('dd');
		mapMaker.Closes = document.getElementsByClassName('close');
		// only loop thru items once
		if( mapMaker.on == false ){
			//loop through each DL on page
			while (mapMaker.DLs.length > i) {
				//only affect DLs with a class of 'plan'
				if (mapMaker.DLs[i].className == 'plan'){
					//strip whitespace
					mapMaker.stripWhitespace(mapMaker.DLs[i]);
					mapMaker.stripWhitespace(mapMaker.DTs[i]);
					mapMaker.stripWhitespace(mapMaker.DDs[i]);
					// loop thru all DT elements
					while (mapMaker.DTs.length > ii){
						// set the link for the current DT
						currentLocation = mapMaker.DTs[ii].firstChild;
						// add events to links
						mapMaker.addEvt(currentLocation,'mouseover',mapMaker.showTooltip);//displays tooltip on mouse over
						mapMaker.addEvt(currentLocation,'focus',mapMaker.showTooltip);//display tooltip on focus, for added keyboard accessibility
						mapMaker.addEvt(currentLocation,'blur',mapMaker.hideTooltip);//hide tooltip on focus, for added keyboard accessibility
						ii++;
					};
					ii=0;
				};
				i++;
			};
			mapMaker.on = true;
			ii=0;
			//loop through close elements and assign event to close link
			while (mapMaker.Closes.length > ii){
				currentLocation = mapMaker.Closes[ii].firstChild;
				mapMaker.addEvt(currentLocation,'click',mapMaker.hideTooltip);//hide tooltip on click of close button
				ii++;
			};
			
		};
	},
	/* SHOW TOOLTIP */
	showTooltip: function() {
		var evt = this;
		var i = 0;
		mapMaker.hideTooltip();
		//Update the attribute value of the Item. Find the Item and attribute name via the ID
		
		if (evt.parentNode.id != null) {
			var nameAndAtt = new Array();
			nameAndAtt = evt.id.split(',');
			callAjaxAction(nameAndAtt[0], '', nameAndAtt[1]);
		}
		
		//Find DD to display - based on currently hovered anchor move to parent DT then next sibling DD
		var objid = evt.parentNode.nextSibling;
		mapMaker.element = objid;//set for the hideTooltip
		//get width and height of background map
		var mapWidth  = objid.parentNode.offsetWidth;
		var mapHeight = objid.parentNode.offsetHeight;
		//get width and height of the DD
		var toopTipWidth = objid.offsetWidth;
		var toopTipHeight = objid.offsetHeight;
		//figure out where tooltip should be places based on point location
		var newX = evt.offsetLeft + mapMaker.offsetX;
		var newY = evt.offsetTop + mapMaker.offsetY;
		//check if tooltip fits map width 
		if ((newX + toopTipWidth) > mapWidth) {
			objid.style.left = newX-toopTipWidth-24 + 'px';
		} else {
			objid.style.left = newX + 'px';
		};
		//check if tooltip fits map height 
		if ((newY + toopTipHeight) > mapHeight) {
			objid.style.top = newY-toopTipHeight-14 + 'px';
		} else {
			objid.style.top = newY + 'px';
		};
	},
	/* HIDE TOOLTIP */
	hideTooltip: function() {
		if (mapMaker.element != null) {
			mapMaker.element.style.left = '-9999px';
		};
	},
	addEvt: function(element, type, handler) {
		// assign each event handler a unique ID
		if (!handler.$$guid) handler.$$guid = mapMaker.addEvt.guid++;
		// create a hash table of event types for the element
		if (!element.events) element.events = {};
		// create a hash table of event handlers for each element/event pair
		var handlers = element.events[type];
		if (!handlers) {
			handlers = element.events[type] = {};
			// store the existing event handler (if there is one)
			if (element["on" + type]) {
				handlers[0] = element["on" + type];
			};
		};
		// store the event handler in the hash table
		handlers[handler.$$guid] = handler;
		// assign a global event handler to do all the work
		element["on" + type] = mapMaker.handleEvent;
	},
	handleEvent: function(event) {
		var returnValue = true;
		// grab the event object (IE uses a global event object)
		event = event || mapMaker.fixEvent(window.event);
		// get a reference to the hash table of event handlers
		var handlers = this.events[event.type];
		// execute each event handler
		for (var i in handlers) {
			this.$$handleEvent = handlers[i];
			if (this.$$handleEvent(event) == false) {
				returnValue = false;
			};
		};
		return returnValue;
	},
	fixEvent: function(event) {
		// add W3C standard event methods
		event.preventDefault = mapMaker.fixEvent.preventDefault;
		event.stopPropagation = mapMaker.fixEvent.stopPropagation;
		return event;
	},
	stripWhitespace: function( el ){
		for(var i = 0; i < el.childNodes.length; i++){
			var node = el.childNodes[i];
			if( node.nodeType == 3 &&
				!/\S/.test(node.nodeValue) ) node.parentNode.removeChild(node);
		}
	}
};
mapMaker.fixEvent.preventDefault = function() {this.returnValue = false;};
mapMaker.fixEvent.stopPropagation = function() {this.cancelBubble = true;};
mapMaker.addEvt.guid = 1;


/* LOAD SCRIPT */
	/* for Mozilla */
		if (document.addEventListener) {
			document.addEventListener("DOMContentLoaded", mapMaker.init, null);
		};
		
	/* for Internet Explorer */
		/*@cc_on @*/
		/*@if (@_win32)
			document.write("<script defer src=ie_onload.js><"+"/script>");
		/*@end @*/
		
	/* for other browsers */
		mapMaker.addEvt( window, 'load', mapMaker.init);
