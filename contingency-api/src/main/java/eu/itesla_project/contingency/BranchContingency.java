/**
 * Copyright (c) 2017, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.contingency;

import eu.itesla_project.contingency.tasks.BranchTripping;
import eu.itesla_project.contingency.tasks.ModificationTask;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class BranchContingency implements ContingencyElement {

    private final String id;
    private final String substationId;

    public BranchContingency(String id) {
        this(id, null);
    }

    public BranchContingency(String id, String substationId) {
        this.id = Objects.requireNonNull(id);
        this.substationId = substationId;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getSubstationId() {
        return substationId;
    }

    @Override
    public ContingencyElementType getType() {
        return ContingencyElementType.BRANCH;
    }

    @Override
    public ModificationTask toTask() {
        return new BranchTripping(id, substationId);
    }

}
