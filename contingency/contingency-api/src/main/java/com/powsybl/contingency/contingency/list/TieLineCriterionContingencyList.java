/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.contingency.list;

import com.powsybl.iidm.network.util.criterion.PropertyCriterion;
import com.powsybl.iidm.network.util.criterion.RegexCriterion;
import com.powsybl.iidm.network.util.criterion.SingleNominalVoltageCriterion;
import com.powsybl.iidm.network.util.criterion.TwoCountriesCriterion;
import com.powsybl.iidm.network.IdentifiableType;

import java.util.List;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class TieLineCriterionContingencyList extends AbstractEquipmentCriterionContingencyList {
    private final TwoCountriesCriterion twoCountriesCriterion;
    private final SingleNominalVoltageCriterion singleNominalVoltageCriterion;

    public TieLineCriterionContingencyList(String name,
                                           TwoCountriesCriterion twoCountriesCriterion,
                                           SingleNominalVoltageCriterion singleNominalVoltageCriterion,
                                           List<PropertyCriterion> propertyCriteria, RegexCriterion regexCriterion) {
        super(name, IdentifiableType.TIE_LINE, propertyCriteria, regexCriterion);
        this.twoCountriesCriterion = twoCountriesCriterion;
        this.singleNominalVoltageCriterion = singleNominalVoltageCriterion;
    }

    @Override
    public String getType() {
        return "tieLineCriterion";
    }

    @Override
    public TwoCountriesCriterion getCountryCriterion() {
        return twoCountriesCriterion;
    }

    @Override
    public SingleNominalVoltageCriterion getNominalVoltageCriterion() {
        return singleNominalVoltageCriterion;
    }
}
