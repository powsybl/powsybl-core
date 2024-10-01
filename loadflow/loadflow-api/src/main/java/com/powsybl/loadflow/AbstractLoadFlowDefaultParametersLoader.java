/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow;

import java.io.InputStream;
import java.util.Objects;

/**
 * @author Hugo Kulesza {@literal <hugo.kulesza at rte-france.com>}
 */
public abstract class AbstractLoadFlowDefaultParametersLoader implements LoadFlowDefaultParametersLoader {

    private final String name;
    private final String jsonParametersFile;

    public AbstractLoadFlowDefaultParametersLoader(String name, String jsonParametersFile) {
        this.name = Objects.requireNonNull(name);
        this.jsonParametersFile = Objects.requireNonNull(jsonParametersFile);
    }

    @Override
    public String getSourceName() {
        return name;
    }

    public InputStream loadDefaultParametersFromFile() {
        return getClass().getResourceAsStream(jsonParametersFile);
    }
}
