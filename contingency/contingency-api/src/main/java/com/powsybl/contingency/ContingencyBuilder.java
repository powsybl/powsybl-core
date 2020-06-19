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

    public ContingencyBuilder branch(String id) {
        elements.add(new BranchContingency(id));
        return this;
    }

    public ContingencyBuilder branch(String id, String voltageLevelId) {
        elements.add(new BranchContingency(id, voltageLevelId));
        return this;
    }

    public ContingencyBuilder busbarSection(String id) {
        elements.add(new BusbarSectionContingency(id));
        return this;
    }

    public ContingencyBuilder generator(String id) {
        elements.add(new GeneratorContingency(id));
        return this;
    }

    public ContingencyBuilder hvdcLine(String id) {
        elements.add(new HvdcLineContingency(id));
        return this;
    }

    public ContingencyBuilder hvdcLine(String id, String voltageLevel) {
        elements.add(new HvdcLineContingency(id, voltageLevel));
        return this;
    }

    public ContingencyBuilder line(String id) {
        // FIXME(mathbagu): Check that the ID is really a line, not a two windings transformer
        elements.add(new BranchContingency(id));
        return this;
    }

    public ContingencyBuilder line(String id, String voltageLevelId) {
        // FIXME(mathbagu): Check that the ID is really a line, not a two windings transformer
        elements.add(new BranchContingency(id, voltageLevelId));
        return this;
    }

    public ContingencyBuilder shuntCompensator(String id) {
        elements.add(new ShuntCompensatorContingency(id));
        return this;
    }

    public ContingencyBuilder staticVarCompensator(String id) {
        elements.add(new StaticVarCompensatorContingency(id));
        return this;
    }

    public ContingencyBuilder twoWindingsTransformer(String id) {
        // FIXME(mathbagu): Check that the ID is really a two windings transformer, not a line
        elements.add(new BranchContingency(id));
        return this;
    }

    public ContingencyBuilder twoWindingsTransformer(String id, String voltageLevelId) {
        // FIXME(mathbagu): Check that the ID is really a two windings transformer, not a line
        elements.add(new BranchContingency(id, voltageLevelId));
        return this;
    }

}
