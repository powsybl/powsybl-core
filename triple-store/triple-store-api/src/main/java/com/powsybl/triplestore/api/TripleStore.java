/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.triplestore.api;

import com.powsybl.commons.datasource.DataSource;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A Triplestore database.
 * A Triplestore database is a database for the storage and retrieval of triples.
 * A triple is a data entity composed of subject-predicate-object.
 * Adding a name to a triple allows to separate them in contexts or named graphs.
 *
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public interface TripleStore {

    /**
     * Obtain the options that have been used to configure this Triplestore
     * @return options Triplestore configuration options
     */
    default TripleStoreOptions getOptions() {
        return null;
    }

    /**
     * Read statements from an input stream and store them in the Triplestore under the given context name.
     *
     * @param is input stream containing statements that will be added to the Triplestore
     * @param base the base URI used to convert relative URI's to absolute URI's
     * @param contextName name of the context where statements will be added
     */
    void read(InputStream is, String base, String contextName);

    /**
     * Write the contents of the Triplestore in the given data source.
     * Statements in each context will be written to separate fileNames in the output data source
     * @param ds the output data source
     */
    void write(DataSource ds);

    /**
     * Write the contents of a context in the given data source.
     * @param ds the output data source
     * @param contextName the context to write
     */
    default void write(DataSource ds, String contextName) {
        throw new UnsupportedOperationException();
    }

    /**
     * Print a summary of the contents of the Triplestore.
     * Typically a list of contexts and its size (number of statements).
     *
     * @param out output stream where the summary will be written
     */
    void print(PrintStream out);

    /**
     * Print a summary of the contents of the Triplestore.
     * Typically a list of contexts and its size (number of statements).
     *
     * @param liner a function that accepts strings (the lines of the summary report)
     */
    void print(Consumer<String> liner);

    /**
     * Delete all the statements in a given context.
     *
     * @param contextName the context to be cleared
     */
    void clear(String contextName);

    /**
     * Define namespace prefix bindings that will be used in the text of queries.
     * Example:
     * <code>
     * tripleStore.defineQueryPrefix("foaf", "http://xmlns.com/foaf/0.1/");
     * </code>
     *
     * @param prefix the prefix to be used in the text of queries as a replacement for the namespace
     * @param namespace the URL of the namespace
     */
    void defineQueryPrefix(String prefix, String namespace);

    /**
     * Perform a SPARQL query on the Triplestore.
     *
     * @param query the text of the query, written in SPARQL query language
     * @return the solution sequence of the query (the ways in which the query matches the data)
     */
    PropertyBags query(String query);

    /**
     * Add to the triple store statements for creating new resources, instances of a specific class
     *
     * @param contextName context where the statements are added
     * @param namespace the namespace of the class of the new resources
     * @param type the class of the new resources
     * @param objects properties of the resources
     */
    void add(String contextName, String namespace, String type, PropertyBags objects);

    /**
     * Add to the triple store statements for creating a new resource, instance of a specific class
     *
     * @param contextName the context where the statements are added
     * @param namespace the namespace of the class of the new resource
     * @param type the class of the new resource
     * @param properties properties of the resource
     * @return the id of the new resource
     */
    String add(String contextName, String namespace, String type, PropertyBag properties);

    /**
     * Add all statements of the source Triplestore to this Triplestore.
     *
     * @param source the Triplestore containing statements to be added to this Triplestore
     */
    void add(TripleStore source);

    /**
     * Get all the context names currently defined in the Triplestore.
     *
     * @return a set of all context names
     */
    Set<String> contextNames();

      /**
     * Perform a SPARQL update on the Triplestore.
     *
     * @param queryText the text of the query, written in SPARQL Update language
     */
    void update(String queryText);

    /**
     * Add a namespace to the triple store
     *
     * @param prefix the prefix of the namespace
     * @param namespace the namespace
     */
    void addNamespace(String prefix, String namespace);

    /**
     * Return the namespaces defined in the triple store
     *
     * @return the list of namespaces defined in the triple store
     */
    List<PrefixNamespace> getNamespaces();

    /**
     * Return the implementation name defined in the triple store
     *
     * @return the string implementation name of the triplestore object
     */
    String getImplementationName();

}
