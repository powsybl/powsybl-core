/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.model;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.powsybl.commons.datasource.ReadOnlyDataSource;

import java.util.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CimArchive {

    private final Multimap<Subset, String> fileNames = HashMultimap.create();

    private final Set<String> namespaces;

    public CimArchive(Set<String> namespaces) {
        this.namespaces = Objects.requireNonNull(namespaces);
    }

    public void addFileName(Subset subset, String fileName) {
        Objects.requireNonNull(subset);
        fileNames.put(subset, fileName);
    }

    public Collection<String> getFileName(Subset subset) {
        Objects.requireNonNull(subset);
        return fileNames.get(subset);
    }

    public Collection<String> getAll() {
        return fileNames.values();
    }

    public String baseName() {
        return fileNames.get(Subset.EQUIPMENT).iterator().next().replace("_EQ", "").replace(".xml", "");
    }

    public String cimNamespace() {
        return namespaces.contains(CgmesNames.CIM_16_NAMESPACE) ? CgmesNames.CIM_16_NAMESPACE
                : CgmesNames.CIM_14_NAMESPACE;
    }

    private static String getEqFileName(ReadOnlyDataSource dataSource) {
        String eqFileName = null;
        if (dataSource.getMainFileName() != null && !dataSource.getMainFileName().isEmpty()) {
            if (dataSource.getMainFileName().contains("_EQ") && dataSource.getMainFileName().endsWith(".xml")) {
                eqFileName = dataSource.getMainFileName();
            }
        } else {
            Set<String> eqFileNames = dataSource.getFileNames(".*_EQ(?!_BD).*.xml");
            if (eqFileNames.size() == 1) {
                eqFileName = eqFileNames.iterator().next();
            }
        }
        return eqFileName;
    }

    public static Optional<CimArchive> load(ReadOnlyDataSource dataSource) {
        Objects.requireNonNull(dataSource);

        String eqFileName = getEqFileName(dataSource);
        if (eqFileName == null) {
            return Optional.empty();
        }
        if (!dataSource.fileExists(eqFileName)) {
            return Optional.empty();
        }
        Set<String> namespaces = NamespaceReader.namespaces(dataSource.newInputStream(eqFileName));
        if (!namespaces.contains(CgmesNames.RDF_NAMESPACE)
                && !namespaces.contains(CgmesNames.CIM_14_NAMESPACE)
                && !namespaces.contains(CgmesNames.CIM_16_NAMESPACE)) {
            return Optional.empty();
        }
        CimArchive archive = new CimArchive(namespaces);
        archive.addFileName(Subset.EQUIPMENT, eqFileName);

        if (namespaces.contains(CgmesNames.CIM_16_NAMESPACE)) {
            String sshFileName = eqFileName.replace("_EQ", "_SSH");
            if (dataSource.fileExists(sshFileName)) {
                archive.addFileName(Subset.STEADY_STATE_HYPOTHESIS, sshFileName);
            }
        }

        String tpFileName = eqFileName.replace("_EQ", "_TP");
        if (dataSource.fileExists(tpFileName)) {
            archive.addFileName(Subset.TOPOLOGY, tpFileName);
        }

        String svFileName = eqFileName.replace("_EQ", "_SV");
        if (dataSource.fileExists(svFileName)) {
            archive.addFileName(Subset.STATE_VARIABLES, svFileName);
        }

        // also add boundary files
        for (String boundaryFileName : dataSource.getFileNames(".*_EQ_BD.*.xml")) {
            archive.addFileName(Subset.EQUIPMENT, boundaryFileName);
        }
        for (String boundaryFileName : dataSource.getFileNames(".*_TP_BD.*.xml")) {
            archive.addFileName(Subset.TOPOLOGY, boundaryFileName);
        }

        return Optional.of(archive);
    }

    public static CimArchive loadOrThrowException(ReadOnlyDataSource dataSource) {
        return load(dataSource).orElseThrow(() -> new CgmesModelException("No valid CGMES data found in data source"));
    }
}
