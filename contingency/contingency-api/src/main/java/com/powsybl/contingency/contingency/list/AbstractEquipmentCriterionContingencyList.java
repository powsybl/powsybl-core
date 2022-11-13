/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.contingency.list;

import com.google.common.collect.ImmutableList;
import com.powsybl.contingency.contingency.list.criterion.*;
import com.powsybl.iidm.network.IdentifiableType;

import java.util.List;
import java.util.Objects;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
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
