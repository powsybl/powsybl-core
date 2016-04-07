/*
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
'use strict';

/* App Module */

var app = angular.module('iteslaApp', [
    'ngRoute',
    'iteslaServices',
    'iteslaControllers',
    'iteslaDirectives',
    'iteslaFilters'
]);

app.config(['$routeProvider',
    function($routeProvider) {
        $routeProvider.
                when('/workflow', {
                    templateUrl: 'partials/onlineWorkflow.html',
                    controller: 'OnLineWorkflowCtrl'
                }).
                when('/computationplatform', {
                    templateUrl: 'partials/computationplatform.html',
                    controller: 'OnLineWorkflowCtrl'
                }).
                when('/forecastResults', {
                    templateUrl: 'partials/forecastResults.html',
                    controller: 'OnLineWorkflowCtrl'
                }).
                when('/workflowList', {
                    templateUrl: 'partials/workflows.html',
                    controller: 'OnLineWorkflowCtrl'
                }).
                otherwise({
                    redirectTo: '/workflowList'
                });
    }]);