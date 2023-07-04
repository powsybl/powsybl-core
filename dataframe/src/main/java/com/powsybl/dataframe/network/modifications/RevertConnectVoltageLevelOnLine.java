/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dataframe.network.modifications;

import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.reporter.ReporterModel;
import com.powsybl.dataframe.SeriesMetadata;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.modification.topology.RevertConnectVoltageLevelOnLineBuilder;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Optional;

import static com.powsybl.dataframe.network.adders.SeriesUtils.applyIfPresent;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class RevertConnectVoltageLevelOnLine implements DataframeNetworkModification {

    public static final String LINE_ID = "line_id";
    public static final String LINE_NAME = "line_name";
    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("line1_id"),
        SeriesMetadata.strings("line2_id"),
        SeriesMetadata.strings(LINE_ID),
        SeriesMetadata.strings(LINE_NAME)
    );

    @Override
    public List<SeriesMetadata> getMetadata() {
        return METADATA;
    }

    private RevertConnectVoltageLevelOnLineBuilder createBuilder(UpdatingDataframe dataframe, int row) {
        RevertConnectVoltageLevelOnLineBuilder builder = new RevertConnectVoltageLevelOnLineBuilder();
        applyIfPresent(dataframe.getStrings("line1_id"), row, builder::withLine1Id);
        applyIfPresent(dataframe.getStrings("line2_id"), row, builder::withLine2Id);
        applyIfPresent(dataframe.getStrings(LINE_NAME), row, builder::withLineName);
        applyIfPresent(dataframe.getStrings(LINE_ID), row, builder::withLineId);
        Optional<String> lineName = dataframe.getStringValue(LINE_NAME, row);
        if (lineName.isEmpty()) {
            applyIfPresent(dataframe.getStrings(LINE_ID), row, builder::withLineName);
        }
        return builder;
    }

    @Override
    public void applyModification(Network network, List<UpdatingDataframe> dataframes, boolean throwException,
                                  ReporterModel reporter) {
        if (dataframes.size() != 1) {
            throw new IllegalArgumentException("Expected only one input dataframe");
        }
        for (int row = 0; row < dataframes.get(0).getRowCount(); row++) {
            RevertConnectVoltageLevelOnLineBuilder builder = createBuilder(dataframes.get(0), row);
            builder.build().apply(network, throwException, reporter == null ? Reporter.NO_OP : reporter);
        }
    }
}
