/*
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
angular.module('iteslaDirectives', []).
        directive('realTimeAreaChart', function() {
    return {
        restrict: 'E',

        template: '<div/>',
        replace: true,

        link: function(scope, element, attrs) {

            var margin = {top: 20, right: 20, bottom: 20, left: 40},
                width = attrs.width - margin.left - margin.right,
                height = attrs.height - margin.top - margin.bottom;

            var svg = d3.select(element[0])
                    .append("svg")
                        .attr("width", width + margin.left + margin.right)
                        .attr("height", height + margin.top + margin.bottom)
                    .append("g")
                        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

            svg.append("defs")
                    .append("clipPath")
                        .attr("id", "clip")
                    .append("rect")
                        .attr("width", width)
                        .attr("height", height);

            attrs.$observe('points', function(value) {

                if (value.length === 0) {
                    return;
                }
                var points = angular.fromJson(value);

                var x = d3.scale.linear()
                    .domain([0, points.length - 1])
                    .range([0, width]);

                var y = d3.scale.linear()
                    .domain([0, Math.max.apply(Math, points)])
                    .range([height, 0]);

                var area = d3.svg.area()
                    .x(function(d, i) { return x(i); })
                    .y0(height)
                    .y1(function(d, i) { return y(d); });

                svg.append("g")
                    .attr("class", "x axis")
                    .attr("transform", "translate(0," + y(0) + ")")
                    .call(d3.svg.axis().scale(x).orient("bottom"));

                var yAxis = d3.svg.axis().scale(y).orient("left");

                svg.append("g")
                    .attr("class", "y axis")
                    .call(yAxis);

                svg.append("g")
                        .attr("clip-path", "url(#clip)")
                    .append("path")
                        .datum(points)
                        .attr("class", "area")
                        .attr("d", area)
                        .style("fill", "steelblue");
            });
        }
    };
});


