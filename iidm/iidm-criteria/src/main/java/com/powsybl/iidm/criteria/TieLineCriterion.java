/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria;

/**
 * <p>{@link NetworkElementCriterion} on tie-lines.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class TieLineCriterion extends AbstractNetworkElementEquipmentCriterion {
    public static final NetworkElementCriterionType TYPE = NetworkElementCriterionType.TIE_LINE;

    private final TwoCountriesCriterion twoCountriesCriterion;
    private final SingleNominalVoltageCriterion singleNominalVoltageCriterion;

    public TieLineCriterion(TwoCountriesCriterion twoCountriesCriterion, SingleNominalVoltageCriterion singleNominalVoltageCriterion) {
        this(null, twoCountriesCriterion, singleNominalVoltageCriterion);
    }

    public TieLineCriterion(String name, TwoCountriesCriterion twoCountriesCriterion, SingleNominalVoltageCriterion singleNominalVoltageCriterion) {
        super(name);
        this.twoCountriesCriterion = twoCountriesCriterion;
        this.singleNominalVoltageCriterion = singleNominalVoltageCriterion;
    }

    @Override
    public NetworkElementCriterionType getNetworkElementCriterionType() {
        return TYPE;
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
