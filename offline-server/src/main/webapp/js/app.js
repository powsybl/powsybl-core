/*
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/*global angular: true, jQuery: true*/

/* App Module */

var app = angular.module('iteslaApp', [
    'ngRoute',
    'ngSanitize',
    'ngAnimate',
    'ui.bootstrap',
    'angularjs-dropdown-multiselect',
    'angularBootstrapNavTree',
    'iteslaServices',
    'iteslaControllers',
    'iteslaDirectives'
]);

app.config(['$routeProvider', function ($routeProvider) {
    'use strict';
    $routeProvider.
        when('/workflows', {
            templateUrl: 'partials/workflows.html',
            controller: 'WorkflowListCtrl'
        }).
        when('/computationresources', {
            templateUrl: 'partials/computationresources.html',
            controller: 'ComputationResourcesCtrl'
        }).
        otherwise({
            redirectTo: '/computationresources'
        });
}]);

app.filter('startFrom', function () {
    'use strict';
    return function (input, start) {
        start = parseInt(start, 10);
        return input.slice(start);
    };
});

app.filter('securityRuleIdFilter', function () {
    'use strict';
    return function (securityRuleIds, attributeSet, contingency, label) {
        var noFilter = {test: function () { return true; }},
            filterAttributeSet = attributeSet !== undefined && attributeSet !== null ? new RegExp(attributeSet, 'i') : noFilter,
            filterContingency = contingency !== undefined && contingency !== null ? new RegExp(contingency, 'i') : noFilter,
            filterLabel = label !== undefined && label !== null ? new RegExp(label, 'i') : noFilter;
        return jQuery.grep(securityRuleIds, function (securityRuleId) {
            return filterAttributeSet.test(securityRuleId.attributeSet)
                && filterContingency.test(securityRuleId.securityIndexId.contingencyId)
                && filterLabel.test(securityRuleId.securityIndexId.securityIndexType.label);
        });
    };
});
