/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation.dsl;

import java.io.InputStream;
import java.nio.file.Path;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.dynamicsimulation.CurvesProvider;
import com.powsybl.dynamicsimulation.CurvesProviderFactory;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
@AutoService(CurvesProviderFactory.class)
public class GroovyDslCurvesProviderFactory implements CurvesProviderFactory {

    @Override
    public CurvesProvider create() {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("groovy-dsl-curves");
        Path dslFile = config.getPathProperty("dsl-file");
        return new GroovyDslCurvesProvider(dslFile);
    }

    /**
     * Creates a provider which will read the DSL from a UTF-8 encoded file.
     */
    @Override
    public CurvesProvider create(Path dslFile) {
        return new GroovyDslCurvesProvider(dslFile);
    }

    /**
     * Creates a provider which will read the DSL from a UTF-8 encoded input stream.
     */
    @Override
    public CurvesProvider create(InputStream dslStream) {
        return new GroovyDslCurvesProvider(dslStream);
    }

}
