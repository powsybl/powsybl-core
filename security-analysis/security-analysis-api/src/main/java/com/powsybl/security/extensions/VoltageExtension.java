/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.extensions;

import com.powsybl.commons.extensions.AbstractPrecontingencyValueExtension;
import com.powsybl.security.LimitViolation;

/**
 * Extension to handle pre-contingency voltage value for a voltage limit violation
 *
 * @author Olivier Bretteville {@literal <olivier.bretteville at rte-france.com>}
 */
public class VoltageExtension extends AbstractPrecontingencyValueExtension<LimitViolation> {

    public VoltageExtension(double value) {
        super(value);
    }

    @Override
    public String getName() {
        return "Voltage";
    }
}
