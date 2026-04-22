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

    private final String foreignKey;

    private Long id;

    private final DataObjectIndex index;

    public DataObjectRef(long id, DataObjectIndex index) {
        this.foreignKey = String.valueOf(id);
        this.id = id;
        this.index = Objects.requireNonNull(index);
    }

    public DataObjectRef(String foreignKey, DataObjectIndex index) {
        this.foreignKey = foreignKey;
        this.id = null;
        this.index = Objects.requireNonNull(index);
    }

    public long getId() {
        if (id != null) {
            return id;
        }
        if (isObjectReference(foreignKey)) {
            id = index.getDataObjectByForeignKey(cleanObjectReference(foreignKey))
                    .map(DataObject::getId)
                    .orElseThrow(() -> new PowerFactoryException("objectReference 'for_name' not found " + foreignKey));
        } else {
            id = Long.parseLong(foreignKey);
        }
        return id;
    }

    public Optional<DataObject> resolve() {
        return index.getDataObjectById(getId());
    }

    private static boolean isObjectReference(String objectReference) {
        return objectReference != null && objectReference.startsWith("##");
    }

    private static String cleanObjectReference(String objectReference) {
        return objectReference.substring(2);
    }
}
