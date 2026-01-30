/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
final class ShuntSerDe extends AbstractShuntCompensatorSerDe {

    static final ShuntSerDe INSTANCE = new ShuntSerDe();
    static final String ROOT_ELEMENT_NAME = "shunt";
    static final String ARRAY_ELEMENT_NAME = "shunts";

    private ShuntSerDe() {
        super(ROOT_ELEMENT_NAME, null, IidmVersion.V_1_15);
    }

}
