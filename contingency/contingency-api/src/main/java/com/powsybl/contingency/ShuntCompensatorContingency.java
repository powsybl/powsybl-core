/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.powsybl.contingency.tasks.AbstractTrippingTask;
import com.powsybl.contingency.tasks.ShuntCompensatorTripping;

/**
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class ShuntCompensatorContingency extends AbstractInjectionContingency {

    public ShuntCompensatorContingency(String id) {
        super(id);
    }

    @Override
    public ContingencyElementType getType() {
        return ContingencyElementType.SHUNT_COMPENSATOR;
    }

    @Override
    public AbstractTrippingTask toTask() {
        return new ShuntCompensatorTripping(id);
    }
}
