/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.action;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class DanglingLineActionBuilder extends AbstractLoadActionBuilder<DanglingLineAction> {
    public DanglingLineAction build() {
        if (relativeValue == null) {
            throw new IllegalArgumentException("For a load action, relativeValue must be provided");
        }
        if (activePowerValue == null && reactivePowerValue == null) {
            throw new IllegalArgumentException("For a load action, activePowerValue or reactivePowerValue must be provided");
        }
        return new DanglingLineAction(id, elementId, relativeValue, activePowerValue, reactivePowerValue);
    }
}
