/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.DanglingLineCharacteristicsAdder;
import com.powsybl.iidm.network.Validable;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public interface GenerationAdderHolder<H extends DanglingLineCharacteristicsAdder<H>> extends Validable, DanglingLineCharacteristicsAdder<H> {

    NetworkImpl getNetwork();

    void setGenerationAdder(GenerationAdderImpl<H> adder);
}
