/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import com.google.auto.service.AutoService;
import com.powsybl.contingency.list.ContingencyList;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Samir Romdhani {@literal <samir.romdhani_externe at rte-france.com>}
 */
@AutoService(ContingenciesProviderFactory.class)
public class JsonContingenciesProviderFactory implements ContingenciesProviderFactory {

    @Override
    public ContingenciesProvider create() {
        return ContingenciesProviders.emptyProvider();
    }

    @Override
    public ContingenciesProvider create(Path contingenciesFile) {
        Objects.requireNonNull(contingenciesFile);
        return ContingencyList.load(contingenciesFile)::getContingencies;
    }

    @Override
    public ContingenciesProvider create(InputStream is) {
        Objects.requireNonNull(is);
        return ContingencyList.load("contingencies.json", is)::getContingencies;
    }

}
