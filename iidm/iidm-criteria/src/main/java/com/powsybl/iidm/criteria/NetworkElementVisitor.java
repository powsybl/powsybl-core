/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria;

import com.powsybl.iidm.criteria.translation.NetworkElement;

/**
 * <p>Visitor used to detect if a given {@link NetworkElement} validates criteria.</p>
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
public class NetworkElementVisitor {

    private final NetworkElement networkElement;

    public NetworkElementVisitor(NetworkElement networkElement) {
        this.networkElement = networkElement;
    }

    public boolean visit(AbstractNetworkElementEquipmentCriterion c) {
        return networkElement.isValidFor(c.getNetworkElementCriterionType())
                && doRespectCriterion(networkElement, c.getCountryCriterion())
                && doRespectCriterion(networkElement, c.getNominalVoltageCriterion());
    }

    public boolean visit(NetworkElementIdListCriterion c) {
        return c.getNetworkElementIds().contains(networkElement.getId());
    }

    private boolean doRespectCriterion(NetworkElement networkElement, Criterion criterion) {
        return criterion == null || criterion.filter(networkElement);
    }
}
