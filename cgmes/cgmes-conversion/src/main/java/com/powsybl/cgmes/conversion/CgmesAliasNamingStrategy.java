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
        }
        idByUuid.put(uuid, id);
        return uuid;
    }

    @Override
    public String getCgmesId(Identifiable<?> identifiable, String subObject) {
        //  This is a hack to save in the naming strategy an identifier for something that is not an identifiable:
        //  Connectivity nodes linked to bus/breaker view buses,
        //  tap changers linked to transformers ????
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
        }
        idByUuid.put(uuid, id);
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
