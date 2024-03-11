/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dataframe.network;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class ExtensionInformation {
    private final String id;
    private final String description;
    private final String attributes;

    public ExtensionInformation(String id, String description, String attributes) {
        this.id = id;
        this.description = description;
        this.attributes = attributes;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getAttributes() {
        return attributes;
    }
}
