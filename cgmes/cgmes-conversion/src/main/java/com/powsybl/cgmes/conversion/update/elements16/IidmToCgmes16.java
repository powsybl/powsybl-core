/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update.elements16;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.CgmesReferences;
import com.powsybl.cgmes.conversion.update.IidmChange;
import com.powsybl.cgmes.conversion.update.IidmToCgmes;
import com.powsybl.cgmes.model.CgmesNames;
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

    public IidmToCgmes16(CgmesReferences cgmesReferences) {
        this.cgmesReferences = cgmesReferences;
        this.generator = new GeneratorToSynchronousMachine();
        this.loadEc = new LoadToEnergyConsumer();
        this.loadEs = new LoadToEnergySource();
        this.loadAm = new LoadToAsynchronousMachine();
        this.line = new LineToACLineSegment();
        this.t2 = new TwoWindingsTransformerToPowerTransformer();
        this.shunt = new ShuntCompensatorToShuntCompensator();
        this.vl = new VoltageLevelToVoltageLevel();
    }

    public IidmToCgmes findConversion(IidmChange change) {
        String cgmesType;
        Identifiable o = change.getIdentifiable();
        if (o instanceof Generator) {
            return generator;
        } else if (o instanceof Load) {
            cgmesType = cgmesReferences.getIdentifiableType(o.getId());
            if (cgmesType.equals(CgmesNames.ENERGY_CONSUMER) ||
                cgmesType.equals(CgmesNames.CONFORM_LOAD)) {
                return loadEc;
            }
            if (cgmesType.equals(CgmesNames.ASYNCHRONOUS_MACHINE)) {
                return loadAm;
            }
            if (cgmesType.equals(CgmesNames.ENERGY_SOURCE)) {
                return loadEs;
            }
            LOG.warn("Currently not supported conversion for type {}", cgmesType);
            return null;
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

    private CgmesReferences cgmesReferences;
    private final IidmToCgmes generator;
    private final IidmToCgmes loadEc;
    private final IidmToCgmes loadEs;
    private final IidmToCgmes loadAm;
    private final IidmToCgmes line;
    private final IidmToCgmes t2;
    private final IidmToCgmes shunt;
    private final IidmToCgmes vl;

    private static final Logger LOG = LoggerFactory.getLogger(IidmToCgmes16.class);
}
