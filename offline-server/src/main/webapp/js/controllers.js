/*
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/*global angular: true, window: true*/
'use strict';

/* Controllers */

var controllers = angular.module('iteslaControllers', []);

controllers.controller('ApplicationCtrl', ['offlineService', '$scope', '$location',
    function (offlineService, $scope, $location, $log) {

        $scope.credentials = {};

        $scope.init = function (credentials) {
            credentials.username = "";
            credentials.password = "";
        };

        $scope.$watch(
            function () {
                return offlineService.currentUsername;
            },
            function (currentUsername) {
                $scope.currentUsername = currentUsername;
            }
        );

        $scope.$watch(
            function () {
                return offlineService.loginErrorMsg;
            },
            function (loginErrorMsg) {
                $scope.loginErrorMsg = loginErrorMsg;
            }
        );

        $scope.isActive = function (viewLocation) {
            return $location.path().indexOf(viewLocation) !== -1;
        };

        $scope.login = function (credentials) {
            offlineService.login(credentials);
        };

        $scope.logout = function () {
            offlineService.logout();
        };
    }]);

controllers.controller('ComputationResourcesCtrl', ['offlineService', '$scope',
    function (offlineService, $scope) {
        $scope.$watch(
            function () {
                return offlineService.busyCoresSeries;
            },
            function (busyCoresSeries) {
                $scope.busyCoresSeries = busyCoresSeries;
            }
        );
    }]);

/**
 * Controller for the workflow tab:
 * - Open the modal box for workflow creation,
 * - Open the modal box for worflow removal,
 * - Hold the active workflow and its id.
 */
controllers.controller('WorkflowListCtrl', ['offlineService', '$scope', '$modal', '$log',
    function (offlineService, $scope, $modal, $log) {
        
        $scope.openCreateWorkflowModal = function () {

            var modalInstance = $modal.open({
                templateUrl: 'partials/createWorkflowModal.html',
                controller: 'CreateWorkflowCtrl',
                resolve: {
                    params: function () {
                        return {
                            baseCaseDate: new Date('2013-01-15T18:45:00+01:00'),
                            histoInterval: {
                                start: new Date('2013-01-01T00:00:00+01:00'),
                                end: new Date('2013-01-31T23:59:00+01:00')
                            },
                            countries: [{id: 'FR'}]
                        };
                    }
                }
            });

            modalInstance.result.then(function (params) {
                offlineService.createWorkflow(params);
            });
        };

        $scope.openRemoveWorkflowModal = function (workflowId) {

            var modalInstance = $modal.open({
                templateUrl: 'partials/removeWorkflowModal.html',
                controller: 'RemoveWorkflowCtrl',
                resolve: {
                    params: function () {
                        return {
                            workflowId: workflowId
                        };
                    }
                }
            });

            modalInstance.result.then(function (workflowId) {
                offlineService.workflows[workflowId].remove();
            });
        };

        $scope.selectActiveWorkflow = function (workflowId) {
            $log.debug('selectActiveWorkflow ' + workflowId);
            $scope.activeWorkflowId = workflowId;
            if (workflowId !== null) {
                $scope.activeWorkflow = offlineService.workflows[workflowId];
                
                $scope.activeWorkflow.getSecurityRules();
            } else {
                $scope.activeWorkflow = null;
            }

        };

        /* Local scope variables, watched from parent scope */
        $scope.workflows = null;
        $scope.activeWorkflow = null;
        $scope.activeWorkflowId = null;

        /**
         * Update the workflows collection.
         */
        $scope.$watchCollection(
            function () {
                return offlineService.workflows;
            },
            function (workflows) {
                var workflowIds = Object.getOwnPropertyNames(workflows).sort();
                $log.debug('watch workflows', workflows);
                $scope.workflows = workflows;
                if (workflowIds.length > 0 && ($scope.activeWorkflowId === null || workflows[$scope.activeWorkflowId] === undefined)) {
                    $scope.selectActiveWorkflow(workflowIds[0]);
                } else if (workflowIds.length === 0) {
                    $scope.selectActiveWorkflow(null);
                }
            }
        );
    }]);

/**
 * Controller for the workflow creation modal box,
 * see partials/createWorkflowModal.html
 */
