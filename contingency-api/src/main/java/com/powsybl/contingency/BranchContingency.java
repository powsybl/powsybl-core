/**
 * Copyright (c) 2017, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.powsybl.contingency.tasks.BranchTripping;
import com.powsybl.contingency.tasks.AbstractTrippingTask;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class BranchContingency implements ContingencyElement {

    private final String id;
    private final String voltageLevelId;

    public BranchContingency(String id) {
        this(id, null);
    }

    public BranchContingency(String id, String voltageLevelId) {
        this.id = Objects.requireNonNull(id);
        this.voltageLevelId = voltageLevelId;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getVoltageLevelId() {
        return voltageLevelId;
    }

    @Override
    public ContingencyElementType getType() {
        return ContingencyElementType.BRANCH;
    }

    @Override
    public AbstractTrippingTask toTask() {
        return new BranchTripping(id, voltageLevelId);
    }

}
