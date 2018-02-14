package com.powsybl.ampl.converter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;

public class FooExtensionExporter implements AmplExtensionExporter {

    private DataSource dataSource;
    private Network network;

    public FooExtensionExporter(Network network, DataSource dataSource, int actionNum, int faultNum,
            boolean append, StringToIntMapper<AmplSubset> mapper, AmplExportConfig config) {
        this.dataSource = dataSource;
        this.network = network;
    }

    @Override
    public void write() throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("foo-extension", "txt", false), StandardCharsets.UTF_8)) {
            HvdcLine l = network.getHvdcLine("L");
            if (l != null && l.getExtension(FooExtension.class) != null) {
                writer.write(l.getExtension(FooExtension.class).getName() + "\n");
            }

        }
    }

}
