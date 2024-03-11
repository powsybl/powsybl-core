/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitreduction;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

/**
 * Contains the definitions of the applied limit reductions.
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */

public class LimitReductionDefinitionList {
    public static final String VERSION = "1.0";
    private final List<LimitReductionDefinition> limitReductionDefinitions;

    public LimitReductionDefinitionList(List<LimitReductionDefinition> limitReductionDefinitions) {
        this.limitReductionDefinitions = ImmutableList.copyOf(Objects.requireNonNull(limitReductionDefinitions));
    }

    public List<LimitReductionDefinition> getLimitReductionDefinitions() {
        return limitReductionDefinitions;
    }
}
