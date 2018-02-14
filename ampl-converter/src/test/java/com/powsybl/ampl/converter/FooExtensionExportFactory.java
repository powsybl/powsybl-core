package com.powsybl.ampl.converter;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.iidm.network.Network;

public class FooExtensionExportFactory implements AmplExtensionExportFactory {

    @Override
    public AmplExtensionExporter create(Network network, DataSource dataSource, int faultNum, int actionNum,
            boolean append, StringToIntMapper<AmplSubset> mapper, AmplExportConfig config) {
        return new FooExtensionExporter(network, dataSource, actionNum, faultNum, append, mapper, config);
    }

}
