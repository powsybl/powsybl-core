/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.powsybl.network.modification.tripping.AbstractTripping;
import com.powsybl.network.modification.tripping.LoadTripping;

/**
 * @author Hadrien Godard <hadrien.godard at artelys.com>
 */
public class LoadContingency extends AbstractInjectionContingency {

    public LoadContingency(String id) {
        super(id);
    }

    @Override
    public ContingencyElementType getType() {
        return ContingencyElementType.LOAD;
    }

    @Override
    public AbstractTripping toTask() {
        return new LoadTripping(id);
    }

}