controllers.controller('CreateWorkflowCtrl', ['$scope', '$modalInstance', '$http', '$log', 'params',
    function ($scope, $modalInstance, $http, $log, params) {
        $scope.params = params;
        $scope.countries = [];

        $scope.countrySelectSettings = {
            smartButtonMaxItems: 16,
            smartButtonTextConverter: function(itemText, originalItem) {
                return originalItem.id;
            },
            enableSearch: true,
            scrollable: true,
            buttonClasses: 'btn btn-default form-control'
        };

        $http.post(iteslaResourcesUrl + '/offline/countries').success(function (data, state) {
            if(state === 200) {
                $scope.countries = data.countries;
            }
        }).error(function (e) { $log.error(e); });

        $scope.ok = function () {
            var selectedCountries = [];
            for (var country in $scope.params.countries) {
                selectedCountries.push($scope.params.countries[country].id);
            }
            $scope.params.countries = selectedCountries;
            $log.debug($scope.params);
            $modalInstance.close($scope.params);
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };

        $scope.baseCaseOpen = function ($event) {
            $event.preventDefault();
            $event.stopPropagation();
            $scope.baseCaseOpened = true;
        };

        $scope.histoStartOpen = function ($event) {
            $event.preventDefault();
            $event.stopPropagation();
            $scope.histoStartOpened = true;
        };

        $scope.histoEndOpen = function ($event) {
            $event.preventDefault();
            $event.stopPropagation();
            $scope.histoEndOpened = true;
        };
    }]);

/**
 * Controller for the workflow removal modal box,
 * see partials/removeWorkflowModal.html
 */
