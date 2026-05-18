/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

/**
 * @author Samir Romdhani {@literal <samir.romdhani_externe at rte-france.com>}
 */
final class ShuntCompensatorSerDe extends AbstractShuntCompensatorSerDe {

    static final ShuntCompensatorSerDe INSTANCE = new ShuntCompensatorSerDe();
    static final String ROOT_ELEMENT_NAME = "shuntCompensator";
    static final String ARRAY_ELEMENT_NAME = "shuntCompensators";

    private ShuntCompensatorSerDe() {
        super(ROOT_ELEMENT_NAME, IidmVersion.V_1_16, null);
    }
}
