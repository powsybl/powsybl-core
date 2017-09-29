/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.action.dsl;

import eu.itesla_project.commons.config.ModuleConfig;
import eu.itesla_project.commons.config.PlatformConfig;
import eu.itesla_project.contingency.ContingenciesProviderFactory;

import java.nio.file.Path;
import java.util.Objects;

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

    @Override
    public GroovyDslContingenciesProvider create(Path dslFile) {
        Objects.requireNonNull(dslFile);
        return new GroovyDslContingenciesProvider(dslFile);
    }

}
