/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.stateestimation;

/**
 * A quantity of a network element that measurements determine.
 *
 * <p>The quantities modeled by {@code InjectionObservability} and {@code BranchObservability}, so
 * that a result carrying them can be published onto the network without translating it first.
 * Shared by {@link ObservabilityResult} and {@link StateEstimationResult}, which report different
 * things about the same quantities.
 *
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
public enum Quantity {
    /** Active power, in MW. */
    ACTIVE_POWER,
    /** Reactive power, in MVar. */
    REACTIVE_POWER,
    /** Voltage magnitude, in kV. */
    VOLTAGE
}
