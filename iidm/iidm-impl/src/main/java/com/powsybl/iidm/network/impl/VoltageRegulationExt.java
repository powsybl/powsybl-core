/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationAdder;
import com.powsybl.iidm.network.regulation.VoltageRegulationBuilder;
import com.powsybl.iidm.network.regulation.VoltageRegulationHolder;

/**
 * @author Matthieu SAUR {@literal <matthieu.saur at rte-france.com>}
 */
public interface VoltageRegulationExt extends VoltageRegulation, MultiVariantObject, Referrer<Terminal> {

    /**
     * To set the validable, used by the validation methods
     * The validable and the parent (VoltageRegulationHolder) can be different (for example the adder)
     */
    void updateValidable(Validable validable);

    /**
     * To set the holder, used by the validation methods
     * The Parent is set when the VoltageRegulation is build with {@link VoltageRegulationBuilder}
     * but must be set in the equipment's constructor when we use an adder {@link VoltageRegulationAdder}
     */
    void setHolder(VoltageRegulationHolder holder);

    void remove();
}
