/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.contingency.list;

import com.powsybl.contingency.contingency.list.criterion.*;
import com.powsybl.iidm.network.IdentifiableType;

import java.util.Objects;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public abstract class AbstractEquipmentCriterionContingencyList implements ContingencyList {
    private final String name;
    private final IdentifiableType identifiableType;
    private final PropertyCriterion propertyCriterion;
    private final RegexCriterion regexCriterion;

    protected AbstractEquipmentCriterionContingencyList(String name, IdentifiableType identifiableType, PropertyCriterion propertyCriterion, RegexCriterion regexCriterion) {
        this.name = Objects.requireNonNull(name);
        this.identifiableType = identifiableType;
        this.propertyCriterion = propertyCriterion;
        this.regexCriterion = regexCriterion;
    }

    @Override
    public String getName() {
        return name;
    }

    public IdentifiableType getIdentifiableType() {
        return identifiableType;
    }

    public PropertyCriterion getPropertyCriterion() {
        return propertyCriterion;
    }

    public RegexCriterion getRegexCriterion() {
        return regexCriterion;
    }

    public abstract Criterion getCountryCriterion();

    public abstract Criterion getNominalVoltageCriterion();
}
