/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.model;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.triplestore.api.PrefixNamespace;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import com.powsybl.triplestore.api.TripleStore;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public final class EmptyTripleStore implements TripleStore {
    @Override
    public void read(InputStream is, String base, String contextName) {
        // do nothing
    }

    @Override
    public void write(DataSource ds) {
        // do nothing
    }

    @Override
    public void print(PrintStream out) {
        // do nothing
    }

    @Override
    public void print(Consumer<String> liner) {
        // do nothing
    }

    @Override
    public void clear(String contextName) {
        // do nothing
    }

    @Override
    public void defineQueryPrefix(String prefix, String namespace) {
        // do nothing
    }

    @Override
    public PropertyBags query(String query) {
        return new PropertyBags();
    }

    @Override
    public void add(String contextName, String namespace, String type, PropertyBags objects) {
        // do nothing
    }

    @Override
    public String add(String contextName, String namespace, String type, PropertyBag properties) {
        return null;
    }

    @Override
    public void add(TripleStore source) {
        // do nothing
    }

    @Override
    public Set<String> contextNames() {
        return Collections.emptySet();
    }

    @Override
    public void update(String queryText) {
        // do nothing
    }

    @Override
    public void addNamespace(String prefix, String namespace) {
        // do nothing
    }

    @Override
    public List<PrefixNamespace> getNamespaces() {
        return Collections.emptyList();
    }

    @Override
    public String getImplementationName() {
        return null;
    }
}
