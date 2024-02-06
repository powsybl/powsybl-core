/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.detectors.criterion.network;

import com.powsybl.iidm.network.util.criterion.Criterion;
import com.powsybl.iidm.network.util.translation.NetworkElementInterface;

public class NetworkElementVisitor {

    private final NetworkElementInterface networkElement;

    public NetworkElementVisitor(NetworkElementInterface networkElement) {
        this.networkElement = networkElement;
    }

    public boolean visit(LineCriterion c) {
        return c.getNetworkElementIds().contains(networkElement.getId())
                || doRespectCriterion(networkElement, c.getTwoCountriesCriterion())
                    && doRespectCriterion(networkElement, c.getSingleNominalVoltageCriterion());
    }

    public boolean visit(TwoWindingsTransformerCriterion c) {
        return c.getNetworkElementIds().contains(networkElement.getId())
                || doRespectCriterion(networkElement, c.getSingleCountryCriterion())
                    && doRespectCriterion(networkElement, c.getTwoNominalVoltageCriterion());
    }

    public boolean visit(ThreeWindingsTransformerCriterion c) {
        return c.getNetworkElementIds().contains(networkElement.getId())
                || doRespectCriterion(networkElement, c.getSingleCountryCriterion())
                    && doRespectCriterion(networkElement, c.getThreeNominalVoltageCriterion());
    }

    private boolean doRespectCriterion(NetworkElementInterface networkElement, Criterion criterion) {
        return criterion == null || criterion.filter(networkElement);
    }
}
