/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.offline;

import com.google.common.base.Predicate;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.histo.HistoDbAttr;
import eu.itesla_project.modules.histo.HistoDbNetworkAttributeId;
import eu.itesla_project.simulation.securityindexes.SecurityIndex;
import eu.itesla_project.simulation.securityindexes.SecurityIndexId;

import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Offline database API.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface OfflineDb extends AutoCloseable {

    Predicate<HistoDbNetworkAttributeId> ATTRIBUTE_FILTER = attributeId -> {
        switch (attributeId.getAttributeType()) {
            case P:
            case Q:
            case V:
            case PGEN:
            case QGEN:
            case PLOAD:
            case QLOAD:
            case QSHUNT:
            case RTC:
            case PTC:
            case QR:
            case BC:
                return true;

            default:
                return false;
        }
    };

    Predicate<HistoDbNetworkAttributeId> BRANCH_ATTRIBUTE_FILTER = attributeId -> (attributeId.getSide() != null
            && (attributeId.getAttributeType() == HistoDbAttr.P
            || attributeId.getAttributeType() == HistoDbAttr.Q
            || attributeId.getAttributeType() == HistoDbAttr.V));

    Predicate<HistoDbNetworkAttributeId> ACTIVE_POWER_ATTRIBUTE_FILTER = attributeId -> attributeId.getAttributeType() == HistoDbAttr.P;

    /**
     * Get workflows id stored in the db.
     * @return list of workflow id
     */
    List<String> listWorkflows();

    /**
     * Create a new workflow. A workflow is fully identified by its network date
     * and historical data time interval. This method must return an existing
     * workflow if the database already contains one with the same network date
     * and historical data time interval.
     *
     * @param workflowId workflow id of null to let the db generating an id
     * @param parameters workflow parameters
     * @return a unique id of the workflow
     */
    String createWorkflow(String workflowId, OfflineWorkflowCreationParameters parameters);

    /**
     * Get parameters associated to a workflow.
     * @param workflowId workflow id
     * @return workflow parameters
     */
    OfflineWorkflowCreationParameters getParameters(String workflowId);

    /**
     * Removes a workflow from the historical databaser
     * @param workflowId the workflow id
     */
    void deleteWorkflow(String workflowId);

    /**
     * For a given workflown, create a new sample. The id returned must me unique
     * at least for a given workflow id.
     *
     * @param workflowId the workflow id
     * @return a unique id of the sample
     */
    int createSample(String workflowId);

    /**
     * For a given workflow and sample, store state attributes needed for offline
     * rules computation in the offline db.
     *
     * @param workflowId the workflow id
     * @param sampleId the sample id
     * @param network the network with a current state corresponding to the sample
     */
    void storeState(String workflowId, int sampleId, Network network, Set<Country> countryFilter);

    /**
     * For a given workflow and sample, store a task status.
     * @param workflowId the workflow id
     * @param sampleId the sample id
     * @param taskType task type
     * @param taskStatus task status
     * @param taskFailureReason task failure reason
     */
    void storeTaskStatus(String workflowId, int sampleId, OfflineTaskType taskType, OfflineTaskStatus taskStatus, String taskFailureReason);

    /**
     * For a given workflow and sample, store security indexes computed for all
     * the contingencies in the offline db.
     *
     * @param workflowId the workflow id
     * @param sampleId the sample id
     * @param securityIndexes security indexes
     */
    void storeSecurityIndexes(String workflowId, int sampleId, Collection<SecurityIndex> securityIndexes);

    /**
     * For a given workflow, get the nuber of samples.
     * @param workflowId the workflow id
     * @return the number of sample
     */
    int getSampleCount(String workflowId);

    /**
     * For a given workflow, get a list of security indexes that can be computed.
     * @param workflowId the workflow id
     * @return list of security indexes
     */
    Collection<SecurityIndexId> getSecurityIndexIds(String workflowId);

    Map<Integer, SecurityIndex> getSecurityIndexes(String workflowId, SecurityIndexId securityIndexId);

    /**
     * Get security indexes synthesis, nomber of stable and unstable sample
     * for each of the contingency/security index type couple.
     * @param workflowId the workflow id
     * @return
     */
    SecurityIndexSynthesis getSecurityIndexesSynthesis(String workflowId);

    /**
     * For a given workflow, export the db content as a CSV.
     * @param workflowId the workflow id
     * @param writer
     * @param config export configuration
     */
    void exportCsv(String workflowId, Writer writer, OfflineDbCsvExportConfig config);

    default void flush(String workflowId) {
    }

}
