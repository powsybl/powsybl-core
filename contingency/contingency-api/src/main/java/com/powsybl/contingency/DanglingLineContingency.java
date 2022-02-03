/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.powsybl.network.modification.tripping.AbstractTripping;
import com.powsybl.network.modification.tripping.DanglingLineTripping;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class DanglingLineContingency extends AbstractInjectionContingency {

    public DanglingLineContingency(String id) {
        super(id);
    }

    @Override
    public ContingencyElementType getType() {
        return ContingencyElementType.DANGLING_LINE;
    }

    @Override
    public AbstractTripping toModification() {
        return new DanglingLineTripping(id);
    }
}
