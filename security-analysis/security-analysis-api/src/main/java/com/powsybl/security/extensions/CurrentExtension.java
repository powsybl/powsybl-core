/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.extensions;

import com.powsybl.commons.extensions.AbstractPrecontingencyValueExtension;
import com.powsybl.security.LimitViolation;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class CurrentExtension extends AbstractPrecontingencyValueExtension<LimitViolation> {

    public CurrentExtension(double value) {
        super(value);
    }

    @Override
    public String getName() {
        return "Current";
    }
}
