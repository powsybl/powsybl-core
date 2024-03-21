/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria;

/**
 * <p>{@link NetworkElementCriterion} on lines.</p>
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
public class LineCriterion extends AbstractNetworkElementEquipmentCriterion {
    public static final NetworkElementCriterionType TYPE = NetworkElementCriterionType.LINE;

    private final TwoCountriesCriterion twoCountriesCriterion;
    private final TwoNominalVoltageCriterion twoNominalVoltageCriterion;

    public LineCriterion(TwoCountriesCriterion twoCountriesCriterion, TwoNominalVoltageCriterion twoNominalVoltageCriterion) {
        this(null, twoCountriesCriterion, twoNominalVoltageCriterion);
    }

    public LineCriterion(String name, TwoCountriesCriterion twoCountriesCriterion, TwoNominalVoltageCriterion twoNominalVoltageCriterion) {
        super(name);
        this.twoCountriesCriterion = twoCountriesCriterion;
        this.twoNominalVoltageCriterion = twoNominalVoltageCriterion;
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
    public TwoNominalVoltageCriterion getNominalVoltageCriterion() {
        return twoNominalVoltageCriterion;
    }
}
