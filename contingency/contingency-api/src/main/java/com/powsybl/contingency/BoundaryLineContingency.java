/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.powsybl.iidm.modification.tripping.BoundaryLineTripping;
import com.powsybl.iidm.modification.tripping.Tripping;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class BoundaryLineContingency extends AbstractContingency {

    public BoundaryLineContingency(String id) {
        super(id);
    }

    @Override
    public ContingencyElementType getType() {
        return ContingencyElementType.BOUNDARY_LINE;
    }

    @Override
    public Tripping toModification() {
        return new BoundaryLineTripping(id);
    }
}
