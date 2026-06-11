/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.strategy.condition;

import com.powsybl.iidm.network.TerminalNumber;

/**
 * Condition on an acdc converter triggered by a threshold
 *
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class AcDcConverterThresholdCondition extends AbstractThresholdCondition {

    public static final String NAME = "AC_DC_CONVERTER_THRESHOLD_CONDITION";

    // True to target AC side, false for DC side
    private final boolean acSide;
    // Terminal number to target
    private final TerminalNumber terminalNumber;

    public AcDcConverterThresholdCondition(String equipmentId, Variable variable, ComparisonType comparisonType,
                                           double threshold, boolean acSide, TerminalNumber terminalNumber) {
        super(equipmentId, variable, comparisonType, threshold);
        this.acSide = acSide;
        this.terminalNumber = terminalNumber;
    }

    public boolean isAcSide() {
        return acSide;
    }

    public TerminalNumber getTerminalNumber() {
        return terminalNumber;
    }

    @Override
    public String getType() {
        return NAME;
    }
}
