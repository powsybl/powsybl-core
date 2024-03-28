/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.contingency.list;

import com.google.common.collect.ImmutableList;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyElement;
import com.powsybl.iidm.criteria.Criterion;
import com.powsybl.iidm.criteria.PropertyCriterion;
import com.powsybl.iidm.criteria.RegexCriterion;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public abstract class AbstractEquipmentCriterionContingencyList implements ContingencyList {
    private final String name;
    private final IdentifiableType identifiableType;
    private final List<PropertyCriterion> propertyCriteria;
    private final RegexCriterion regexCriterion;

    protected AbstractEquipmentCriterionContingencyList(String name, IdentifiableType identifiableType, List<PropertyCriterion> propertyCriteria, RegexCriterion regexCriterion) {
        this.name = Objects.requireNonNull(name);
        this.identifiableType = identifiableType;
        this.propertyCriteria = ImmutableList.copyOf(Objects.requireNonNull(propertyCriteria));
        this.regexCriterion = regexCriterion;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Contingency> getContingencies(Network network) {
        return network.getIdentifiableStream(getIdentifiableType())
                .filter(identifiable -> getCountryCriterion() == null || getCountryCriterion().filter(identifiable, getIdentifiableType()))
                .filter(identifiable -> getNominalVoltageCriterion() == null || getNominalVoltageCriterion().filter(identifiable, getIdentifiableType()))
                .filter(identifiable -> getPropertyCriteria().stream().allMatch(propertyCriterion -> propertyCriterion.filter(identifiable, getIdentifiableType())))
                .filter(identifiable -> getRegexCriterion() == null || getRegexCriterion().filter(identifiable, getIdentifiableType()))
                .map(identifiable -> new Contingency(identifiable.getId(), ContingencyElement.of(identifiable)))
                .collect(Collectors.toList());
    }

    public IdentifiableType getIdentifiableType() {
        return identifiableType;
    }

    public List<PropertyCriterion> getPropertyCriteria() {
        return propertyCriteria;
    }

    public RegexCriterion getRegexCriterion() {
        return regexCriterion;
    }

    public abstract Criterion getCountryCriterion();

    public abstract Criterion getNominalVoltageCriterion();
}
