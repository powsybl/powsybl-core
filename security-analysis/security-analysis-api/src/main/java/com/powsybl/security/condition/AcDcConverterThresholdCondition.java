/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.condition;

/**
 * Condition triggered by a threshold
 *
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class AcDcConverterThresholdCondition extends AbstractThresholdCondition {

    public static final String NAME = "AC_DC_CONVERTER_THRESHOLD_CONDITION";

    // For AcDcConverter true if AC side, false if DC side
    private final boolean acSide;
    // For AcDcConverter, terminal number
    private final int terminalNumber;

    public AcDcConverterThresholdCondition(double threshold, AbstractThresholdCondition.ComparisonType type, String equipmentId,
                                           AbstractThresholdCondition.Variable variable, boolean acSide, int terminalNumber) {
        super(threshold, type, equipmentId, variable);
        this.acSide = acSide;
        this.terminalNumber = terminalNumber;
    }

    public boolean isAcSide() {
        return acSide;
    }

    public int getTerminalNumber() {
        return terminalNumber;
    }

    @Override
    public String getType() {
        return NAME;
    }
}
