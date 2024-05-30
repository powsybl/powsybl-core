/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow;

/**
 * @author Hugo Kulesza {@literal <hugo.kulesza at rte-france.com>}
 */
public class LoadFlowDefaultParametersLoaderMock extends AbstractLoadFlowDefaultParametersLoader {

    private static final String RESOURCE_FILE = "/LoadFlowParametersUpdate.json";

    LoadFlowDefaultParametersLoaderMock(String name) {
        super(name, RESOURCE_FILE);
    }
}
