/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.dynamicsimulation.groovy;

import java.util.Objects;

import com.powsybl.dynamicsimulation.DynamicModel;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
class DummyDynamicModel implements DynamicModel {

    private final String id;

    private final String parameterSetId;

    DummyDynamicModel(String id, String parameterSetId) {
        this.id = Objects.requireNonNull(id);
        this.parameterSetId = Objects.requireNonNull(parameterSetId);
    }

    String getId() {
        return id;
    }

    String getParameterSetId() {
        return parameterSetId;
    }

}
