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

    private TwoCountriesCriterion twoCountriesCriterion;
    private SingleNominalVoltageCriterion singleNominalVoltageCriterion;

    public TieLineCriterion() {
        super();
    }

    public TieLineCriterion(String name) {
        super(name);
    }

    @Override
    public NetworkElementCriterionType getNetworkElementCriterionType() {
        return TYPE;
    }

    @Override
    public TwoCountriesCriterion getCountryCriterion() {
        return twoCountriesCriterion;
    }

    public TieLineCriterion setTwoCountriesCriterion(TwoCountriesCriterion twoCountriesCriterion) {
        this.twoCountriesCriterion = twoCountriesCriterion;
        return this;
    }

    @Override
    public SingleNominalVoltageCriterion getNominalVoltageCriterion() {
        return singleNominalVoltageCriterion;
    }

    public TieLineCriterion setSingleNominalVoltageCriterion(SingleNominalVoltageCriterion singleNominalVoltageCriterion) {
        this.singleNominalVoltageCriterion = singleNominalVoltageCriterion;
        return this;
    }
}
