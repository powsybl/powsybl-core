/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import com.powsybl.iidm.network.ThreeSides;

import java.util.OptionalDouble;

/**
 * An action activating or deactivating the regulation of a ratio transformer
 *
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public class RatioTapChangerRegulationAction extends AbstractTapChangerRegulationAction {

    public static final String NAME = "RATIO_TAP_CHANGER_REGULATION";
    private final Double targetV;

    public RatioTapChangerRegulationAction(String id, String transformerId, boolean regulating, Double targetV) {
        this(id, transformerId, null, regulating, targetV);
    }

    public RatioTapChangerRegulationAction(String id, String transformerId, ThreeSides side, boolean regulating, Double targetV) {
        super(id, transformerId, side, regulating);
        this.targetV = targetV;
    }

    @Override
    public String getType() {
        return NAME;
    }

    public static RatioTapChangerRegulationAction activateRegulationAndChangeTargetV(String id, String transformerId, Double targetV) {
        return new RatioTapChangerRegulationAction(id, transformerId, null, true, targetV);
    }

    public static RatioTapChangerRegulationAction activateRegulation(String id, String transformerId) {
        return new RatioTapChangerRegulationAction(id, transformerId, null, true, null);
    }

    public static RatioTapChangerRegulationAction activateRegulationAndChangeTargetV(String id, String transformerId, ThreeSides side, Double targetV) {
        return new RatioTapChangerRegulationAction(id, transformerId, side, true, targetV);
    }

    public static RatioTapChangerRegulationAction activateRegulation(String id, String transformerId, ThreeSides side) {
        return new RatioTapChangerRegulationAction(id, transformerId, side, true, null);
    }

    public static RatioTapChangerRegulationAction deactivateRegulation(String id, String transformerId) {
        return new RatioTapChangerRegulationAction(id, transformerId, null, false, null);
    }

    public static RatioTapChangerRegulationAction deactivateRegulation(String id, String transformerId, ThreeSides side) {
        return new RatioTapChangerRegulationAction(id, transformerId, side, false, null);
    }

    public OptionalDouble getTargetV() {
        return targetV == null ? OptionalDouble.empty() : OptionalDouble.of(targetV);
    }
}
