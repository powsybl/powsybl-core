/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Injection;

import java.util.Optional;

/**
 * @author Ghiles Abdellah {@literal <ghiles.abdellah at rte-france.com>}
 */
public interface ActivePowerControl<I extends Injection<I>> extends Extension<I> {

    String NAME = "activePowerControl";

    @Override
    default String getName() {
        return NAME;
    }

    boolean isParticipate();

    void setParticipate(boolean participate);

    /**
     * This is the change in generator power output divided by the change in frequency
     * normalized by the nominal power of the generator and the nominal frequency and
     * expressed in percent and negated. A positive value of speed change droop provides
     * additional generator output upon a drop in frequency.
     * @return Governor Speed Changer Droop.
     */
    double getDroop();

    /**
     * @param droop new Governor Speed Changer Droop value
     */
    void setDroop(double droop);

    /**
     * Generating unit participation factor.
     * The sum of the participation factors across generating units does not have to sum to one.
     * It is used for representing distributed slack participation factor.
     * The attribute shall be a positive value or zero.
     * @return Generating unit participation factor.
     */
    double getParticipationFactor();

    /**
     * @param participationFactor new Generating unit participation factor value
     */
    void setParticipationFactor(double participationFactor);

    /**
     * @return if present, provides the overridden value of pmin to be used for active power control operations.
     */
    Optional<Double> getMinPOverride();

    /**
     * Sets the overridden minimal active power.
     * @param pMinOverride  The overridden value of Pmin. A Nan value removes the override.
     */
    void setMinPOverride(double pMinOverride);

    /**
     * @return if present, provides the overridden value of pmax to be used for active power control operations.
     */
    Optional<Double> getMaxPOverride();

    /**
     * Sets the overridden maximal active power.
     * @param pMaxOverride The overridden value of Pmax. A Nan value removes the override.
     */
    void setMaxPOverride(double pMaxOverride);
}
