/*
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
(function ($) {

    this.ITESLA = this.ITESLA || {}

    var treeDiv = $("<div class='tree'></div>").hide().appendTo(document.body)

    this.ITESLA.init = function() {
        PEPITE.DATASERVER.registerCellRenderer(
            new PEPITE.DATASERVER.CellRenderer(
                function (name) {
                    return name == "tree"
                },
                function (value, container, cells, headers) {
                    var algoTypeIdx, contingencyIdIdx, indexTypeIdx
                    _.each(headers, function(value, idx) {
                        if (value == 'algoType') algoTypeIdx = idx
                        if (value == 'contingencyId') contingencyIdIdx = idx
                        if (value == 'indexType') indexTypeIdx = idx
                    })

                    var algoType = cells[algoTypeIdx];
                    var contingencyId = cells[contingencyIdIdx];
                    var indexType = cells[indexTypeIdx];
                    container.append(
                        $("<a>View</a>").click(function () {
                            var treeObj = $.parseJSON(value)
                            treeDiv.show()
                            var decisionTreeView = new DecisionTreeView(treeDiv)
                            decisionTreeView.renderResult(treeObj)
                            treeDiv.find(".edgeAction .action_close").click(
                                function() {
                                    treeDiv.empty()
                                    treeDiv.hide()
                                })
                            treeDiv.draggable({ handle: ".action_move" })

                            treeDiv.resizable()
                        })
                    ).append("&nbsp;").append(
                        $(" <a href='itesla/rules/"+algoType+"/"+contingencyId+"/"+indexType+".cond'>Txt</a>")
                    ).append("&nbsp;").append(
                        $(" <a href='itesla/rules/"+algoType+"/"+contingencyId+"/"+indexType+".json'>JSON</a>")
                    )

                }))

        PEPITE.DATASERVER.registerCellRenderer(
            new PEPITE.DATASERVER.CellRenderer(
                function (name) {
                    return name == "DT"
                },
                function (value, container, cells, headers) {

                    var algoTypeIdx, contingencyIdIdx, indexTypeIdx
                    _.each(headers, function(value, idx) {
                        if (value == 'algoType') algoTypeIdx = idx
                        if (value == 'contingencyId') contingencyIdIdx = idx
                        if (value == 'indexType') indexTypeIdx = idx
                    })

                    var algoType = cells[algoTypeIdx];
                    var contingencyId = cells[contingencyIdIdx];
                    var indexType = cells[indexTypeIdx];

                    var ruleUrl = "itesla/rules/"+algoType+"/"+contingencyId+"/"+indexType;

                    container.append(
                        $("<a>View</a>").click(function () {
                            $.getJSON(ruleUrl+'.json',
                                function(treeData) {
                                    treeDiv.show()
                                    var decisionTreeView = new DecisionTreeView(treeDiv)
                                    decisionTreeView.renderResult(treeData[0].tree)
                                    treeDiv.find(".edgeAction .action_close").click(
                                        function() {
                                            treeDiv.empty()
                                            treeDiv.hide()
                                        })
                                    treeDiv.draggable({ handle: ".action_move" })

                                    treeDiv.resizable()
                                }
                            )

                        })
                    ).append("&nbsp;").append(
                        $(" <a href='"+ruleUrl+".cond'>Txt</a>")
                    ).append("&nbsp;").append(
                        $(" <a href='"+ruleUrl+".json'>JSON</a>")
                    )

                }))



    }
})(jQuery)