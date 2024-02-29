/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria;

/**
 * <p>{@link NetworkElementCriterion} on two windings transformers.</p>
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
public class TwoWindingsTransformerCriterion extends AbstractNetworkElementEquipmentCriterion {
    public static final NetworkElementCriterionType TYPE = NetworkElementCriterionType.TWO_WINDINGS_TRANSFORMER;

    private SingleCountryCriterion singleCountryCriterion;
    private TwoNominalVoltageCriterion twoNominalVoltageCriterion;

    public TwoWindingsTransformerCriterion() {
        super();
    }

    public TwoWindingsTransformerCriterion(String name) {
        super(name);
    }

    @Override
    public NetworkElementCriterionType getNetworkElementCriterionType() {
        return TYPE;
    }

    @Override
    public SingleCountryCriterion getCountryCriterion() {
        return singleCountryCriterion;
    }

    public TwoWindingsTransformerCriterion setSingleCountryCriterion(SingleCountryCriterion singleCountryCriterion) {
        this.singleCountryCriterion = singleCountryCriterion;
        return this;
    }

    @Override
    public TwoNominalVoltageCriterion getNominalVoltageCriterion() {
        return twoNominalVoltageCriterion;
    }

    public TwoWindingsTransformerCriterion setTwoNominalVoltageCriterion(TwoNominalVoltageCriterion twoNominalVoltageCriterion) {
        this.twoNominalVoltageCriterion = twoNominalVoltageCriterion;
        return this;
    }

}
