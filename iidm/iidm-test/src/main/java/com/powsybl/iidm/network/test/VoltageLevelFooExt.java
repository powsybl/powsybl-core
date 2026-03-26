/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.test;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.VoltageLevel;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class VoltageLevelFooExt extends AbstractExtension<VoltageLevel> {

    public VoltageLevelFooExt(VoltageLevel voltagelLevel) {
        super(voltagelLevel);
    }

    @Override
    public String getName() {
        return "voltageLevelFoo";
    }
}
