/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.naming;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.NameBasedGenerator;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.export.CgmesExportUtil;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Identifiable;
import com.univocity.parsers.csv.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.ref;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public abstract class AbstractCgmesAliasNamingStrategy implements NamingStrategy {

    private final BiMap<String, String> idByUuid = HashBiMap.create();
    private final Map<String, String> uuidSeed = new HashMap<>();
    private final NameBasedGenerator nameBasedGenerator;

    protected AbstractCgmesAliasNamingStrategy(UUID uuidNamespace) {
        // The namespace for generating stable name-based UUIDs is also a UUID
        this.nameBasedGenerator = uuidNamespace == null ? Generators.nameBasedGenerator() : Generators.nameBasedGenerator(uuidNamespace);
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
    public String getIidmName(String type, String name) {
        return name;
    }

    @Override
    public String getCgmesId(Identifiable<?> identifiable) {
        if (CgmesExportUtil.isValidCimMasterRID(identifiable.getId())) {
            return identifiable.getId();
        } else {
            String uuid = getUniqueId(ref(identifiable));
            idByUuid.put(uuid, identifiable.getId());
            return uuid;
        }
    }

    @Override
    public String getCgmesId(Identifiable<?> identifiable, String subObject) {
        //  This is a hack to save in the naming strategy an identifier for something that is not an identifiable:
        //  Connectivity nodes linked to bus/breaker view buses
        String id = identifiable.getId() + "_" + subObject;
        return getCgmesId(identifiable, id, "_" + subObject + "_" + "UUID");
    }

    @Override
    public String getCgmesIdFromAlias(Identifiable<?> identifiable, String aliasType) {
        // This is a hack to save in the naming strategy an identifier for something comes as an alias of an identifiable
        // Equivalent injections of dangling lines
        // Transformer ends of power transformers
        // Tap changers of power transformers
        String id;
        Identifiable<?> realIdentifiable = identifiable;
        if (identifiable instanceof DanglingLine dl) {
            id = identifiable.getAliasFromType(aliasType).or(() -> dl.getTieLine().flatMap(tl -> tl.getAliasFromType(aliasType))).orElseThrow(() -> new PowsyblException("Missing alias " + aliasType + " in " + identifiable.getId()));
            if (dl.isPaired()) {
                realIdentifiable = dl.getTieLine().orElseThrow(IllegalStateException::new);
            }
        } else {
            id = identifiable.getAliasFromType(aliasType)
                    .orElseThrow(() -> new PowsyblException("Missing alias " + aliasType + " in " + identifiable.getId()));
        }
        return getCgmesId(realIdentifiable, id, "_" + aliasType + "_" + "UUID");
    }

    @Override
    public String getCgmesIdFromProperty(Identifiable<?> identifiable, String propertyName) {
        // This is a hack to save in the naming strategy an identifier for something comes as named property of identifiable
        // Generating units and regulating controls of generators
        String id = identifiable.getProperty(propertyName);
        // May be empty
        if (id == null) {
            return null;
        } else {
            return getCgmesId(identifiable, id, "_" + propertyName + "_" + "UUID");
        }
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
            uuid = getUniqueId(ref(identifier));
            // Only store the IDs that have been created during the export
            idByUuid.put(uuid, identifier);
        }
        return uuid;
    }

    private String getCgmesId(Identifiable<?> identifiable, String id, String aliasName) {
        if (idByUuid.containsValue(id)) {
            return idByUuid.inverse().get(id);
        }
        String uuid;
        Optional<String> uuidFromAlias = identifiable.getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + aliasName);
        if (uuidFromAlias.isPresent()) {
            uuid = uuidFromAlias.get();
        } else if (CgmesExportUtil.isValidCimMasterRID(id)) {
            uuid = id;
        } else {
            uuid = getUniqueId(ref(id));
            // Only store the IDs that have been created during the export
            idByUuid.put(uuid, id);
        }
        return uuid;
    }

    @Override
    public void xxxDebug(String baseName, DataSource ds) {
        if (!LOG.isDebugEnabled()) {
            return;
        }
        String mappingFilename = baseName + "_debug_naming_strategy.csv";
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ds.newOutputStream(mappingFilename, false)))) {
            CsvWriterSettings settings = new CsvWriterSettings();
            settings.getFormat().setLineSeparator(System.lineSeparator());
            settings.getFormat().setDelimiter(';');
            settings.getFormat().setQuoteEscape('"');
            CsvWriter csvWriter = new CsvWriter(writer, settings);
            try {
                String[] nextLine = new String[3];
                nextLine[0] = "CgmesUuid";
                nextLine[1] = "IidmId";
                nextLine[2] = "seed";
                csvWriter.writeRow(nextLine);

                for (Map.Entry<String, String> e : idByUuid.entrySet()) {
                    String uuid = e.getKey();
                    nextLine[0] = uuid;
                    nextLine[1] = e.getValue();
                    nextLine[2] = uuidSeed.get(uuid);
                    csvWriter.writeRow(nextLine);
                }
                for (Map.Entry<String, String> e : uuidSeed.entrySet()) {
                    String uuid = e.getKey();
                    if (!idByUuid.containsKey(uuid)) {
                        nextLine[0] = uuid;
                        nextLine[1] = "unknown";
                        nextLine[2] = uuidSeed.get(uuid);
                        csvWriter.writeRow(nextLine);
                    }
                }
            } finally {
                csvWriter.close();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // FIXME(Luma) For easy testing while we work on moving to name-based UUIDs
    private static final boolean XXX_USE_NAME_BASED_UUIDS = true;

    @Override
    public String getUniqueId(CgmesObjectReference... refs) {
        if (XXX_USE_NAME_BASED_UUIDS) {
            String seed = "_" + CgmesObjectReference.combine(refs);
            String uuid = nameBasedGenerator.generate(seed).toString();
            if (uuidSeed.containsKey(uuid)) {
                LOG.debug("Unique ID for seed {} called multiple times ", seed);
            }
            uuidSeed.put(uuid, seed);
            return uuid;
        } else {
            return CgmesExportUtil.getUniqueRandomId();
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCgmesAliasNamingStrategy.class);
}
