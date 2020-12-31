/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.model.pf.PsseBus;
import com.powsybl.psse.model.pf.PsseGenerator;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class GeneratorConverter extends AbstractConverter {

    public GeneratorConverter(PsseGenerator psseGenerator, ContainersMapping containerMapping, Network network) {
        super(containerMapping, network);
        this.psseGenerator = psseGenerator;
    }

    public void create(PsseBus psseBus) {
        String busId = getBusId(psseGenerator.getI());
        VoltageLevel voltageLevel = getNetwork().getVoltageLevel(getContainersMapping().getVoltageLevelId(psseGenerator.getI()));
        Generator generator =  voltageLevel.newGenerator()
                .setId(getGeneratorId(busId))
                .setConnectableBus(busId)
                .setTargetP(psseGenerator.getPg())
                .setMaxP(psseGenerator.getPt())
                .setMinP(psseGenerator.getPb())
                .setVoltageRegulatorOn(false)
                .setTargetQ(psseGenerator.getQg())
                .add();

        generator.newMinMaxReactiveLimits()
                .setMinQ(psseGenerator.getQb())
                .setMaxQ(psseGenerator.getQt())
                .add();

        if (psseBus.getIde() != 3) {
            // The "if" added to be compliant with the IEEE 24 case where type 3 bus is regulating out of its Qmin Qmax
            // Assuming this is true in general for all PSSE cases for type 3 buses
            generator.newMinMaxReactiveLimits()
                    .setMinQ(psseGenerator.getQb())
                    .setMaxQ(psseGenerator.getQt())
                    .add();
        }

        if (psseGenerator.getStat() == 1) {
            generator.getTerminal().connect();
        }

        if (psseGenerator.getVs() > 0 && ((psseGenerator.getQt() - psseGenerator.getQb()) > 0.002 || psseBus.getIde() == 3)) {
            if (psseGenerator.getIreg() == 0) {
                //PV group
                generator.setTargetV(psseGenerator.getVs() * voltageLevel.getNominalV());
                generator.setVoltageRegulatorOn(true);
                generator.setTargetQ(psseGenerator.getQg());
            } else {
                //TODO : implement remote voltage control regulation
                LOGGER.warn("Remote Voltage control not supported ({})", generator.getId());
            }
        }
        //TODO: take into account zr zx Mbase...
    }

    private String getGeneratorId(String busId) {
        return busId + "-G" + psseGenerator.getId();
    }

    private final PsseGenerator psseGenerator;

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratorConverter.class);
}
