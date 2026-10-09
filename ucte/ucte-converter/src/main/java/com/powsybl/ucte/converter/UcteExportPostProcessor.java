/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * Copyright (c) 2026, TenneT (https://www.tennet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter;

import com.powsybl.iidm.network.Network;
import com.powsybl.ucte.network.UcteNetwork;
import com.powsybl.ucte.network.UcteNode;

import java.util.Comparator;
import java.util.Optional;

/**
 * Plugin called by {@link UcteExporter} after the conversion of the IIDM network and before the writing of the UCTE
 * file. Implementations are discovered with {@link java.util.ServiceLoader} (e.g. using
 * {@link com.google.auto.service.AutoService}) and activated by name with the
 * {@value UcteExporter#POST_PROCESSORS} parameter, in the given order.
 * <p>
 * A new instance is requested for each export, so an implementation may keep state between
 * {@link #process(Network, UcteNetwork, UcteExporterContext)} and {@link #getNodeComparator()}.
 *
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public interface UcteExportPostProcessor {

    /**
     * Get the post-processor name, which has to be unique among all UCTE export post-processors.
     */
    String getName();

    /**
     * Modify the UCTE network before it is written, e.g. add comments to the first comment block with
     * {@link UcteNetwork#getComments()}, or in front of a given block with {@link UcteNetwork#getComments(com.powsybl.ucte.network.UcteBlock)}.
     *
     * @param network the exported IIDM network
     * @param ucteNetwork the UCTE network resulting from the conversion
     * @param context the export context, whose naming strategy maps IIDM elements to UCTE codes, and whose report node
     *                is dedicated to this post-processor
     */
    void process(Network network, UcteNetwork ucteNetwork, UcteExporterContext context);

    /**
     * Get the order of the nodes inside each ##Z block, called after {@link #process(Network, UcteNetwork, UcteExporterContext)}.
     * Comparators of the active post-processors are chained in activation order: a comparator only breaks ties of
     * the previous ones, so a comparator defining a total order makes the next ones useless.
     */
    default Optional<Comparator<UcteNode>> getNodeComparator() {
        return Optional.empty();
    }
}