controllers.controller('RemoveWorkflowCtrl', ['$scope', '$modalInstance', 'params',
    function ($scope, $modalInstance, params) {
        $scope.params = params;

        $scope.ok = function () {
            $modalInstance.close(params.workflowId);
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

/**
 * Controller for the workflow startup modal box,
 * see partials/startWorkflowModal.html
 */
controllers.controller('StartWorkflowCtrl', ['$scope', '$modalInstance', 'params',
    function ($scope, $modalInstance, params) {
        $scope.params = params;

        $scope.ok = function () {
            $modalInstance.close($scope.params);
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

/**
 * Controller for the main panel of the workflow tab.
 */
controllers.controller('SelectedWorkflowCtrl', ['$scope', '$modal', '$log', '$http', '$timeout',
    function ($scope, $modal, $log, $http, $timeout) {

        $scope.tasks = function (sample) {
            switch (sample.lastTaskEvent.type) {
            case 'SAMPLING':
                return [sample.lastTaskEvent.status, sample.lastTaskEvent.status === 'SUCCEED' ? 'ONGOING' : 'NOT_STARTED', 'NOT_STARTED', 'NOT_STARTED', 'NOT_STARTED'];
            case 'STARTING_POINT_INITIALIZATION':
                return ['SUCCEED', sample.lastTaskEvent.status, sample.lastTaskEvent.status === 'SUCCEED' ? 'ONGOING' : 'NOT_STARTED', 'NOT_STARTED', 'NOT_STARTED'];
            case 'LOAD_FLOW':
                return ['SUCCEED', 'SUCCEED', sample.lastTaskEvent.status, sample.lastTaskEvent.status === 'SUCCEED' ? 'ONGOING' : 'NOT_STARTED', 'NOT_STARTED'];
            case 'STABILIZATION':
                return ['SUCCEED', 'SUCCEED', 'SUCCEED', sample.lastTaskEvent.status, sample.lastTaskEvent.status === 'SUCCEED' ? 'ONGOING' : 'NOT_STARTED'];
            case 'IMPACT_ANALYSIS':
                return ['SUCCEED', 'SUCCEED', 'SUCCEED', 'SUCCEED', sample.lastTaskEvent.status];
            }
        };

        $scope.activeTab = "samples";

        $scope.openStartWorkflowModal = function () {

            var modalInstance = $modal.open({
                templateUrl: 'partials/startWorkflowModal.html',
                controller: 'StartWorkflowCtrl',
                resolve: {
                    params: function () {
                        return {
                            sampleQueueSize: 1,
                            samplingThreads: 1,
                            samplesPerThread: 1,
                            stateQueueSize: 2,
                            duration: 5
                        };
                    }
                }
            });

            modalInstance.result.then(function (params) {
                $scope.$parent.activeWorkflow.start(params);
            }, function () {
                $log.info('Workflow start canceled');
            });
        };

        $scope.stopWorkflow = function () {
            $scope.$parent.activeWorkflow.stop();
        };

        $scope.computeSecurityRules = function () {
            $scope.$parent.activeWorkflow.computeSecurityRules();
        };

        $scope.$watch(
            function () {
                return $scope.$parent.activeWorkflow.running;
            },
            function (running) {
                if (running) {
                    $timeout($scope.animateProgressBar, 1000);
                }
            }
        );
        
        $scope.progressBar = 0;
        
        $scope.animateProgressBar = function () {
            var workflow = $scope.$parent.activeWorkflow;
            if (workflow !== null && workflow.running) {
                $scope.progressBar = 100 * (Date.now() - workflow.startTime) / workflow.duration;
                $timeout($scope.animateProgressBar, 1000);
            } else {
                $scope.progressBar = 0;
            }
        };

        /**
         * Maximum bound for the current page number
         */
        $scope.$watch(
            function () {
                return $scope.numPages;
            },
            function (numPages) {
                if (numPages < $scope.currentPage) {
                    $scope.currentPage = numPages;
                }
            }
        );

        $scope.openSecurityRule = function (securityRuleId) {
            var promise = $http.post(iteslaResourcesUrl
                    + '/offline/rule/' + $scope.$parent.activeWorkflow.id
                    + '/' + securityRuleId.attributeSet + '/'
                    + securityRuleId.securityIndexId.securityIndexType.name
                    + '/' + securityRuleId.securityIndexId.contingencyId);
            $log.debug(securityRuleId);
            promise.success(function (data, status) {
                if (status === 200) {
                    $modal.open({
                        templateUrl: 'partials/securityRule.html',
                        controller: 'SecurityRuleCtrl',
                        size: 'lg',
                        resolve: {
                            params: function () {
                                return {
                                    securityRule: data,
                                    securityRuleId: securityRuleId
                                };
                            }
                        }
                    });
                }
            }).error(function (data) { $log.error(data); });
        };
    }]);

controllers.controller('SecurityIndexesCtrl', ['$scope', '$http', '$log',
    function ($scope, $http, $log) {
        $scope.percent = false;
        $scope.printOk = function (securityIndex) {
            return $scope.percent ? Math.floor(100 * securityIndex.ok / (securityIndex.ok + securityIndex.nok)) + "%" : securityIndex.ok;
        };
        $scope.printNok = function (securityIndex) {
            return $scope.percent ? Math.ceil(100 * securityIndex.nok / (securityIndex.ok + securityIndex.nok)) + "%" : securityIndex.nok;
        };
        $scope.reload = function () {
            var promise = $http.post(iteslaResourcesUrl
                    + '/offline/workflow/' + $scope.$parent.activeWorkflow.id
                    + '/getsecurityindexes');
            promise.success(function (data, status) {
                if (status === 200) {
                    $scope.securityIndexesSynthesis = data.securityIndexesSynthesis;
                    $scope.reloadTime = Date.now();
                }
            }).error(function (data) {
                $log.error(data);
            });
        };
        /*
        $scope.$watch(
            function () {
                return $scope.$parent.$parent.activeWorkflow.securityIndexesSynthesis;
            },
            function (securityIndexesSynthesis) {
                $scope.securityIndexesSynthesis = securityIndexesSynthesis;
            }
        );
        */
    }]);

/**
 * SecurityRule popup controller
 */
controllers.controller('SecurityRuleCtrl', ['$scope', '$modalInstance', 'params', '$log',
    function ($scope, $modalInstance, params, $log) {
        $log.debug(params);
        
        $scope.params = params;

        $scope.stabilityExpression = [];

        if (params.securityRule) {
            $scope.stabilityExpression[0] = params.securityRule;
        }

        $scope.ok = function () {
            $modalInstance.close($scope.params);
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);
