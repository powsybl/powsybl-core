/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.contingency;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
public class ContingencyBuilder {

    private final String id;

    private final List<ContingencyElement> elements;

    private String name;

    ContingencyBuilder(String id) {
        this.id = Objects.requireNonNull(id);
        this.elements = new ArrayList<>();
    }

    public Contingency build() {
        return new Contingency(id, name, elements);
    }

    public ContingencyBuilder addBattery(String id) {
        elements.add(new BatteryContingency(id));
        return this;
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

    public ContingencyBuilder addThreeWindingsTransformer(String id) {
        elements.add(new ThreeWindingsTransformerContingency(id));
        return this;
    }

    public ContingencyBuilder addLoad(String id) {
        elements.add(new LoadContingency(id));
        return this;
    }

    public ContingencyBuilder addSwitch(String id) {
        elements.add(new SwitchContingency(id));
        return this;
    }

    public ContingencyBuilder addBus(String id) {
        elements.add(new BusContingency(id));
        return this;
    }

    public ContingencyBuilder addTieLine(String id) {
        elements.add(new TieLineContingency(id));
        return this;
    }

    public ContingencyBuilder addTieLine(String id, String voltageLevelId) {
        elements.add(new TieLineContingency(id, voltageLevelId));
        return this;
    }

    public ContingencyBuilder addIdentifiable(String id, Network network) {
        Identifiable<?> identifiable = network.getIdentifiable(id);
        if (identifiable == null) {
            throw new PowsyblException(String.format("Element %s has not been found in the network", id));
        }
        return addIdentifiable(identifiable);
    }

    public ContingencyBuilder addIdentifiable(Identifiable<?> identifiable) {
        elements.add(ContingencyElement.of(identifiable));
        return this;
    }

    public ContingencyBuilder addName(String name) {
        this.name = name;
        return this;
    }
}
