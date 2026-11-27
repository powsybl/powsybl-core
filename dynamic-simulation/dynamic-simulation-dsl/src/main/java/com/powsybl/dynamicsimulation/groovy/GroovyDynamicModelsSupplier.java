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
import com.powsybl.dynamicsimulation.DynamicModel;
import com.powsybl.dynamicsimulation.DynamicModelsSupplier;
import com.powsybl.iidm.network.Network;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * <p><b>Security warning:</b> the Groovy script is evaluated as-is, without any sandbox (no {@code SecureASTCustomizer},
 * no class or method restriction), with the full privileges of the JVM. PowSyBl is a library: sandboxing untrusted
 * scripts is the responsibility of the application embedding it. Only run scripts from trusted sources.
 * See <a href="https://powsybl.readthedocs.io/projects/powsybl-core/en/stable/user/groovy-security.html">Groovy scripts security</a>.
 *
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public class GroovyDynamicModelsSupplier extends AbstractGroovySupplier<DynamicModel, DynamicModelGroovyExtension>
        implements DynamicModelsSupplier {

    public GroovyDynamicModelsSupplier(Path path) {
        this(path, Collections.emptyList());
    }

    public GroovyDynamicModelsSupplier(Path path, List<DynamicModelGroovyExtension> extensions) {
        super(GroovyScripts.load(path), Objects.requireNonNull(extensions));
    }

    public GroovyDynamicModelsSupplier(InputStream is, List<DynamicModelGroovyExtension> extensions) {
        super(GroovyScripts.load(is), Objects.requireNonNull(extensions));
    }

    @Override
    public List<DynamicModel> get(Network network, ReportNode reportNode) {
        return evaluateScript(network, DynamicSimulationReports.supplyGroovyDynamicModels(reportNode));
    }
}
