/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.entsoe.cases;

import com.google.common.collect.Multimap;
import eu.itesla_project.commons.config.PlatformConfig;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.entsoe.util.EntsoeGeographicalCode;

import java.nio.file.Path;
import java.util.Collection;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class EntsoeAndXmlCaseRepositoryConfig extends EntsoeCaseRepositoryConfig {

    public EntsoeAndXmlCaseRepositoryConfig(Path rootDir, Multimap<EntsoeGeographicalCode, String> forbiddenFormatsByGeographicalCode) {
        super(rootDir, forbiddenFormatsByGeographicalCode);
    }

    public static EntsoeCaseRepositoryConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    static EntsoeCaseRepositoryConfig load(PlatformConfig platformConfig) {
        return load(platformConfig, Importers.getFormats());
    }

    static EntsoeCaseRepositoryConfig load(PlatformConfig platformConfig, Collection<String> supportedFormats) {
        return EntsoeCaseRepositoryConfig.load("entsoeandxmlcaserepo", platformConfig, supportedFormats);
    }
}
