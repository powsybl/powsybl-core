/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface PhaseTapChangerHolder {

    /**
     * Get a builder to create and associate a phase tap changer to the
     * transformer.
     */
    PhaseTapChangerAdder newPhaseTapChanger();

    /**
     * Get a builder to create and associate a phase tap changer to the
     * transformer. The builder is initialized with all the values of the given phase tap changer.
     */
    default PhaseTapChangerAdder newPhaseTapChanger(PhaseTapChanger ptc) {
        Objects.requireNonNull(ptc);
        PhaseTapChangerAdder adder = newPhaseTapChanger()
                .setLowTapPosition(ptc.getLowTapPosition())
                .setRegulating(ptc.isRegulating())
                .setRegulationMode(ptc.getRegulationMode())
                .setTapPosition(ptc.getTapPosition())
                .setRegulationTerminal(ptc.getRegulationTerminal())
                .setRegulationValue(ptc.getRegulationValue())
                .setTargetDeadband(ptc.getTargetDeadband());
        ptc.getAllSteps().forEach((i, step) -> adder.beginStep()
                .setAlpha(step.getAlpha())
                .setRho(step.getRho())
                .setR(step.getR())
                .setX(step.getX())
                .setG(step.getG())
                .setB(step.getB())
                .endStep());
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
     *  Check if a phase tap changer is present
     */
    default boolean hasPhaseTapChanger() {
        return getPhaseTapChanger() != null;
    }
}
