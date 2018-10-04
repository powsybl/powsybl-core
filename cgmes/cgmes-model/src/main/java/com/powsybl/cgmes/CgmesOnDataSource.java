package com.powsybl.cgmes;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.powsybl.commons.datasource.ReadOnlyDataSource;

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
        return foundNamespaces.contains(CIM_16_NAMESPACE);
    }

    public boolean existsCim14() {
        // check that RDF and CIM16 are defined as namespaces in the data source
        Set<String> foundNamespaces = namespaces();
        if (!foundNamespaces.contains(RDF_NAMESPACE)) {
            return false;
        }
        return foundNamespaces.contains(CIM_14_NAMESPACE);
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
        Set<String> names;
        try {
            names = dataSource.listNames(REGEX_VALID_NAME);
        } catch (IOException x) {
            throw new CgmesModelException(
                    String.format("Listing CGMES names in data source %s", dataSource), x);
        }
        if (names.isEmpty()) {
            throw new CgmesModelException(
                    String.format("Data source %s does not contain CGMES data", dataSource));
        }
        return names;
    }

    public Set<String> namespaces() {
        Set<String> ns = new HashSet<>();
        names().stream().forEach(n -> {
            try {
                ns.addAll(NamespaceReader.namespaces(dataSource.newInputStream(n)));
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
                .filter(ns -> ns.indexOf("CIM-schema-cim") >= 0)
                .findFirst().orElse(CIM_16_NAMESPACE);
    }

    private final ReadOnlyDataSource dataSource;

    private static final String      REGEX_VALID_NAME_IDS = Stream.of(
            Stream.of("ME"),
            Arrays.stream(Subset.values()).map(s -> s.getIdentifier()))
            .flatMap(s -> s)
            .collect(Collectors.joining("|", "(", ")"));
    private static final String      REGEX_VALID_NAME     = ""
            // Ignore case
            + "(?i)"
            // From beginning of name, any number of characters
            + "^.*"
            // Contains one of the valid subset ids
            + REGEX_VALID_NAME_IDS
            // Any number of characters and ending with extension .xml
            + ".*\\.XML$";

    // cim14 is the CIM version corresponding to ENTSO-E Profile 1
    // It is used in this project to explore how to support future CGMES versions
    // We have sample models in cim14 and we use a different set of queries to obtain data

    private static final String      CIM_16_NAMESPACE     = "http://iec.ch/TC57/2013/CIM-schema-cim16#";
    private static final String      CIM_14_NAMESPACE     = "http://iec.ch/TC57/2009/CIM-schema-cim14#";
    private static final String      RDF_NAMESPACE        = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
}
