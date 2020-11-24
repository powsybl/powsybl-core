/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.contingency;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class ContingencyBuilder {

    private final String id;

    private final List<ContingencyElement> elements;

    ContingencyBuilder(String id) {
        this.id = Objects.requireNonNull(id);
        this.elements = new ArrayList<>();
    }

    public Contingency build() {
        return new Contingency(id, elements);
    }

    public ContingencyBuilder addBranch(String id) {
        elements.add(new BranchContingency(id));
        return this;
    }

    public ContingencyBuilder addBranch(String id, String voltageLevelId) {
        elements.add(new BranchContingency(id, voltageLevelId));
        return this;
    }

    public ContingencyBuilder addBusbarSection(String id) {
        elements.add(new BusbarSectionContingency(id));
        return this;
    }

    public ContingencyBuilder addGenerator(String id) {
        elements.add(new GeneratorContingency(id));
        return this;
    }

    public ContingencyBuilder addHvdcLine(String id) {
        elements.add(new HvdcLineContingency(id));
        return this;
    }

    public ContingencyBuilder addHvdcLine(String id, String voltageLevel) {
        elements.add(new HvdcLineContingency(id, voltageLevel));
        return this;
    }

    public ContingencyBuilder addLine(String id) {
        elements.add(new LineContingency(id));
        return this;
    }

    public ContingencyBuilder addLine(String id, String voltageLevelId) {
        elements.add(new LineContingency(id, voltageLevelId));
        return this;
    }

    public ContingencyBuilder addShuntCompensator(String id) {
        elements.add(new ShuntCompensatorContingency(id));
        return this;
    }

    public ContingencyBuilder addStaticVarCompensator(String id) {
        elements.add(new StaticVarCompensatorContingency(id));
        return this;
    }

    public ContingencyBuilder addTwoWindingsTransformer(String id) {
        elements.add(new TwoWindingsTransformerContingency(id));
        return this;
    }

    public ContingencyBuilder addTwoWindingsTransformer(String id, String voltageLevelId) {
        elements.add(new TwoWindingsTransformerContingency(id, voltageLevelId));
        return this;
    }

    public ContingencyBuilder addDanglingLine(String id) {
        elements.add(new DanglingLineContingency(id));
        return this;
    }
}
