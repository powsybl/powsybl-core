/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.triplestore.api;

/**
* A factory service that allows creation of Triplestore databases.
*
* @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
*/
public interface TripleStoreFactoryService {

    /**
     * Create an instance of a Triplestore.
     *
     * @return Triplestore instance
     */
    TripleStore create();

    /**
     * Create an instance of a Triplestore with given options.
     *
     * @param options that configure the Triplestore behaviour
     * @return Triplestore instance
     */
    default TripleStore create(TripleStoreOptions options) {
        return create();
    }

    /**
     * Create an instance of a Triplestore that is a copy of a given Triplestore.
     *
     * @param source the source Triplestore
     * @return a copy of the source Triplestore
     */
    TripleStore copy(TripleStore source);

    /**
     * Get name of the Triplestore factory.
     *
     * @return name of the Triplestore factory
     */
    String getImplementationName();

    /**
     * Check if Triplestores from this factory support SPARQL queries with nested graph clauses.
     *
     * @return true if the Triplestores created by this factory support SPARQL queries with nested graph clauses
     */
    boolean isWorkingWithNestedGraphClauses();

}
