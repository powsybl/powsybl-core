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
import com.powsybl.iidm.network.util.criterion.TwoNominalVoltageCriterion;
import com.powsybl.iidm.network.IdentifiableType;

import java.util.List;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class TwoWindingsTransformerCriterionContingencyList extends AbstractEquipmentCriterionContingencyList {
    private final SingleCountryCriterion singleCountryCriterion;
    private final TwoNominalVoltageCriterion twoNominalVoltageCriterion;

    public TwoWindingsTransformerCriterionContingencyList(String name,
                                                          SingleCountryCriterion singleCountryCriterion,
                                                          TwoNominalVoltageCriterion twoNominalVoltageCriterion,
                                                          List<PropertyCriterion> propertyCriteria, RegexCriterion regexCriterion) {
        super(name, IdentifiableType.TWO_WINDINGS_TRANSFORMER, propertyCriteria, regexCriterion);
        this.singleCountryCriterion = singleCountryCriterion;
        this.twoNominalVoltageCriterion = twoNominalVoltageCriterion;
    }

    @Override
    public String getType() {
        return "twoWindingsTransformerCriterion";
    }

    @Override
    public SingleCountryCriterion getCountryCriterion() {
        return singleCountryCriterion;
    }

    @Override
    public TwoNominalVoltageCriterion getNominalVoltageCriterion() {
        return twoNominalVoltageCriterion;
    }
}
