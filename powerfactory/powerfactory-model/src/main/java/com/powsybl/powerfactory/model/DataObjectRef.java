/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.model;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class DataObjectRef {

    private final DataObjectRefKey key;

    private final DataObjectIndex index;

    public DataObjectRef(DataObjectRefKey key, DataObjectIndex index) {
        this.key = Objects.requireNonNull(key);
        this.index = Objects.requireNonNull(index);
    }

    public long getId() {
        return key.resolveId(index);
    }

    public Optional<DataObject> resolve() {
        return index.getDataObjectById(getId());
    }
}
