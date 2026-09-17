/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.gl.CgmesGLExporter;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.network.Network;

/**
 *
 * @author Ferrari Giovanni {@literal <giovanni.ferrari@soft.it>}
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 */
public final class GeographicalLocationExport {

    public static void write(Network network, CgmesExportContext context, DataSource dataSource, String baseName) {
        CgmesGLExporter exporter = new CgmesGLExporter(network, context.getCim());
        exporter.exportData(dataSource, baseName);
    }

    private GeographicalLocationExport() {
    }
}
