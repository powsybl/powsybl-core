/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.detectors.criterion.network;

import com.powsybl.iidm.network.util.criterion.SingleNominalVoltageCriterion;
import com.powsybl.iidm.network.util.criterion.TwoCountriesCriterion;

import java.util.Set;

public class LineCriterion extends AbstractNetworkElementCriterion {

    private TwoCountriesCriterion twoCountriesCriterion;
    private SingleNominalVoltageCriterion singleNominalVoltageCriterion;

    public LineCriterion(Set<String> networkElementIds) {
        super(networkElementIds);
    }

    @Override
    public NetworkElementCriterionType getNetworkElementCriterionType() {
        return NetworkElementCriterionType.LINE;
    }

    @Override
    public boolean accept(NetworkElementVisitor networkElementVisitor) {
        return networkElementVisitor.visit(this);
    }

    public TwoCountriesCriterion getTwoCountriesCriterion() {
        return twoCountriesCriterion;
    }

    public LineCriterion setTwoCountriesCriterion(TwoCountriesCriterion twoCountriesCriterion) {
        this.twoCountriesCriterion = twoCountriesCriterion;
        return this;
    }

    public SingleNominalVoltageCriterion getSingleNominalVoltageCriterion() {
        return singleNominalVoltageCriterion;
    }

    public LineCriterion setSingleNominalVoltageCriterion(SingleNominalVoltageCriterion singleNominalVoltageCriterion) {
        this.singleNominalVoltageCriterion = singleNominalVoltageCriterion;
        return this;
    }

}