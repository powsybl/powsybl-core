package com.powsybl.ampl.converter.version;

import com.powsybl.ampl.converter.AmplExportConfig;
import com.powsybl.ampl.converter.AmplSubset;
import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.iidm.network.Network;

public interface AmplExportVersion {
    interface Factory {
        AmplColumnsExporter create(AmplExportConfig config, Network network, StringToIntMapper<AmplSubset> mapper,
                                   int variantIndex, int faultNum, int actionNum);
    }

    Factory getColumnsExporter();
}
