/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util.criterion;

public class ThreeWindingsTransformerCriterion extends AbstractNetworkElementCriterion {
    public static final NetworkElementCriterionType TYPE = NetworkElementCriterionType.THREE_WINDING_TRANSFORMER;

    private SingleCountryCriterion singleCountryCriterion;
    private ThreeNominalVoltageCriterion threeNominalVoltageCriterion;

    public ThreeWindingsTransformerCriterion() {
        super(null);
    }

    public ThreeWindingsTransformerCriterion(String name) {
        super(name);
    }

    @Override
    public NetworkElementCriterionType getNetworkElementCriterionType() {
        return TYPE;
    }

    @Override
    public boolean accept(NetworkElementVisitor networkElementVisitor) {
        return networkElementVisitor.visit(this);
    }

    public SingleCountryCriterion getSingleCountryCriterion() {
        return singleCountryCriterion;
    }

    public ThreeWindingsTransformerCriterion setSingleCountryCriterion(SingleCountryCriterion singleCountryCriterion) {
        this.singleCountryCriterion = singleCountryCriterion;
        return this;
    }

    public ThreeNominalVoltageCriterion getThreeNominalVoltageCriterion() {
        return threeNominalVoltageCriterion;
    }

    public ThreeWindingsTransformerCriterion setThreeNominalVoltageCriterion(ThreeNominalVoltageCriterion threeNominalVoltageCriterion) {
        this.threeNominalVoltageCriterion = threeNominalVoltageCriterion;
        return this;
    }
}
