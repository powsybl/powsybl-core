/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.triplestore.api;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.util.ServiceLoaderCache;

/**
 * Factory for the creation of Triplestore databases.
 * It relies on named factory services to create instances.
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public final class TripleStoreFactory {

    private static final ServiceLoaderCache<TripleStoreFactoryService> LOADER = new ServiceLoaderCache<>(TripleStoreFactoryService.class);

    private TripleStoreFactory() {
    }

    /**
     * Create a Triplestore database using the default implementation.
     *
     * @return a Triplestore based on the default implementation
     */
    public static TripleStore create() {
        return create(DEFAULT_IMPLEMENTATION);
    }

    /**
     * Crate a Triplestore that is a copy of the given Triplestore.
     * Copied Triplestore will be based on the same implementation of the source Triplestore.
     *
     * @param source the source Triplestore
     * @return a new Triplestore that is a copy of the source Triplestore
     */
    public static TripleStore copy(TripleStore source) {
        // Use the same implementation of the source TripleStore
        String impl = source.getImplementationName();
        for (TripleStoreFactoryService ts : LOADER.getServices()) {
            if (ts.getImplementationName().equals(impl)) {
                return ts.copy(source);
            }
        }
        throw new PowsyblException("No implementation available for triple store " + impl);
    }

    /**
     * Create a Triplestore database using the given implementation.
     *
     * @param impl the name of the Triplestore implementation that must be used
     * @return a Triplestore based on the given implementation
     */
    public static TripleStore create(String impl) {
        Objects.requireNonNull(impl);
        for (TripleStoreFactoryService ts : LOADER.getServices()) {
            if (ts.getImplementationName().equals(impl)) {
                return ts.create();
            }
        }
        throw new PowsyblException("No implementation available for triple store " + impl);
    }

    /**
     * List all Triplestore implementations available.
     *
     * @return a list with the names of all available Triplestore implementations
     */
    public static List<String> allImplementations() {
        return LOADER.getServices().stream().map(TripleStoreFactoryService::getImplementationName)
                .collect(Collectors.toList());
    }

    /**
     * List all available Triplestore implementations that support nested graph clauses in SPARQL queries.
     *
     * @return a list with the names of all available Triplestore implementations supporting nested graph clauses in SPARQL queries
     */
    public static List<String> implementationsWorkingWithNestedGraphClauses() {
        return LOADER.getServices().stream().filter(TripleStoreFactoryService::isWorkingWithNestedGraphClauses)
                .map(TripleStoreFactoryService::getImplementationName).collect(Collectors.toList());
    }

    /**
     * List all available Triplestore implementations that have problems with nested graph clauses in SPARQL queries.
     *
     * @return a list with the names of all available Triplestore implementations that have problems with nested graph clauses in SPARQL queries
     */
    public static List<String> implementationsBadNestedGraphClauses() {
        return LOADER.getServices().stream().filter(ts -> !ts.isWorkingWithNestedGraphClauses())
                .map(TripleStoreFactoryService::getImplementationName).collect(Collectors.toList());
    }

    /**
     * Get a list containing only the name of the default Triplestore implementation.
     *
     * @return a list with the name of the default Triplestore implementation
     */
    public static List<String> onlyDefaultImplementation() {
        return Collections.singletonList(DEFAULT_IMPLEMENTATION);
    }

    /**
     * Get the name of the default Triplestore implementation.
     *
     * @return the name of the default Triplestore implementation
     */
    public static String defaultImplementation() {
        return DEFAULT_IMPLEMENTATION;
    }

    private static final String DEFAULT_IMPLEMENTATION = "rdf4j";
}
