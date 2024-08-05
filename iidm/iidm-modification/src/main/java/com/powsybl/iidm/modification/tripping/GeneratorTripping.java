/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016-2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.tripping;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.topology.NamingStrategy;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
public class GeneratorTripping extends AbstractInjectionTripping {

    public GeneratorTripping(String id) {
        super(id);
    }

    @Override
    protected Generator getInjection(Network network) {
        Generator injection = network.getGenerator(id);
        if (injection == null) {
            throw new PowsyblException("Generator '" + id + "' not found");
        }

        return injection;
    }

    @Override
    protected boolean applyDryRun(Network network, NamingStrategy namingStrategy, ComputationManager computationManager, ReportNode reportNode) {
        if (network.getGenerator(id) == null) {
            dryRunConclusive = false;
            reportOnInconclusiveDryRun(reportNode,
                "GeneratorTripping",
                "Generator '" + id + "' not found");
        }
        return dryRunConclusive;
    }
}
