/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.triplestore.api;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class QueryCatalog extends HashMap<String, String> {

    private InputStream resourceStream(String resource) {
        return getClass().getClassLoader().getResourceAsStream(resource);
    }

    public QueryCatalog(String resource) {
        this.resource = resource;
        load(resourceStream(resource));
    }

    public String resource() {
        return resource;
    }

    private void load(InputStream ir) {
        ParsingContext context = new ParsingContext();
        try (Stream<String> stream = new BufferedReader(
                new InputStreamReader(ir)).lines()) {
            stream.forEach(line -> parse(line, context));
        }
        context.close();
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
        if (!(obj instanceof QueryCatalog)) {
            return false;
        }
        QueryCatalog q = (QueryCatalog) obj;
        return resource.equals(q.resource);
    }

    private void parse(String line, ParsingContext context) {
        if (queryDefinition(line)) {
            context.enterQuery(line);
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

    class ParsingContext {
        ParsingContext() {
            queryText = new StringBuilder(2048);
            queryName = null;
        }

        void enterQuery(String line) {
            if (queryName != null) {
                leaveQuery();
            }
            int p = line.indexOf(QUERY_DEFINITION);
            assert p > 0;
            queryName = line.substring(p + QUERY_DEFINITION.length()).trim();
        }

        void leaveQuery() {
            LOG.debug("loaded query [{}]", queryName);
            put(queryName, queryText.toString());
            queryName = null;
            queryText.setLength(0);
        }

        void appendText(String line) {
            queryText.append(line);
            queryText.append(LINE_SEPARATOR);
        }

        void close() {
            if (queryName != null) {
                leaveQuery();
            }
        }

        private final StringBuilder queryText;
        private String queryName;
    }

    private final String resource;

    private static final String QUERY_DEFINITION = "query:";
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final Logger LOG = LoggerFactory.getLogger(QueryCatalog.class);
}
