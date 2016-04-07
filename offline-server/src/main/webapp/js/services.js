/*
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/*global angular: true, window: true*/

/* Controllers */

var iteslaMessagesUrl = (window.location.protocol === 'http:' ? 'ws:' : 'wss:') + '//' + window.location.host + '/itesla/messages';

var iteslaResourcesUrl = window.location.protocol + '//' + window.location.host + '/itesla/resources';

var services = angular.module('iteslaServices', []);

services.factory('offlineService', ['$http', '$log', '$timeout',
    function ($http, $log, $timeout) {
        'use strict';

        function Workflow(id) {

            this.id = id;

            this.remove = function () {
                $http.post(iteslaResourcesUrl + '/offline/workflow/' + this.id + '/remove')
                        .error(function (data) { $log.error(data); });
            };

            this.start = function(params) {
                $http.post(iteslaResourcesUrl + '/offline/workflow/' + this.id + '/start', params)
                    .error(function(data) { $log.error(data); });
            };

            this.stop = function () {
                $http.post(iteslaResourcesUrl + '/offline/workflow/' + this.id + '/stop')
                        .error(function (data) { $log.error(data); });
            };

            this.computeSecurityRules = function () {
                $http.post(iteslaResourcesUrl + '/offline/workflow/' + this.id + '/computesecurityrules')
                        .error(function (data) { $log.error(data); });
            };

            this.getSecurityRules = function () {
                $http.post(iteslaResourcesUrl + '/offline/workflow/' + this.id + '/getsecurityrules')
                        .error(function (data) { $log.error(data); });
            };

            this.progress = 0;

            this.samplesSynthesis = null;

            this.securityRules = [];

            /**
             * Worklow running state
             * @type boolean
             */
            this.running = false;
            /* start time in millis */
            this.startTime = null;
            /* duration in millis */
            this.duration = null;
            /* securityRules computation progress */
            this.computationProgress = 0;

            /**
             * Workflow running step
             * @type String
             */
            this.step = null;

            /**
             * Creation parameters
             */
            this.baseCaseDate = null;
            this.intervalStart = null;
            this.intervalStop = null;
            this.countries = null;

            this.update = function (workflowStatus) {
                $log.debug("update ", this.id);
                $log.debug(workflowStatus);
                this.step = workflowStatus.step;
                if(this.running !== workflowStatus.running) {
                    this.running = workflowStatus.running;
                    this.startTime = new Date(workflowStatus.startTime).getTime();
                    this.duration = workflowStatus.duration * 60000;
                }
                this.baseCaseDate = workflowStatus.baseCaseDate;
                this.intervalStart = workflowStatus.intervalStart;
                this.intervalStop = workflowStatus.intervalStop;
                this.countries = workflowStatus.countries;
            };
        }

        var wsUrl = iteslaMessagesUrl + '/offline',
            ws = null,
            r = {
                login: function (credentials) {
                    // add username and password parameters to http requests header
                    $http.defaults.headers.common.Username = credentials.username;
                    $http.defaults.headers.common.Password = credentials.password;

                    // check authentication
                    $http.post(iteslaResourcesUrl + '/offline/login').
                        success(function () {
                            // create a websocket session
                            ws = new WebSocket(wsUrl);

                            ws.onopen = function () {
                                $log.debug('WebSocket \'' + wsUrl + '\' opened');

                                // authenticate websocket session
                                ws.send(angular.toJson(credentials));
                            };

                            ws.onclose = function (event) {
                                $log.debug('WebSocket \'' + wsUrl + '\' closed');
                                // remove username and password parameters from http requests header
                                $http.defaults.headers.common.Username = undefined;
                                $http.defaults.headers.common.Password = undefined;

                                $timeout(function () {
                                    r.workflows = {};
                                    r.securityRules = [];
                                    r.busyCoresSeries = null;
                                    r.currentUsername = null;
                                    r.loginErrorMsg = event.code === 1011 ? event.reason : null;
                                });
                            };

                            // start listening websocket messages
                            ws.onmessage = function (event) {
                                var msg = angular.fromJson(event.data);
                                if(msg.type !== "busyCoresSeries") {
                                    $log.info(msg.type);
                                    $log.debug(msg);
                                }
                                switch (msg.type) {
                                case "workflowStatus":
                                    $timeout(function () {
                                        if (r.workflows[msg.workflowId] !== null) {
                                            r.workflows[msg.workflowId].update(msg);
                                        }
                                    });
                                    break;
                                case "workflowList":
                                    $timeout(function () {
                                        var workflow,
                                            workflowId,
                                            workflowStatus,
                                            i;
                                        for (i in msg.workflowStatuses) {
                                            if (msg.workflowStatuses.hasOwnProperty(i)) {
                                                workflowStatus = msg.workflowStatuses[i];
                                                workflowId = workflowStatus.workflowId;
                                                workflow = new Workflow(workflowId);
                                                workflow.update(workflowStatus);
                                                r.workflows[workflowId] = workflow;
                                            }
                                        }
                                    });
                                    break;
                                case "samplesSynthesis":
                                    $timeout(function () {
                                        r.workflows[msg.workflowId].samplesSynthesis = msg.samplesSynthesis;
                                    });
                                    break;
                                case "busyCoresSeries":
                                    $timeout(function () {
                                        r.busyCoresSeries = msg.busyCoresSeries;
                                    });
                                    break;
                                case "workflowCreation":
                                    $timeout(function () {
                                        r.workflows[msg.workflowId] = new Workflow(msg.workflowId);
                                        r.workflows[msg.workflowId].update(msg);
                                    });
                                    break;
                                case "workflowRemoval":
                                    $timeout(function () {
                                        delete r.workflows[msg.workflowId];
                                    });
                                    break;
                                case "securityRuleComputation":
                                    $timeout(function () {
                                        r.workflows[msg.workflowId].securityRules.push(msg.ruleId);
                                    });
                                    break;
                                case "securityRulesChange":
                                    $timeout(function () {
                                        r.workflows[msg.workflowId].securityRules = msg.ruleIds;
                                    });
                                    break;
                                case "securityRulesProgress":
                                    $timeout(function () {
                                        r.workflows[msg.workflowId].computationProgress = msg.progress;
                                    });
                                    break;
                                case "login":
                                    $timeout(function () {
                                        $log.debug("User " + credentials.username + " logged in");
                                        $timeout(function () {
                                            r.currentUsername = credentials.username;
                                            r.loginErrorMsg = null;
                                        });
                                    });
                                    break;
                                default:
                                    $log.error("Unknown message type " + msg.type);
                                }
                            };
                        }).
                        error(function () {
                            $timeout(function () {
                                r.loginErrorMsg = "Login denied because your username or password is invalid.";
                            });

                            $log.error("User " + credentials.username + " denied");
                        });
                },

                logout: function () {
                    $log.debug("User " + r.currentUsername + " logged out");

                    // close the web socket
                    ws.close();
                    ws = null;
                },

                createWorkflow: function (params) {
                    $http.post(iteslaResourcesUrl + '/offline/workflow/create', params)
                            .error(function (data) { $log.error(data); });
                },

                workflows: {},

                busyCoresSeries: null,

                currentUsername: null,

                loginErrorMsg: null

            };

        return r;
    }]);
