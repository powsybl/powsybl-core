/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.online;

import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.security.LimitViolation;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Online Database
 *
 * @author Quinary <itesla@quinary.com>
 */
public interface OnlineDb extends AutoCloseable {

    /**
     * Get the details of the workflows stored in the db.
     * @return list of details of workflows
     */
    List<OnlineWorkflowDetails> listWorkflows();

    /**
     * Get the details of the workflows, run on a basecase, stored in the db.
     * @return list of details of workflows
     */
    List<OnlineWorkflowDetails> listWorkflows(DateTime basecaseDate);


    /**
     * Get the details of the workflows, run on basecases included in an interval, stored in the db.
     * @return list of details of workflows
     */
    List<OnlineWorkflowDetails> listWorkflows(Interval basecaseInterval);


    /**
     * Get the details of the workflow
     * @param workflowId the if of the workflow
     * @return the details of the workflow, null if the workflow is not stored in the db
     */
    OnlineWorkflowDetails getWorkflowDetails(String workflowId);

    /**
     * Store the results of an online workflow
     * @param workflowId the id of the workflow
     * @param results the results of the online workflow
     */
    void storeResults(String workflowId, OnlineWorkflowResults results);

    /**
     * Get the results of a workflow
     * @param workflowId the id of the workflow
     * @return the results of the online workflow, null if no data about the workflow is available
     */
    OnlineWorkflowResults getResults(String workflowId);


    /**
     * Store the metrics associated to a step of a workflow
     * @param workflowId the id of the workflow
     * @param step the step of the workflow
     * @param metrics the metrics, set of key/value
     */
    void storeMetrics(String workflowId, OnlineStep step, Map<String, String> metrics);

    /**
     * Store the metrics associated to a state and a step of the workflow
     * @param workflowId the id of the workflow
     * @param stateId the id of the state
     * @param step the step of the workflow
     * @param metrics the metrics, set of key/value
     */
    void storeMetrics(String workflowId, Integer stateId, OnlineStep step, Map<String, String> metrics);

    /**
     * Get the metrics associated to a step of a workflow
     * @param workflowId the id of the workflow
     * @param step the step of the workflow
     * @return the metrics, set of key/value, null if no data about the workflow is available
     */
    Map<String, String> getMetrics(String workflowId, OnlineStep step);

    /**
     * Get the metrics associated to a state and a step of the workflow
     * @param workflowId the id of the workflow
     * @param stateId the id of the state
     * @param step the step of the workflow
     * @return the metrics, set of key/value, null if no data about the workflow is available
     */
    Map<String, String> getMetrics(String workflowId, Integer stateId, OnlineStep step);

    /**
     * Get, in CSV format, all the metrics associated to a step of the workflow, for all states
     * @param workflowId the id of 
     * @param step of the workflow
     * @return all the metrics associated to a step of the workflow, for all states, in CSV format, null if no data about the workflow is available
     */
    String getCsvMetrics(String workflowId, OnlineStep step);


    /**
     * Store the results of the application of the security rules during an online workflow
     * @param workflowId the id of the workflow
     * @param results the results of the application of the security rules
     */
    void storeRulesResults(String workflowId, OnlineWorkflowRulesResults results);

    /**
     * Get the results of the application of the security rules during an online workflow
     * @param workflowId the id of the workflow
     * @return the results of the application of the security rules
     */
    OnlineWorkflowRulesResults getRulesResults(String workflowId);

    /**
     * Store the results of the application of the Worst Case Approach during an online workflow
     * @param workflowId the id of the workflow
     * @param results the results of the application of the Worst Case Approach 
     */
    void storeWcaResults(String workflowId, OnlineWorkflowWcaResults results);

    /**
     * Get the results of the application of the Worst Case Approach during an online workflow
     * @param workflowId the id of the workflow
     * @return the results of the application of the Worst Case Approach 
     */
    OnlineWorkflowWcaResults getWcaResults(String workflowId);


    /**
     * Store the configuration parameter of an online workflow
     * @param workflowId the id of the workflow
     * @param parameters the configuration parameters of the the workflow
     */
    void storeWorkflowParameters(String workflowId, OnlineWorkflowParameters parameters);

    /**
     * Get the configuration parameter of an online workflow
     * @param workflowId the id of the workflow
     * @return the configuration parameters of the the workflow
     */
    OnlineWorkflowParameters getWorkflowParameters(String workflowId);

    /**
     * Store the status (success, failed) of the processing steps for the different states generated in an online workflow
     * @param workflowId the id of the workflow
     * @param statesProcessingStatus the status of the processing steps for the different states
     */
    void storeStatesProcessingStatus(String workflowId, Map<Integer, ? extends StateProcessingStatus> statesProcessingStatus);

    /**
     * Get the status (success, failed) of the processing steps for the different states generated in an online workflow
     * @param workflowId the id of the workflow
     * @return the status of the processing steps for the different states
     */
    Map<Integer, ? extends StateProcessingStatus> getStatesProcessingStatus(String workflowId);


    /**
     * Store the network state of a workflow 
     * @param workflowId the id of the workflow
     * @param stateId the id of the state
     * @param network the network
     */
    void storeState(String workflowId, Integer stateId, Network network);

    /**
     * List the ids of the stored states of a network
     * @param workflowId the if of the workflow
     * @return the list of stored state ids
     */
    List<Integer> listStoredStates(String workflowId);

    /**
     * Get a network state of a workflow 
     * @param workflowId the id of the workflow
     * @param stateId the id of the state
     * @return the network state
     */
    Network getState(String workflowId, Integer stateId);

