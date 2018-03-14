/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.powsybl.contingency.tasks.BusbarSectionTripping;
import com.powsybl.contingency.tasks.AbstractTrippingTask;

import java.util.Objects;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class BusbarSectionContingency implements ContingencyElement {

    private final String busbarSectionId;

    public BusbarSectionContingency(String busbarSectionId) {
        this.busbarSectionId = Objects.requireNonNull(busbarSectionId);
    }

    @Override
    public String getId() {
        return busbarSectionId;
    }

    @Override
    public ContingencyElementType getType() {
        return ContingencyElementType.BUSBAR_SECTION;
    }

    @Override
    public AbstractTrippingTask toTask() {
        return new BusbarSectionTripping(busbarSectionId);
    }

    @Override
    public int hashCode() {
        return busbarSectionId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BusbarSectionContingency) {
            BusbarSectionContingency other = (BusbarSectionContingency) obj;
            return busbarSectionId.equals(other.busbarSectionId);
        }
        return false;
    }
}
