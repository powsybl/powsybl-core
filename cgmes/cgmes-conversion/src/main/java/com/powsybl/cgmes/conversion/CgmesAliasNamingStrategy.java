/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.powsybl.cgmes.conversion.export.CgmesExportUtil;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.network.Identifiable;
import com.univocity.parsers.csv.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class CgmesAliasNamingStrategy implements NamingStrategy {

    private final BiMap<String, String> idByUuid = HashBiMap.create();

    public CgmesAliasNamingStrategy() {
    }

    public CgmesAliasNamingStrategy(Map<String, String> idByUuid) {
        this.idByUuid.putAll(Objects.requireNonNull(idByUuid));
    }

    public CgmesAliasNamingStrategy(InputStream is) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            CsvParserSettings settings = new CsvParserSettings();
            setFormat(settings.getFormat());
            CsvParser csvParser = new CsvParser(settings);
            for (String[] nextLine : csvParser.iterate(reader)) {
                if (nextLine.length != 2) {
                    throw new PowsyblException("Invalid line '" + Arrays.toString(nextLine) + "'");
                }
                idByUuid.put(nextLine[0], nextLine[1]);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void setFormat(CsvFormat format) {
        format.setLineSeparator(System.lineSeparator());
        format.setDelimiter(';');
        format.setQuoteEscape('"');
    }

    @Override
    public String getGeographicalTag(String geo) {
        return geo;
    }

    @Override
    public String getIidmId(String type, String id) {
        return idByUuid.getOrDefault(id, id);
    }

    @Override
    public String getCgmesId(Identifiable<?> identifiable) {
        String id = identifiable.getId();
        if (idByUuid.containsValue(id)) {
            return idByUuid.inverse().get(id);
        }
        String uuid;
        Optional<String> uuidFromAlias = identifiable.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "UUID");
        if (uuidFromAlias.isPresent()) {
            uuid = uuidFromAlias.get();
        } else if (CgmesExportUtil.isValidCimMasterRID(id)) {
            uuid = id;
        } else {
            uuid = CgmesExportUtil.getUniqueId();
            // Only store the IDs that have been created during the export
            idByUuid.put(uuid, id);
        }
        return uuid;
    }

    @Override
    public String getCgmesId(Identifiable<?> identifiable, String subObject) {
        //  This is a hack to save in the naming strategy an identifier for something that is not an identifiable:
        //  Connectivity nodes linked to bus/breaker view buses
        String id = identifiable.getId() + "_" + subObject;
        if (idByUuid.containsValue(id)) {
            return idByUuid.inverse().get(id);
        }
        String uuid;
        Optional<String> uuidFromAlias = identifiable.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "_" + subObject + "_" + "UUID");
        if (uuidFromAlias.isPresent()) {
            uuid = uuidFromAlias.get();
        } else if (CgmesExportUtil.isValidCimMasterRID(id)) {
            uuid = id;
        } else {
            uuid = CgmesExportUtil.getUniqueId();
            // Only store the IDs that have been created during the export
            idByUuid.put(uuid, id);
        }
        return uuid;
    }

    @Override
    public String getCgmesIdFromAlias(Identifiable<?> identifiable, String aliasType) {
        // This is a hack to save in the naming strategy an identifier for something comes as an alias of an identifiable
        // Equivalent injections of dangling lines
        // Transformer ends of power transformers
        // Tap changers of power transformers
        String id = identifiable.getAliasFromType(aliasType)
                .orElseThrow(() -> new PowsyblException("Missing alias " + aliasType + " in " + identifiable.getId()));
        if (idByUuid.containsValue(id)) {
            return idByUuid.inverse().get(id);
        }
        String uuid;
        Optional<String> uuidFromAlias = identifiable.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "_" + aliasType + "_" + "UUID");
        if (uuidFromAlias.isPresent()) {
            uuid = uuidFromAlias.get();
        } else if (CgmesExportUtil.isValidCimMasterRID(id)) {
            uuid = id;
        } else {
            uuid = CgmesExportUtil.getUniqueId();
            // Only store the IDs that have been created during the export
            idByUuid.put(uuid, id);
        }
        return uuid;
    }

    @Override
    public String getCgmesIdFromProperty(Identifiable<?> identifiable, String propertyName) {
        // This is a hack to save in the naming strategy an identifier for something comes as named property of identifiable
        // Generating units and regulating controls of generators
        String id = identifiable.getProperty(propertyName);
        // May be empty
        if (id == null) {
            return null;
        }
        if (idByUuid.containsValue(id)) {
            return idByUuid.inverse().get(id);
        }
        String uuid;
        Optional<String> uuidFromAlias = identifiable.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "_" + propertyName + "_" + "UUID");
        if (uuidFromAlias.isPresent()) {
            uuid = uuidFromAlias.get();
        } else if (CgmesExportUtil.isValidCimMasterRID(id)) {
            uuid = id;
        } else {
            uuid = CgmesExportUtil.getUniqueId();
            // Only store the IDs that have been created during the export
            idByUuid.put(uuid, id);
        }
        return uuid;
    }

    @Override
    public String getCgmesId(String identifier) {
        // This is a hack to save in the naming strategy an identifier for something that has no related IIDM object
        // Control Area identifiers
        if (idByUuid.containsValue(identifier)) {
            return idByUuid.inverse().get(identifier);
        }
        String uuid;
        if (CgmesExportUtil.isValidCimMasterRID(identifier)) {
            uuid = identifier;
        } else {
            uuid = CgmesExportUtil.getUniqueId();
            // Only store the IDs that have been created during the export
            idByUuid.put(uuid, identifier);
        }
        return uuid;
    }

    @Override
    public String getName(String type, String name) {
        return name;
    }

    @Override
    public void readIdMapping(Identifiable<?> identifiable, String type) {
        if (idByUuid.containsValue(identifiable.getId())) {
            String uuid = idByUuid.inverse().get(identifiable.getId());
            // alias UUID is only created on request, for selected IIDM objects.
            // There is no problem if the mapping contains more objects than the ones that are stored in UUID aliases
            identifiable.addAlias(uuid, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "UUID");
        }
    }

    @Override
    public void writeIdMapping(Path path) {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writeIdMapping(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeIdMapping(String mappingFileName, DataSource ds) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ds.newOutputStream(mappingFileName, false)))) {
            writeIdMapping(writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeIdMapping(BufferedWriter writer) {
        CsvWriterSettings settings = new CsvWriterSettings();
        setFormat(settings.getFormat());
        CsvWriter csvWriter = new CsvWriter(writer, settings);
        try {
            String[] nextLine = new String[2];
            for (Map.Entry<String, String> e : idByUuid.entrySet()) {
                nextLine[0] = e.getKey();
                nextLine[1] = e.getValue();
                csvWriter.writeRow(nextLine);
            }
        } finally {
            csvWriter.close();
        }
    }
}
