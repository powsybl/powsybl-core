/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.detectors;

import com.powsybl.contingency.ContingencyContext;
import com.powsybl.iidm.network.LimitType;

import java.util.List;

/**
 * Contains the definitions of the applied limit reductions.
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */

public class LimitReductionDefinitionList {

    private List<LimitReductionDefinition> limitReductionDefinitions;

    public class LimitReductionDefinition {
        private double limitReduction;
        private LimitType limitType;
        private List<ContingencyContext> contingencyContexts;
        private List<BranchCriterion> branchCriteria;
        private List<LimitDurationCriterion> durationCriteria;
    }
}
