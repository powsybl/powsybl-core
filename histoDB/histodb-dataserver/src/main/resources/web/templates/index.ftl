<!--

    Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
    This Source Code Form is subject to the terms of the Mozilla Public
    License, v. 2.0. If a copy of the MPL was not distributed with this
    file, You can obtain one at http://mozilla.org/MPL/2.0/.

-->
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <title>iTesla DataServer</title>
    <meta name="description" content="">
    <meta name="author" content="">

    <!-- Styles -->
    <link rel="stylesheet" type="text/css" href="${restRelativeUrl}/../static/libs/bootstrap-2.0.4/css/bootstrap.min.css" />
    <link rel="stylesheet" href="//code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
    <link rel="stylesheet" type="text/css" href="${restRelativeUrl}/../static/css/dataserver.css" />
    <link rel="stylesheet" type="text/css" href="${restRelativeUrl}/../static/css/itesla.css" />
</head>

<body>

<div class="navbar navbar-fixed-top">
    <div class="navbar-inner">
        <a class="brand" href="#">DATAserver</a>
        <ul class="nav">
            <li class="active"><a href="${restRelativeUrl}">Data</a></li>
            <li><a href="${restRelativeUrl}/../static/doc.html">Doc</a></li>
        </ul>
    </div>
</div>

<div class="sourceList"></div>
<div class="main">


    <div class="content">
         <i>Please select a data source on the left</i>
    </div>
</div>

<script type="text/template" id="resourceTabsTemplate">
    <ul class="nav nav-tabs " id="mainTabs">
        <li id="dataSourceTitle">${resourceRelativeUrl} >></li>
        <li><a id="statusLink" href="${restRelativeUrl}/${resourceRelativeUrl}">Overview</a></li>
        <li><a id="metadataLink" href="${restRelativeUrl}/${resourceRelativeUrl}/metadata">Metadata</a></li>
        <li><a id="dataLink" href="${restRelativeUrl}/${resourceRelativeUrl}/data?start=0&count=10&headers=true">Data</a></li>
        <li><a id="statsLink" href="${restRelativeUrl}/${resourceRelativeUrl}/stats?start=0&count=100">Stats</a></li>
    </ul>
</script>

<script type="text/template" id="dataContent">
    <div class="query">
        <span class="dataUrl"></span>?<input id="dataQuery"><br/>

    </div>
    <div class="queryResult"></div>
</script>

<script type="text/template" id="metadataContent">
    <div class="metadataContent"></div>
</script>

<script type="text/template" id="overviewContent">
    <div class="overviewContent">
        <table>
            <tr><td>Status</td><td class="status"></td></tr>
            <tr><td>Load time</td><td class="loadtime"></td></tr>
            <tr><td>Record count</td><td class="rowCount"></td></tr>
            <tr><td>Column count</td><td class="colCount"></td></tr>
        </table>

        <h4>Actions</h4>
        <button id="saveConfig">Save Config</button>
        <button id="reloadConfig">Reload Config</button>
        <button id="rebuildMetadata">Rebuild Metadata</button>
        <button id="clearData">Clear Data</button>

        <h4>Harvest dir</h4>
        <form method="POST" action="./itesla" enctype="application/x-www-form-urlencoded" onsubmit="this.action = PEPITE.DATASERVER.urlParts.restRelativeUrl+'/'+$('#dbPath').val()+'/itesla'">
            DB: <input id="dbPath" size="100" value="/DB/Table/Collection"><br/>
            Dir: <input name="dir" size="100"><br/>
            <input type="submit"/>
        </form>

        <!--
        <h4>Set reference network</h4>
        <form method="POST" action="./itesla/referenceCIM" onsubmit="this.action = PEPITE.DATASERVER.urlParts.restRelativeUrl+'/'+$('#dbPath').val()+'/itesla'">
            CIM path: <input id="referenceCimPath" size="100" value=""><br/>
        </form>
        -->
    </div>
</script>

<script type="text/template" id="treePanel">
    <div class="edgeAction">
        <i class="action_move icon-move"></i>
        <i class="action_close icon-remove"></i></div>
    <div class="tree-graph" style="overflow: hidden;"></div>
    <!--
    <div class="tree-toolbar btn-toolbar">
        <div class="btn-group">
            <button type="button" class="btn reset-btn">Reset</button>
            <button type="button" class="btn zoom-in-btn"><i class="icon-zoom-in"></i></button>
            <button type="button" class="btn zoom-out-btn"><i class="icon-zoom-out"></i></button>
        </div>
    </div>
    <div class="tree-stats"></div>
    -->
</script>

<script src="${restRelativeUrl}/../static/libs/jquery/jquery-1.7.1.min.js"></script>
<script src="//code.jquery.com/ui/1.11.4/jquery-ui.js"></script>
<script src="${restRelativeUrl}/../static/libs/underscore-1.3.3/underscore-min.js"></script>
<script src="${restRelativeUrl}/../static/libs/bootstrap-2.0.4/js/bootstrap.js"></script>
<script src="${restRelativeUrl}/../static/js/dataserver.js"></script>
<script src="${restRelativeUrl}/../static/js/backbone.js"></script>
<script src="${restRelativeUrl}/../static/js/tree.js"></script>
<script src="${restRelativeUrl}/../static/js/itesla.js"></script>
<script>
    $(document).ready( function() {

        var urlParts = {
            restAbsoluteUrl: '${restAbsoluteUrl}',
            restRelativeUrl: '${restRelativeUrl}',
            resourceRelativeUrl: '${resourceRelativeUrl}',
            subResource: '${subResourceUrl}',
            storeId: '${storeId!}',
            dataId: '${dataId!}'
        }


        PEPITE.DATASERVER.init(
                urlParts,
                function() {
                    PEPITE.DATASERVER.display(urlParts.resourceRelativeUrl, urlParts.subResource)
                })

        ITESLA.init();

    });
</script>
</body>

</html>