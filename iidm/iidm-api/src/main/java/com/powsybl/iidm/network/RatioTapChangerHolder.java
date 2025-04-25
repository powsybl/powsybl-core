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
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface RatioTapChangerHolder {

    /**
     * Get a builder to create and associate a ratio tap changer to the
     * transformer.
     */
    RatioTapChangerAdder newRatioTapChanger();

    /**
     * Get a builder to create and associate a ratio tap changer to the
     * transformer, initialized with the values of an existing ratio tap changer.
     */
    default RatioTapChangerAdder newRatioTapChanger(RatioTapChanger ratioTapChanger) {
        RatioTapChangerAdder adder = this.newRatioTapChanger()
                .setRegulationTerminal(ratioTapChanger.getRegulationTerminal())
                .setRegulationMode(ratioTapChanger.getRegulationMode())
                .setRegulationValue(ratioTapChanger.getRegulationValue())
                .setLoadTapChangingCapabilities(ratioTapChanger.hasLoadTapChangingCapabilities())
                .setTargetV(ratioTapChanger.getTargetV())
                .setLowTapPosition(ratioTapChanger.getLowTapPosition())
                .setTapPosition(ratioTapChanger.getTapPosition())
                .setRegulating(ratioTapChanger.isRegulating())
                .setTargetDeadband(ratioTapChanger.getTargetDeadband());
        for (int tapPosition = ratioTapChanger.getLowTapPosition(); tapPosition <= ratioTapChanger.getHighTapPosition(); tapPosition++) {
            RatioTapChangerStep step = ratioTapChanger.getStep(tapPosition);
            adder.beginStep()
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
     * Get the ratio tap changer.
     * <p>Could return <code>null</code> if the leg is not associated to a ratio
     * tap changer.
     */
    RatioTapChanger getRatioTapChanger();

    /**
     * Get the optional ratio tap changer.
     */
    default Optional<RatioTapChanger> getOptionalRatioTapChanger() {
        return Optional.ofNullable(getRatioTapChanger());
    }

    /**
     * Check if a phase tap changer is present
     */
    default boolean hasRatioTapChanger() {
        return getRatioTapChanger() != null;
    }
}
