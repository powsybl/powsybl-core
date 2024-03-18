/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria;

/**
 * <p>{@link NetworkElementCriterion} on dangling lines.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class DanglingLineCriterion extends AbstractNetworkElementEquipmentCriterion {
    public static final NetworkElementCriterionType TYPE = NetworkElementCriterionType.DANGLING_LINE;

    private final SingleCountryCriterion singleCountryCriterion;
    private final SingleNominalVoltageCriterion singleNominalVoltageCriterion;

    public DanglingLineCriterion(SingleCountryCriterion singleCountryCriterion, SingleNominalVoltageCriterion singleNominalVoltageCriterion) {
        this(null, singleCountryCriterion, singleNominalVoltageCriterion);
    }

    public DanglingLineCriterion(String name, SingleCountryCriterion singleCountryCriterion, SingleNominalVoltageCriterion singleNominalVoltageCriterion) {
        super(name);
        this.singleCountryCriterion = singleCountryCriterion;
        this.singleNominalVoltageCriterion = singleNominalVoltageCriterion;
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
    public SingleNominalVoltageCriterion getNominalVoltageCriterion() {
        return singleNominalVoltageCriterion;
    }
}
