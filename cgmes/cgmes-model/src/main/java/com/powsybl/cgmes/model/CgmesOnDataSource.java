/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model;

import static com.powsybl.cgmes.model.CgmesNamespace.CIM_14_NAMESPACE;
import static com.powsybl.cgmes.model.CgmesNamespace.CIM_16_NAMESPACE;
import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;

import com.powsybl.commons.datasource.ReadOnlyDataSource;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class CgmesOnDataSource {
    public CgmesOnDataSource(ReadOnlyDataSource ds) {
        this.dataSource = ds;
    }

    public ReadOnlyDataSource dataSource() {
        return dataSource;
    }

    public boolean exists() {
        // check that RDF and CIM16 are defined as namespaces in the data source
        Set<String> foundNamespaces = namespaces();
        if (!foundNamespaces.contains(RDF_NAMESPACE)) {
            return false;
        }
        if (!foundNamespaces.contains(CIM_16_NAMESPACE)) {
            return false;
        }
        return names().stream().anyMatch(CgmesSubset.EQUIPMENT::isValidName);
    }

    public boolean existsCim14() {
        // check that RDF and CIM16 are defined as namespaces in the data source
        Set<String> foundNamespaces = namespaces();
        if (!foundNamespaces.contains(RDF_NAMESPACE)) {
            return false;
        }
        if (!foundNamespaces.contains(CIM_14_NAMESPACE)) {
            return false;
        }
        return names().stream().anyMatch(CgmesSubset.EQUIPMENT::isValidName);
    }

    public String baseName() {
        // Build an absolute IRI from the data source base name
        String name = dataSource.getBaseName().toLowerCase();
        if (name.isEmpty()) {
            name = "default-cgmes-model";
        }
        return "http://" + name;
    }

    public Set<String> names() {
        try {
            // the set of names may be empty if the data source does not contain CGMES data
            Set<String> allNames = dataSource.listNames(REGEX_VALID_NAME);
            allNames.removeIf(n -> !containsValidNamespace(n));
            return allNames;
        } catch (IOException x) {
            throw new CgmesModelException(String.format("Listing CGMES names in data source %s", dataSource), x);
        }
    }

    private boolean containsValidNamespace(String name) {
        try (InputStream is = dataSource.newInputStream(name)) {
            Set<String> ns = NamespaceReader.namespaces1(is);
            return ns.contains(RDF_NAMESPACE) && (ns.contains(CIM_16_NAMESPACE) || ns.contains(CIM_14_NAMESPACE));
        } catch (XMLStreamException e) {
            return false;
        } catch (IOException x) {
            throw new CgmesModelException(String.format("Listing CGMES names in data source %s", dataSource), x);
        }
    }

    public Set<String> namespaces() {
        Set<String> ns = new HashSet<>();
        names().forEach(n -> {
            try (InputStream is = dataSource.newInputStream(n)) {
                ns.addAll(NamespaceReader.namespaces(is));
            } catch (IOException x) {
                throw new UncheckedIOException(x);
            }
        });
        return ns;
    }

    public String cimNamespace() {
        // Return the first namespace that contains the string "CIM-schema-cim"
        // If no namespace is found, return CIM16 namespace
        Set<String> foundNamespaces = namespaces();
        return foundNamespaces.stream()
            .filter(ns -> ns.contains("CIM-schema-cim"))
            .findFirst().orElse(CIM_16_NAMESPACE);
    }

    private final ReadOnlyDataSource dataSource;

    private static final String REGEX_VALID_NAME_IDS = Stream.of(
        Stream.of("ME"),
        Arrays.stream(CgmesSubset.values()).map(CgmesSubset::getIdentifier))
        .flatMap(s -> s)
        .collect(Collectors.joining("|", "(", ")"));
    private static final String REGEX_VALID_NAME = ""
        // Ignore case
        + "(?i)"
        // Any number of characters from the start
        + "^.*"
        // Contains one of the valid subset ids
        + REGEX_VALID_NAME_IDS
        // Any number of characters and ending with extension .xml
        + ".*\\.XML$";
}
