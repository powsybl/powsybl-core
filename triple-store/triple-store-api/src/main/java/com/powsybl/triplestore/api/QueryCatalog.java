/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.triplestore.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public class QueryCatalog extends HashMap<String, String> {

    private InputStream resourceStream(String resource) {
        return getClass().getClassLoader().getResourceAsStream(resource);
    }

    public QueryCatalog(String resource) {
        this.resource = resource;
        load(resourceStream(resource), new ParsingContext());
    }

    public String resource() {
        return resource;
    }

    private void load(InputStream ir, ParsingContext context) {
        try (Stream<String> stream = new BufferedReader(
                new InputStreamReader(ir)).lines()) {
            stream.forEach(line -> parse(line, context));
        }
        context.leaveQuery();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), resource);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof QueryCatalog q)) {
            return false;
        }
        return resource.equals(q.resource);
    }

    private void parse(String line, ParsingContext context) {
        if (queryDefinition(line)) {
            context.enterQuery(line);
        } else if (include(line)) {
            context.leaveQuery();
            load(resourceStream(includedResource(line)), context);
        } else if (comment(line)) {
            // Ignore the line
        } else {
            context.appendText(line);
        }
    }

    private static boolean queryDefinition(String line) {
        // A line with a query definition is a comment line
        // that contains the keyword for query definition
        if (!comment(line)) {
            return false;
        }
        return line.contains(QUERY_DEFINITION);
    }

    private static boolean comment(String line) {
        return line.trim().startsWith("#");
    }

    private static boolean include(String line) {
        return line.contains(INCLUDE);
    }

    private static String includedResource(String line) {
        int p = line.indexOf(INCLUDE);
        if (p <= 0) {
            throw new IllegalStateException("p should be > 0");
        }
        return line.substring(p + INCLUDE.length()).trim();
    }

    class ParsingContext {
        ParsingContext() {
            queryText = new StringBuilder(2048);
            queryName = null;
        }

        void enterQuery(String line) {
            leaveQuery();
            int p = line.indexOf(QUERY_DEFINITION);
            if (p <= 0) {
                throw new IllegalStateException("p should be > 0");
            }
            queryName = line.substring(p + QUERY_DEFINITION.length()).trim();
        }

        void leaveQuery() {
            if (queryName != null) {
                LOG.debug("loaded query [{}]", queryName);
                put(queryName, queryText.toString());
                queryName = null;
                queryText.setLength(0);
            }
        }

        void appendText(String line) {
            queryText.append(line);
            queryText.append(LINE_SEPARATOR);
        }

        private final StringBuilder queryText;
        private String queryName;
    }

    private final String resource;

    private static final String QUERY_DEFINITION = "query:";
    private static final String INCLUDE = "include:";
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final Logger LOG = LoggerFactory.getLogger(QueryCatalog.class);
}
