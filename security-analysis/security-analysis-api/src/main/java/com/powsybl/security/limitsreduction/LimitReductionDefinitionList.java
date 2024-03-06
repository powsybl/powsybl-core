/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitsreduction;

import java.util.*;

/**
 * Contains the definitions of the applied limit reductions.
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */

public class LimitReductionDefinitionList {

    private List<LimitReductionDefinition> limitReductionDefinitions = Collections.emptyList();

    public List<LimitReductionDefinition> getLimitReductionDefinitions() {
        return Collections.unmodifiableList(limitReductionDefinitions);
    }

    public LimitReductionDefinitionList setLimitReductionDefinitions(List<LimitReductionDefinition> definitions) {
        limitReductionDefinitions = Objects.requireNonNull(definitions);
        return this;
    }
}
