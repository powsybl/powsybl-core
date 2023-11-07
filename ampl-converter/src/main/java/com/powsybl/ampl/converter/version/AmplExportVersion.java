/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.converter.version;

import com.powsybl.ampl.converter.AmplExportConfig;
import com.powsybl.ampl.converter.AmplSubset;
import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.iidm.network.Network;

/**
 * @author Nicolas Pierre {@literal <nicolas.pierre at artelys.com>}
 */
public enum AmplExportVersion {
    /**
     * Legacy export
     */
    V1_LEGACY(BasicAmplExporter.getFactory());

    public interface Factory {
        AmplColumnsExporter create(AmplExportConfig config, Network network, StringToIntMapper<AmplSubset> mapper,
                                   int variantIndex, int faultNum, int actionNum);
    }

    private final Factory factory;

    AmplExportVersion(Factory factory) {
        this.factory = factory;
    }

    public Factory getColumnsExporter() {
        return factory;
    }
}
