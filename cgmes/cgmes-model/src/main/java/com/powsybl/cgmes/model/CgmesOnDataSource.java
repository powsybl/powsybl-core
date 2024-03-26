/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.model;

import com.powsybl.commons.datasource.ReadOnlyDataSource;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.powsybl.cgmes.model.CgmesNamespace.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
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
        // FIXME(Luma) This is legacy behaviour, we do not consider CIM14 valid in this check
        // But I think we do not need to support 14 separately?
        return foundNamespaces.contains(CIM_16_NAMESPACE) || foundNamespaces.contains(CIM_100_NAMESPACE);
    }

    public boolean existsCim14() {
        // check that RDF and CIM16 are defined as namespaces in the data source
        Set<String> foundNamespaces = namespaces();
        if (!foundNamespaces.contains(RDF_NAMESPACE)) {
            return false;
        }
        // FIXME(Luma) This is legacy behaviour, we do not consider CIM14 valid in this check
        // But I think we do not need to support 14 separately?
        if (!foundNamespaces.contains(CIM_14_NAMESPACE)) {
            return false;
        }

        return names().stream().anyMatch(CgmesSubset.EQUIPMENT::isValidName);
    }

    public String baseName() {
        // Get the base URI if present, else build an absolute URI from the data source base name
        return names().stream()
                .map(n -> {
                    try (InputStream is = dataSource.newInputStream(n)) {
                        return NamespaceReader.base(is);
                    } catch (IOException x) {
                        throw new UncheckedIOException(x);
                    }
                })
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
            allNames.removeIf(n -> !containsValidNamespace(n));
            return allNames;
        } catch (IOException x) {
            throw new CgmesModelException(String.format("Listing CGMES names in data source %s", dataSource), x);
        }
    }

    private boolean containsValidNamespace(String name) {
        try (InputStream is = dataSource.newInputStream(name)) {
            Set<String> ns = NamespaceReader.namespaces1(is);
            return ns.contains(RDF_NAMESPACE) && ns.stream().anyMatch(CgmesNamespace::isValid);
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
        + "\\.XML$";
}
