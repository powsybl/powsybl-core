/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.action;

import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.ThreeWindingsTransformer;

/**
 *  An action modifying the regulation of a phase-shifting transformer
 *
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class PhaseTapChangerRegulationAction extends AbstractTapChangerRegulationAction {

    public static final String NAME = "PHASE_TAP_CHANGER_REGULATION";
    private final PhaseTapChanger.RegulationMode regulationMode;

    public PhaseTapChangerRegulationAction(String id, String transformerId, ThreeWindingsTransformer.Side side, boolean regulating, PhaseTapChanger.RegulationMode regulationMode) {
        super(id, transformerId, side, regulating);
        this.regulationMode = regulationMode;
    }

    @Override
    public String getType() {
        return NAME;
    }

    public PhaseTapChanger.RegulationMode getRegulationMode() {
        return regulationMode;
    }
}
