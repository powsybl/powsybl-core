/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import com.powsybl.iidm.modification.tripping.ShuntCompensatorTripping;
import com.powsybl.iidm.modification.tripping.Tripping;

/**
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
public class ShuntCompensatorContingency extends AbstractContingency {

    public ShuntCompensatorContingency(String id) {
        super(id);
    }

    @Override
    public ContingencyElementType getType() {
        return ContingencyElementType.SHUNT_COMPENSATOR;
    }

    @Override
    public Tripping toModification() {
        return new ShuntCompensatorTripping(id);
    }
}
