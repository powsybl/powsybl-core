/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.util.ServiceLoaderCache;

import java.util.List;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public class ExportersServiceLoader implements ExportersLoader {

    private static final ServiceLoaderCache<Exporter> EXPORTER_LOADER = new ServiceLoaderCache<>(Exporter.class);

    @Override
    public List<Exporter> loadExporters() {
        return EXPORTER_LOADER.getServices();
    }
}
