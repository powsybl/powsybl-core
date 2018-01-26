/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl;

import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.contingency.ContingenciesProviderFactory;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class GroovyDslContingenciesProviderFactory implements ContingenciesProviderFactory {

    @Override
    public GroovyDslContingenciesProvider create() {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("groovy-dsl-contingencies");
        Path dslFile = config.getPathProperty("dsl-file");
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
