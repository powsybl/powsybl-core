/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.converter;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import com.powsybl.nc.model.NcModel;

import java.util.Objects;

/**
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
abstract class AbstractNcConverter {
    protected final NcModel model;
    protected final Network network;

    /**
     * Collects the source objects this converter rejects. Diagnostics are reported here rather than logged,
     * so that callers can inspect them together with the rest of the conversion.
     */
    protected final ReportNode reportNode;

    AbstractNcConverter(NcModel model, Network network, ReportNode reportNode) {
        this.model = Objects.requireNonNull(model);
        this.network = Objects.requireNonNull(network);
        this.reportNode = Objects.requireNonNull(reportNode);
    }
}
