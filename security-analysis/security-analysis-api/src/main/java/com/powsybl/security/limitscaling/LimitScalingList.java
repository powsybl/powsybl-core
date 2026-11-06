/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitscaling;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

/**
 * Contains the list of the applied limit scalings.
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */

public class LimitScalingList {
    // v1.2 introduces limit increase (reduction with value above 1)
    // v1.3 renames limitReductions to limitScalings
    public static final String VERSION = "1.3";
    private final List<LimitScaling> limitScalings;

    public LimitScalingList(List<LimitScaling> limitScalings) {
        this.limitScalings = ImmutableList.copyOf(Objects.requireNonNull(limitScalings));
    }

    public List<LimitScaling> getLimitScalings() {
        return limitScalings;
    }
}
