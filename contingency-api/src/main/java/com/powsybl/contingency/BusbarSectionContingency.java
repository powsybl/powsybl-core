/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.contingency;

import eu.itesla_project.contingency.tasks.BusbarSectionTripping;
import eu.itesla_project.contingency.tasks.AbstractTrippingTask;

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
}
