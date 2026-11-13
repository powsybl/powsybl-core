/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import com.powsybl.ucte.network.UcteNetwork;

/**
 * @author Antoine Limouzin {@literal <antoine.limouzin at rte.com>}
 */
public interface UcteImportPostProcessor {

    /**
     * Get the post-processor name, which has to be unique among all UCTE import post-processors.
     */
    String getName();

    /**
     * Processes the Network to be imported, with additional information from the UCTE file
     * @param network the imported network, as a PowSyBl network
     * @param ucteNetwork a representation of the UCTE file's network
     * @param reportNode the report node dedicated to this post-processor
     */
    default void process(Network network, UcteNetwork ucteNetwork, ReportNode reportNode) {
    }
}
