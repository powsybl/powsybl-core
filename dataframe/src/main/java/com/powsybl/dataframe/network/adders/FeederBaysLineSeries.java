package com.powsybl.dataframe.network.adders;

import com.powsybl.dataframe.SeriesMetadata;
import com.powsybl.dataframe.update.UpdatingDataframe;

import java.util.List;

public class FeederBaysLineSeries extends AbstractFeederBaysSeries {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("id"),
        SeriesMetadata.doubles("b1"),
        SeriesMetadata.doubles("b2"),
        SeriesMetadata.doubles("g1"),
        SeriesMetadata.doubles("g2"),
        SeriesMetadata.doubles("r"),
        SeriesMetadata.doubles("x"),
        SeriesMetadata.strings("bus_or_busbar_section_id_1"),
        SeriesMetadata.strings("bus_or_busbar_section_id_2"),
        SeriesMetadata.ints("position_order_1"),
        SeriesMetadata.ints("position_order_2"),
        SeriesMetadata.strings("direction_1"),
        SeriesMetadata.strings("direction_2")
    );

    public FeederBaysLineSeries() {
    }

    public static List<SeriesMetadata> getSeriesMetadata() {
        return METADATA;
    }

    @Override
    AbstractBranchSeries createTypedSeries(UpdatingDataframe dataframe) {
        return new LineSeries(dataframe);
    }
}
