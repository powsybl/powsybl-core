/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update.elements16;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.update.IidmChange;
import com.powsybl.cgmes.conversion.update.IidmToCgmes;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VoltageLevel;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class IidmToCgmes16 {

    public IidmToCgmes findConversion(IidmChange change) {
        Identifiable o = change.getIdentifiable();
        if (o instanceof Generator) {
            return generator;
        } else if (o instanceof Load) {
            return load;
        } else if (o instanceof Line) {
            return line;
        } else if (o instanceof TwoWindingsTransformer) {
            return t2;
        } else if (o instanceof ShuntCompensator) {
            return shunt;
        } else if (o instanceof VoltageLevel) {
            return vl;
        } else {
            LOG.warn("Currently not supported conversion for {}", o.getClass().getSimpleName());
            return null;
        }
    }

    private final IidmToCgmes generator = new GeneratorToSynchronousMachine();
    private final IidmToCgmes load = new LoadToEnergyConsumer();
    private final IidmToCgmes line = new LineToACLineSegment();
    private final IidmToCgmes t2 = new TwoWindingsTransformerToPowerTransformer();
    private final IidmToCgmes shunt = new ShuntCompensatorToShuntCompensator();
    private final IidmToCgmes vl = new VoltageLevelToVoltageLevel();

    private static final Logger LOG = LoggerFactory.getLogger(IidmToCgmes16.class);
}
