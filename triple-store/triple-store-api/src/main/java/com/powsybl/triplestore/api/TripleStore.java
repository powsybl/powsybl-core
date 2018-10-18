/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.triplestore.api;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Set;
import java.util.function.Consumer;

import com.powsybl.commons.datasource.DataSource;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public interface TripleStore {

    void read(String base, String contextName, InputStream is);

    void write(DataSource ds);

    void print(PrintStream out);

    void print(Consumer<String> liner);

    void clear(String contextName);

    void defineQueryPrefix(String prefix, String namespace);

    PropertyBags query(String query);

    void add(String contextName, String type, PropertyBags objects);

    Set<String> contextNames();
}
