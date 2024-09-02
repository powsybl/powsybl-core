/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion;

import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerPhaseAngleClockAdder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerPhaseAngleClockAdder;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.TripleStore;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
@AutoService(CgmesImportPostProcessor.class)
public class PhaseAngleClock implements CgmesImportPostProcessor {

    @Override
    public String getName() {
        return "PhaseAngleClock";
    }

    @Override
    public void process(Network network, TripleStore tripleStore) {
        CgmesModelExtension cgmesExtension = network.getExtension(CgmesModelExtension.class);
        if (cgmesExtension == null) {
            LOG.warn("PhaseAngleClock-PostProcessor: Unexpected null cgmesExtension pointer");
            return;
        }
        CgmesModel cgmes = cgmesExtension.getCgmesModel();
        if (cgmes == null) {
            LOG.warn("PhaseAngleClock-PostProcessor: Unexpected null cgmesModel pointer");
            return;
        }

        cgmes.groupedTransformerEnds().forEach((t, ends) -> {
            if (ends.size() == 2) {
                phaseAngleClockTwoWindingTransformer(ends, network);
            } else if (ends.size() == 3) {
                phaseAngleClockThreeWindingTransformer(ends, network);
            } else {
                throw new PowsyblException(String.format("Unexpected TransformerEnds: ends %d", ends.size()));
            }
        });
    }

    private void phaseAngleClockTwoWindingTransformer(PropertyBags ends, Network network) {
        PropertyBag end1 = ends.get(0);
        PropertyBag end2 = ends.get(1);

        int phaseAngleClock1 = end1.asInt(CgmesNames.PHASE_ANGLE_CLOCK, 0);
        int phaseAngleClock2 = end2.asInt(CgmesNames.PHASE_ANGLE_CLOCK, 0);
        if (phaseAngleClock1 == 0 && phaseAngleClock2 == 0) {
            return;
        }

        String id = end1.getId(CgmesNames.POWER_TRANSFORMER);
        TwoWindingsTransformer tx = (TwoWindingsTransformer) network.getIdentifiable(id);
        if (tx != null) {
            if (phaseAngleClock1 != 0) {
                String reason = "Unsupported modelling: twoWindingsTransformer with phaseAngleClock at end1";
                String what = "PhaseAngleClock-PostProcessor";
                LOG.warn("Ignored {}. Reason: {}.", what, reason);
            }
            if (phaseAngleClock2 != 0) {
                tx.newExtension(TwoWindingsTransformerPhaseAngleClockAdder.class).withPhaseAngleClock(phaseAngleClock2).add();
            }
        }
    }

    private void phaseAngleClockThreeWindingTransformer(PropertyBags ends, Network network) {
        PropertyBag end1 = ends.get(0);
        PropertyBag end2 = ends.get(1);
        PropertyBag end3 = ends.get(2);

        int phaseAngleClock1 = end1.asInt(CgmesNames.PHASE_ANGLE_CLOCK, 0);
        int phaseAngleClock2 = end2.asInt(CgmesNames.PHASE_ANGLE_CLOCK, 0);
        int phaseAngleClock3 = end3.asInt(CgmesNames.PHASE_ANGLE_CLOCK, 0);
        if (phaseAngleClock1 == 0 && phaseAngleClock2 == 0 && phaseAngleClock3 == 0) {
            return;
        }

        String id = end1.getId(CgmesNames.POWER_TRANSFORMER);
        ThreeWindingsTransformer tx = (ThreeWindingsTransformer) network.getIdentifiable(id);
        if (tx != null) {
            if (phaseAngleClock1 != 0) {
                String reason = "Unsupported modelling: threeWindingsTransformer with phaseAngleClock at end1";
                String what = "PhaseAngleClock-PostProcessor";
                LOG.warn("Ignored {}. Reason: {}.", what, reason);
            }

            if (phaseAngleClock2 != 0 || phaseAngleClock3 != 0) {
                tx.newExtension(ThreeWindingsTransformerPhaseAngleClockAdder.class)
                        .withPhaseAngleClockLeg2(phaseAngleClock2).withPhaseAngleClockLeg3(phaseAngleClock3).add();

            }
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(PhaseAngleClock.class);
}

