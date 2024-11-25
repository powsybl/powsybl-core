/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.dynamicsimulation.groovy;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.dsl.GroovyScripts;
import com.powsybl.dynamicsimulation.OutputVariable;
import com.powsybl.dynamicsimulation.OutputVariablesSupplier;
import com.powsybl.iidm.network.Network;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
public class GroovyOutputVariablesSupplier extends AbstractGroovySupplier<OutputVariable, OutputVariableGroovyExtension>
        implements OutputVariablesSupplier {

    public GroovyOutputVariablesSupplier(Path path) {
        this(path, Collections.emptyList());
    }

    public GroovyOutputVariablesSupplier(Path path, List<OutputVariableGroovyExtension> extensions) {
        super(GroovyScripts.load(path), Objects.requireNonNull(extensions));
    }

    public GroovyOutputVariablesSupplier(InputStream is, List<OutputVariableGroovyExtension> extensions) {
        super(GroovyScripts.load(is), Objects.requireNonNull(extensions));
    }

    @Override
    public List<OutputVariable> get(Network network, ReportNode reportNode) {
        return evaluateScript(network,
                createReportNode(reportNode, "groovyOutputVariables", "Groovy Output Variables Supplier"));
    }
}
