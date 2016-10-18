/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.test;

import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.offline.*;
import eu.itesla_project.simulation.securityindexes.SecurityIndex;
import eu.itesla_project.simulation.securityindexes.SecurityIndexId;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class InMemoryOfflineDb implements OfflineDb {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryOfflineDb.class);

    private final List<String> workflowIds = new ArrayList<>();

    private final AtomicInteger nextWorkflowId = new AtomicInteger();
    private final AtomicInteger nextSampleId = new AtomicInteger();

    public InMemoryOfflineDb() {
    }

    @Override
    public List<String> listWorkflows() {
        return workflowIds;
    }

    @Override
    public String createWorkflow(String workflowId, OfflineWorkflowCreationParameters parameters) {
        if (workflowId != null) {
            throw new UnsupportedOperationException("Named workflow not supported");
        }
        String workflowId2 = Integer.toString(nextWorkflowId.getAndIncrement());
        workflowIds.add(workflowId2);
        return workflowId2;
    }

    @Override
    public OfflineWorkflowCreationParameters getParameters(String workflowId) {
        return new OfflineWorkflowCreationParameters(EnumSet.of(Country.FR), DateTime.now(), new Interval(DateTime.now(), DateTime.now().plus(1)), false, false);
    }

    @Override
    public void deleteWorkflow(String workflowId) {
        // TODO
    }

    @Override
    public int createSample(String workflowId) {
        return nextSampleId.getAndIncrement();
    }

    @Override
    public void storeState(String workflowId, int sampleId, Network network, Set<Country> countryFilter) {
        LOGGER.debug("State stored");
    }

    @Override
    public void storeTaskStatus(String workflowId, int sampleId, OfflineTaskType taskType, OfflineTaskStatus taskState, String taskFailureReason) {
        // TODO
    }

    @Override
    public void storeSecurityIndexes(String workflowId, int sampleId, Collection<SecurityIndex> securityIndexes) {
        LOGGER.debug("Security indexes stored");
    }

    @Override
    public int getSampleCount(String workflowId) {
        return nextSampleId.get();
    }

    @Override
    public Collection<SecurityIndexId> getSecurityIndexIds(String workflowId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Integer, SecurityIndex> getSecurityIndexes(String workflowId, SecurityIndexId securityIndexId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SecurityIndexSynthesis getSecurityIndexesSynthesis(String workflowId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void exportCsv(String workflowId, Writer writer, OfflineDbCsvExportConfig config) {
    }

    @Override
    public void close() throws Exception {
    }

}
