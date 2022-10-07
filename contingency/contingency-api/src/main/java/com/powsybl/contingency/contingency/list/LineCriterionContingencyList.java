/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.contingency.list;

import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyElement;
import com.powsybl.contingency.contingency.list.criterion.PropertyCriterion;
import com.powsybl.contingency.contingency.list.criterion.RegexCriterion;
import com.powsybl.contingency.contingency.list.criterion.SingleNominalVoltageCriterion;
import com.powsybl.contingency.contingency.list.criterion.TwoCountriesCriterion;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class LineCriterionContingencyList extends AbstractEquipmentCriterionContingencyList {
    private final TwoCountriesCriterion twoCountriesCriterion;
    private final SingleNominalVoltageCriterion singleNominalVoltageCriterion;

    public LineCriterionContingencyList(String name,
                                        TwoCountriesCriterion twoCountriesCriterion,
                                        SingleNominalVoltageCriterion singleNominalVoltageCriterion,
                                        PropertyCriterion propertyCriterion, RegexCriterion regexCriterion) {
        super(name, IdentifiableType.LINE, propertyCriterion, regexCriterion);
        this.twoCountriesCriterion = twoCountriesCriterion;
        this.singleNominalVoltageCriterion = singleNominalVoltageCriterion;
    }

    @Override
    public String getType() {
        return "lineCriterion";
    }

    @Override
    public List<Contingency> getContingencies(Network network) {
        return network.getIdentifiableStream(getIdentifiableType())
                .filter(identifiable -> twoCountriesCriterion == null || twoCountriesCriterion.filter(identifiable, getIdentifiableType()))
                .filter(identifiable -> singleNominalVoltageCriterion == null || singleNominalVoltageCriterion.filter(identifiable, getIdentifiableType()))
                .filter(identifiable -> getPropertyCriterion() == null || getPropertyCriterion().filter(identifiable, getIdentifiableType()))
                .filter(identifiable -> getRegexCriterion() == null || getRegexCriterion().filter(identifiable, getIdentifiableType()))
                .map(identifiable -> new Contingency(identifiable.getId(), ContingencyElement.of(identifiable)))
                .collect(Collectors.toList());
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
