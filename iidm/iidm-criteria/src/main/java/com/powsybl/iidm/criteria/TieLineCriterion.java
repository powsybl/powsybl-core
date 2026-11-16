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
    private final TwoNominalVoltageCriterion twoNominalVoltageCriterion;

    public TieLineCriterion(TwoCountriesCriterion twoCountriesCriterion, TwoNominalVoltageCriterion twoNominalVoltageCriterion) {
        this(null, twoCountriesCriterion, twoNominalVoltageCriterion);
    }

    public TieLineCriterion(String name, TwoCountriesCriterion twoCountriesCriterion, TwoNominalVoltageCriterion twoNominalVoltageCriterion) {
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
