/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.contingency.list;

import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyElement;
import com.powsybl.contingency.contingency.list.criterion.*;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class HvdcLineCriterionContingencyList extends AbstractEquipmentCriterionContingencyList {

    private final TwoCountriesCriterion twoCountriesCriterion;
    private final TwoNominalVoltageCriterion twoNominalVoltageCriterion;

    public HvdcLineCriterionContingencyList(String name, TwoCountriesCriterion twoCountriesCriterion,
                                            TwoNominalVoltageCriterion twoNominalVoltageCriterion,
                                            List<PropertyCriterion> propertyCriteria, RegexCriterion regexCriterion) {
        super(name, IdentifiableType.HVDC_LINE, propertyCriteria, regexCriterion);
        this.twoCountriesCriterion = twoCountriesCriterion;
        this.twoNominalVoltageCriterion = twoNominalVoltageCriterion;
    }

    @Override
    public String getType() {
        return "hvdcCriterion";
    }

    @Override
    public List<Contingency> getContingencies(Network network) {
        return network.getIdentifiableStream(getIdentifiableType())
                .filter(identifiable -> twoCountriesCriterion == null || twoCountriesCriterion.filter(identifiable, getIdentifiableType()))
                .filter(identifiable -> twoNominalVoltageCriterion == null || twoNominalVoltageCriterion.filter(identifiable, getIdentifiableType()))
                .filter(identifiable -> getPropertyCriteria().stream().allMatch(propertyCriterion -> propertyCriterion.filter(identifiable, getIdentifiableType())))
                .filter(identifiable -> getRegexCriterion() == null || getRegexCriterion().filter(identifiable, getIdentifiableType()))
                .map(identifiable -> new Contingency(identifiable.getId(), ContingencyElement.of(identifiable)))
                .collect(Collectors.toList());
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
