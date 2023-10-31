/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.contingency.list;

import com.powsybl.iidm.network.util.criterion.PropertyCriterion;
import com.powsybl.iidm.network.util.criterion.RegexCriterion;
import com.powsybl.iidm.network.util.criterion.SingleCountryCriterion;
import com.powsybl.iidm.network.util.criterion.SingleNominalVoltageCriterion;
import com.powsybl.iidm.network.IdentifiableType;

import java.util.List;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class InjectionCriterionContingencyList extends AbstractEquipmentCriterionContingencyList {

    private final SingleCountryCriterion singleCountryCriterion;
    private final SingleNominalVoltageCriterion singleNominalVoltageCriterion;

    public InjectionCriterionContingencyList(String name, String identifiableType,
                                             SingleCountryCriterion singleCountryCriterion,
                                             SingleNominalVoltageCriterion singleNominalVoltageCriterion, List<PropertyCriterion> propertyCriteria, RegexCriterion regexCriterion) {
        this(name, IdentifiableType.valueOf(identifiableType), singleCountryCriterion, singleNominalVoltageCriterion, propertyCriteria, regexCriterion);
    }

    public InjectionCriterionContingencyList(String name, IdentifiableType identifiableType,
                                             SingleCountryCriterion singleCountryCriterion,
                                             SingleNominalVoltageCriterion singleNominalVoltageCriterion, List<PropertyCriterion> propertyCriteria, RegexCriterion regexCriterion) {
        super(name, identifiableType, propertyCriteria, regexCriterion);
        this.singleCountryCriterion = singleCountryCriterion;
        this.singleNominalVoltageCriterion = singleNominalVoltageCriterion;
    }

    @Override
    public String getType() {
        return "injectionCriterion";
    }

    @Override
    public SingleCountryCriterion getCountryCriterion() {
        return singleCountryCriterion;
    }

    @Override
    public SingleNominalVoltageCriterion getNominalVoltageCriterion() {
        return singleNominalVoltageCriterion;
    }
}
