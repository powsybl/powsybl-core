/*
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
angular.module('iteslaDirectives', []).
        directive('busyCoresChart', function() {

    return {
        restrict: 'E',
        template: '<div/>',
        replace: true,

        link: function(scope, element, attrs) {
                //DOM elements
            var parentElement = d3.select(element[0]),
                svg = parentElement.append("svg"),
                graph = svg.append("g"),
                xAxisElt = graph.append("g")
                    .attr("class", "x axis"),
                xAxisLegendElt = xAxisElt.append("text")
                    .style("text-anchor", "end")
                    .text("Time"),
                yAxisElt = graph.append("g")
                    .attr("class", "y axis"),
                yAxisLegendElt = yAxisElt.append("text")
                    .attr("dy", ".71em")
                    .style("text-anchor", "end")
                    .text("Busy cores"),
                seriesAreaElt = graph.append("path")
                    .attr("class", "area"),
                seriesLineElt = graph.append("path")
                    .attr("class", "line")
                    .style('marker-start', "url(#circle-symbol)")
                    .style('marker-mid', "url(#circle-symbol)")
                    .style('marker-end', "url(#circle-symbol)"),
                //D3.js objects
                parseDate = d3.time.format("%d/%m/%Y %H:%M:%S").parse,
                x = d3.time.scale(),
                y = d3.scale.linear(),
                xAxis = d3.svg.axis()
                    .scale(x)
                    .orient("bottom")
                    .ticks(6)
                    .tickFormat(d3.time.format("%H:%M:%S")),
                yAxis = d3.svg.axis()
                    .scale(y)
                    .orient("left"),
                area = d3.svg.area()
                    .x(function(d) { return x(d.date); })
                    .y1(function(d) { return y(d.busyCores); }),
                line = d3.svg.line()
                    .x(function(d) { return x(d.date); })
                    .y(function(d) { return y(d.busyCores); }),
                //locals
                margin = {top: 20, right: 20, bottom: 30, left: 30},
                width,
                height,
                resize = function () {
                    width = parseInt(parentElement.style("width")) - margin.left - margin.right;
                    height = parseInt(parentElement.style("height")) - margin.top - margin.bottom;
                    svg.attr("width", width + margin.left + margin.right);
                    svg.attr("height", height + margin.top + margin.bottom);
                    graph.attr("transform", "translate(" + margin.left + "," + margin.top + ")");
                    xAxisElt.attr("transform", "translate(0," + height + ")");
                    xAxisLegendElt.attr("x", width)
                    xAxisLegendElt.attr("y", -6);
                    yAxisLegendElt.attr("transform", "rotate(-90)");
                    yAxisLegendElt.attr("y", 6);
                    x.range([0, width]);
                    y.range([height, 0]);
                    area.y0(height);
                    redraw();
                },
                series,
                redraw = function () {
                    if(series === undefined) {
                        return;
                    }

                    // update x and y domain with new data
                    x.domain(d3.extent(series.values, function(d) { return d.date; }));
                    y.domain([0, series.availableCores]);

                    // redraw axis
                    xAxisElt.call(xAxis);
                    yAxisElt.call(yAxis);

                    // redraw series
                    seriesAreaElt.datum(series.values)
                            .attr("d", area);

                    seriesLineElt.datum(series.values)
                            .attr("d", line);
                };
            
            svg.append('svg:defs').append('svg:marker')
                .attr('id', 'circle-symbol')
                .attr('markerWidth', 5)
                .attr('refX', 3)
                .attr('markerHeight', 5)
                .attr('refY', 3)
                .append('svg:circle')
                    .attr('cx', 3)
                    .attr('cy', 3)
                    .attr('r', 1.5)
                    .attr('class', 'marker');

            attrs.$observe('series', function(value) {
                if (value.length === 0) {
                    return;
                }
                series = angular.fromJson(value);
                // parse dates
                series.values.forEach(function(d) { d.date = parseDate(d.date); });
                // and redraw
                redraw();
            });

            d3.select(window).on('resize', resize);
            
            resize();
        }
    };
});


