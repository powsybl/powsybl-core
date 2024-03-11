/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation;

import java.util.Collections;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
final class DynamicModelsSupplierMock {

    private DynamicModelsSupplierMock() {
    }

    static DynamicModelsSupplier empty() {
        return (network, reportNode) -> Collections.emptyList();
    }

}