    /**
     * Export, in CSV format, the data of a network state of a workflow
     * @param workflowId the id of the workflow
     * @param stateId the id of the state
     * @param folder the folder where to store the data of the network state 
     */
    void exportState(String workflowId, Integer stateId, Path folder);

    /**
     * Export, in CSV format, the data of the network states of a workflow
     * @param workflowId the id of the workflow
     * @param file the file where to stored the data of the network state
     */
    void exportStates(String workflowId, Path file);


    /**
     * delete a stored workflow
     * @param workflowId the od of the workflow
     * @return true if the workflow has been deleted, false otherwise
     */
    boolean deleteWorkflow(String workflowId);

    /**
     * Delete the stored states of a workflow
     * @param workflowId the id of the workflow
     * @return true if the workflow states has been deleted, false otherwise
     */
    boolean deleteStates(String workflowId);


    /**
     * Store the limit violations of a state of a workflow, related to a step
     * @param workflowId the id of the workflow
     * @param stateId the id of the state
     * @param step the step of the workflow
     * @param violations the list of limit violations
     */
    void storeViolations(String workflowId, Integer stateId, OnlineStep step, List<LimitViolation> violations);

    /**
     * Get the limit violations of a state of a workflow, related to a step
     * @param workflowId the id of the workflow
     * @param stateId the id of the state
     * @param step the step of the workflow
     * @return the list of limit violations
     */
    List<LimitViolation> getViolations(String workflowId, Integer stateId, OnlineStep step);

    /**
     * Get the limit violations of a state of a workflow
     * @param workflowId the id of the workflow
     * @param stateId the id of the state
     * @return a map containing, for each step, the list of limit violations
     */
    Map<OnlineStep, List<LimitViolation>> getViolations(String workflowId, Integer stateId);

    /**
     * Get the limit violations of a step of a workflow
     * @param workflowId the id of the workflow
     * @param step the step of the workflow
     * @return a map containing, for each state, the list of limit violations
     */
    Map<Integer, List<LimitViolation>> getViolations(String workflowId, OnlineStep step);


    /**
     * Get the limit violations of a workflow
     * @param workflowId the id of the workflow
     * @return a map containing, for each state and step, the list of limit violations
     */
    Map<Integer, Map<OnlineStep, List<LimitViolation>>> getViolations(String workflowId);


    /**
     * Store the results of the application of wca security rules during an online workflow
     * @param workflowId the id of the workflow
     * @param results the results of the application of wca security rules
     */
    void storeWcaRulesResults(String workflowId, OnlineWorkflowRulesResults results);

    /**
     * Get the results of the application of wca security rules during an online workflow
     * @param workflowId the id of the workflow
     * @return the results of the application of wca security rules
     */
    OnlineWorkflowRulesResults getWcaRulesResults(String workflowId);


    /**
     * Store the limit violations of a post contingency state of a workflow
     * @param workflowId the id of the workflow
     * @param stateId the id of the state
     * @param contingencyId the id of the contingency
     * @param loadflowConverge the convergence of the post contingency loadflow
     * @param violations the list of limit violations
     */
    void storePostContingencyViolations(String workflowId, Integer stateId, String contingencyId, boolean loadflowConverge, List<LimitViolation> violations);

    /**
     * Get the limit violations of a post contingency state of a workflow
     * @param workflowId the id of the workflow
     * @param stateId the id of the state
     * @param contingencyId the id of the contingency
     * @return the list of post contingency limit violations
     */
    List<LimitViolation> getPostContingencyViolations(String workflowId, Integer stateId, String contingencyId);

    /**
     * Get the limit violations of a post contingency state of a workflow
     * @param workflowId the id of the workflow
     * @param stateId the id of the state
     * @return a map containing, for each contingency, the list of post contingency limit violations
     */
    Map<String, List<LimitViolation>> getPostContingencyViolations(String workflowId, Integer stateId);

    /**
     * Get the limit violations of the post contingency states of a workflow
     * @param workflowId the id of the workflow
     * @param contingencyId the id of the contingency
     * @return a map containing, for each state, the list of post contingency limit violations
     */
    Map<Integer, List<LimitViolation>> getPostContingencyViolations(String workflowId, String contingencyId);


    /**
     * Get the post contingency limit violations of all states of a workflow
     * @param workflowId the id of the workflow
     * @return a map containing, for each state and contingency, the list of post contingency limit violations
     */
    Map<Integer, Map<String, List<LimitViolation>>> getPostContingencyViolations(String workflowId);

    /**
     * Get the load flow convergence of a post contingency state of a workflow
     * @param workflowId the id of the workflow
     * @param stateId the id of the state
     * @return a map containing, for each contingency, the convergence of the post contingency load flow
     */
    Map<String, Boolean> getPostContingencyLoadflowConvergence(String workflowId, Integer stateId);

    /**
     * Get the load flow convergence of the post contingency states of a workflow
     * @param workflowId the id of the workflow
     * @param contingencyId the id of the contingency
     * @return a map containing, for each state, the convergence of the post contingency load flow
     */
    Map<Integer, Boolean> getPostContingencyLoadflowConvergence(String workflowId, String contingencyId);


    /**
     * Get the post contingency load flow convergence of all states of a workflow
     * @param workflowId the id of the workflow
     * @return a map containing, for each state and contingency, the convergence of the post contingency load flow
     */
    Map<Integer, Map<String, Boolean>> getPostContingencyLoadflowConvergence(String workflowId);

}
