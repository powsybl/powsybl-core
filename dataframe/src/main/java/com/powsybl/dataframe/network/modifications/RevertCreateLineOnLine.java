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
import com.powsybl.iidm.modification.topology.RevertCreateLineOnLineBuilder;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Optional;

import static com.powsybl.dataframe.network.adders.SeriesUtils.applyIfPresent;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class RevertCreateLineOnLine implements DataframeNetworkModification {

    public static final String LINE_ID = "merged_line_id";
    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("line_to_be_merged1_id"),
        SeriesMetadata.strings("line_to_be_merged2_id"),
        SeriesMetadata.strings("line_to_be_deleted"),
        SeriesMetadata.strings(LINE_ID),
        SeriesMetadata.strings("merged_line_name")
    );

    @Override
    public List<SeriesMetadata> getMetadata() {
        return METADATA;
    }

    private RevertCreateLineOnLineBuilder createBuilder(UpdatingDataframe dataframe, int row) {
        RevertCreateLineOnLineBuilder builder = new RevertCreateLineOnLineBuilder();
        applyIfPresent(dataframe.getStrings("line_to_be_merged1_id"), row, builder::withLineToBeMerged1Id);
        applyIfPresent(dataframe.getStrings("line_to_be_merged2_id"), row, builder::withLineToBeMerged2Id);
        applyIfPresent(dataframe.getStrings("line_to_be_deleted"), row, builder::withLineToBeDeletedId);
        applyIfPresent(dataframe.getStrings(LINE_ID), row, builder::withMergedLineId);
        Optional<String> mergedLineName = dataframe.getStringValue("merged_line_name", row);
        if (mergedLineName.isEmpty()) {
            applyIfPresent(dataframe.getStrings(LINE_ID), row, builder::withMergedLineName);
        } else {
            builder.withMergedLineName(mergedLineName.get());
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
            RevertCreateLineOnLineBuilder builder = createBuilder(dataframes.get(0), row);
            builder.build().apply(network, throwException, reporter == null ? Reporter.NO_OP : reporter);
        }
    }
}
