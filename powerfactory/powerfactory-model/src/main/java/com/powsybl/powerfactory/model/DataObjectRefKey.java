/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.model;

import java.util.Objects;

/**
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public record DataObjectRefKey(Long id, String foreignKey) {

    private static final String OBJECT_REFERENCE_PREFIX = "##";

    public static DataObjectRefKey ofId(long id) {
        return new DataObjectRefKey(id, String.valueOf(id));
    }

    public static DataObjectRefKey ofForeignKey(String foreignKey) {
        return new DataObjectRefKey(null, Objects.requireNonNull(foreignKey));
    }

    public static DataObjectRefKey parse(String s) {
        if (s.startsWith(OBJECT_REFERENCE_PREFIX)) {
            return ofForeignKey(s);
        } else {
            return ofId(Long.parseLong(s));
        }
    }

    public long resolveId(DataObjectIndex index) {
        Objects.requireNonNull(index);

        if (id != null) {
            return id;
        }

        if (isObjectReference()) {
            return index.getDataObjectByForeignKey(cleanForeignKey())
                    .map(DataObject::getId)
                    .orElseThrow(() -> new PowerFactoryException(
                            "objectReference 'for_name' not found " + foreignKey));
        }

        return Long.parseLong(foreignKey);
    }

    private boolean isObjectReference() {
        return foreignKey != null && foreignKey.startsWith(OBJECT_REFERENCE_PREFIX);
    }

    private String cleanForeignKey() {
        return foreignKey.substring(OBJECT_REFERENCE_PREFIX.length());
    }
}
