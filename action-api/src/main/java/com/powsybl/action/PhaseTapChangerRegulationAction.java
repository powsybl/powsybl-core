/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action;

import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.ThreeSides;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * An action modifying the regulation of a phase-shifting transformer
 *
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public class PhaseTapChangerRegulationAction extends AbstractTapChangerRegulationAction {

    public static final String NAME = "PHASE_TAP_CHANGER_REGULATION";
    private final PhaseTapChanger.RegulationMode regulationMode;
    private final Double regulationValue;

    public PhaseTapChangerRegulationAction(String id, String transformerId, ThreeSides side, boolean regulating, PhaseTapChanger.RegulationMode regulationMode, Double regulationValue) {
        super(id, transformerId, side, regulating);
        this.regulationMode = regulationMode;
        this.regulationValue = regulationValue;
        if (!regulating && this.regulationMode != null) {
            throw new IllegalArgumentException("PhaseTapChangerRegulationAction can not have a regulation mode " +
                    "if it is not regulating");
        }
    }

    @Override
    public String getType() {
        return NAME;
    }

    public Optional<PhaseTapChanger.RegulationMode> getRegulationMode() {
        return Optional.ofNullable(regulationMode);
    }

    public OptionalDouble getRegulationValue() {
        return regulationValue == null ? OptionalDouble.empty() : OptionalDouble.of(regulationValue);
    }

    public static PhaseTapChangerRegulationAction activateRegulation(String id, String transformerId) {
        return new PhaseTapChangerRegulationAction(id, transformerId, null, true, null, null);
    }

    public static PhaseTapChangerRegulationAction activateRegulation(String id, String transformerId, ThreeSides side) {
        return new PhaseTapChangerRegulationAction(id, transformerId, side, true, null, null);
    }

    public static PhaseTapChangerRegulationAction activateAndChangeRegulationMode(String id, String transformerId, PhaseTapChanger.RegulationMode regulationMode, Double regulationValue) {
        return new PhaseTapChangerRegulationAction(id, transformerId, null, true, regulationMode, regulationValue);
    }

    public static PhaseTapChangerRegulationAction activateAndChangeRegulationMode(String id, String transformerId, ThreeSides side, PhaseTapChanger.RegulationMode regulationMode, Double regulationValue) {
        return new PhaseTapChangerRegulationAction(id, transformerId, side, true, regulationMode, regulationValue);
    }

    public static PhaseTapChangerRegulationAction deactivateRegulation(String id, String transformerId) {
        return new PhaseTapChangerRegulationAction(id, transformerId, null, false, null, null);
    }

    public static PhaseTapChangerRegulationAction deactivateRegulation(String id, String transformerId, ThreeSides side) {
        return new PhaseTapChangerRegulationAction(id, transformerId, side, false, null, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        PhaseTapChangerRegulationAction that = (PhaseTapChangerRegulationAction) o;
        return regulationMode == that.regulationMode && Objects.equals(regulationValue, that.regulationValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), regulationMode, regulationValue);
    }
}
