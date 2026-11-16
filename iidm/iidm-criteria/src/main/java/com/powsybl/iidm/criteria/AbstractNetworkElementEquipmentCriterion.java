/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria;

/**
 * <p>Abstract classes for {@link NetworkElementCriterion} implementations corresponding to real network elements.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public abstract class AbstractNetworkElementEquipmentCriterion extends AbstractNetworkElementCriterion {

    protected AbstractNetworkElementEquipmentCriterion(String name) {
        super(name);
    }

    public abstract Criterion getCountryCriterion();

    public abstract Criterion getNominalVoltageCriterion();

    @Override
    public boolean accept(NetworkElementVisitor networkElementVisitor) {
        return networkElementVisitor.visit(this);
    }
}
