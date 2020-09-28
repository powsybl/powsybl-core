/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.converter;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.TieLine;
import com.powsybl.iidm.reducer.ReducerNamingStrategy;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class XnodeReducerNamingStrategy implements ReducerNamingStrategy {
    @Override
    public String getReplacementId(Branch<?> branch) {
        if (branch instanceof TieLine) {
            TieLine tieLine = (TieLine) branch;
            return tieLine.getUcteXnodeCode();
        } else {
            return branch.getId();
        }
    }
}
