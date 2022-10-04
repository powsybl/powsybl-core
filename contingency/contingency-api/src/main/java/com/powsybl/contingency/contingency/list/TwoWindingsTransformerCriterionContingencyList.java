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
import com.powsybl.contingency.contingency.list.criterion.SingleCountryCriterion;
import com.powsybl.contingency.contingency.list.criterion.TwoNominalVoltageCriterion;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class TwoWindingsTransformerCriterionContingencyList implements ContingencyList {

    // VERSION = 1.0 : first version
    public static final String VERSION = "1.0";

    private final String name;
    private final IdentifiableType identifiableType = IdentifiableType.TWO_WINDINGS_TRANSFORMER;
    private final SingleCountryCriterion countryCriterion;
    private final TwoNominalVoltageCriterion nominalVoltageCriterion;
    private final PropertyCriterion propertyCriterion;

    public TwoWindingsTransformerCriterionContingencyList(String name,
                                                          SingleCountryCriterion countryCriterion,
                                                          TwoNominalVoltageCriterion nominalVoltageCriterion,
                                                          PropertyCriterion propertyCriterion) {
        this.name = Objects.requireNonNull(name);
        this.countryCriterion = countryCriterion;
        this.nominalVoltageCriterion = nominalVoltageCriterion;
        this.propertyCriterion = propertyCriterion;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return "twoWindingsTransformerCriterion";
    }

    @Override
    public List<Contingency> getContingencies(Network network) {
        return network.getIdentifiableTypeStream(identifiableType)
                .filter(identifiable -> countryCriterion == null || countryCriterion.filter(identifiable, identifiableType))
                .filter(identifiable -> nominalVoltageCriterion == null || nominalVoltageCriterion.filter(identifiable, identifiableType))
                .filter(identifiable -> propertyCriterion == null || propertyCriterion.filter(identifiable, identifiableType))
                .map(identifiable -> new Contingency(identifiable.getId(), ContingencyElement.of(identifiable)))
                .collect(Collectors.toList());
    }

    public static String getVersion() {
        return VERSION;
    }

    public IdentifiableType getIdentifiableType() {
        return identifiableType;
    }

    public SingleCountryCriterion getCountryCriterion() {
        return countryCriterion;
    }

    public TwoNominalVoltageCriterion getNominalVoltageCriterion() {
        return nominalVoltageCriterion;
    }

    public PropertyCriterion getPropertyCriterion() {
        return propertyCriterion;
    }
}
