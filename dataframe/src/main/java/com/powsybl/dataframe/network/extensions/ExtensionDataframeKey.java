/**
 * Copyright (c) 2021-2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dataframe.network.extensions;

import java.util.Objects;

/**
 * @author Hugo Kulesza <hugo.kulesza@rte-france.com>
 */
public class ExtensionDataframeKey {
    private final String extensionName;
    private final String tableName;

    private final int hashCode;

    public ExtensionDataframeKey(String extensionName, String tableName) {
        this.extensionName = Objects.requireNonNull(extensionName);
        this.tableName = tableName;
        this.hashCode = Objects.hash(extensionName, tableName);
    }

    public String getExtensionName() {
        return extensionName;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ExtensionDataframeKey)) {
            return false;
        }
        ExtensionDataframeKey key = (ExtensionDataframeKey) o;

        if (key.tableName == null) {
            return this.tableName == null && key.extensionName.equals(this.extensionName);
        }
        return key.tableName.equals(this.tableName) && key.extensionName.equals(this.extensionName);
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }
}
