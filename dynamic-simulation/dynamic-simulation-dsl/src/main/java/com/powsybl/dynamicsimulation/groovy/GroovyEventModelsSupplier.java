/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.dynamicsimulation.groovy;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.powsybl.commons.report.ReportNode;

import com.powsybl.dsl.GroovyScripts;
import com.powsybl.dynamicsimulation.EventModelsSupplier;
import com.powsybl.dynamicsimulation.EventModel;
import com.powsybl.iidm.network.Network;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public class GroovyEventModelsSupplier extends AbstractGroovySupplier<EventModel, EventModelGroovyExtension>
        implements EventModelsSupplier {

    public GroovyEventModelsSupplier(Path path) {
        this(path, Collections.emptyList());
    }

    public GroovyEventModelsSupplier(Path path, List<EventModelGroovyExtension> extensions) {
        super(GroovyScripts.load(path), Objects.requireNonNull(extensions));
    }

    public GroovyEventModelsSupplier(InputStream is, List<EventModelGroovyExtension> extensions) {
        super(GroovyScripts.load(is), Objects.requireNonNull(extensions));
    }

    @Override
    public List<EventModel> get(Network network, ReportNode reportNode) {
        return evaluateScript(network, DynamicSimulationReports.supplyGroovyEventModels(reportNode));
    }
}
