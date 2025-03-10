/**
 * Copyright (c) 2017-2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.model;

import com.powsybl.commons.compress.SafeZipInputStream;
import com.powsybl.commons.datasource.CompressionFormat;
import com.powsybl.commons.datasource.ReadOnlyDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

import static com.powsybl.cgmes.model.CgmesNamespace.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class CgmesOnDataSource {
    private static final String LISTING_CGMES_NAMES_IN_DATA_SOURCE = "Listing CGMES names in data source %s";
    private static final String EXTENSION = "xml";

    public CgmesOnDataSource(ReadOnlyDataSource ds) {
        this.dataSource = ds;
    }

    public ReadOnlyDataSource dataSource() {
        return dataSource;
    }

    private boolean checkIfMainFileNotWithCgmesData(boolean isCim14) throws IOException {
        if (dataSource.getDataExtension() == null || dataSource.getDataExtension().isEmpty() || !dataSource.exists(null, dataSource.getDataExtension())) {
            return false;
        } else if (EXTENSION.equals(dataSource.getDataExtension())) {
            try (InputStream is = dataSource.newInputStream(null, EXTENSION)) {
                return isCim14 ? !existsNamespacesCim14(NamespaceReader.namespaces(is)) : !existsNamespaces(NamespaceReader.namespaces(is));
            }
        }
        return true;
    }

    public boolean exists() throws IOException {
        // Check that the main file is a CGMES file
        if (checkIfMainFileNotWithCgmesData(false)) {
            return false;
        }
        // check that RDF and CIM16 are defined as namespaces in the data source
        return existsNamespaces(namespaces());
    }

    private boolean existsNamespaces(Set<String> namespaces) {
        if (!namespaces.contains(RDF_NAMESPACE)) {
            return false;
        }
        // FIXME(Luma) This is legacy behaviour, we do not consider CIM14 valid in this check
        // But I think we do not need to support 14 separately?
        return namespaces.contains(CIM_16_NAMESPACE) || namespaces.contains(CIM_100_NAMESPACE);
    }

    public boolean existsCim14() throws IOException {
        // Check that the main file is a CGMES file
        if (checkIfMainFileNotWithCgmesData(true)) {
            return false;
        }
        // check that RDF and CIM16 are defined as namespaces in the data source
        return existsNamespacesCim14(namespaces());
    }

    private boolean existsNamespacesCim14(Set<String> namespaces) {
        if (!namespaces.contains(RDF_NAMESPACE)) {
            return false;
        }
        // FIXME(Luma) This is legacy behaviour, we do not consider CIM14 valid in this check
        // But I think we do not need to support 14 separately?
        if (!namespaces.contains(CIM_14_NAMESPACE)) {
            return false;
        }
        return names().stream().anyMatch(CgmesSubset.EQUIPMENT::isValidName);
    }

    public String baseName() {
        // Get the base URI if present, else build an absolute URI from the data source base name
        return names().stream()
                .map(n -> loadInputStreamAndGetNamespace(n, NamespaceReader::base))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(() -> {
                    String name = dataSource.getBaseName().toLowerCase();
                    if (name.isEmpty()) {
                        name = "default-cgmes-model";
                    }
                    return "http://" + name;
                });
    }

    public Set<String> names() {
        try {
            // the set of names may be empty if the data source does not contain CGMES data
            Set<String> allNames = dataSource.listNames(REGEX_VALID_NAME);
            allNames.removeIf(n -> !existsInDatasource(n) || !containsValidNamespace(n));
            return allNames;
        } catch (IOException x) {
            throw new CgmesModelException(String.format(LISTING_CGMES_NAMES_IN_DATA_SOURCE, dataSource), x);
        }
    }

    private boolean existsInDatasource(String fileName) {
        try {
            return dataSource.exists(fileName);
        } catch (IOException e) {
            return false;
        }
    }

    private <T> T loadInputStreamAndGetNamespace(String n, Function<InputStream, T> namespaceGetter) {
        try (InputStream in = dataSource.newInputStream(n)) {
            String fileExtension = n.substring(n.lastIndexOf('.') + 1);
            if (fileExtension.equals(CompressionFormat.ZIP.getExtension())) {
                try (SafeZipInputStream zis = new SafeZipInputStream(new ZipInputStream(in), 1024)) {
                    zis.getNextEntry();
                    return namespaceGetter.apply(zis);
                }
            } else {
                return namespaceGetter.apply(in);
            }
        } catch (IOException e) {
            throw new CgmesModelException(String.format(LISTING_CGMES_NAMES_IN_DATA_SOURCE, dataSource), e);
        }
    }

    private boolean containsValidNamespace(String name) {
        Set<String> ns = loadInputStreamAndGetNamespace(name, NamespaceReader::namespacesOrEmpty);
        return ns.contains(RDF_NAMESPACE) && ns.stream().anyMatch(CgmesNamespace::isValid);
    }

    public Set<String> namespaces() {
        return names().stream()
                .map(name -> loadInputStreamAndGetNamespace(name, NamespaceReader::namespaces))
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    public String cimNamespace() {
        // If no cim namespace is found, return CIM16 namespace
        return namespaces().stream()
            .filter(CgmesNamespace::isValid)
            .findFirst()
            .orElseThrow(() -> new CgmesModelException("CIM Namespace not found"));
    }

    private final ReadOnlyDataSource dataSource;
    private static final String REGEX_VALID_NAME = ""
        // Ignore case
        + "(?i)"
        // Any number of characters from the start
        + "^.*"
        // Ending with extension .xml
        + "\\.(XML|ZIP)$";
}
