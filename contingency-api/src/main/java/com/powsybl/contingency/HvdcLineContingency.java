/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.powsybl.contingency.tasks.AbstractTrippingTask;
import com.powsybl.contingency.tasks.HvdcLineTripping;

import java.util.Objects;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class HvdcLineContingency implements ContingencyElement {

    private final String id;

    private final String voltageLevelId;

    public HvdcLineContingency(String id) {
        this(id, null);
    }

    public HvdcLineContingency(String id, String voltageLevelId) {
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
        return ContingencyElementType.HVDC_LINE;
    }

    @Override
    public AbstractTrippingTask toTask() {
        return new HvdcLineTripping(id, voltageLevelId);
    }
}
