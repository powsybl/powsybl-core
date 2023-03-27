/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.contingency.list;

import com.powsybl.contingency.contingency.list.criterion.PropertyCriterion;
import com.powsybl.contingency.contingency.list.criterion.RegexCriterion;
import com.powsybl.contingency.contingency.list.criterion.SingleNominalVoltageCriterion;
import com.powsybl.contingency.contingency.list.criterion.TwoCountriesCriterion;
import com.powsybl.iidm.network.IdentifiableType;

import java.util.List;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class LineCriterionContingencyList extends AbstractEquipmentCriterionContingencyList {

    public LineCriterionContingencyList(String name,
                                        TwoCountriesCriterion twoCountriesCriterion,
                                        SingleNominalVoltageCriterion singleNominalVoltageCriterion,
                                        List<PropertyCriterion> propertyCriteria, RegexCriterion regexCriterion) {
        super(name, IdentifiableType.LINE, twoCountriesCriterion, singleNominalVoltageCriterion, propertyCriteria, regexCriterion);
    }

    @Override
    public String getType() {
        return "lineCriterion";
    }
}
