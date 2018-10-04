/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PropertiesModuleConfigRepository implements ModuleConfigRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesModuleConfigRepository.class);

    private final Path configDir;

    public PropertiesModuleConfigRepository(Path configDir) {
        this.configDir = Objects.requireNonNull(configDir);
    }

    private Path getModulePath(String name) {
        return configDir.resolve(name + ".properties");
    }

    @Override
    public boolean moduleExists(String name) {
        Path path = getModulePath(name);
        return Files.exists(path);
    }

    @Override
    public Optional<ModuleConfig> getModuleConfig(String name) {
        Path path = getModulePath(name);
        if (!Files.exists(path)) {
            return Optional.empty();
        }
        LOGGER.info("Reading property file {}", path);
        Properties properties = new Properties();
        try {
            try (InputStream is = Files.newInputStream(path)) {
                properties.load(is);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return Optional.of(new MapModuleConfig(properties, configDir.getFileSystem()));
    }

    public static void writeXml(Path configDir, Path xmlFile) throws IOException, XMLStreamException {
        XMLOutputFactory output = XMLOutputFactory.newInstance();
        try (Writer writer = Files.newBufferedWriter(xmlFile, StandardCharsets.UTF_8)) {
            XMLStreamWriter xmlWriter = output.createXMLStreamWriter(writer);
            try {
                xmlWriter.writeStartDocument(StandardCharsets.UTF_8.toString(), "1.0");
                xmlWriter.writeStartElement("config");
                try (DirectoryStream<Path> ds = Files.newDirectoryStream(configDir, entry -> Files.isRegularFile(entry) && entry.getFileName().toString().endsWith(".properties"))) {
                    for (Path file : ds) {
                        String fileName = file.getFileName().toString();
                        String fileNameWithoutExtension = fileName.substring(0, fileName.length() - 11);
                        xmlWriter.writeStartElement(fileNameWithoutExtension);
                        Properties properties = new Properties();
                        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                            properties.load(reader);
                        }
                        for (String name : properties.stringPropertyNames()) {
                            String value = properties.getProperty(name);
                            xmlWriter.writeStartElement(name);
                            xmlWriter.writeCharacters(value);
                            xmlWriter.writeEndElement();
                        }
                        xmlWriter.writeEndElement();
                    }
                }
                xmlWriter.writeEndElement();
                xmlWriter.writeEndDocument();
            } finally {
                xmlWriter.close();
            }
        }
    }
}
