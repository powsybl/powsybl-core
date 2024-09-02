/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria;

import com.powsybl.iidm.criteria.translation.NetworkElement;
import com.powsybl.iidm.network.ThreeSides;

/**
 * <p>Visitor used to detect if a given {@link NetworkElement} validates criteria.</p>
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
public class NetworkElementVisitor {

    private final NetworkElement networkElement;
    private final ThreeSides side;

    public NetworkElementVisitor(NetworkElement networkElement) {
        this(networkElement, null);
    }

    public NetworkElementVisitor(NetworkElement networkElement, ThreeSides side) {
        this.networkElement = networkElement;
        this.side = side;
    }

    public boolean visit(AbstractNetworkElementEquipmentCriterion c) {
        return networkElement.isValidFor(c.getNetworkElementCriterionType())
                && doRespectCriterion(c.getCountryCriterion())
                && doRespectCriterion(c.getNominalVoltageCriterion());
    }

    public boolean visit(NetworkElementIdListCriterion c) {
        return c.getNetworkElementIds().contains(networkElement.getId());
    }

    private boolean doRespectCriterion(Criterion criterion) {
        return criterion == null || criterion.filter(networkElement, side);
    }
}
