/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class ReportRow {

    public ReportRow(String prefix) {
        s = new StringBuilder();
        s.append(prefix);
        s.append("\t");
    }

    public void col(Object o) {
        s.append(o).append("\t");
    }

    public void end(Consumer<String> out) {
        if (out != null) {
            out.accept(s.toString());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("REPORT {}", s);
        }
    }

    private final StringBuilder s;

    private static final Logger LOG = LoggerFactory.getLogger(ReportRow.class);
}
