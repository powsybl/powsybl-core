/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.entsoe.cases;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import eu.itesla_project.commons.config.ModuleConfig;
import eu.itesla_project.commons.config.PlatformConfig;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.entsoe.util.EntsoeGeographicalCode;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EntsoeCaseRepositoryConfig {

    private final Path rootDir;

    private final Multimap<EntsoeGeographicalCode, String> forbiddenFormatsByGeographicalCode;

    public static EntsoeCaseRepositoryConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    static EntsoeCaseRepositoryConfig load(PlatformConfig platformConfig) {
        return load(platformConfig, Importers.getFormats());
    }

    static EntsoeCaseRepositoryConfig load(PlatformConfig platformConfig, Collection<String> supportedFormats) {
        return load("entsoecaserepo", platformConfig, supportedFormats);
    }

    static EntsoeCaseRepositoryConfig load(String moduleConfigName, PlatformConfig platformConfig, Collection<String> supportedFormats) {
        ModuleConfig config = platformConfig.getModuleConfig(moduleConfigName);
        Path rootDir = config.getPathProperty("rootDir");
        Multimap<EntsoeGeographicalCode, String> forbiddenFormatsByCountry = HashMultimap.create();
        for (EntsoeGeographicalCode geographicalCode : EntsoeGeographicalCode.values()) {
            List<String> forbiddenFormats = config.getStringListProperty("forbiddenFormats_" + geographicalCode, Collections.emptyList());
            if (forbiddenFormats.size() > 0) {
                forbiddenFormatsByCountry.putAll(geographicalCode, forbiddenFormats);
            }
        }
        return new EntsoeCaseRepositoryConfig(rootDir, checkedFormats(forbiddenFormatsByCountry, supportedFormats));
    }

    private static Multimap<EntsoeGeographicalCode, String> checkedFormats(Multimap<EntsoeGeographicalCode, String> forbiddenFormatsByGeographicalCode,
                                                                  Collection<String> supportedFormats) {
        // check that formats are valids
        for (String format : forbiddenFormatsByGeographicalCode.values()) {
            if (!supportedFormats.contains(format)) {
                throw new IllegalArgumentException("Unsupported import format " + format);
            }
        }
        return forbiddenFormatsByGeographicalCode;
    }

    public EntsoeCaseRepositoryConfig(Path rootDir, Multimap<EntsoeGeographicalCode, String> forbiddenFormatsByGeographicalCode) {
        this.rootDir = rootDir;
        this.forbiddenFormatsByGeographicalCode = forbiddenFormatsByGeographicalCode;
    }

    public Path getRootDir() {
        return rootDir;
    }

    public Multimap<EntsoeGeographicalCode, String> getForbiddenFormatsByGeographicalCode() {
        return forbiddenFormatsByGeographicalCode;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [rootDir=" + rootDir +
                ", forbiddenFormatsByGeographicalCode=" + forbiddenFormatsByGeographicalCode +
                "]";
    }

}
