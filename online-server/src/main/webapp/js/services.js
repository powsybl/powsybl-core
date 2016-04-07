/*
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
'use strict';

/* services */

var iteslaMessagesUrl = (window.location.protocol === 'http:' ? 'ws:' : 'wss:') + '//' + window.location.host + '/online/messages';

var iteslaResourcesUrl = window.location.protocol + '//' + window.location.host+ '/online/resources';

var services = angular.module('iteslaServices', []);

services.factory('OnlineWorkflowService', ['$rootScope', '$http', '$log',
    function($rootScope, $http, $log) {

        var wsUrl = iteslaMessagesUrl + '/online/workflow';

        var ws = new WebSocket(wsUrl);

        ws.onopen = function(event) {
            $log.debug('WebSocket \'' + wsUrl + '\' opened');

            $http.post(iteslaResourcesUrl + '/online/workflow/notifyListeners')
                        .error(function(data) { $log.error(data); });
        };

        ws.onclose = function(event) {
            $log.debug('WebSocket \'' + wsUrl + '\' closed');
            $log.error(event);
        };

        var running;
        var wcaRunning = false;
        var statesWithActionsSyntesis;
        var statesWithIndexesSyntesis;
        var statesWithSecurityRulesResultSyntesis;
        var busyCores;
        var workStatus;
      //  var stableContingencies;
       // var unstableContingencies;
        var workflowStatus;
        var activeWorkflowId;
        var wcaContingencies;
        
        ws.onmessage = function(event) 
        {
        	$log.debug('onMessage'+event.data);
            var msg = angular.fromJson(event.data);
            $log.debug('MessageType'+msg.type);
            switch (msg.type)
            {
            	case "status":           		
            		$rootScope.$broadcast('status', [msg.body]);
            		workflowStatus = msg.body.status;           		
            		//workflows[msg.body.workflowId].status=msg.body.status;            		
            		
            		break;
            	 case "connection":
                     $rootScope.$broadcast('connection', [msg.body]);
                     break;
                case "running":
                    $rootScope.$broadcast('running', [msg.body]);
                    running = msg.body;
                    break;
                case "wcaRunning":
                    $rootScope.$broadcast('wcaRunning', [msg.body]);
                    wcaRunning = msg.body;
                    break;
                case "statesWithActionsSyntesis":
                	$log.debug('statesWithActionsSyntesis message  received');
                    $rootScope.$broadcast('statesWithActionsSyntesis', [msg.body]);
                    statesWithActionsSyntesis = msg.body.statesWithActionsSyntesis;
                    break;
                case "statesWithIndexesSyntesis":
                	$log.debug('statesWithIndexesSyntesis message  received');
                    $rootScope.$broadcast('statesWithIndexesSyntesis', [msg.body]);
                    statesWithIndexesSyntesis = msg.body;
                    break;    
                    
                case "statesWithSecurityRulesResultSyntesis":
                	$log.debug('statesWithSecurityRulesResult message  received');
                    $rootScope.$broadcast('statesWithSecurityRulesResultSyntesis', [msg.body]);
                    statesWithSecurityRulesResultSyntesis = msg.body;
                    break;    
                    
                case "busyCores":
                    $rootScope.$broadcast('busyCores', [msg.body.busyCores]);
                    busyCores = msg.body.busyCores;
                    break;
                case "workStatus":
               	 	$log.debug('workStatus message  received');
               	 	$rootScope.$broadcast('workStatus', [msg.body]);
               	 	workStatus = msg.body.workStatus;
               	 	break;
               // case "stableContingencies":
                 // 	 $log.debug('stableContingencies message  received');
                   //  $rootScope.$broadcast('stableContingencies', [msg.body]);
                    // stableContingencies = msg.body.stableContingencies;
                    // break;
                    
                case "workflows":
                	$log.debug('workflows message received ');
                  	$rootScope.$broadcast('workflows', [msg.body]);
                	//workflows = angular.fromJson(msg.body);
                	
            		break;
                case "selectedWorkFlowInfo":
                	 $log.debug('selectedWorkFlowInfo message received ');
                	 selectedWorkFlowInfo = msg.body.selectedWorkFlowInfo;
                     break;
                case "wcaContingencies":
                	$log.debug('wcaContingencies message received ');
                     wcaContingencies = msg.body.wcaContingencies;
                     $rootScope.$broadcast('wcaContingencies', [msg.body]);
                     break;
                     
                // case "unstableContingencies":
                //	$log.debug('unstableContingencies message received');
                // unstableContingencies = msg.body.unstableContingencies;
                // $rootScope.$broadcast('unstableContingencies', [msg.body]);
                // break;
                            
                default:
                    $log.error("Unknown message type " + msg.type);
            }
        };
        
        return {
        	
        	close: function(){ ws.close();},
        	        	
            start: function() {
                $http.post(iteslaResourcesUrl + '/online/workflow/start')
                        .error(function(data) { $log.error(data); });
               
                statesWithIndexesSyntesis=[];
				statesWithActionsSyntesis=[];		
				statesWithSecurityRulesResultSyntesis=[];
            },

            stop: function() {
                $http.post(iteslaResourcesUrl + '/online/workflow/stop')
                        .error(function(data) { $log.error(data); });
            },

            getAvailableCores: function() {
                return $http.get(iteslaResourcesUrl + '/online/workflow/availableCores');
            },

            isRunning: function(wfId) {return $http.get(iteslaResourcesUrl + '/online/workflow/running/'+wfId);            					},
            
            isWcaRunning: function(wfId) {return $http.get(iteslaResourcesUrl + '/online/workflow/wcaRunning/'+wfId); },

            getStatesWithIndexesSyntesis: function(wfId) { return  $http.get(iteslaResourcesUrl + '/online/workflow/statesIndexes/'+wfId); },

            getStatesWithActionsSyntesis: function(wfId) { return $http.get(iteslaResourcesUrl + '/online/workflow/statesActions/'+wfId);},

            getStatesWithSecurityRulesSyntesis: function(wfId) { return $http.get(iteslaResourcesUrl + '/online/workflow/statesIndexesSecurityRules/'+wfId);},
            
            getBusyCores: function() { return busyCores; },
            
            getWorkStatus: function(wfId) { return $http.get(iteslaResourcesUrl + '/online/workflow/workStatus/'+wfId);},
            
            getWorkflowStatus: function(wfId) { return $http.get(iteslaResourcesUrl + '/online/workflow/status/'+wfId);},
            
            //getStableCcontingencies: function(wfId) { return $http.get(iteslaResourcesUrl + '/online/workflow/stables/'+wfId);    },
            
            getWorkflowIds: function() { return $http.get(iteslaResourcesUrl + '/online/workflow/workflowids');},
            
            getWorkflows: function() { return $http.get(iteslaResourcesUrl + '/online/workflow/workflows');},
            
            getSelectedWorkFlowInfo: function(wfId) { return $http.get(iteslaResourcesUrl + '/online/workflow/selectedWorkFlowInfo/'+wfId);},
           
            isWorkflowRunning:function(wfId) { return $http.get(iteslaResourcesUrl + '/online/workflow/isWorkflowRunning/'+wfId);},
          
            isConnected : function() { return $http.get(iteslaResourcesUrl + '/online/workflow/isConnected');},
            
            getWcaContingencies: function(wfId) { return $http.get(iteslaResourcesUrl + '/online/workflow/wcaContingencies/'+wfId);    },
            
           // getUnstableContingencies: function(wfId) { return $http.get(iteslaResourcesUrl + '/online/workflow/unstableContingencies/'+wfId);    },
            
            
        };
    }]);

services.factory('UserService', ['$rootScope', '$http', '$log',
  function($rootScope, $http, $log) {
	return {
    	getCurrentUser: function() {
        var uu =$http.get(iteslaResourcesUrl + '/online/workflow/currentUser');
        $log.debug('getCurrentUser '+uu);
        return uu;
        	
        },
        logout: function() {
        	 $http.post(iteslaResourcesUrl + '/online/workflow/logout');
        	 
             
        }
	};
	
}]);





