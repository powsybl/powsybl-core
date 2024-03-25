/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation;

/**
 * Computation manager factories are in charge of creating instances of {@link ComputationManager}.
 *
 * In particular, they may be referenced in configuration to define what computation managers
 * implementation should be used for computation.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface ComputationManagerFactory {

    /**
     * Creates a new instance of {@link ComputationManager}.
     *
     * @return a new instance of {@link ComputationManager}.
     */
    ComputationManager create();
}
