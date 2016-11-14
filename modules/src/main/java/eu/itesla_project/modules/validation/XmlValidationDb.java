/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.validation;

import eu.itesla_project.iidm.datasource.DataSource;
import eu.itesla_project.iidm.datasource.GzFileDataSource;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.xml.NetworkXml;
import eu.itesla_project.iidm.xml.XMLExportOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class XmlValidationDb implements ValidationDb {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlValidationDb.class);

    private final Path dbDir;

    XmlValidationDb(Path dbDir) {
        this.dbDir = Objects.requireNonNull(dbDir);
    }

    private Path getDir(String path) {
        return dbDir.resolve(path);
    }

    @Override
    public void init(String path) {
        Path dir = getDir(path);
        try {
            if (Files.exists(dir)) {
                try (Stream<Path> stream = Files.list(dir)) {
                    stream.filter(p -> p.getFileName().toString().endsWith(".xml.gz")).forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            LOGGER.error(e.toString(), e);
                        }
                    });
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(Network network, String path, String name) {
        LOGGER.info("Save {} in {} of validation db", name, path);
        Path dir = getDir(path);
        try {
            Files.createDirectories(dir);
            DataSource outputStreamFactory = new GzFileDataSource(dir, name);
            try (OutputStream os = new BufferedOutputStream(outputStreamFactory.newOutputStream(null, "xml", false))) {
                XMLExportOptions options = new XMLExportOptions(true, false, true, false);
                NetworkXml.write(network, options, os);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
