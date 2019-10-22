/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.triplestore.api;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.powsybl.commons.datasource.DataSource;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public interface TripleStore {

    void read(InputStream is, String base, String contextName);

    void write(DataSource ds);

    void print(PrintStream out);

    void print(Consumer<String> liner);

    void clear(String contextName);

    void defineQueryPrefix(String prefix, String namespace);

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

    Set<String> contextNames();

    void update(String queryText);

    void duplicate(TripleStore origin, String baseName);

    String getImplementationName();

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

}
