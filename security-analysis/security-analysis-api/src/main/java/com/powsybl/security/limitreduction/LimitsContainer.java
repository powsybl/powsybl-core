/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitreduction;

/**
 * <p>Class corresponding to the result of the {@link ReducedLimitsComputer} computation.</p>
 * <p>It contains the original and the altered limits. When no reductions apply, both fields contains the same object.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public interface LimitsContainer<L> {

    L getLimits();

    L getOriginalLimits();

    /**
     * <p>Indicate if the limits are the same as the original ones.</p>
     * @return <code>true</code> if the limits are the same as the original.
     */
    boolean isSameAsOriginal();
}
