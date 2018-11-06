/**
 * Copyright (c) 2017, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.powsybl.contingency.tasks.AbstractTrippingTask;
import com.powsybl.contingency.tasks.BranchTripping;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class BranchContingency extends AbstractSidedContingency {

    private ContingencyElementType type = ContingencyElementType.BRANCH;

    public BranchContingency(String id) {
        super(id);
    }

    public BranchContingency(String id, String voltageLevelId) {
        super(id, voltageLevelId);
    }

    @Override
    public ContingencyElementType getType() {
        return this.type;
    }

    @Override
    public AbstractTrippingTask toTask() {
        return new BranchTripping(id, voltageLevelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, voltageLevelId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BranchContingency) {
            BranchContingency other = (BranchContingency) obj;
            return id.equals(other.id) && Objects.equals(voltageLevelId, other.voltageLevelId);
        }
        return false;
    }

    private static void checkType(ContingencyElementType type) {
        if (!type.equals(ContingencyElementType.BRANCH) &&
                !type.equals(ContingencyElementType.LINE) &&
                !type.equals(ContingencyElementType.TWO_WINDINGS_TRANSFORMER)) {
            throw new IllegalArgumentException("Type must be BRANCH, LINE or TWO_WINDINGS_TRANSFORMER");
        }
    }

    public BranchContingency setType(ContingencyElementType type) {
        checkType(type);
        this.type = type;
        return this;
    }
}
