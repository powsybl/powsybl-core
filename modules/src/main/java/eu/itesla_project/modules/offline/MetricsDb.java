/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.offline;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface MetricsDb extends AutoCloseable {

    void create(String workflowId);

    void remove(String workflowId);

    /**
     * For a given workflow and sample, store metrics associated with the execution
     * of one of the offline task.
     *
     * @param workflowId the workflow id
     * @param target a target which the metrics is associated
     * @param moduleName module that generates the metrics
     * @param metrics metrics, set of key/value
     */
    void store(String workflowId, String target, String moduleName, Map<String, String> metrics);


    /**
     * For a given workflow, export all metrics as a csv table.
     * @param workflowId the workflow id
     * @param writer
     * @throws IOException
     */
    void exportCsv(String workflowId, Writer writer, char delimiter);

}
