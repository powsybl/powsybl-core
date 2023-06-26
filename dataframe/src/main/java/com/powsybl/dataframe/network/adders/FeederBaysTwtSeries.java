package com.powsybl.dataframe.network.adders;

import com.powsybl.dataframe.SeriesMetadata;
import com.powsybl.dataframe.update.UpdatingDataframe;

import java.util.List;

public class FeederBaysTwtSeries extends AbstractFeederBaysSeries {
    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("id"),
        SeriesMetadata.strings("bus_or_busbar_section_id_1"),
        SeriesMetadata.strings("bus_or_busbar_section_id_2"),
        SeriesMetadata.ints("position_order_1"),
        SeriesMetadata.ints("position_order_2"),
        SeriesMetadata.strings("direction_1"),
        SeriesMetadata.strings("direction_2"),
        SeriesMetadata.strings("voltage_level1_id"),
        SeriesMetadata.strings("voltage_level2_id"),
        SeriesMetadata.doubles("r"),
        SeriesMetadata.doubles("x"),
        SeriesMetadata.doubles("g"),
        SeriesMetadata.doubles("b"),
        SeriesMetadata.doubles("rated_u1"),
        SeriesMetadata.doubles("rated_u2"),
        SeriesMetadata.doubles("rated_s")
    );

    public FeederBaysTwtSeries() {
    }

    public static List<SeriesMetadata> getSeriesMetadata() {
        return METADATA;
    }

    @Override
    AbstractBranchSeries createTypedSeries(UpdatingDataframe dataframe) {
        return new TwoWindingsTransformerSeries(dataframe);
    }
}
