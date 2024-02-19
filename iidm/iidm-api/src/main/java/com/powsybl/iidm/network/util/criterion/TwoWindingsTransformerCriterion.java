/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util.criterion;

import java.util.Set;

public class TwoWindingsTransformerCriterion extends AbstractNetworkElementCriterion<TwoWindingsTransformerCriterion> {

    private SingleCountryCriterion singleCountryCriterion;
    private TwoNominalVoltageCriterion twoNominalVoltageCriterion;

    public TwoWindingsTransformerCriterion() {
        super();
    }

    public TwoWindingsTransformerCriterion(Set<String> networkElementIds) {
        super();
        setNetworkElementIds(networkElementIds);
    }

    @Override
    public NetworkElementCriterionType getNetworkElementCriterionType() {
        return NetworkElementCriterionType.TWO_WINDING_TRANSFORMER;
    }

    @Override
    public boolean accept(NetworkElementVisitor networkElementVisitor) {
        return networkElementVisitor.visit(this);
    }

    public SingleCountryCriterion getSingleCountryCriterion() {
        return singleCountryCriterion;
    }

    public TwoWindingsTransformerCriterion setSingleCountryCriterion(SingleCountryCriterion singleCountryCriterion) {
        this.singleCountryCriterion = singleCountryCriterion;
        return this;
    }

    public TwoNominalVoltageCriterion getTwoNominalVoltageCriterion() {
        return twoNominalVoltageCriterion;
    }

    public TwoWindingsTransformerCriterion setTwoNominalVoltageCriterion(TwoNominalVoltageCriterion twoNominalVoltageCriterion) {
        this.twoNominalVoltageCriterion = twoNominalVoltageCriterion;
        return this;
    }

}
