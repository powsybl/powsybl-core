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

import java.io.*;
import java.util.*;

import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.ref;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public abstract class AbstractCgmesAliasNamingStrategy implements NamingStrategy {

    private final BiMap<String, String> idByUuid = HashBiMap.create();
    private final NameBasedGenerator nameBasedGenerator;

    protected AbstractCgmesAliasNamingStrategy(UUID uuidNamespace) {
        this(Collections.emptyMap(), uuidNamespace);
    }

    protected AbstractCgmesAliasNamingStrategy(Map<String, String> idByUuid, UUID uuidNamespace) {
        this.idByUuid.putAll(Objects.requireNonNull(idByUuid));
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
    public String getCgmesId(Identifiable<?> identifiable) {
        String id = identifiable.getId();
        return getCgmesId(identifiable, id, "UUID");
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
    public void debugIdMapping(String baseName, DataSource ds) {
        String mappingFilename = baseName + "_debug_id_mapping.csv";
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ds.newOutputStream(mappingFilename, false)))) {
            CsvWriterSettings settings = new CsvWriterSettings();
            settings.getFormat().setLineSeparator(System.lineSeparator());
            settings.getFormat().setDelimiter(';');
            settings.getFormat().setQuoteEscape('"');
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
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // FIXME(Luma) For easy testing while we work on moving to name-based UUIDs
    private static final boolean XXX_USE_NAME_BASED_UUIDS = true;
    // FIXME(Luma) adding or not a prefix should be specified by a specific strategy or a configuration parameter
    private static final String XXX_PREFIX = "_";

    @Override
    public String getUniqueId(CgmesObjectReference... refs) {
        if (XXX_USE_NAME_BASED_UUIDS) {
            String seed = XXX_PREFIX + CgmesObjectReference.combine(refs);
            return nameBasedGenerator.generate(seed).toString();
        } else {
            return CgmesExportUtil.getUniqueRandomId();
        }
    }

}
