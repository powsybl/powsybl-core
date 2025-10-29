package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.gl.CgmesGLExporter;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.network.Network;

public final class GeographicalLocationExport {

    public static void write(Network network, CgmesExportContext context, DataSource dataSource) {
        CgmesGLExporter exporter = new CgmesGLExporter(network, context.getCim());
        exporter.exportData(dataSource);
    }

    private GeographicalLocationExport() {
    }
}
