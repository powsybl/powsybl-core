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
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.iidm.network.Network;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Nicolas Pierre {@literal <nicolas.pierre at artelys.com>}
 */
public enum AmplExportVersion {

    V1_0("1.0", BasicAmplExporter.getFactory()),
    V2_0("2.0", AmplExporterV2.getFactory());

    public interface Factory {
        AmplColumnsExporter create(AmplExportConfig config, Network network, StringToIntMapper<AmplSubset> mapper,
                                   int variantIndex, int faultNum, int actionNum);
    }

    private static final Map<String, AmplExportVersion> VERSION_BY_EXPORTER_ID = Arrays.stream(values())
            .collect(Collectors.toMap(AmplExportVersion::getExporterId, Function.identity()));

    private final String exporterId;
    private final Factory factory;

    AmplExportVersion(String exporterId, Factory factory) {
        this.exporterId = exporterId;
        this.factory = factory;
    }

    public String getExporterId() {
        return this.exporterId;
    }

    public Factory getColumnsExporter() {
        return this.factory;
    }

    public static Set<String> exporterIdValues() {
        return VERSION_BY_EXPORTER_ID.keySet();
    }

    public static AmplExportVersion fromExporterId(String exporterId) {
        AmplExportVersion version = VERSION_BY_EXPORTER_ID.get(exporterId);
        if (version == null) {
            throw new PowsyblException("exporterId " + exporterId + " is not in the exporterId possible values: " + exporterIdValues());
        }
        return version;
    }

    public static AmplExportVersion defaultVersion() {
        return V1_0;
    }
}
