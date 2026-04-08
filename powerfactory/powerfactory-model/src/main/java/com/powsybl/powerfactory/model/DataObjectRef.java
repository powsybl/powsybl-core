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

    private final long id;

    private final DataObjectIndex index;

    public DataObjectRef(long id, DataObjectIndex index) {
        this.id = id;
        this.index = Objects.requireNonNull(index);
    }

    public long getId() {
        return id;
    }

    public Optional<DataObject> resolve() {
        return index.getDataObjectById(id);
    }
}
