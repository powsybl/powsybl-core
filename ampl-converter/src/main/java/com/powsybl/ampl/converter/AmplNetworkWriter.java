/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016-2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.converter;

import com.powsybl.ampl.converter.util.AmplDatTableFormatter;
import com.powsybl.ampl.converter.version.AmplColumnsExporter;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.extensions.Extendable;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.io.table.TableFormatter;
import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.HvdcConverterStation.HvdcType;
import com.powsybl.iidm.network.util.ConnectedComponents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static com.powsybl.ampl.converter.AmplConstants.DEFAULT_VARIANT_INDEX;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class AmplNetworkWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmplNetworkWriter.class);

    private final Network network;

    private final int variantIndex;

    private final DataSource dataSource;

    private final boolean append;

    private final StringToIntMapper<AmplSubset> mapper; // id mapper

    private final Map<String, List<AmplExtension>> extensionMap;

    private final AmplExportConfig config;

    private final AmplColumnsExporter columnsExporter;

    private static class AmplExportContext {

        private int otherCcNum = Integer.MAX_VALUE;

        public final Set<String> busIdsToExport = new HashSet<>();

        public final Set<String> voltageLevelIdsToExport = new HashSet<>();

        public final Set<String> generatorIdsToExport = new HashSet<>();

        public final Set<String> batteryIdsToExport = new HashSet<>();

        public final Set<String> loadsToExport = new HashSet<>();

    }

    public AmplNetworkWriter(Network network, int variantIndex, DataSource dataSource, int faultNum, int actionNum,
                             boolean append, StringToIntMapper<AmplSubset> mapper, AmplExportConfig config) {
        this.network = Objects.requireNonNull(network);
        this.variantIndex = variantIndex;
        this.dataSource = Objects.requireNonNull(dataSource);
        this.append = append;
        this.mapper = Objects.requireNonNull(mapper);
        this.config = Objects.requireNonNull(config);
        extensionMap = new HashMap<>();
        this.columnsExporter = config.getVersion()
            .getColumnsExporter()
            .create(config, network, mapper, variantIndex, faultNum, actionNum);
    }

    public AmplNetworkWriter(Network network, DataSource dataSource, int faultNum, int actionNum,
                             boolean append, StringToIntMapper<AmplSubset> mapper, AmplExportConfig config) {
        this(network, DEFAULT_VARIANT_INDEX, dataSource, faultNum, actionNum, append, mapper, config);
    }

    public AmplNetworkWriter(Network network, DataSource dataSource, StringToIntMapper<AmplSubset> mapper,
                             AmplExportConfig config) {
        this(network, DEFAULT_VARIANT_INDEX, dataSource, 0, 0, false, mapper, config);
    }

    public AmplNetworkWriter(Network network, DataSource dataSource, AmplExportConfig config) {
        this(network, DEFAULT_VARIANT_INDEX, dataSource, 0, 0, false, AmplUtil.createMapper(network), config);
    }

    private <I extends Identifiable<?>> Iterable<I> getSortedIdentifiables(Stream<I> equipments) {
        return config.isExportSorted() ? equipments.sorted(Comparator.comparing(Identifiable::getId)).toList() : equipments.toList();
    }

    public static String getTableTitle(Network network, String tableName) {
        return tableName + " (" + network.getId() + "/" + network.getVariantManager().getWorkingVariantId() + ")";
    }

    private static int getThreeWindingsTransformerMiddleBusComponentNum(AmplExportContext context, ThreeWindingsTransformer twt) {
        Terminal t1 = twt.getLeg1().getTerminal();
        Terminal t2 = twt.getLeg2().getTerminal();
        Terminal t3 = twt.getLeg3().getTerminal();
        Bus b1 = AmplUtil.getBus(t1);
        Bus b2 = AmplUtil.getBus(t2);
        Bus b3 = AmplUtil.getBus(t3);
        int middleCcNum;
        if (b1 != null) {
            middleCcNum = ConnectedComponents.getCcNum(b1);
        } else if (b2 != null) {
            middleCcNum = ConnectedComponents.getCcNum(b2);
        } else if (b3 != null) {
            middleCcNum = ConnectedComponents.getCcNum(b3);
        } else {
            middleCcNum = context.otherCcNum--;
        }

        return middleCcNum;
    }

    private static int getDanglingLineMiddleBusComponentNum(AmplExportContext context, DanglingLine dl) {
        Bus b = AmplUtil.getBus(dl.getTerminal());
        int middleCcNum;
        // if the connection bus of the dangling line is null or not in the main cc, the middle bus is
        // obviously not in the main cc
        if (b != null) {
            middleCcNum = ConnectedComponents.getCcNum(b);
        } else {
            middleCcNum = context.otherCcNum--;
        }

        return middleCcNum;
    }

    private static int getTieLineMiddleBusComponentNum(AmplExportContext context, TieLine tieLine) {
        Terminal t1 = tieLine.getDanglingLine1().getTerminal();
        Terminal t2 = tieLine.getDanglingLine2().getTerminal();
        Bus b1 = AmplUtil.getBus(t1);
        Bus b2 = AmplUtil.getBus(t2);
        int xNodeCcNum;
        if (b1 != null) {
            xNodeCcNum = ConnectedComponents.getCcNum(b1);
        } else if (b2 != null) {
            xNodeCcNum = ConnectedComponents.getCcNum(b2);
        } else {
            xNodeCcNum = context.otherCcNum--;
        }

        return xNodeCcNum;
    }

    private static boolean isBusExported(AmplExportContext context, String busId) {
        return busId != null && context.busIdsToExport.contains(busId);
    }

    private String getTableTitle(String tableName) {
        return getTableTitle(network, tableName);
    }

    private void writeSubstations() throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("_network_substations", "txt", append),
            StandardCharsets.UTF_8);
             TableFormatter formatter = new AmplDatTableFormatter(writer,
                 getTableTitle("Substations"),
                 AmplConstants.INVALID_FLOAT_VALUE,
                 !append,
                 AmplConstants.LOCALE,
                 columnsExporter.getSubstationsColumns())) {
            for (VoltageLevel vl : getSortedIdentifiables(network.getVoltageLevelStream())) {
                columnsExporter.writeVoltageLevelToFormatter(formatter, vl);
                addExtensions(mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vl.getId()), vl);
            }
            // voltage level associated to 3 windings transformers middle bus
            for (ThreeWindingsTransformer twt : getSortedIdentifiables(network.getThreeWindingsTransformerStream())) {
                columnsExporter.writeThreeWindingsTransformerVoltageLevelToFormatter(formatter, twt);
                addExtensions(mapper.getInt(AmplSubset.VOLTAGE_LEVEL,
                    AmplUtil.getThreeWindingsTransformerMiddleVoltageLevelId(twt)), twt);
            }
            // voltage level associated to dangling lines middle bus
            for (DanglingLine dl : getSortedIdentifiables(network.getDanglingLineStream(DanglingLineFilter.UNPAIRED))) {
                columnsExporter.writeDanglingLineVoltageLevelToFormatter(formatter, dl);
                addExtensions(mapper.getInt(AmplSubset.VOLTAGE_LEVEL, AmplUtil.getDanglingLineMiddleVoltageLevelId(dl)),
                    dl);
            }
            if (config.isExportXNodes()) {
                for (TieLine tieLine : getSortedIdentifiables(network.getTieLineStream())) {
                    columnsExporter.writeTieLineVoltageLevelToFormatter(formatter, tieLine);
                    addExtensions(mapper.getInt(AmplSubset.VOLTAGE_LEVEL, AmplUtil.getXnodeVoltageLevelId(tieLine)),
                        tieLine);
                }
            }
        }
    }

    private boolean isOnlyMainCc() {
        return switch (config.getExportScope()) {
            case ONLY_MAIN_CC, ONLY_MAIN_CC_AND_CONNECTABLE_GENERATORS_AND_SHUNTS, ONLY_MAIN_CC_AND_CONNECTABLE_GENERATORS_AND_SHUNTS_AND_ALL_LOADS ->
                true;
            case ALL -> false;
        };
    }

    private boolean connectedComponentToExport(int numCC) {
        return !(isOnlyMainCc() && numCC != ComponentConstants.MAIN_NUM);
    }

    private void writeBuses(AmplExportContext context) throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("_network_buses", "txt", append),
            StandardCharsets.UTF_8);
             TableFormatter formatter = new AmplDatTableFormatter(writer,
                 getTableTitle("Buses"),
                 AmplConstants.INVALID_FLOAT_VALUE,
                 !append,
                 AmplConstants.LOCALE,
                 columnsExporter.getBusesColumns())) {

            writeBuses(context, formatter);

            writeThreeWindingsTransformerMiddleBuses(context, formatter);

            writeDanglingLineMiddleBuses(context, formatter);

            if (config.isExportXNodes()) {
                writeTieLineMiddleBuses(context, formatter);
            }
        }
    }

    private void writeBuses(AmplExportContext context, TableFormatter formatter) throws IOException {
        for (Bus b : AmplUtil.getBuses(network)) {
            int ccNum = ConnectedComponents.getCcNum(b);
            if (connectedComponentToExport(ccNum)) {
                context.busIdsToExport.add(b.getId());
                columnsExporter.writeBusesColumnsToFormatter(formatter, b);
                addExtensions(mapper.getInt(AmplSubset.BUS, b.getId()), b);
            }
        }
    }

    private <E> void addExtensions(int extendedNum, Extendable<E> extendable) {
        for (Extension<E> ext : extendable.getExtensions()) {
            List<AmplExtension> extList = extensionMap.computeIfAbsent(ext.getName(), k -> new ArrayList<>());
            extList.add(new AmplExtension(extendedNum, extendable, ext));
            extensionMap.put(ext.getName(), extList);
        }
    }

    private void addNetworkExtensions() {
        int networkNum = mapper.getInt(AmplSubset.NETWORK, network.getId());
        addExtensions(networkNum, network);
    }

    private void exportExtensions() throws IOException {

        for (Entry<String, List<AmplExtension>> entry : extensionMap.entrySet()) {
            AmplExtensionWriter extWriter = AmplExtensionWriters.getWriter(entry.getKey());
            if (extWriter != null) {
                extWriter.write(entry.getValue(), network, variantIndex, mapper, dataSource, append, config);
            }
        }
    }

    private void writeThreeWindingsTransformerMiddleBuses(AmplExportContext context, TableFormatter formatter) throws IOException {
        for (ThreeWindingsTransformer twt : getSortedIdentifiables(network.getThreeWindingsTransformerStream())) {
            int middleCcNum = getThreeWindingsTransformerMiddleBusComponentNum(context, twt);

            if (connectedComponentToExport(middleCcNum)) {
                String middleBusId = AmplUtil.getThreeWindingsTransformerMiddleBusId(twt);
                context.busIdsToExport.add(middleBusId);
                columnsExporter.writeThreeWindingsTranformersMiddleBusesColumnsToFormatter(formatter, twt, middleCcNum);
            }
        }
    }

    private void writeDanglingLineMiddleBuses(AmplExportContext context, TableFormatter formatter) throws IOException {
        for (DanglingLine dl : getSortedIdentifiables(network.getDanglingLineStream(DanglingLineFilter.UNPAIRED))) {
            int middleCcNum = getDanglingLineMiddleBusComponentNum(context, dl);
            if (connectedComponentToExport(middleCcNum)) {
                context.busIdsToExport.add(AmplUtil.getDanglingLineMiddleBusId(dl));
                columnsExporter.writeDanglingLineMiddleBusesToFormatter(formatter, dl, middleCcNum);
            }
        }
    }

    private void writeTieLineMiddleBuses(AmplExportContext context, TableFormatter formatter) throws IOException {
        for (TieLine tieLine : getSortedIdentifiables(network.getTieLineStream())) {

            int xNodeCcNum = getTieLineMiddleBusComponentNum(context, tieLine);
            if (connectedComponentToExport(xNodeCcNum)) {
                columnsExporter.writeTieLineMiddleBusesToFormatter(formatter, tieLine, xNodeCcNum);
            }
        }
    }

    private void writeBranches(AmplExportContext context) throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("_network_branches", "txt", append),
            StandardCharsets.UTF_8);
             TableFormatter formatter = new AmplDatTableFormatter(writer,
                 getTableTitle("Branches"),
                 AmplConstants.INVALID_FLOAT_VALUE,
                 !append,
                 AmplConstants.LOCALE,
                 columnsExporter.getBranchesColumns()
             )) {

            writeLines(context, formatter);

            writeTieLines(context, formatter);

            writeTwoWindingsTransformers(context, formatter);

            writeThreeWindingsTransformers(context, formatter);

            writeDanglingLines(context, formatter);
        }
    }

    private void writeLines(AmplExportContext context, TableFormatter formatter) throws IOException {
        for (Line l : getSortedIdentifiables(network.getLineStream())) {
            Terminal t1 = l.getTerminal1();
            Terminal t2 = l.getTerminal2();
            if (addVoltageLevelIdsToExport(context, t1, t2, l.getId())) {
                columnsExporter.writeLinesToFormatter(formatter, l);
                addExtensions(mapper.getInt(AmplSubset.BRANCH, l.getId()), l);
            }
        }
    }

    private boolean addVoltageLevelIdsToExport(AmplExportContext context, Terminal t1, Terminal t2, String id) {
        Bus bus1 = AmplUtil.getBus(t1);
        Bus bus2 = AmplUtil.getBus(t2);
        if (bus2 != null && bus1 == bus2) {
            LOGGER.warn("Skipping line '{}' connected to the same bus at both sides", id);
            return false;
        }
        String bus1Id = AmplUtil.getBusId(bus1);
        String bus2Id = AmplUtil.getBusId(bus2);
        if (isOnlyMainCc() && !(isBusExported(context, bus1Id) || isBusExported(context, bus2Id))) {
            return false;
        }
        context.voltageLevelIdsToExport.add(t1.getVoltageLevel().getId());
        context.voltageLevelIdsToExport.add(t2.getVoltageLevel().getId());
        return true;
    }

    private void writeTieLines(AmplExportContext context, TableFormatter formatter) throws IOException {
        for (TieLine l : getSortedIdentifiables(network.getTieLineStream())) {
            Terminal t1 = l.getDanglingLine1().getTerminal();
            Terminal t2 = l.getDanglingLine2().getTerminal();
            if (addVoltageLevelIdsToExport(context, t1, t2, l.getId())) {
                columnsExporter.writeTieLineToFormatter(formatter, l);
                addExtensions(mapper.getInt(AmplSubset.BRANCH, l.getId()), l);
            }
        }
    }

    private void writeTwoWindingsTransformers(AmplExportContext context, TableFormatter formatter) throws IOException {
        for (TwoWindingsTransformer twt : getSortedIdentifiables(network.getTwoWindingsTransformerStream())) {
            Terminal t1 = twt.getTerminal1();
            Terminal t2 = twt.getTerminal2();
            Bus bus1 = AmplUtil.getBus(t1);
            Bus bus2 = AmplUtil.getBus(t2);
            if (bus1 != null && bus1 == bus2) {
                LOGGER.warn("Skipping transformer '{}' connected to the same bus at both sides", twt.getId());
            } else if (!isOnlyMainCc() || isBusExported(context, AmplUtil.getBusId(bus1)) || isBusExported(context, AmplUtil.getBusId(bus2))) {
                context.voltageLevelIdsToExport.add(t1.getVoltageLevel().getId());
                context.voltageLevelIdsToExport.add(t2.getVoltageLevel().getId());
                columnsExporter.writeTwoWindingsTranformerToFormatter(formatter, twt);
                addExtensions(mapper.getInt(AmplSubset.BRANCH, twt.getId()), twt);
            }
        }
    }

    private void writeThreeWindingsTransformers(AmplExportContext context, TableFormatter formatter) throws IOException {
        for (ThreeWindingsTransformer twt : getSortedIdentifiables(network.getThreeWindingsTransformerStream())) {
            Terminal t1 = twt.getLeg1().getTerminal();
            Terminal t2 = twt.getLeg2().getTerminal();
            Terminal t3 = twt.getLeg3().getTerminal();
            Bus bus1 = AmplUtil.getBus(t1);
            Bus bus2 = AmplUtil.getBus(t2);
            Bus bus3 = AmplUtil.getBus(t3);
            // TODO could be connected to the same bus at 2 or 3 ends ?
            VoltageLevel vl1 = t1.getVoltageLevel();
            VoltageLevel vl2 = t2.getVoltageLevel();
            VoltageLevel vl3 = t3.getVoltageLevel();
            context.voltageLevelIdsToExport.add(vl1.getId());
            context.voltageLevelIdsToExport.add(vl2.getId());
            context.voltageLevelIdsToExport.add(vl3.getId());
            String id1 = twt.getId() + AmplConstants.LEG1_SUFFIX;
            String id2 = twt.getId() + AmplConstants.LEG2_SUFFIX;
            String id3 = twt.getId() + AmplConstants.LEG3_SUFFIX;
            int num1 = mapper.getInt(AmplSubset.BRANCH, id1);
            int num2 = mapper.getInt(AmplSubset.BRANCH, id2);
            int num3 = mapper.getInt(AmplSubset.BRANCH, id3);
            String bus1Id = AmplUtil.getBusId(bus1);
            String bus2Id = AmplUtil.getBusId(bus2);
            String bus3Id = AmplUtil.getBusId(bus3);

            int middleVlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL,
                AmplUtil.getThreeWindingsTransformerMiddleVoltageLevelId(twt));
            String middleBusId = AmplUtil.getThreeWindingsTransformerMiddleBusId(twt);
            int middleBusNum = mapper.getInt(AmplSubset.BUS, middleBusId);

            if (!isOnlyMainCc() || isBusExported(context, middleBusId) || isBusExported(context, bus1Id)) {
                columnsExporter.writeThreeWindingsTransformerLegToFormatter(formatter, twt, middleBusNum, middleVlNum,
                    ThreeSides.ONE);
                addExtensions(num1, twt);
            }
            if (!isOnlyMainCc() || isBusExported(context, middleBusId) || isBusExported(context, bus2Id)) {
                columnsExporter.writeThreeWindingsTransformerLegToFormatter(formatter, twt, middleBusNum, middleVlNum,
                    ThreeSides.TWO);
                addExtensions(num2, twt);
            }
            if (!isOnlyMainCc() || isBusExported(context, middleBusId) || isBusExported(context, bus3Id)) {
                columnsExporter.writeThreeWindingsTransformerLegToFormatter(formatter, twt, middleBusNum, middleVlNum,
                    ThreeSides.THREE);
                addExtensions(num3, twt);
            }
        }
    }

    private void writeDanglingLines(AmplExportContext context, TableFormatter formatter) throws IOException {
        for (DanglingLine dl : getSortedIdentifiables(network.getDanglingLineStream(DanglingLineFilter.UNPAIRED))) {
            Terminal t = dl.getTerminal();
            Bus bus1 = AmplUtil.getBus(t);
            String bus1Id = AmplUtil.getBusId(bus1);
            String middleBusId = AmplUtil.getDanglingLineMiddleBusId(dl);
            if (!isOnlyMainCc() || isBusExported(context, bus1Id) || isBusExported(context, middleBusId)) {
                VoltageLevel vl = t.getVoltageLevel();
                String middleVlId = AmplUtil.getDanglingLineMiddleVoltageLevelId(dl);
                context.voltageLevelIdsToExport.add(vl.getId());
                context.voltageLevelIdsToExport.add(middleVlId);
                columnsExporter.writeDanglingLineToFormatter(formatter, dl);
                addExtensions(mapper.getInt(AmplSubset.BRANCH, dl.getId()), dl);
            }
        }
    }

    private void writeTapChangerTable() throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("_network_tct", "txt", append),
            StandardCharsets.UTF_8);
             TableFormatter formatter = new AmplDatTableFormatter(writer,
                 getTableTitle("Tap changer table"),
                 AmplConstants.INVALID_FLOAT_VALUE,
                 !append,
                 AmplConstants.LOCALE,
                 columnsExporter.getTapChangerTableColumns())) {

            writeTwoWindingsTransformerTapChangerTable(formatter);

            writeThreeWindingsTransformerTapChangerTable(formatter);
        }
    }

    private void writeTwoWindingsTransformerTapChangerTable(TableFormatter formatter) throws IOException {
        for (TwoWindingsTransformer twt : getSortedIdentifiables(network.getTwoWindingsTransformerStream())) {
            columnsExporter.writeTwoWindingsTransformerTapChangerTableToFormatter(formatter, twt);
        }
    }

    private void writeThreeWindingsTransformerTapChangerTable(TableFormatter formatter) throws IOException {
        for (ThreeWindingsTransformer twt : getSortedIdentifiables(network.getThreeWindingsTransformerStream())) {
            columnsExporter.writeThreeWindingsTransformerTapChangerTableToFormatter(formatter, twt);
        }
    }

    private void writeRatioTapChangers() throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("_network_rtc", "txt", append),
            StandardCharsets.UTF_8);
             TableFormatter formatter = new AmplDatTableFormatter(writer,
                 getTableTitle("Ratio tap changers"),
                 AmplConstants.INVALID_FLOAT_VALUE,
                 !append,
                 AmplConstants.LOCALE,
                 columnsExporter.getRtcColumns())) {
            columnsExporter.writeRtcToFormatter(formatter);
        }
    }

    private void writePhaseTapChangers() throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("_network_ptc", "txt", append),
            StandardCharsets.UTF_8);
             TableFormatter formatter = new AmplDatTableFormatter(writer,
                 getTableTitle("Phase tap changers"),
                 AmplConstants.INVALID_FLOAT_VALUE,
                 !append,
                 AmplConstants.LOCALE,
                 columnsExporter.getPtcColumns())) {
            columnsExporter.writePtcToFormatter(formatter);
        }
    }

    private boolean exportLoad(AmplExportContext context, String busId) {
        return switch (config.getExportScope()) {
            case ALL, ONLY_MAIN_CC_AND_CONNECTABLE_GENERATORS_AND_SHUNTS_AND_ALL_LOADS -> true;
            case ONLY_MAIN_CC, ONLY_MAIN_CC_AND_CONNECTABLE_GENERATORS_AND_SHUNTS -> isBusExported(context, busId);
        };
    }

    private void writeLoads(AmplExportContext context) throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("_network_loads", "txt", append),
            StandardCharsets.UTF_8);
             TableFormatter formatter = new AmplDatTableFormatter(writer,
                 getTableTitle("Loads"),
                 AmplConstants.INVALID_FLOAT_VALUE,
                 !append,
                 AmplConstants.LOCALE,
                 columnsExporter.getLoadsColumns())) {
            List<String> skipped = new ArrayList<>();
            for (Load l : getSortedIdentifiables(network.getLoadStream())) {
                Terminal t = l.getTerminal();
                Bus bus = AmplUtil.getBus(t);
                String busId = null;
                if (bus != null) {
                    busId = bus.getId();
                }
                if (!exportLoad(context, busId)) {
                    skipped.add(l.getId());
                } else {
                    context.loadsToExport.add(l.getId());
                    columnsExporter.writeLoadtoFormatter(formatter, l);
                    addExtensions(mapper.getInt(AmplSubset.LOAD, l.getId()), l);
                }
            }
            for (DanglingLine dl : getSortedIdentifiables(network.getDanglingLineStream(DanglingLineFilter.UNPAIRED))) {
                String middleBusId = AmplUtil.getDanglingLineMiddleBusId(dl);
                if (!exportLoad(context, middleBusId)) {
                    skipped.add(dl.getId());
                } else {
                    columnsExporter.writeDanglingLineLoadToFormatter(formatter, dl);
                }
            }
            if (!skipped.isEmpty()) {
                LOGGER.trace("Skip loads {} because not connected and not connectable", skipped);
            }
        }
    }

    private boolean exportGeneratorOrShunt(AmplExportContext context, String busId, String conBusId) {
        return switch (config.getExportScope()) {
            case ALL -> true;
            case ONLY_MAIN_CC -> isBusExported(context, busId);
            case ONLY_MAIN_CC_AND_CONNECTABLE_GENERATORS_AND_SHUNTS, ONLY_MAIN_CC_AND_CONNECTABLE_GENERATORS_AND_SHUNTS_AND_ALL_LOADS ->
                isBusExported(context, conBusId);
        };
    }

    private void writeShunts(AmplExportContext context) throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("_network_shunts", "txt", append),
            StandardCharsets.UTF_8);
             TableFormatter formatter = new AmplDatTableFormatter(writer,
                 getTableTitle("Shunts"),
                 AmplConstants.INVALID_FLOAT_VALUE,
                 !append,
                 AmplConstants.LOCALE,
                 columnsExporter.getShuntsColumns())) {
            List<String> skipped = new ArrayList<>();
            for (ShuntCompensator sc : getSortedIdentifiables(network.getShuntCompensatorStream())) {
                Terminal t = sc.getTerminal();
                Bus bus = AmplUtil.getBus(t);
                String busId = null;
                if (bus != null) {
                    busId = bus.getId();
                }
                // take connectable bus if exists
                String conBusId = null;
                Bus conBus = AmplUtil.getConnectableBus(t);
                if (conBus != null) {
                    conBusId = conBus.getId();
                }
                if (!exportGeneratorOrShunt(context, busId, conBusId)) {
                    skipped.add(sc.getId());
                } else {
                    columnsExporter.writeShuntCompensatorToFormatter(formatter, sc);
                    addExtensions(mapper.getInt(AmplSubset.SHUNT, sc.getId()), sc);
                }
            }
            if (!skipped.isEmpty()) {
                LOGGER.trace("Skip shunts {} because not connected and not connectable", skipped);
            }
        }
    }

    private void writeStaticVarCompensators() throws IOException {
        try (Writer writer = new OutputStreamWriter(
            dataSource.newOutputStream("_network_static_var_compensators", "txt", append), StandardCharsets.UTF_8);
             TableFormatter formatter = new AmplDatTableFormatter(writer,
                 getTableTitle("Static VAR compensators"),
                 AmplConstants.INVALID_FLOAT_VALUE,
                 !append,
                 AmplConstants.LOCALE,
                 columnsExporter.getStaticVarCompensatorColumns())) {
            List<String> skipped = new ArrayList<>();
            for (StaticVarCompensator svc : getSortedIdentifiables(network.getStaticVarCompensatorStream())) {
                columnsExporter.writeStaticVarCompensatorToFormatter(formatter, svc);
                addExtensions(mapper.getInt(AmplSubset.STATIC_VAR_COMPENSATOR, svc.getId()), svc);
            }
            if (!skipped.isEmpty()) {
                LOGGER.trace("Skip static VAR compensators {} because not connected and not connectable", skipped);
            }
        }
    }

    private void writeGenerators(AmplExportContext context) throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("_network_generators", "txt", append),
            StandardCharsets.UTF_8);
             TableFormatter formatter = new AmplDatTableFormatter(writer,
                 getTableTitle("Generators"),
                 AmplConstants.INVALID_FLOAT_VALUE,
                 !append,
                 AmplConstants.LOCALE,
                 columnsExporter.getGeneratorsColumns())) {
            List<String> skipped = new ArrayList<>();
            for (Generator g : getSortedIdentifiables(network.getGeneratorStream())) {
                Terminal t = g.getTerminal();
                Bus bus = AmplUtil.getBus(t);
                String busId = null;
                if (bus != null) {
                    busId = bus.getId();
                }
                String conBusId = null;
                // take connectable bus if exists
                Bus conBus = AmplUtil.getConnectableBus(t);
                if (conBus != null) {
                    conBusId = conBus.getId();
                }
                if (!exportGeneratorOrShunt(context, busId, conBusId)) {
                    skipped.add(g.getId());
                } else {
                    context.generatorIdsToExport.add(g.getId());
                    columnsExporter.writeGeneratorToFormatter(formatter, g);
                    addExtensions(mapper.getInt(AmplSubset.GENERATOR, g.getId()), g);
                }
            }
            if (!skipped.isEmpty()) {
                LOGGER.trace("Skip generators {} because not connected and not connectable", skipped);
            }
        }
    }

    private void writeBatteries(AmplExportContext context) throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("_network_batteries", "txt", append),
            StandardCharsets.UTF_8);
             TableFormatter formatter = new AmplDatTableFormatter(writer,
                 getTableTitle("Batteries"),
                 AmplConstants.INVALID_FLOAT_VALUE,
                 !append,
                 AmplConstants.LOCALE,
                 columnsExporter.getBatteriesColumns())) {
            List<String> skipped = new ArrayList<>();
            for (Battery b : getSortedIdentifiables(network.getBatteryStream())) {
                Terminal t = b.getTerminal();
                Bus bus = AmplUtil.getBus(t);
                String busId = null;
                if (bus != null) {
                    busId = bus.getId();
                }
                // take connectable bus if exists
                if (!isBusExported(context, busId)) {
                    skipped.add(b.getId());
                } else {
                    context.batteryIdsToExport.add(b.getId());
                    columnsExporter.writeBatteryToFormatter(formatter, b);
                    addExtensions(mapper.getInt(AmplSubset.BATTERY, b.getId()), b);
                }
            }
            if (!skipped.isEmpty()) {
                LOGGER.trace("Skip batteries {} because not connected and not connectable", skipped);
            }
        }
    }

    private void writeCurrentLimits() throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("_network_limits", "txt", append),
            StandardCharsets.UTF_8);
             TableFormatter formatter = new AmplDatTableFormatter(writer,
                 getTableTitle("Temporary current limits"),
                 AmplConstants.INVALID_FLOAT_VALUE,
                 !append,
                 AmplConstants.LOCALE,
                 columnsExporter.getCurrentLimitsColumns())) {

            columnsExporter.writeCurrentLimits(formatter);
        }
    }

    private void writeHvdcLines() throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("_network_hvdc", "txt", append),
            StandardCharsets.UTF_8);
             TableFormatter formatter = new AmplDatTableFormatter(writer,
                 getTableTitle("HVDC lines"),
                 AmplConstants.INVALID_FLOAT_VALUE,
                 !append,
                 AmplConstants.LOCALE,
                 columnsExporter.getHvdcLinesColumns())) {
            for (HvdcLine hvdcLine : getSortedIdentifiables(network.getHvdcLineStream())) {
                columnsExporter.writeHvdcToFormatter(formatter, hvdcLine);
                addExtensions(mapper.getInt(AmplSubset.HVDC_LINE, hvdcLine.getId()), hvdcLine);
            }
        }
    }

    private void writeLccConverterStations() throws IOException {
        try (Writer writer = new OutputStreamWriter(
            dataSource.newOutputStream("_network_lcc_converter_stations", "txt", append), StandardCharsets.UTF_8);
             TableFormatter formatter = new AmplDatTableFormatter(writer,
                 getTableTitle("LCC Converter Stations"),
                 AmplConstants.INVALID_FLOAT_VALUE,
                 !append,
                 AmplConstants.LOCALE,
                 columnsExporter.getLccConverterStationsColumns())) {

            for (HvdcConverterStation<?> hvdcStation : getSortedIdentifiables(network.getHvdcConverterStationStream())) {
                if (hvdcStation.getHvdcType().equals(HvdcType.LCC)) {
                    LccConverterStation lccStation = (LccConverterStation) hvdcStation;
                    columnsExporter.writeLccConverterStationToFormatter(formatter, lccStation);
                    addExtensions(mapper.getInt(AmplSubset.LCC_CONVERTER_STATION, lccStation.getId()), lccStation);
                }
            }
        }
    }

    private void writeVscConverterStations() throws IOException {
        try (Writer writer = new OutputStreamWriter(
            dataSource.newOutputStream("_network_vsc_converter_stations", "txt", append), StandardCharsets.UTF_8);
             TableFormatter formatter = new AmplDatTableFormatter(writer,
                 getTableTitle("VSC Converter Stations"),
                 AmplConstants.INVALID_FLOAT_VALUE,
                 !append,
                 AmplConstants.LOCALE,
                 columnsExporter.getVscConverterStationsColumns())) {

            for (HvdcConverterStation<?> hvdcStation : getSortedIdentifiables(network.getHvdcConverterStationStream())) {
                if (hvdcStation.getHvdcType().equals(HvdcType.VSC)) {
                    VscConverterStation vscStation = (VscConverterStation) hvdcStation;
                    columnsExporter.writeVscConverterStationToFormatter(formatter, vscStation);
                    addExtensions(mapper.getInt(AmplSubset.VSC_CONVERTER_STATION, vscStation.getId()), vscStation);

                }
            }
        }
    }

    public void write() throws IOException {
        write(new AmplExportContext());
    }

    public void write(AmplExportContext context) throws IOException {
        extensionMap.clear();
        writeBuses(context);
        writeTapChangerTable();
        writeRatioTapChangers();
        writePhaseTapChangers();
        writeBranches(context);
        writeCurrentLimits();
        writeGenerators(context);
        writeBatteries(context);
        writeLoads(context);
        writeShunts(context);
        writeStaticVarCompensators();
        writeSubstations();
        writeVscConverterStations();
        writeLccConverterStations();
        writeHvdcLines();
        writeHeaders();

        addNetworkExtensions();
        exportExtensions();
    }

    private void writeHeaders() throws IOException {
        try (Writer writer = new OutputStreamWriter(
            dataSource.newOutputStream("_headers", "txt", append), StandardCharsets.UTF_8)
        ) {
            columnsExporter.writeHeaderFile(writer);
        }
    }
}
