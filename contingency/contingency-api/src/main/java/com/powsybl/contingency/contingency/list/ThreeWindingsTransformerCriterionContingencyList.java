/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.contingency.list;

import com.powsybl.contingency.contingency.list.criterion.PropertyCriterion;
import com.powsybl.contingency.contingency.list.criterion.RegexCriterion;
import com.powsybl.contingency.contingency.list.criterion.SingleCountryCriterion;
import com.powsybl.contingency.contingency.list.criterion.ThreeNominalVoltageCriterion;
import com.powsybl.iidm.network.IdentifiableType;

import java.util.List;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class ThreeWindingsTransformerCriterionContingencyList extends AbstractEquipmentCriterionContingencyList {

    public ThreeWindingsTransformerCriterionContingencyList(String name,
                                                            SingleCountryCriterion singleCountryCriterion,
                                                            ThreeNominalVoltageCriterion threeNominalVoltageCriterion,
                                                            List<PropertyCriterion> propertyCriteria, RegexCriterion regexCriterion) {
        super(name, IdentifiableType.THREE_WINDINGS_TRANSFORMER, singleCountryCriterion, threeNominalVoltageCriterion, propertyCriteria, regexCriterion);
    }

    @Override
    public String getType() {
        return "threeWindingsTransformerCriterion";
    }
}
