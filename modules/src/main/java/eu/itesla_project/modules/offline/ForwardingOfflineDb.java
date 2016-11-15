/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.offline;

import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.simulation.securityindexes.SecurityIndex;
import eu.itesla_project.simulation.securityindexes.SecurityIndexId;

import java.io.Writer;
import java.util.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ForwardingOfflineDb<T extends OfflineDb> implements OfflineDb {

    protected final T delegate;

    public ForwardingOfflineDb(T delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public List<String> listWorkflows() {
        return delegate.listWorkflows();
    }

    @Override
    public String createWorkflow(String workflowId, OfflineWorkflowCreationParameters parameters) {
        return delegate.createWorkflow(workflowId, parameters);
    }

    @Override
    public OfflineWorkflowCreationParameters getParameters(String workflowId) {
        return delegate.getParameters(workflowId);
    }

    @Override
    public void deleteWorkflow(String workflowId) {
        delegate.deleteWorkflow(workflowId);
    }

    @Override
    public int createSample(String workflowId) {
        return delegate.createSample(workflowId);
    }

    @Override
    public void storeState(String workflowId, int sampleId, Network network, Set<Country> countryFilter) {
        delegate.storeState(workflowId, sampleId, network, countryFilter);
    }

    @Override
    public void storeTaskStatus(String workflowId, int sampleId, OfflineTaskType taskType, OfflineTaskStatus taskStatus, String taskFailureReason) {
        delegate.storeTaskStatus(workflowId, sampleId, taskType, taskStatus, taskFailureReason);
    }

    @Override
    public void storeSecurityIndexes(String workflowId, int sampleId, Collection<SecurityIndex> securityIndexes) {
        delegate.storeSecurityIndexes(workflowId, sampleId, securityIndexes);
    }

    @Override
    public int getSampleCount(String workflowId) {
        return delegate.getSampleCount(workflowId);
    }

    @Override
    public Collection<SecurityIndexId> getSecurityIndexIds(String workflowId) {
        return delegate.getSecurityIndexIds(workflowId);
    }

    @Override
    public Map<Integer, SecurityIndex> getSecurityIndexes(String workflowId, SecurityIndexId securityIndexId) {
        return delegate.getSecurityIndexes(workflowId, securityIndexId);
    }

    @Override
    public SecurityIndexSynthesis getSecurityIndexesSynthesis(String workflowId) {
        return delegate.getSecurityIndexesSynthesis(workflowId);
    }

    @Override
    public void exportCsv(String workflowId, Writer writer, OfflineDbCsvExportConfig config) {
        delegate.exportCsv(workflowId, writer, config);
    }

    @Override
    public void close() throws Exception {
        delegate.close();
    }

}
