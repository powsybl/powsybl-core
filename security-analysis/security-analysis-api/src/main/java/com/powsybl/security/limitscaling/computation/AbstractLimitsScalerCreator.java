/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitscaling.computation;

import com.powsybl.security.limitscaling.AbstractLimitScalingsApplier;

/**
 * <p>Interface for objects generating an {@link AbstractLimitsScaler} object
 * configured for a network element, with its original limits of generic type {@link L}.</p>
 * <p>{@link AbstractLimitScalingsApplier} implementations dealing with limits of type {@link L} need
 * an implementation of this interface to compute scaled limits.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public interface AbstractLimitsScalerCreator<L, R extends AbstractLimitsScaler<L>> {
    R create(String networkElementId, String limitsGroupId, L originalLimits);
}
