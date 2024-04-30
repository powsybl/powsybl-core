/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitreduction.computation;

import com.powsybl.security.limitreduction.AbstractLimitReductionsApplier;

/**
 * <p>Interface for objects generating an {@link AbstractLimitsReducer} object
 * configured for a network element, with its original limits of generic type {@link L}.</p>
 * <p>{@link AbstractLimitReductionsApplier} implementations dealing with limits of type {@link L} need
 * an implementation of this interface to compute reduced limits.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public interface AbstractLimitsReducerCreator<L, R extends AbstractLimitsReducer<L>> {
    R create(String networkElementId, L originalLimits);
}
