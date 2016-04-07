/*
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
var filters=angular.module('iteslaFilters', []);

filters.filter('orderObjectBy', function($log) {
	  return function(items, field, reverse) {
	    var filtered = [];
	    angular.forEach(items, function(item) {
	    filtered.push(item);
	    //$log.debug("  item:  "+item);
	    });
	    
	    filtered.sort(function (a, b) {
	     //$log.debug('a : '+a[field] + "  b:  "+ b[field]);
	      return (a[field] > b[field] ? 1 : -1);
	    });
	    
	    if(reverse) filtered.reverse();
	    return filtered;
	  };
	});
