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
public interface RatioTapChangerHolder {

    /**
     * Get a builder to create and associate a ratio tap changer to the
     * transformer.
     */
    RatioTapChangerAdder newRatioTapChanger();

    /**
     * Get a builder to create and associate a ratio tap changer to the
     * transformer. The builder is initialized with all the values of the given ratio tap changer.
     */
    default RatioTapChangerAdder newRatioTapChanger(RatioTapChanger rtc) {
        Objects.requireNonNull(rtc);
        RatioTapChangerAdder adder = newRatioTapChanger()
                .setLoadTapChangingCapabilities(rtc.hasLoadTapChangingCapabilities())
                .setLowTapPosition(rtc.getLowTapPosition())
                .setRegulating(rtc.isRegulating())
                .setTapPosition(rtc.getTapPosition())
                .setRegulationTerminal(rtc.getRegulationTerminal())
                .setTargetDeadband(rtc.getTargetDeadband())
                .setTargetV(rtc.getTargetV());
        rtc.getAllSteps().forEach((i, step) -> adder.beginStep()
                .setRho(step.getRho())
                .setR(step.getR())
                .setX(step.getX())
                .setG(step.getG())
                .setB(step.getB())
                .endStep());
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
