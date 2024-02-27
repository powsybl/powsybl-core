/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.contingency.list;

import com.powsybl.iidm.criteria.PropertyCriterion;
import com.powsybl.iidm.criteria.RegexCriterion;
import com.powsybl.iidm.criteria.TwoCountriesCriterion;
import com.powsybl.iidm.criteria.TwoNominalVoltageCriterion;
import com.powsybl.iidm.network.IdentifiableType;

import java.util.List;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public abstract class AbstractLineCriterionContingencyList extends AbstractEquipmentCriterionContingencyList {
    private final TwoCountriesCriterion twoCountriesCriterion;
    private final TwoNominalVoltageCriterion twoNominalVoltageCriterion;

    protected AbstractLineCriterionContingencyList(String name, IdentifiableType type, TwoCountriesCriterion twoCountriesCriterion,
                                            TwoNominalVoltageCriterion twoNominalVoltageCriterion,
                                            List<PropertyCriterion> propertyCriteria, RegexCriterion regexCriterion) {
        super(name, type, propertyCriteria, regexCriterion);
        this.twoCountriesCriterion = twoCountriesCriterion;
        this.twoNominalVoltageCriterion = twoNominalVoltageCriterion;
    }

    @Override
    public TwoCountriesCriterion getCountryCriterion() {
        return twoCountriesCriterion;
    }

    @Override
    public TwoNominalVoltageCriterion getNominalVoltageCriterion() {
        return twoNominalVoltageCriterion;
    }
}
