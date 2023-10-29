/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.detectors;

/**
 * Implements the behaviour for limit violation detection with limit reductions.
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */

public class LimitViolationDetectorWithLimitReduction extends DefaultLimitViolationDetector {
    private LimitReductionDefinitionList limitReductionDefinitionList;

    public LimitViolationDetectorWithLimitReduction(LimitReductionDefinitionList limitReductionDefinitionList) {
        this.limitReductionDefinitionList = limitReductionDefinitionList;
    }
}
