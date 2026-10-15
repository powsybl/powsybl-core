/**
 * Copyright (c) 2026, TenneT (https://www.tennet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter;

import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.commons.datasource.ReadOnlyMemDataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;

/**
 * Helpers shared by the UCTE exporter tests.
 *
 * @author Arthur Michaut {@literal <arthur.michaut at artelys.com>}
 */
final class UcteExporterTestUtils {

    private UcteExporterTestUtils() {
    }

    /**
     * Re-imports a network exported to a {@link MemDataSource}. A plain {@link MemDataSource} always stores its
     * content under an empty base name, which {@link UcteImporter} would otherwise turn into an invalid empty
     * network id, so the exported bytes are copied into a named {@link ReadOnlyMemDataSource} first.
     */
    static Network reimport(MemDataSource dataSource) {
        byte[] exported = dataSource.getData(null, "uct");
        ReadOnlyMemDataSource namedDataSource = new ReadOnlyMemDataSource("network");
        namedDataSource.putData("network.uct", exported);
        return new UcteImporter().importData(namedDataSource, NetworkFactory.findDefault(), null);
    }
}
