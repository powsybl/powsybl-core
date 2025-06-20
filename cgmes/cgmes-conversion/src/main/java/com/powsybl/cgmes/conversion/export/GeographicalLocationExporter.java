package com.powsybl.cgmes.conversion.export;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.network.Network;

public final class GeographicalLocationExporter {

    public static void write(Network network, DataSource dataSource) {
        CgmesGLExporter exporter = new CgmesGLExporter(network);
        exporter.exportData(dataSource);
    }

    private GeographicalLocationExporter() {
    }
}
