/*
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
'use strict';

/* Controllers */

var controllers = angular.module('iteslaControllers', ['ngCookies','ngSanitize']);

controllers.controller('NavController', ['UserService','OnlineWorkflowService','$scope','$http', '$location','$window','$route','$cookieStore',
    function(UserService,OnlineWorkflowService,$scope, $http, $location, $window,$route,$cookieStore) {
        $scope.isActive = function (viewLocation) {
            return $location.path().indexOf(viewLocation) != -1;
        };

        UserService.getCurrentUser()
        	.success(function(data) { $scope.currentUser= data; })
                .error(function(data) { $scope.currentUser=null; });
        
       $scope.logout = function() { 
    	   UserService.logout();    
    	   OnlineWorkflowService.close();
    	   $scope.currentUser=null;
    	   $cookieStore.remove("JSESSIONID");
    	   window.top.location.href='/online/loggedOut.html';
    	   $location.path('/online/loggedOut.html');
    	  // $window.location.href='/online';
    	   $route.reload();
    	   
       };

    }]);

controllers.controller('OnLineWorkflowCtrl', ['OnlineWorkflowService', '$scope', '$log','$routeParams',
    function(OnlineWorkflowService, $scope, $log,$routeParams) {
	
		$scope.activeWorkflowId= $routeParams.wfId;
		
		$scope.keys = function(obj){
			  return obj? Object.keys(obj) : [];
			};
		$scope.value = function(obj,k){
				  return obj? obj[k] : [];
				};


        $scope.tasks = function(sample) {
            switch (sample.lastTaskEvent.type) {
                case 'SAMPLING':                return [sample.lastTaskEvent.status, sample.lastTaskEvent.status == 'SUCCEED' ? 'ONGOING' : 'NOT_STARTED', 'NOT_STARTED', 'NOT_STARTED', 'NOT_STARTED'];
                case 'LOAD FLOW': 				return ['SUCCEED', sample.lastTaskEvent.status, sample.lastTaskEvent.status == 'SUCCEED' ? 'ONGOING' : 'NOT_STARTED', 'NOT_STARTED', 'NOT_STARTED'];
                case 'SECURITY RULES':          return ['SUCCEED', 'SUCCEED', sample.lastTaskEvent.status, sample.lastTaskEvent.status == 'SUCCEED' ? 'ONGOING' : 'NOT_STARTED', 'NOT_STARTED'];
                case 'OPTIMIZER':               return ['SUCCEED', 'SUCCEED', 'SUCCEED', sample.lastTaskEvent.status, sample.lastTaskEvent.status == 'SUCCEED' ? 'ONGOING' : 'NOT_STARTED'];
                case 'TIME DOMAIN':             return ['SUCCEED', 'SUCCEED', 'SUCCEED', 'SUCCEED', sample.lastTaskEvent.status];
            }
        };

        $scope.start = function() { 
        	 $scope.statesWithActionsSyntesis = [];
             $scope.statesWithIndexesSyntesis = [];
             $scope.workStatus=[];
             $scope.stableContingencies = [];
             
             
        	return OnlineWorkflowService.start();  };
        	
        	
        $scope.stop = function() { return OnlineWorkflowService.stop(); };

        OnlineWorkflowService.getAvailableCores()
                .success(function(data) { $scope.availableCores = parseInt(data); })
                .error(function(data) { $log.error(data); });
        
        if(!angular.isUndefined($scope.activeWorkflowId))
        {
        OnlineWorkflowService.getWorkflowStatus($scope.activeWorkflowId)
        		.success(function(data) { $scope.workflowStatus = parseInt(data); })
                .error(function(data) { $log.error(data); });

        OnlineWorkflowService.isRunning($scope.activeWorkflowId).success( function(data){ $scope.running=(data==='true'); })
							.error(function(data){$scope.running=false;});
        
		 OnlineWorkflowService.isWcaRunning($scope.activeWorkflowId).success( function(data){ $scope.wcaRunning=(data==='true'); })
								.error(function(data){$scope.wcaRunning=false;});
		
		OnlineWorkflowService.getStatesWithActionsSyntesis($scope.activeWorkflowId)
			.success(function(data){ if(data != null && data != "") $scope.statesWithActionsSyntesis=angular.fromJson(data).body;})
			.error(function(data){ $scope.statesWithActionsSyntesis=null;});
		    
		//statesWithIndexesSyntesis
		OnlineWorkflowService.getStatesWithIndexesSyntesis($scope.activeWorkflowId)
        	.success(function(data){ if(data != null && data != "") $scope.statesWithIndexesSyntesis=angular.fromJson(data).body;})
			.error(function(data){ $scope.statesWithIndexesSyntesis=null;});
		
        //-- statesWithSecurityRulesResultSynthesis
		OnlineWorkflowService.getStatesWithSecurityRulesSyntesis($scope.activeWorkflowId)
		.success(function(data){ if(data != null && data != "") $scope.statesWithSecurityRulesResultSyntesis=angular.fromJson(data).body;})
		.error(function(data){ $scope.statesWithSecurityRulesResultSyntesis=null;});
		
         OnlineWorkflowService.getWorkStatus($scope.activeWorkflowId)
	        .success(function(data){if(data != null && data != "") $scope.workStatus=angular.fromJson(data).body.status;})
			.error(function(data){$scope.workStatus=null;});
        
       /*  OnlineWorkflowService.getStableCcontingencies($scope.activeWorkflowId)
	        .success(function(data){ if(data != null && data != "") $scope.stableContingencies=angular.fromJson(data).body.contingencies;})
			.error(function(data){$scope.stableContingencies=null;});
         */
        }
        
       /* OnlineWorkflowService.getUnstableContingencies($scope.activeWorkflowId)
        	.success(function(data){ if(data != null && data != "") $scope.unstableContingencies=angular.fromJson(data).body;})
        	.error(function(data){$scope.unstableContingencies=null;});     
      */
        
        OnlineWorkflowService.getWcaContingencies($scope.activeWorkflowId)
    	.success(function(data){ if(data != null && data != "") $scope.wcaContingencies=angular.fromJson(data).body;})
    	.error(function(data){$scope.wcaContingencies=null;});
        
        OnlineWorkflowService.isConnected()
        .success(function(data){ $scope.connected=(data==='true'); })
		.error(function(data){$scope.connected=false;});
        
        $scope.busyCores = OnlineWorkflowService.getBusyCores();
        
        $scope.startDisabled = function(){
        	for (var w in $scope.workflows)
        	{	
        		if( $scope.workflows[w].status==1)
        			return true;
        	}
        	return false;
        };
        

        
        $scope.statusImage = function(value) { 
        	
        	if(value == 'RUNNING')
        		return 'img/spinner.gif'; 
        	else if((value == 'SUCCESS'))
        		return 'img/ok.png';
        	else if((value == 'FAILED'))
        		return 'img/nok.png';
        	else
        		return 'img/idle.png';
        	
        	};
        	
        $scope.statusClass=function(status) {
        	//$log.debug("statusClass "+status );
        	if (status == "RUNNING")
        		return 'ongoing';
        	else 
        		return 'idle';
        };
        
        	
        	OnlineWorkflowService.getWorkflows()
    		.success(function(data){
    			 if(data != null && data != "")
    				 $scope.workflows=angular.fromJson(data).body;})
    		.error(function(data){ 
    			$scope.workflows=null;});
    	
        	/*
    	 OnlineWorkflowService.isWorkflowRunning($scope.activeWorkflowId)
         .success(function(data){$scope.workStatusFlowId=angular.fromJson(data).body;})
    		.error(function(data){$scope.workStatusFlowId=null;});
    	 */
    	$scope.$on('workflows', 
    	   function(event, args) {
    			$log.debug(" workflows ");
    			$scope.$apply(function() {
    				
    				$scope.workflows = args[0];
    	    		});
    	       });
    			
        
           /*
        $scope.$watchCollection(
        		function (){
        			
        			return  OnlineWorkflowService.getWorkflows(); 
        		}); 	
        	*/
     
        
        
    	$scope.$on('status', function(event, args) {
            $scope.$apply(function() {
            	
            	$scope.workflows[args[0].workflowId].status=args[0].status;  
            	
            	if(args[0].workflowId == $scope.activeWorkflowId)
            		$scope.workflowStatus = args[0].status;
            	
            	
            });
            
        });
        
    	$scope.$on('connection', function(event, args) {
            $scope.$apply(function() {
            		$log.debug("connection : "+args[0]);
            		$scope.connected = args[0];
            });
        });

        $scope.$on('running', function(event, args) {
            $scope.$apply(function() {
            	if(args[0].workflowId == $scope.activeWorkflowId)
            		$scope.running = args[0].running;
            });
        });
        
        $scope.$on('wcaRunning', function(event, args) {
            $scope.$apply(function() {
            	if(args[0].workflowId == $scope.activeWorkflowId)
            		$scope.wcaRunning = args[0].running;
            });
        });
        
        $scope.$on('workStatus', function(event, args) {
            $scope.$apply(function() {
            	if(args[0].workflowId == $scope.activeWorkflowId)
            		$scope.workStatus = args[0].status;
            });
        });
        $scope.$on('statesWithActionsSyntesis', function(event, args) {
            $scope.$apply(function() {
            	if(args[0].workflowId == $scope.activeWorkflowId)
            		$scope.statesWithActionsSyntesis = args[0];
            });
        });
        
        $scope.$on('statesWithIndexesSyntesis', function(event, args) {
            $scope.$apply(function() {
            	if(args[0].workflowId == $scope.activeWorkflowId)
            		$scope.statesWithIndexesSyntesis = args[0];
            });
        });
        
        
        $scope.$on('statesWithSecurityRulesResultSyntesis',function(event, args) {
            $scope.$apply(function() {
            	if(args[0].workflowId == $scope.activeWorkflowId)
            		$scope.statesWithSecurityRulesResultSyntesis = args[0];
            });
        });
        
        
        //$scope.$on('stableContingencies', function(event, args) {
        //  $scope.$apply(function() {
        //	if(args[0].workflowId == $scope.activeWorkflowId)
        //	$scope.stableContingencies = args[0].contingencies;
        //  });
        // });
                
        // $scope.$on('unstableContingencies', function(event, args) {
        //   $scope.$apply(function() {
        //            	if(args[0].workflowId == $scope.activeWorkflowId)
        //          		$scope.unstableContingencies = args[0];
        //        });
        //  });
        
        
        $scope.$on('wcaContingencies', function(event, args) {
        $scope.$apply(
        		function() {
        			if(args[0].workflowId == $scope.activeWorkflowId)
        				$scope.wcaContingencies = args[0];
        		});
        });
        
        
        $scope.$on('busyCores', function(event, args) {
            $scope.$apply(function() {
                $scope.busyCores = args[0];
            });
        });
        
        
        
        $scope.indexImage = function(index) { 
        	 
        	if(index.description=='') return 'img/idle.png';
        	else 
        		if (index.ok)	return 'img/ok.png'; 
        		else
        		return 'img/nok.png';
        	
        		        	
        	};
        	
        	$scope.indexStatusImage = function(indexStatus) { 
            	 
             	if(indexStatus.description=='') return 'img/idle.png';
             	else
             	{
             		if(indexStatus.status=='SAFE') return 'img/ok.png'; 
             		if(indexStatus.status=='UNSAFE') return 'img/nok.png';
             		
             	}        	
        	};
        	
        	
        	$scope.stateStatusImage = function(indexStatus) { 
           	 if(indexStatus=='SAFE') return 'img/flag_white_16.png'; 
             if(indexStatus=='UNSAFE') return 'img/flag_red_16.png'; 
             if(indexStatus=='SAFE_WITH_CORRECTIVE_ACTIONS')  return 'img/flag_yellow_16.png'; 
             
        	};
        	
        	$scope.indexStatusClass = function(indexStatus) { 
        		//$log.debug(" indexStatus "+indexStatus.status);
           	 
             	if(indexStatus.description=='') 
             		return 'empty';
             	else 
             	{
             		if(indexStatus.status=='SAFE') {
             			//$log.debug(" ok ");
             			return 'ok'; 
             		}
             		if(indexStatus.status=='UNSAFE') {
             			//$log.debug(" nok ");
             			return 'nok';
             			
             		}
             		if(indexStatus.status=='SAFE_WITH_CORRECTIVE_ACTIONS') {
             			//$log.debug("  CC ok ");
             			return 'warning'; 
             		}
             	}
        	};
        	
        	
        	$scope.clusterImage = function(clusterIndex, pos) 
        	{
        		if (clusterIndex != pos) return 'img/idle.png';
        		else   return 'img/tick.gif'; 
                     	 
             };
        	
        	
        	/*$scope.indexClass= function(index) { 
        		//$log.debug(" indexClass description: "+index.description + " isOk: "+index.ok );
           	 
             	if(index.description=='') 
             		return 'empty';
             	else 
             	{
             		if(index.ok) {
             			$log.debug(" ok ");
             			return 'ok'; 
             		}
             		else   {
             			$log.debug(" nok ");
             		return 'nok';
             		}
             		
             	}
        	};*/
             
            $scope.mostraDivScorrevole = function(id){
            	$log.debug(" elemento "+id);
            	$("#divScorrevole"+id).animate(	{
            		"height": "toggle",
            		"display": "none",
            		"border": "1px dashed Navy",
            		"padding": "10px 10px 10px 10px",
            		"margin-top": "10px",
            		"width": "500px",
            		"text-align": "justify"
            	}, { duration: 1000 });
         		
         	};
    }]);

	

                     
   

