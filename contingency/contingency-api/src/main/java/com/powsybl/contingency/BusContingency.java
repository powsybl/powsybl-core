/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import com.powsybl.iidm.modification.tripping.BusTripping;
import com.powsybl.iidm.modification.tripping.Tripping;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class BusContingency extends AbstractContingency {

    public BusContingency(String id) {
        super(id);
    }

    @Override
    public ContingencyElementType getType() {
        return ContingencyElementType.BUS;
    }

    @Override
    public Tripping toModification() {
        return new BusTripping(id);
    }

}
