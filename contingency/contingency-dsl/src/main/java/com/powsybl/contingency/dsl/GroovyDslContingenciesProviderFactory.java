/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.dsl;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.contingency.ContingenciesProviderFactory;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ContingenciesProviderFactory.class)
public class GroovyDslContingenciesProviderFactory implements ContingenciesProviderFactory {

    @Override
    public GroovyDslContingenciesProvider create() {
        Path dslFile = PlatformConfig.defaultConfig().getOptionalModuleConfig("groovy-dsl-contingencies")
                .map(config -> config.getOptionalPathProperty("dsl-file").orElseThrow(() -> new PowsyblException("PlatformConfig incomplete: property dsl-file not found in module groovy-dsl-contingencies")))
                .orElseThrow(() -> new PowsyblException("PlatformConfig incomplete: Module groovy-dsl-contingencies not found"));
        return new GroovyDslContingenciesProvider(dslFile);
    }

    /**
     * Creates a provider which will read the DSL from a UTF-8 encoded file.
     */
    @Override
    public GroovyDslContingenciesProvider create(Path dslFile) {
        return new GroovyDslContingenciesProvider(dslFile);
    }

    /**
     * Creates a provider which will read the DSL from a UTF-8 encoded input stream.
     */
    @Override
    public GroovyDslContingenciesProvider create(InputStream dslStream) {
        return new GroovyDslContingenciesProvider(dslStream);
    }

}
