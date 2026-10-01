/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.model.io;

import com.powsybl.nc.model.NcKeyword;
import com.powsybl.nc.model.NcModelExtension;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * Read-only access to selected RDF contexts for NC model extensions.
 *
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
public interface NcQueryContext {
    String CONTEXTS_PLACEHOLDER = "${contexts}";

    /**
     * Executes one query over all selected profiles with the requested keyword. The query template must use
     * {@value #CONTEXTS_PLACEHOLDER} inside a {@code VALUES ?context} clause and query the corresponding named
     * graphs through {@code GRAPH ?context}.
     */
    PropertyBags query(NcKeyword keyword, String contextsQueryTemplate);

    <E extends NcModelExtension> void addExtension(Class<E> type, E extension);
}
