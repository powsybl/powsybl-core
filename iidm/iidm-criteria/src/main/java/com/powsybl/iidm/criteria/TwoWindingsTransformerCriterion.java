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

    private final SingleCountryCriterion singleCountryCriterion;
    private final TwoNominalVoltageCriterion twoNominalVoltageCriterion;

    public TwoWindingsTransformerCriterion(SingleCountryCriterion singleCountryCriterion, TwoNominalVoltageCriterion twoNominalVoltageCriterion) {
        this(null, singleCountryCriterion, twoNominalVoltageCriterion);
    }

    public TwoWindingsTransformerCriterion(String name, SingleCountryCriterion singleCountryCriterion, TwoNominalVoltageCriterion twoNominalVoltageCriterion) {
        super(name);
        this.singleCountryCriterion = singleCountryCriterion;
        this.twoNominalVoltageCriterion = twoNominalVoltageCriterion;
    }

    @Override
    public NetworkElementCriterionType getNetworkElementCriterionType() {
        return TYPE;
    }

    @Override
    public SingleCountryCriterion getCountryCriterion() {
        return singleCountryCriterion;
    }

    @Override
    public TwoNominalVoltageCriterion getNominalVoltageCriterion() {
        return twoNominalVoltageCriterion;
    }
}
