/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import com.google.common.collect.ImmutableList;
import com.powsybl.commons.PowsyblException;
import com.powsybl.contingency.contingency.list.criterion.Criterion;
import com.powsybl.iidm.network.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class CriterionContingencyList implements ContingencyList {

    // VERSION = 1.0 : first version
    public static final String VERSION = "1.0";

    private final String name;
    private IdentifiableType identifiableType;
    private final List<Criterion> criteria;

    public CriterionContingencyList(String name, String identifiableType, List<Criterion> criteria) {
        this(name, IdentifiableType.valueOf(identifiableType), criteria);
    }

    public CriterionContingencyList(String name, IdentifiableType identifiableType, List<Criterion> criteria) {
        this.name = Objects.requireNonNull(name);
        this.identifiableType = identifiableType;
        this.criteria = ImmutableList.copyOf(criteria);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return "criterion";
    }

    @Override
    public List<Contingency> getContingencies(Network network) {
        return network.getIdentifiableTypeStream(identifiableType)
                .filter(identifiable -> criteria.stream().allMatch(criterion -> criterion.filter(identifiable, identifiableType)))
                .map(identifiable -> new Contingency(identifiable.getId(), getContingencyElement(identifiable)))
                .collect(Collectors.toList());
    }

    ContingencyElement getContingencyElement(Identifiable identifiable) {
        switch (identifiableType) {
            case LINE:
                return new LineContingency(identifiable.getId());
            case BUSBAR_SECTION:
                return new BusbarSectionContingency(identifiable.getId());
            case TWO_WINDINGS_TRANSFORMER:
                return new TwoWindingsTransformerContingency(identifiable.getId());
            case THREE_WINDINGS_TRANSFORMER:
                return new ThreeWindingsTransformerContingency(identifiable.getId());
            case GENERATOR:
                return new GeneratorContingency(identifiable.getId());
            case SWITCH:
                return new SwitchContingency(identifiable.getId());
            case DANGLING_LINE:
                return new DanglingLineContingency(identifiable.getId());
            case LOAD:
                return new LoadContingency(identifiable.getId());
            case HVDC_LINE:
                return new HvdcLineContingency(identifiable.getId());
            case SHUNT_COMPENSATOR:
                return new ShuntCompensatorContingency(identifiable.getId());
            case STATIC_VAR_COMPENSATOR:
                return new StaticVarCompensatorContingency(identifiable.getId());
            default:
                throw new PowsyblException(identifiableType + " can not be a ContingencyElement");
        }
    }

    public static String getVersion() {
        return VERSION;
    }

    public IdentifiableType getIdentifiableType() {
        return identifiableType;
    }

    public List<Criterion> getCriteria() {
        return criteria;
    }
}
