package com.powsybl.cgmes.conversion;

/*
 * #%L
 * CGMES conversion
 * %%
 * Copyright (C) 2017 - 2018 RTE (http://rte-france.com)
 * %%
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * #L%
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TempDiagnosticRow {

    public TempDiagnosticRow(String prefix) {
        s = new StringBuilder();
        s.append(prefix);
        s.append("\t");
    }

    public void col(Object o) {
        s.append(o).append("\t");
    }

    public void end() {
        if (LOG.isInfoEnabled()) {
            LOG.info(s.toString());
        }
    }

    private final StringBuilder s;
    private static final Logger LOG = LoggerFactory.getLogger(TempDiagnosticRow.class);
}
