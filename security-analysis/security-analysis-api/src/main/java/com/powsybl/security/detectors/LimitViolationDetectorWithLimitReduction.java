/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.detectors;

import com.powsybl.security.LimitViolation;

import java.util.List;
import java.util.function.Consumer;

/**
 * Implements the behaviour for limit violation detection with limit reductions.
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */

public class LimitViolationDetectorWithLimitReduction extends DefaultLimitViolationDetector {

    public void checkLimitViolationWithLimitReductions(LimitReductionDefinitionList limitReductionDefinitionList, Consumer<LimitViolation> consumer) {
        List<LimitReductionDefinitionList.LimitReductionDefinition> limitReductionDefinitions = limitReductionDefinitionList.getLimitReductionDefinitions();

        for (LimitReductionDefinitionList.LimitReductionDefinition limitReductionDefinition : limitReductionDefinitions) {

        }

    }

}
