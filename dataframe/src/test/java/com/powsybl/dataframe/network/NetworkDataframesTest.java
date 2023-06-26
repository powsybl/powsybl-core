/**
 * Copyright (c) 2021-2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network;

import com.google.common.collect.ImmutableMap;
import com.powsybl.dataframe.DataframeElementType;
import com.powsybl.dataframe.DataframeFilter;
import com.powsybl.dataframe.DoubleIndexedSeries;
import com.powsybl.dataframe.impl.DefaultDataframeHandler;
import com.powsybl.dataframe.impl.Series;
import com.powsybl.dataframe.network.extensions.NetworkExtensions;
import com.powsybl.dataframe.update.DefaultUpdatingDataframe;
import com.powsybl.dataframe.update.TestDoubleSeries;
import com.powsybl.dataframe.update.TestStringSeries;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.HvdcAngleDroopActivePowerControlAdder;
import com.powsybl.iidm.network.extensions.HvdcOperatorActivePowerRangeAdder;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControl;
import com.powsybl.iidm.network.extensions.SecondaryVoltageControlAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.powsybl.dataframe.DataframeElementType.*;
import static com.powsybl.dataframe.DataframeFilter.AttributeFilterType.ALL_ATTRIBUTES;
import static com.powsybl.dataframe.Networks.createEurostagTutorialExample1WithApcExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
class NetworkDataframesTest {

    private static List<Series> createDataFrame(DataframeElementType type, Network network) {
        return createDataFrame(type, network, new DataframeFilter());
    }

    private static List<Series> createDataFrame(DataframeElementType type, Network network,
                                                DataframeFilter dataframeFilter) {
        List<Series> series = new ArrayList<>();
        NetworkDataframeMapper mapper = NetworkDataframes.getDataframeMapper(type);
        assertNotNull(mapper);
        mapper.createDataframe(network, new DefaultDataframeHandler(series::add), dataframeFilter);
        return series;
    }

    private static List<Series> createExtensionDataFrame(String name, Network network) {
        return createExtensionDataFrame(name, null, network);
    }

    private static List<Series> createExtensionDataFrame(String name, String tableName, Network network) {
        List<Series> series = new ArrayList<>();
        NetworkDataframeMapper mapper = NetworkDataframes.getExtensionDataframeMapper(name, tableName);
        assertNotNull(mapper);
        mapper.createDataframe(network, new DefaultDataframeHandler(series::add), new DataframeFilter());
        return series;
    }

    private static void updateExtension(String name, Network network, UpdatingDataframe updatingDataframe) {
        NetworkDataframeMapper mapper = NetworkDataframes.getExtensionDataframeMapper(name, null);
        assertNotNull(mapper);
        mapper.updateSeries(network, updatingDataframe);
    }

    private DoubleIndexedSeries createInput(List<String> names, double... values) {
        return new DoubleIndexedSeries() {
            @Override
            public int getSize() {
                return names.size();
            }

            @Override
            public String getId(int index) {
                return names.get(index);
            }

            @Override
            public double getValue(int index) {
                return values[index];
            }
        };
    }

    @Test
    void buses() {
        Network network = EurostagTutorialExample1Factory.create();
        List<Series> series = createDataFrame(BUS, network);
        assertThat(series)
            .extracting(Series::getName)
            .containsExactly("id", "name", "v_mag", "v_angle", "connected_component", "synchronous_component",
                "voltage_level_id");
        assertThat(series.get(2).getDoubles())
            .containsExactly(Double.NaN, Double.NaN, Double.NaN, Double.NaN);
        assertThat(series.get(4).getInts())
            .containsExactly(0, 0, 0, 0);
        assertThat(series.get(4).getInts())
            .containsExactly(0, 0, 0, 0);
        assertThat(series.get(6).getStrings())
            .containsExactly("VLGEN", "VLHV1", "VLHV2", "VLLOAD");
    }

    @Test
    void generators() {
        Network network = EurostagTutorialExample1Factory.create();
        List<Series> series = createDataFrame(GENERATOR, network);

        assertThat(series)
            .extracting(Series::getName)
            .containsExactly("id", "name", "energy_source", "target_p", "min_p", "max_p", "min_q", "max_q",
                "reactive_limits_kind",
                "target_v", "target_q", "voltage_regulator_on", "regulated_element_id", "p", "q", "i",
                "voltage_level_id",
                "bus_id", "connected");

        assertThat(series.get(3).getDoubles())
            .containsExactly(607);

        List<Series> allAttributeSeries = createDataFrame(GENERATOR, network,
            new DataframeFilter(ALL_ATTRIBUTES, Collections.emptyList()));
        assertThat(allAttributeSeries)
            .extracting(Series::getName)
            .containsExactly("id", "name", "energy_source", "target_p", "min_p", "max_p", "min_q", "max_q",
                "min_q_at_target_p", "max_q_at_target_p", "min_q_at_p", "max_q_at_p", "reactive_limits_kind",
                "target_v", "target_q", "voltage_regulator_on", "regulated_element_id", "p", "q", "i",
                "voltage_level_id",
                "bus_id", "bus_breaker_bus_id", "node", "connected", "fictitious");
    }

    @Test
    void generatorsDisconnected() {
        Network network = EurostagTutorialExample1Factory.create();
        Map<String, Series> attributes = createDataFrame(GENERATOR, network,
            new DataframeFilter(ALL_ATTRIBUTES, Collections.emptyList()))
            .stream().collect(ImmutableMap.toImmutableMap(Series::getName, Function.identity()));
        assertThat(attributes.get("bus_id").getStrings()).containsExactly("VLGEN_0");
        assertThat(attributes.get("bus_breaker_bus_id").getStrings()).containsExactly("NGEN");
        assertThat(attributes.get("connected").getBooleans()).containsExactly(true);

        network.getGenerator("GEN").getTerminal().disconnect();
        attributes = createDataFrame(GENERATOR, network, new DataframeFilter(ALL_ATTRIBUTES, Collections.emptyList()))
            .stream().collect(ImmutableMap.toImmutableMap(Series::getName, Function.identity()));
        assertThat(attributes.get("bus_id").getStrings()).containsExactly("");
        assertThat(attributes.get("bus_breaker_bus_id").getStrings()).containsExactly("NGEN");
        assertThat(attributes.get("connected").getBooleans()).containsExactly(false);
    }

    @Test
    void generatorsExtension() {
        Network network = createEurostagTutorialExample1WithApcExtension();
        List<Series> series = createExtensionDataFrame("activePowerControl", network);
        assertThat(series)
            .extracting(Series::getName)
            .containsExactly("id", "droop", "participate");
        assertThat(series.get(1).getDoubles())
            .containsExactly(1.1f);
        assertThat(series.get(2).getBooleans())
            .containsExactly(true);

        DefaultUpdatingDataframe dataframe = new DefaultUpdatingDataframe(1);
        dataframe.addSeries("id", true, new TestStringSeries("GEN"));
        dataframe.addSeries("droop", false, new TestDoubleSeries(1.2));
        updateExtension("activePowerControl", network, dataframe);
        series = createExtensionDataFrame("activePowerControl", network);
        assertThat(series)
            .extracting(Series::getName)
            .containsExactly("id", "droop", "participate");
        assertThat(series.get(1).getDoubles())
            .containsExactly(1.2f);
        assertThat(series.get(2).getBooleans())
            .containsExactly(true);

        NetworkExtensions.removeExtensions(network, "activePowerControl",
            network.getGeneratorStream().map(Generator::getNameOrId).collect(Collectors.toList()));
        series = createExtensionDataFrame("activePowerControl", network);
        assertEquals(0, series.get(0).getStrings().length);
    }

    @Test
    void secondaryVoltageControlExtension() {
        Network network = EurostagTutorialExample1Factory.createWithMoreGenerators();
        SecondaryVoltageControl control = network.newExtension(SecondaryVoltageControlAdder.class)
            .addControlZone(new SecondaryVoltageControl.ControlZone("z1",
                new SecondaryVoltageControl.PilotPoint(List.of("NLOAD"), 15d),
                List.of(new SecondaryVoltageControl.ControlUnit("GEN", false))))
            .add();

        List<Series> zoneSeries = createExtensionDataFrame("secondaryVoltageControl", "zones", network);
        assertThat(zoneSeries)
            .extracting(Series::getName)
            .containsExactly("name", "target_v", "bus_ids");
        assertThat(zoneSeries.get(1).getDoubles())
            .containsExactly(15d);
        assertThat(zoneSeries.get(2).getStrings())
            .containsExactly("NLOAD");

        List<Series> unitSeries = createExtensionDataFrame("secondaryVoltageControl", "units", network);
        assertThat(unitSeries)
            .extracting(Series::getName)
            .containsExactly("unit_id", "participate", "zone_name");
        assertThat(unitSeries.get(1).getBooleans())
            .containsExactly(false);
        assertThat(unitSeries.get(2).getStrings())
            .containsExactly("z1");
    }

    @Test
    void batteries() {
        Network network = EurostagTutorialExample1Factory.create();
        List<Series> series = createDataFrame(BATTERY, network);
        assertThat(series)
            .extracting(Series::getName)
            .containsExactly("id", "name", "max_p", "min_p", "min_q", "max_q", "reactive_limits_kind", "target_p",
                "target_q", "p", "q", "i", "voltage_level_id", "bus_id", "connected");
        List<Series> allAttributeSeries = createDataFrame(BATTERY, network,
            new DataframeFilter(ALL_ATTRIBUTES, Collections.emptyList()));
        assertThat(allAttributeSeries)
            .extracting(Series::getName)
            .containsExactly("id", "name", "max_p", "min_p", "min_q", "max_q", "reactive_limits_kind", "target_p",
                "target_q", "p", "q", "i", "voltage_level_id",
                "bus_id", "bus_breaker_bus_id", "node", "connected", "fictitious");
    }

    @Test
    void loads() {
        Network network = EurostagTutorialExample1Factory.create();
        List<Series> series = createDataFrame(LOAD, network);

        assertThat(series)
            .extracting(Series::getName)
            .containsExactly("id", "name", "type", "p0", "q0", "p", "q", "i", "voltage_level_id", "bus_id",
                "connected");
        List<Series> allAttributeSeries = createDataFrame(LOAD, network,
            new DataframeFilter(ALL_ATTRIBUTES, Collections.emptyList()));
        assertThat(allAttributeSeries)
            .extracting(Series::getName)
            .containsExactly("id", "name", "type", "p0", "q0", "p", "q", "i", "voltage_level_id",
                "bus_id", "bus_breaker_bus_id", "node", "connected", "fictitious");
    }

    @Test
    void danglingLines() {
        Network network = EurostagTutorialExample1Factory.create();
        List<Series> series = createDataFrame(DANGLING_LINE, network);

        assertThat(series)
            .extracting(Series::getName)
            .containsExactly("id", "name", "r", "x", "g", "b", "p0", "q0", "p", "q", "i", "voltage_level_id", "bus_id",
                "connected", "ucte-x-node-code", "tie_line_id");
        List<Series> allAttributeSeries = createDataFrame(DANGLING_LINE, network,
            new DataframeFilter(ALL_ATTRIBUTES, Collections.emptyList()));
        assertThat(allAttributeSeries)
            .extracting(Series::getName)
            .containsExactly("id", "name", "r", "x", "g", "b", "p0", "q0", "p", "q", "i",
                "voltage_level_id", "bus_id", "bus_breaker_bus_id", "node", "connected", "ucte-x-node-code",
                "fictitious", "tie_line_id");
    }

    @Test
    void tieLines() {
        Network network = EurostagTutorialExample1Factory.createWithTieLine();
        List<Series> series = createDataFrame(TIE_LINE, network);

        assertThat(series)
            .extracting(Series::getName)
            .containsExactly("id", "name", "dangling_line1_id", "dangling_line2_id", "ucte_xnode_code");
        List<Series> allAttributeSeries = createDataFrame(TIE_LINE, network,
            new DataframeFilter(ALL_ATTRIBUTES, Collections.emptyList()));
        assertThat(allAttributeSeries)
            .extracting(Series::getName)
            .containsExactly("id", "name", "dangling_line1_id", "dangling_line2_id", "ucte_xnode_code", "fictitious");
    }

    @Test
    void lines() {
        Network network = EurostagTutorialExample1Factory.create();
        List<Series> series = createDataFrame(LINE, network);

        assertThat(series)
            .extracting(Series::getName)
            .containsExactly("id", "name", "r", "x", "g1", "b1", "g2", "b2", "p1", "q1", "i1", "p2", "q2", "i2",
                "voltage_level1_id", "voltage_level2_id", "bus1_id", "bus2_id", "connected1", "connected2");
        List<Series> allAttributeSeries = createDataFrame(LINE, network,
            new DataframeFilter(ALL_ATTRIBUTES, Collections.emptyList()));
        assertThat(allAttributeSeries)
            .extracting(Series::getName)
            .containsExactly("id", "name", "r", "x", "g1", "b1", "g2", "b2", "p1", "q1", "i1", "p2", "q2", "i2",
                "voltage_level1_id", "voltage_level2_id", "bus1_id", "bus_breaker_bus1_id", "node1",
                "bus2_id", "bus_breaker_bus2_id", "node2", "connected1", "connected2", "fictitious");
    }

    @Test
    void shunts() {
        Network network = EurostagTutorialExample1Factory.create();
        List<Series> series = createDataFrame(SHUNT_COMPENSATOR, network);

        assertThat(series)
            .extracting(Series::getName)
            .containsExactly("id", "name", "g", "b", "model_type", "max_section_count", "section_count",
                "voltage_regulation_on",
                "target_v", "target_deadband", "regulating_bus_id", "p", "q", "i", "voltage_level_id", "bus_id",
                "connected");
        List<Series> allAttributeSeries = createDataFrame(SHUNT_COMPENSATOR, network,
            new DataframeFilter(ALL_ATTRIBUTES, Collections.emptyList()));
        assertThat(allAttributeSeries)
            .extracting(Series::getName)
            .containsExactly("id", "name", "g", "b", "model_type", "max_section_count", "section_count",
                "voltage_regulation_on",
                "target_v", "target_deadband", "regulating_bus_id", "p", "q", "i",
                "voltage_level_id", "bus_id", "bus_breaker_bus_id", "node", "connected", "fictitious");
    }

    @Test
    void lccs() {
        Network network = EurostagTutorialExample1Factory.create();
        List<Series> series = createDataFrame(LCC_CONVERTER_STATION, network);

        assertThat(series)
            .extracting(Series::getName)
            .containsExactly("id", "name", "power_factor", "loss_factor", "p", "q", "i", "voltage_level_id", "bus_id",
                "connected");
        List<Series> allAttributeSeries = createDataFrame(LCC_CONVERTER_STATION, network,
            new DataframeFilter(ALL_ATTRIBUTES, Collections.emptyList()));
        assertThat(allAttributeSeries)
            .extracting(Series::getName)
            .containsExactly("id", "name", "power_factor", "loss_factor", "p", "q", "i", "voltage_level_id", "bus_id",
                "bus_breaker_bus_id", "node", "connected", "fictitious");
    }

    @Test
    void vscs() {
        Network network = EurostagTutorialExample1Factory.create();
        List<Series> series = createDataFrame(VSC_CONVERTER_STATION, network);

        assertThat(series)
            .extracting(Series::getName)
            .containsExactly("id", "name", "loss_factor", "min_q", "max_q", "reactive_limits_kind", "target_v",
                "target_q", "voltage_regulator_on", "regulated_element_id",
                "p", "q", "i", "voltage_level_id", "bus_id", "connected");
        List<Series> allAttributeSeries = createDataFrame(VSC_CONVERTER_STATION, network,
            new DataframeFilter(ALL_ATTRIBUTES, Collections.emptyList()));
        assertThat(allAttributeSeries)
            .extracting(Series::getName)
            .containsExactly("id", "name", "loss_factor", "min_q", "max_q", "min_q_at_p", "max_q_at_p",
                "reactive_limits_kind", "target_v", "target_q", "voltage_regulator_on", "regulated_element_id",
                "p", "q", "i", "voltage_level_id", "bus_id", "bus_breaker_bus_id", "node", "connected", "fictitious");
    }

    @Test
    void twoWindingTransformers() {
        Network network = EurostagTutorialExample1Factory.create();
        List<Series> series = createDataFrame(TWO_WINDINGS_TRANSFORMER, network);

        assertThat(series)
            .extracting(Series::getName)
            .containsExactly("id", "name", "r", "x", "g", "b", "rated_u1", "rated_u2", "rated_s", "p1", "q1", "i1",
                "p2", "q2", "i2",
                "voltage_level1_id", "voltage_level2_id", "bus1_id", "bus2_id", "connected1", "connected2");
        List<Series> allAttributeSeries = createDataFrame(TWO_WINDINGS_TRANSFORMER, network,
            new DataframeFilter(ALL_ATTRIBUTES, Collections.emptyList()));
        assertThat(allAttributeSeries)
            .extracting(Series::getName)
            .containsExactly("id", "name", "r", "x", "g", "b", "rated_u1", "rated_u2", "rated_s", "p1", "q1", "i1",
                "p2", "q2", "i2",
                "voltage_level1_id", "voltage_level2_id", "bus1_id", "bus_breaker_bus1_id", "node1", "bus2_id",
                "bus_breaker_bus2_id", "node2",
                "connected1", "connected2", "fictitious");
    }

    @Test
    void threeWindingTransformers() {
        Network network = EurostagTutorialExample1Factory.create();
        List<Series> series = createDataFrame(THREE_WINDINGS_TRANSFORMER, network);

        assertThat(series)
            .extracting(Series::getName)
            .containsExactly("id", "name", "rated_u0",
                "r1", "x1", "g1", "b1", "rated_u1", "rated_s1", "ratio_tap_position1", "phase_tap_position1", "p1",
                "q1", "i1", "voltage_level1_id", "bus1_id", "connected1",
                "r2", "x2", "g2", "b2", "rated_u2", "rated_s2", "ratio_tap_position2", "phase_tap_position2", "p2",
                "q2", "i2", "voltage_level2_id", "bus2_id", "connected2",
                "r3", "x3", "g3", "b3", "rated_u3", "rated_s3", "ratio_tap_position3", "phase_tap_position3", "p3",
                "q3", "i3", "voltage_level3_id", "bus3_id", "connected3");
        List<Series> allAttributeSeries = createDataFrame(THREE_WINDINGS_TRANSFORMER, network,
            new DataframeFilter(ALL_ATTRIBUTES, Collections.emptyList()));
        assertThat(allAttributeSeries)
            .extracting(Series::getName)
            .containsExactly("id", "name", "rated_u0",
                "r1", "x1", "g1", "b1", "rated_u1", "rated_s1", "ratio_tap_position1", "phase_tap_position1", "p1",
                "q1", "i1", "voltage_level1_id", "bus1_id", "bus_breaker_bus1_id", "node1", "connected1",
                "r2", "x2", "g2", "b2", "rated_u2", "rated_s2", "ratio_tap_position2", "phase_tap_position2", "p2",
                "q2", "i2", "voltage_level2_id", "bus2_id", "bus_breaker_bus2_id", "node2", "connected2",
                "r3", "x3", "g3", "b3", "rated_u3", "rated_s3", "ratio_tap_position3", "phase_tap_position3", "p3",
                "q3", "i3", "voltage_level3_id", "bus3_id", "bus_breaker_bus3_id", "node3", "connected3",
                "fictitious");
    }

    @Test
    void hvdcs() {
        Network network = EurostagTutorialExample1Factory.create();
        List<Series> series = createDataFrame(HVDC_LINE, network);

        assertThat(series)
            .extracting(Series::getName)
            .containsExactly("id", "name", "converters_mode", "target_p", "max_p", "nominal_v", "r",
                "converter_station1_id", "converter_station2_id", "connected1", "connected2");
    }

    @Test
    void hvdcsExtensions() {
        Network network = HvdcTestNetwork.createLcc();

        HvdcLine hvdcLine = network.getHvdcLine("L");
        hvdcLine.newExtension(HvdcAngleDroopActivePowerControlAdder.class)
            .withEnabled(true)
            .withDroop(0.1f)
            .withP0(200)
            .add();
        hvdcLine.newExtension(HvdcOperatorActivePowerRangeAdder.class)
            .withOprFromCS1toCS2(1.0f)
            .withOprFromCS2toCS1(2.0f)
            .add();

        List<Series> ext1Series = createExtensionDataFrame("hvdcAngleDroopActivePowerControl", network);
        List<Series> ext2Series = createExtensionDataFrame("hvdcOperatorActivePowerRange", network);

        assertThat(ext1Series)
            .extracting(Series::getName)
            .containsExactly("id", "droop", "p0", "enabled");

        assertThat(ext1Series.get(1).getDoubles())
            .containsExactly(0.1f);
        assertThat(ext1Series.get(2).getDoubles())
            .containsExactly(200);
        assertThat(ext1Series.get(3).getBooleans())
            .containsExactly(true);

        assertThat(ext2Series)
            .extracting(Series::getName)
            .containsExactly("id", "opr_from_cs1_to_cs2", "opr_from_cs2_to_cs1");

        assertThat(ext2Series.get(1).getDoubles())
            .containsExactly(1.0f);
        assertThat(ext2Series.get(2).getDoubles())
            .containsExactly(2.0f);

        DefaultUpdatingDataframe dataframe = new DefaultUpdatingDataframe(1);
        dataframe.addSeries("id", true, new TestStringSeries("L"));
        dataframe.addSeries("droop", false, new TestDoubleSeries(0.2));
        updateExtension("hvdcAngleDroopActivePowerControl", network, dataframe);
        ext1Series = createExtensionDataFrame("hvdcAngleDroopActivePowerControl", network);
        assertThat(ext1Series.get(1).getDoubles()).containsExactly(0.2f);

        dataframe = new DefaultUpdatingDataframe(1);
        dataframe.addSeries("id", true, new TestStringSeries("L"));
        dataframe.addSeries("opr_from_cs1_to_cs2", false, new TestDoubleSeries(1.2));
        updateExtension("hvdcOperatorActivePowerRange", network, dataframe);
        ext2Series = createExtensionDataFrame("hvdcOperatorActivePowerRange", network);
        assertThat(ext2Series.get(1).getDoubles()).containsExactly(1.2f);

        NetworkExtensions.removeExtensions(network, "hvdcAngleDroopActivePowerControl",
            network.getHvdcLineStream().map(HvdcLine::getId).collect(Collectors.toList()));
        List<Series> series = createExtensionDataFrame("hvdcAngleDroopActivePowerControl", network);
        assertEquals(0, series.get(0).getStrings().length);

        NetworkExtensions.removeExtensions(network, "hvdcOperatorActivePowerRange",
            network.getHvdcLineStream().map(HvdcLine::getId).collect(Collectors.toList()));
        series = createExtensionDataFrame("hvdcOperatorActivePowerRange", network);
        assertEquals(0, series.get(0).getStrings().length);
    }

    @Test
    void svcs() {
        Network network = EurostagTutorialExample1Factory.create();
        List<Series> series = createDataFrame(STATIC_VAR_COMPENSATOR, network);

        assertThat(series)
            .extracting(Series::getName)
            .containsExactly("id", "name", "b_min", "b_max", "target_v", "target_q", "regulation_mode",
                "regulated_element_id",
                "p", "q", "i", "voltage_level_id", "bus_id", "connected");
        List<Series> allAttributeSeries = createDataFrame(STATIC_VAR_COMPENSATOR, network,
            new DataframeFilter(ALL_ATTRIBUTES, Collections.emptyList()));
        assertThat(allAttributeSeries)
            .extracting(Series::getName)
            .containsExactly("id", "name", "b_min", "b_max", "target_v", "target_q", "regulation_mode",
                "regulated_element_id", "p", "q", "i", "voltage_level_id", "bus_id", "bus_breaker_bus_id",
                "node", "connected", "fictitious");
    }

    @Test
    void substations() {
        Network network = EurostagTutorialExample1Factory.create();
        List<Series> series = createDataFrame(SUBSTATION, network);

        assertThat(series)
            .extracting(Series::getName)
            .containsExactly("id", "name", "TSO", "geo_tags", "country");
    }

    @Test
    void properties() {
        Network network = EurostagTutorialExample1Factory.create();
        network.getSubstation("P1").setProperty("prop1", "val1");
        network.getSubstation("P2").setProperty("prop2", "val2");
        List<Series> series = createDataFrame(SUBSTATION, network,
            new DataframeFilter(ALL_ATTRIBUTES, Collections.emptyList()));

        assertThat(series)
            .extracting(Series::getName)
            .containsExactly("id", "name", "TSO", "geo_tags", "country", "fictitious", "prop1", "prop2");
    }

    @Test
    void voltageLevels() {
        Network network = EurostagTutorialExample1Factory.create();
        List<Series> series = createDataFrame(VOLTAGE_LEVEL, network);

        assertThat(series)
            .extracting(Series::getName)
            .containsExactly("id", "name", "substation_id", "nominal_v", "high_voltage_limit", "low_voltage_limit");

        List<Series> allAttributesSeries = createDataFrame(VOLTAGE_LEVEL, network,
            new DataframeFilter(ALL_ATTRIBUTES, Collections.emptyList()));

        assertThat(allAttributesSeries)
            .extracting(Series::getName)
            .containsExactly("id", "name", "substation_id", "nominal_v", "high_voltage_limit", "low_voltage_limit",
                "fictitious", "topology_kind");
    }

    @Test
    void ratioTapChangerSteps() {
        Network network = EurostagTutorialExample1Factory.create();
        List<Series> series = createDataFrame(RATIO_TAP_CHANGER_STEP, network);

        assertThat(series)
            .extracting(Series::getName)
            .containsExactly("id", "position", "rho", "r", "x", "g", "b");
    }

    @Test
    void phaseTapChangerSteps() {
        Network network = EurostagTutorialExample1Factory.create();
        List<Series> series = createDataFrame(PHASE_TAP_CHANGER_STEP, network);

        assertThat(series)
            .extracting(Series::getName)
            .containsExactly("id", "position", "rho", "alpha", "r", "x", "g", "b");
    }

    @Test
    void ratioTapChangers() {
        Network network = EurostagTutorialExample1Factory.create();
        List<Series> series = createDataFrame(RATIO_TAP_CHANGER, network);

        assertThat(series)
            .extracting(Series::getName)
            .containsExactly("id", "tap", "low_tap", "high_tap", "step_count", "on_load", "regulating", "target_v",
                "target_deadband", "regulating_bus_id", "rho", "alpha");
    }

    @Test
    void phaseTapChangers() {
        Network network = EurostagTutorialExample1Factory.create();
        List<Series> series = createDataFrame(PHASE_TAP_CHANGER, network);

        assertThat(series)
            .extracting(Series::getName)
            .containsExactly("id", "tap", "low_tap", "high_tap", "step_count", "regulating", "regulation_mode",
                "regulation_value", "target_deadband", "regulating_bus_id");
    }

    @Test
    void reactiveLimits() {
        Network network = EurostagTutorialExample1Factory.create();
        List<Series> series = createDataFrame(REACTIVE_CAPABILITY_CURVE_POINT, network);

        assertThat(series)
            .extracting(Series::getName)
            .containsExactly("id", "num", "p", "min_q", "max_q");
    }

    @Test
    void attributesFiltering() {
        Network network = EurostagTutorialExample1Factory.create();
        network.getSubstation("P1").setProperty("prop1", "val1");
        network.getSubstation("P2").setProperty("prop2", "val2");
        List<Series> seriesDefaults = createDataFrame(SUBSTATION, network,
            new DataframeFilter());
        assertThat(seriesDefaults)
            .extracting(Series::getName)
            .containsExactly("id", "name", "TSO", "geo_tags", "country");

        List<Series> seriesAll = createDataFrame(SUBSTATION, network,
            new DataframeFilter(ALL_ATTRIBUTES, Collections.emptyList()));
        assertThat(seriesAll)
            .extracting(Series::getName)
            .containsExactly("id", "name", "TSO", "geo_tags", "country", "fictitious", "prop1", "prop2");

        List<Series> seriesAttributesSubset = createDataFrame(SUBSTATION, network,
            new DataframeFilter(DataframeFilter.AttributeFilterType.INPUT_ATTRIBUTES,
                List.of("name", "name", "geo_tags", "prop1", "fictitious")));
        assertThat(seriesAttributesSubset)
            .extracting(Series::getName)
            .containsExactly("id", "name", "geo_tags", "fictitious", "prop1");
    }

    @Test
    void testIdentifiables() {
        Network network = EurostagTutorialExample1Factory.create();
        List<Series> series = createDataFrame(IDENTIFIABLE, network);

        assertThat(series)
            .extracting(Series::getName)
            .containsExactly("id", "type");
    }

    @Test
    void testInjections() {
        Network network = EurostagTutorialExample1Factory.create();
        List<Series> series = createDataFrame(INJECTION, network);

        assertThat(series)
            .extracting(Series::getName)
            .containsExactly("id", "type", "voltage_level_id", "bus_id");
    }

    @Test
    void testBranches() {
        Network network = EurostagTutorialExample1Factory.create();
        List<Series> series = createDataFrame(BRANCH, network);

        assertThat(series)
            .extracting(Series::getName)
            .containsExactly("id", "type", "voltage_level1_id", "bus1_id", "connected1", "voltage_level2_id", "bus2_id",
                "connected2");
    }

    @Test
    void testTerminals() {
        Network network = EurostagTutorialExample1Factory.create();
        List<Series> series = createDataFrame(TERMINAL, network);

        assertThat(series)
            .extracting(Series::getName)
            .containsExactly("element_id", "voltage_level_id", "bus_id", "element_side", "connected");
    }
}
