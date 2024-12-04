/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface PhaseTapChangerHolder {

    /**
     * Get a builder to create and associate a phase tap changer to the
     * transformer.
     */
    PhaseTapChangerAdder newPhaseTapChanger();

    /**
     * Get a builder to create and associate a phase tap changer to the
     * transformer, initialized with the values of an existing ratio tap changer.
     */
    default PhaseTapChangerAdder newPhaseTapChanger(PhaseTapChanger phaseTapChanger) {
        PhaseTapChangerAdder adder = this.newPhaseTapChanger()
                .setRegulationTerminal(phaseTapChanger.getRegulationTerminal())
                .setRegulationMode(phaseTapChanger.getRegulationMode())
                .setRegulationValue(phaseTapChanger.getRegulationValue())
                .setLowTapPosition(phaseTapChanger.getLowTapPosition())
                .setTapPosition(phaseTapChanger.getTapPosition())
                .setRegulating(phaseTapChanger.isRegulating())
                .setTargetDeadband(phaseTapChanger.getTargetDeadband());
        for (int tapPosition = phaseTapChanger.getLowTapPosition(); tapPosition <= phaseTapChanger.getHighTapPosition(); tapPosition++) {
            PhaseTapChangerStep step = phaseTapChanger.getStep(tapPosition);
            adder.beginStep()
                    .setAlpha(step.getAlpha())
                    .setRho(step.getRho())
                    .setB(step.getB())
                    .setG(step.getG())
                    .setX(step.getX())
                    .setR(step.getR())
                    .endStep();
        }
        return adder;
    }

    /**
     * Get the phase tap changer.
     * <p>Could return <code>null</code> if the transfomer is not associated to
     * a phase tap changer.
     */
    PhaseTapChanger getPhaseTapChanger();

    /**
     * Get the optional ratio tap changer.
     */
    default Optional<PhaseTapChanger> getOptionalPhaseTapChanger() {
        return Optional.ofNullable(getPhaseTapChanger());
    }

    /**
     * Check if a phase tap changer is present
     */
    default boolean hasPhaseTapChanger() {
        return getPhaseTapChanger() != null;
    }
}
