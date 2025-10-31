/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.dot;

import com.google.re2j.Pattern;
import com.powsybl.commons.util.Colors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class IidmDOTUtils {
    private IidmDOTUtils() {
        // Utility class
    }

    /** Line separator. */
    public static final String LINE_SEPARATOR = "\\l";
    /** Attributes */
    public static final String FILL_COLOR = "fillcolor";
    public static final String FONT_SIZE = "fontsize";
    public static final String LABEL = "label";
    public static final String SHAPE = "shape";
    public static final String STYLE = "style";
    public static final String TOOL_TIP = "tooltip";
    /** Keyword for representing strict graphs. */
    static final String DONT_ALLOW_MULTIPLE_EDGES_KEYWORD = "strict";
    /** Keyword for directed graphs. */
    static final String DIRECTED_GRAPH_KEYWORD = "digraph";
    /** Keyword for undirected graphs. */
    static final String UNDIRECTED_GRAPH_KEYWORD = "graph";
    /** Edge operation for directed graphs. */
    static final String DIRECTED_GRAPH_EDGEOP = "->";
    /** Edge operation for undirected graphs. */
    static final String UNDIRECTED_GRAPH_EDGEOP = "--";
    /** Indentation. */
    static final String INDENT = "  ";
    static final String DOUBLE_INDENT = INDENT + INDENT;

    // patterns for IDs
    private static final Pattern ALPHA_DIG = Pattern.compile("[a-zA-Z_][\\w]*");
    private static final Pattern DOUBLE_QUOTE = Pattern.compile("\".*\"");
    private static final Pattern DOT_NUMBER = Pattern.compile("[-]?([.][0-9]+|[0-9]+([.][0-9]*)?)");
    private static final Pattern HTML = Pattern.compile("<.*>");

    public static Map<String, String> createBusColorScale(Random random, List<String> busIds) {
        Map<String, String> busColor = new HashMap<>();
        String[] colors = Colors.generateColorScale(busIds.size(), random);
        for (int i = 0; i < busIds.size(); i++) {
            busColor.put(busIds.get(i), colors[i]);
        }
        return busColor;
    }

    /**
     * Test if the ID candidate is a valid ID.
     *
     * @param idCandidate the ID candidate.
     *
     * @return <code>true</code> if it is valid; <code>false</code> otherwise.
     */
    static boolean isNotValidID(String idCandidate) {
        return !ALPHA_DIG.matcher(idCandidate).matches()
            && !DOUBLE_QUOTE.matcher(idCandidate).matches()
            && !DOT_NUMBER.matcher(idCandidate).matches() && !HTML.matcher(idCandidate).matches();
    }
}
