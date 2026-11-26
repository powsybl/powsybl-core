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
 * Versions of the AMPL export format.
 * <p>
 * Each version is identified by an exporter id (e.g. {@code "1.0"}) and is associated with a
 * {@link Factory} creating the {@link AmplColumnsExporter} that defines the columns written
 * in the exported AMPL files:
 * <ul>
 *     <li>{@link #V1_0}: basic export, see {@link BasicAmplExporter};</li>
 *     <li>{@link #V1_1}: extended export, see {@link ExtendedAmplExporter};</li>
 *     <li>{@link #V1_2}: see {@link ExtendedAmplExporterV2};</li>
 *     <li>{@link #V1_3}: see {@link ExtendedAmplExporterV3};</li>
 *     <li>{@link #V1_4}: see {@link ExtendedAmplExporterV4} (default version).</li>
 * </ul>
 *
 * @author Nicolas Pierre {@literal <nicolas.pierre at artelys.com>}
 */
public enum AmplExportVersion {

    V1_0("1.0", BasicAmplExporter::new),
    V1_1("1.1", ExtendedAmplExporter::new),
    V1_2("1.2", ExtendedAmplExporterV2::new),
    V1_3("1.3", ExtendedAmplExporterV3::new),
    V1_4("1.4", ExtendedAmplExporterV4::new);

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

    /**
     * Returns the AMPL export version used when none is specified.
     *
     * @return the default version, currently {@link #V1_4}
     */
    public static AmplExportVersion defaultVersion() {
        return V1_4;
    }
}
