/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.loadflow.validation;

import java.util.Objects;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
 */
public enum FlowOutputWriter {
    CSV(FlowsFormatterCsvWriterFactory.class),
    CSV_MULTILINE(FlowsFormatterCsvMultilineWriterFactory.class);

    private final Class<? extends FlowsWriterFactory> flowsWriterFactory;

    FlowOutputWriter(Class<? extends FlowsWriterFactory> flowsWriterFactory) {
        this.flowsWriterFactory = Objects.requireNonNull(flowsWriterFactory);
    }

    public Class<? extends FlowsWriterFactory> getFlowsWriterFactory() {
        return flowsWriterFactory;
    }

}
