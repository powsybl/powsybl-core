/**
 * Copyright (c) 2017, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import com.powsybl.iidm.modification.tripping.BranchTripping;
import com.powsybl.iidm.modification.tripping.Tripping;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class BranchContingency extends AbstractSidedContingency {

    public BranchContingency(String id) {
        super(id);
    }

    public BranchContingency(String id, String voltageLevelId) {
        super(id, voltageLevelId);
    }

    @Override
    public ContingencyElementType getType() {
        return ContingencyElementType.BRANCH;
    }

    @Override
    public Tripping toModification() {
        return new BranchTripping(id, voltageLevelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, voltageLevelId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BranchContingency other) {
            return id.equals(other.id) && Objects.equals(voltageLevelId, other.voltageLevelId);
        }
        return false;
    }
}
