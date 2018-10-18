/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016-2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.converter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.ampl.converter.util.AmplDatTableFormatter;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.extensions.Extendable;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.TableFormatter;
import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.iidm.network.CurrentLimits.TemporaryLimit;
import com.powsybl.iidm.network.HvdcConverterStation.HvdcType;
import com.powsybl.iidm.network.StaticVarCompensator.RegulationMode;
import com.powsybl.iidm.network.util.ConnectedComponents;
import com.powsybl.iidm.network.util.SV;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AmplNetworkWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmplNetworkWriter.class);

    private static final String XNODE_COUNTRY_NAME = "XNODE";
    private static final String FAULT = "fault";
    private static final String DESCRIPTION = "description";
    private static final String SUBSTATION = "substation";
    private static final String TARGET_V = "targetV (pu)";
    private static final String CON_BUS = "con. bus";
    private static final String MAXP = "maxP (MW)";
    private static final String V_REGUL = "v regul.";
    private static final String ACTIVE_POWER = "P (MW)";
    private static final String REACTIVE_POWER = "Q (MVar)";

    private final Network network;

    private final int faultNum;

    private final int actionNum;

    private final DataSource dataSource;

    private final boolean append;

    private final StringToIntMapper<AmplSubset> mapper; // id mapper

    private final Map<String, List<AmplExtension>> extensionMap;

    private final AmplExportConfig config;

    private static class AmplExportContext {

        private int otherCcNum = Integer.MAX_VALUE;

        public final Set<String> busIdsToExport = new HashSet<>();

        public final Set<String> voltageLevelIdsToExport = new HashSet<>();

        public final Set<String> generatorIdsToExport = new HashSet<>();

        public final Set<String> loadsToExport = new HashSet<>();

    }

    public AmplNetworkWriter(Network network, DataSource dataSource, int faultNum, int actionNum,
                boolean append, StringToIntMapper<AmplSubset> mapper, AmplExportConfig config) {
        this.network = Objects.requireNonNull(network);
        this.faultNum = faultNum;
        this.actionNum = actionNum;
        this.dataSource = Objects.requireNonNull(dataSource);
        this.append = append;
        this.mapper = Objects.requireNonNull(mapper);
        this.config = Objects.requireNonNull(config);
        extensionMap = new HashMap<>();
    }

    public AmplNetworkWriter(Network network, DataSource dataSource, StringToIntMapper<AmplSubset> mapper,
                             AmplExportConfig config) {
        this(network, dataSource, 0, 0, false, mapper, config);
    }

    public AmplNetworkWriter(Network network, DataSource dataSource, AmplExportConfig config) {
        this(network, dataSource, 0, 0, false, AmplUtil.createMapper(network), config);
    }

    public static String getTableTitle(Network network, String tableName) {
        return tableName + " (" + network.getId() + "/" + network.getStateManager().getWorkingStateId() + ")";
    }

    private String getTableTitle(String tableName) {
        return getTableTitle(network, tableName);
    }

    private static String getThreeWindingsTransformerMiddleBusId(ThreeWindingsTransformer twt) {
        return twt.getId(); // same id as the transformer
    }

    private static String getThreeWindingsTransformerMiddleVoltageLevelId(ThreeWindingsTransformer twt) {
        return twt.getId(); // same id as the transformer
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

    private static String getDanglingLineMiddleBusId(DanglingLine dl) {
        return dl.getId(); // same id as the dangling line
    }

    private static String getDanglingLineMiddleVoltageLevelId(DanglingLine dl) {
        return dl.getId(); // same id as the dangling line
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
        Terminal t1 = tieLine.getTerminal1();
        Terminal t2 = tieLine.getTerminal2();
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

    private void writeSubstations() throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("_network_substations", "txt", append), StandardCharsets.UTF_8);
             TableFormatter formatter = new AmplDatTableFormatter(writer,
                                                                  getTableTitle("Substations"),
                                                                  AmplConstants.INVALID_FLOAT_VALUE,
                                                                  !append,
                                                                  AmplConstants.LOCALE,
                                                                  new Column("num"),
                                                                  new Column("unused1"),
                                                                  new Column("unused2"),
                                                                  new Column("nomV (KV)"),
                                                                  new Column("minV (pu)"),
                                                                  new Column("maxV (pu)"),
                                                                  new Column(FAULT),
                                                                  new Column(config.getActionType().getLabel()),
                                                                  new Column("country"),
                                                                  new Column("id"),
                                                                  new Column(DESCRIPTION))) {
            for (VoltageLevel vl : network.getVoltageLevels()) {
                int num = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vl.getId());
                double nomV = vl.getNominalV();
                double minV = vl.getLowVoltageLimit() / nomV;
                double maxV = vl.getHighVoltageLimit() / nomV;
                formatter.writeCell(num)
                         .writeCell("")
                         .writeCell(0)
                         .writeCell(nomV)
                         .writeCell(minV)
                         .writeCell(maxV)
                         .writeCell(faultNum)
                         .writeCell(actionNum)
                         .writeCell(vl.getSubstation().getCountry().toString())
                         .writeCell(vl.getId())
                         .writeCell(vl.getName());
                addExtensions(num, vl);
            }
            // voltage level associated to 3 windings transformers middle bus
            for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
                String vlId = getThreeWindingsTransformerMiddleVoltageLevelId(twt);
                int num = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vlId);
                Terminal t1 = twt.getLeg1().getTerminal();
                VoltageLevel vl1 = t1.getVoltageLevel();
                formatter.writeCell(num)
                         .writeCell("")
                         .writeCell(0)
                         .writeCell(vl1.getNominalV())
                         .writeCell(Float.NaN)
                         .writeCell(Float.NaN)
                         .writeCell(faultNum)
                         .writeCell(actionNum)
                         .writeCell(vl1.getSubstation().getCountry().toString())
                         .writeCell(vlId)
                         .writeCell("");
                addExtensions(num, twt);
            }
            // voltage level associated to dangling lines middle bus
            for (DanglingLine dl : network.getDanglingLines()) {
                String vlId = getDanglingLineMiddleVoltageLevelId(dl);
                int num = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vlId);
                VoltageLevel vl = dl.getTerminal().getVoltageLevel();
                double nomV = vl.getNominalV();
                double minV = vl.getLowVoltageLimit() / nomV;
                double maxV = vl.getHighVoltageLimit() / nomV;
                formatter.writeCell(num)
                         .writeCell("")
                         .writeCell(0)
                         .writeCell(nomV)
                         .writeCell(minV)
                         .writeCell(maxV)
                         .writeCell(faultNum)
                         .writeCell(actionNum)
                         .writeCell(vl.getSubstation().getCountry().toString())
                         .writeCell(dl.getId() + "_voltageLevel")
                         .writeCell("");
                addExtensions(num, dl);
            }
            if (config.isExportXNodes()) {
                for (Line l : network.getLines()) {
                    if (!l.isTieLine()) {
                        continue;
                    }
                    TieLine tieLine = (TieLine) l;
                    String vlId = AmplUtil.getXnodeVoltageLevelId(tieLine);
                    int num = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vlId);
                    formatter.writeCell(num)
                            .writeCell("")
                            .writeCell(0)
                            .writeCell(l.getTerminal1().getVoltageLevel().getNominalV())
                            .writeCell(Float.NaN)
                            .writeCell(Float.NaN)
                            .writeCell(faultNum)
                            .writeCell(actionNum)
                            .writeCell(XNODE_COUNTRY_NAME)
                            .writeCell(AmplUtil.getXnodeBusId(tieLine) + "_voltageLevel")
                            .writeCell("");
                    addExtensions(num, tieLine);
                }
            }
        }
    }

    private boolean isOnlyMainCc() {
        switch (config.getExportScope()) {
            case ONLY_MAIN_CC:
            case ONLY_MAIN_CC_AND_CONNECTABLE_GENERATORS_AND_SHUNTS:
            case ONLY_MAIN_CC_AND_CONNECTABLE_GENERATORS_AND_SHUNTS_AND_ALL_LOADS:
                return true;

            case ALL:
                return false;

            default:
                throw new AssertionError();
        }
    }

    private boolean connectedComponentToExport(int numCC) {
        return !(isOnlyMainCc() && numCC != ComponentConstants.MAIN_NUM);
    }

    private void writeBuses(AmplExportContext context) throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("_network_buses", "txt", append), StandardCharsets.UTF_8);
             TableFormatter formatter = new AmplDatTableFormatter(writer,
                                                                  getTableTitle("Buses"),
                                                                  AmplConstants.INVALID_FLOAT_VALUE,
                                                                  !append,
                                                                  AmplConstants.LOCALE,
                                                                  new Column("num"),
                                                                  new Column(SUBSTATION),
                                                                  new Column("cc"),
                                                                  new Column("v (pu)"),
                                                                  new Column("theta (rad)"),
                                                                  new Column("p (MW)"),
                                                                  new Column("q (MVar)"),
                                                                  new Column(FAULT),
                                                                  new Column(config.getActionType().getLabel()),
                                                                  new Column("id"))) {

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
                String id = b.getId();
                VoltageLevel vl = b.getVoltageLevel();
                context.busIdsToExport.add(id);
                int num = mapper.getInt(AmplSubset.BUS, id);
                int vlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vl.getId());
                double nomV = vl.getNominalV();
                double v = b.getV() / nomV;
                double theta = Math.toRadians(b.getAngle());
                formatter.writeCell(num)
                    .writeCell(vlNum)
                    .writeCell(ccNum)
                    .writeCell(v)
                    .writeCell(theta)
                    .writeCell(b.getP())
                    .writeCell(b.getQ())
                    .writeCell(faultNum)
                    .writeCell(actionNum)
                    .writeCell(id);
                addExtensions(num, b);
            }
        }
    }

    private <E> void addExtensions(int extendedNum, Extendable<E> extendable) {
        for (Extension<E> ext : extendable.getExtensions()) {
            List<AmplExtension> extList = extensionMap.computeIfAbsent(ext.getName(), k -> new ArrayList<AmplExtension>());
            extList.add(new AmplExtension(extendedNum, extendable, ext));
            extensionMap.put(ext.getName(), extList);
        }
    }

    private void exportExtensions() throws IOException {

        for (Entry<String, List<AmplExtension>> entry : extensionMap.entrySet()) {
            AmplExtensionWriter extWriter = AmplExtensionWriters.getWriter(entry.getKey());
            if (extWriter != null) {
                extWriter.write(entry.getValue(), network, mapper, dataSource, append, config);
            }
        }
    }

    private void writeThreeWindingsTransformerMiddleBuses(AmplExportContext context, TableFormatter formatter) throws IOException {
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            int middleCcNum = getThreeWindingsTransformerMiddleBusComponentNum(context, twt);

            if (connectedComponentToExport(middleCcNum)) {
                String middleBusId = getThreeWindingsTransformerMiddleBusId(twt);
                String middleVlId = getThreeWindingsTransformerMiddleVoltageLevelId(twt);
                context.busIdsToExport.add(middleBusId);
                int middleBusNum = mapper.getInt(AmplSubset.BUS, middleBusId);
                int middleVlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, middleVlId);
                formatter.writeCell(middleBusNum)
                    .writeCell(middleVlNum)
                    .writeCell(middleCcNum)
                    .writeCell(Float.NaN)
                    .writeCell(Double.NaN)
                    .writeCell(0.0)
                    .writeCell(0.0)
                    .writeCell(faultNum)
                    .writeCell(actionNum)
                    .writeCell(middleBusId);
            }
        }
    }

    private void writeDanglingLineMiddleBuses(AmplExportContext context, TableFormatter formatter) throws IOException {
        for (DanglingLine dl : network.getDanglingLines()) {
            Terminal t = dl.getTerminal();
            Bus b = AmplUtil.getBus(dl.getTerminal());

            int middleCcNum = getDanglingLineMiddleBusComponentNum(context, dl);
            if (connectedComponentToExport(middleCcNum)) {
                String middleBusId = getDanglingLineMiddleBusId(dl);
                String middleVlId = getDanglingLineMiddleVoltageLevelId(dl);
                context.busIdsToExport.add(middleBusId);
                int middleBusNum = mapper.getInt(AmplSubset.BUS, middleBusId);
                int middleVlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, middleVlId);
                SV sv = new SV(t.getP(), t.getQ(), b != null ? b.getV() : Double.NaN, b != null ? b.getAngle() : Double.NaN).otherSide(dl);
                double nomV = t.getVoltageLevel().getNominalV();
                double v = sv.getU() / nomV;
                double theta = Math.toRadians(sv.getA());
                formatter.writeCell(middleBusNum)
                    .writeCell(middleVlNum)
                    .writeCell(middleCcNum)
                    .writeCell(v)
                    .writeCell(theta)
                    .writeCell(0.0) // 0 MW injected at dangling line internal bus
                    .writeCell(0.0) // 0 MVar injected at dangling line internal bus
                    .writeCell(faultNum)
                    .writeCell(actionNum)
                    .writeCell(middleBusId);
            }
        }
    }

    private void writeTieLineMiddleBuses(AmplExportContext context, TableFormatter formatter) throws IOException {
        for (Line l : network.getLines()) {
            if (!l.isTieLine()) {
                continue;
            }
            TieLine tieLine = (TieLine) l;

            int xNodeCcNum = getTieLineMiddleBusComponentNum(context, tieLine);
            if (connectedComponentToExport(xNodeCcNum)) {
                String xNodeBusId = AmplUtil.getXnodeBusId(tieLine);
                int xNodeBusNum = mapper.getInt(AmplSubset.BUS, xNodeBusId);
                int xNodeVlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, AmplUtil.getXnodeVoltageLevelId(tieLine));
                formatter.writeCell(xNodeBusNum)
                    .writeCell(xNodeVlNum)
                    .writeCell(xNodeCcNum)
                    .writeCell(Float.NaN)
                    .writeCell(Double.NaN)
                    .writeCell(0.0)
                    .writeCell(0.0)
                    .writeCell(faultNum)
                    .writeCell(actionNum)
                    .writeCell(xNodeBusId);
            }
        }
    }

    private static double getPermanentLimit(CurrentLimits limits) {
        if (limits != null) {
            return limits.getPermanentLimit();
        }
        return Double.NaN;
    }

    private static boolean isBusExported(AmplExportContext context, String busId) {
        return busId != null && context.busIdsToExport.contains(busId);
    }

    private void writeBranches(AmplExportContext context) throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("_network_branches", "txt", append), StandardCharsets.UTF_8);
             TableFormatter formatter = new AmplDatTableFormatter(writer,
                                                                  getTableTitle("Branches"),
                                                                  AmplConstants.INVALID_FLOAT_VALUE,
                                                                  !append,
                                                                  AmplConstants.LOCALE,
                                                                  new Column("num"),
                                                                  new Column("bus1"),
                                                                  new Column("bus2"),
                                                                  new Column("3wt num"),
                                                                  new Column("sub.1"),
                                                                  new Column("sub.2"),
                                                                  new Column("r (pu)"),
                                                                  new Column("x (pu)"),
                                                                  new Column("g1 (pu)"),
                                                                  new Column("g2 (pu)"),
                                                                  new Column("b1 (pu)"),
                                                                  new Column("b2 (pu)"),
                                                                  new Column("cst ratio (pu)"),
                                                                  new Column("ratio tc"),
                                                                  new Column("phase tc"),
                                                                  new Column("p1 (MW)"),
                                                                  new Column("p2 (MW)"),
                                                                  new Column("q1 (MVar)"),
                                                                  new Column("q2 (MVar)"),
                                                                  new Column("patl1 (A)"),
                                                                  new Column("patl2 (A)"),
                                                                  new Column("merged"),
                                                                  new Column(FAULT),
                                                                  new Column(config.getActionType().getLabel()),
                                                                  new Column("id"),
                                                                  new Column(DESCRIPTION))) {

            writeLines(context, formatter);

            writeTwoWindingsTransformers(context, formatter);

            writeThreeWindingsTransformers(context, formatter);

            writeDanglingLines(context, formatter);
        }
    }

    private void writeLines(AmplExportContext context, TableFormatter formatter) throws IOException {
        for (Line l : network.getLines()) {
            Terminal t1 = l.getTerminal1();
            Terminal t2 = l.getTerminal2();
            Bus bus1 = AmplUtil.getBus(t1);
            Bus bus2 = AmplUtil.getBus(t2);
            if (bus1 != null && bus2 != null && bus1 == bus2) {
                LOGGER.warn("Skipping line '{}' connected to the same bus at both sides", l.getId());
                continue;
            }
            String bus1Id = getBusId(bus1);
            int bus1Num = getBusNum(bus1);
            String bus2Id = getBusId(bus2);
            int bus2Num = getBusNum(bus2);
            if (isOnlyMainCc() && !(isBusExported(context, bus1Id) || isBusExported(context, bus2Id))) {
                continue;
            }
            VoltageLevel vl1 = t1.getVoltageLevel();
            VoltageLevel vl2 = t2.getVoltageLevel();
            context.voltageLevelIdsToExport.add(vl1.getId());
            context.voltageLevelIdsToExport.add(vl2.getId());
            String id = l.getId();
            int num = mapper.getInt(AmplSubset.BRANCH, id);
            int vl1Num = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vl1.getId());
            int vl2Num = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vl2.getId());

            double vb = vl1.getNominalV();
            double zb = vb * vb / AmplConstants.SB;

            boolean merged = !config.isExportXNodes() && l.isTieLine();
            if (config.isExportXNodes() && l.isTieLine()) {
                TieLine tl = (TieLine) l;
                String half1Id = tl.getHalf1().getId();
                String half2Id = tl.getHalf2().getId();
                int half1Num = mapper.getInt(AmplSubset.BRANCH, half1Id);
                int half2Num = mapper.getInt(AmplSubset.BRANCH, half2Id);
                String xNodeBusId = AmplUtil.getXnodeBusId(tl);
                String xnodeVoltageLevelId = AmplUtil.getXnodeVoltageLevelId(tl);
                int xNodeBusNum = mapper.getInt(AmplSubset.BUS, xNodeBusId);
                int xNodeVoltageLevelNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, xnodeVoltageLevelId);

                formatter.writeCell(half1Num)
                    .writeCell(bus1Num)
                    .writeCell(xNodeBusNum)
                    .writeCell(-1)
                    .writeCell(vl1Num)
                    .writeCell(xNodeVoltageLevelNum)
                    .writeCell(tl.getHalf1().getR() / zb)
                    .writeCell(tl.getHalf1().getX() / zb)
                    .writeCell(tl.getHalf1().getG1() * zb)
                    .writeCell(tl.getHalf1().getG2() * zb)
                    .writeCell(tl.getHalf1().getB1() * zb)
                    .writeCell(tl.getHalf1().getB2() * zb)
                    .writeCell(1f) // constant ratio
                    .writeCell(-1) // no ratio tap changer
                    .writeCell(-1) // no phase tap changer
                    .writeCell(t1.getP())
                    .writeCell(-tl.getHalf1().getXnodeP()) // xnode node flow side 1
                    .writeCell(t1.getQ())
                    .writeCell(-tl.getHalf1().getXnodeQ()) // xnode node flow side 1
                    .writeCell(getPermanentLimit(l.getCurrentLimits1()))
                    .writeCell(Float.NaN)
                    .writeCell(merged)
                    .writeCell(faultNum)
                    .writeCell(actionNum)
                    .writeCell(half1Id)
                    .writeCell(tl.getHalf1().getName());
                formatter.writeCell(half2Num)
                    .writeCell(xNodeBusNum)
                    .writeCell(bus2Num)
                    .writeCell(-1)
                    .writeCell(xNodeVoltageLevelNum)
                    .writeCell(vl2Num)
                    .writeCell(tl.getHalf2().getR() / zb)
                    .writeCell(tl.getHalf2().getX() / zb)
                    .writeCell(tl.getHalf2().getG1() * zb)
                    .writeCell(tl.getHalf2().getG2() * zb)
                    .writeCell(tl.getHalf2().getB1() * zb)
                    .writeCell(tl.getHalf2().getB2() * zb)
                    .writeCell(1f) // constant ratio
                    .writeCell(-1) // no ratio tap changer
                    .writeCell(-1) // no phase tap changer
                    .writeCell(-tl.getHalf2().getXnodeP()) // xnode node flow side 2
                    .writeCell(t2.getP())
                    .writeCell(-tl.getHalf2().getXnodeQ()) // xnode node flow side 2
                    .writeCell(t2.getQ())
                    .writeCell(Float.NaN)
                    .writeCell(getPermanentLimit(l.getCurrentLimits2()))
                    .writeCell(merged)
                    .writeCell(faultNum)
                    .writeCell(actionNum)
                    .writeCell(half2Id)
                    .writeCell(tl.getHalf2().getName());
            } else {
                formatter.writeCell(num)
                    .writeCell(bus1Num)
                    .writeCell(bus2Num)
                    .writeCell(-1)
                    .writeCell(vl1Num)
                    .writeCell(vl2Num)
                    .writeCell(l.getR() / zb)
                    .writeCell(l.getX() / zb)
                    .writeCell(l.getG1() * zb)
                    .writeCell(l.getG2() * zb)
                    .writeCell(l.getB1() * zb)
                    .writeCell(l.getB2() * zb)
                    .writeCell(1f) // constant ratio
                    .writeCell(-1) // no ratio tap changer
                    .writeCell(-1) // no phase tap changer
                    .writeCell(t1.getP())
                    .writeCell(t2.getP())
                    .writeCell(t1.getQ())
                    .writeCell(t2.getQ())
                    .writeCell(getPermanentLimit(l.getCurrentLimits1()))
                    .writeCell(getPermanentLimit(l.getCurrentLimits2()))
                    .writeCell(merged)
                    .writeCell(faultNum)
                    .writeCell(actionNum)
                    .writeCell(id)
                    .writeCell(l.getName());
            }
            addExtensions(num, l);
        }
    }

    private void writeTwoWindingsTransformers(AmplExportContext context, TableFormatter formatter) throws IOException {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            Terminal t1 = twt.getTerminal1();
            Terminal t2 = twt.getTerminal2();
            Bus bus1 = AmplUtil.getBus(t1);
            Bus bus2 = AmplUtil.getBus(t2);
            if (bus1 != null && bus2 != null && bus1 == bus2) {
                LOGGER.warn("Skipping transformer '{}' connected to the same bus at both sides", twt.getId());
                continue;
            }
            String bus1Id = getBusId(bus1);
            int bus1Num = getBusNum(bus1);
            String bus2Id = getBusId(bus2);
            int bus2Num = getBusNum(bus2);
            if (isOnlyMainCc() && !(isBusExported(context, bus1Id) || isBusExported(context, bus2Id))) {
                continue;
            }
            VoltageLevel vl1 = t1.getVoltageLevel();
            VoltageLevel vl2 = t2.getVoltageLevel();
            context.voltageLevelIdsToExport.add(vl1.getId());
            context.voltageLevelIdsToExport.add(vl2.getId());
            String id = twt.getId();
            int num = mapper.getInt(AmplSubset.BRANCH, id);
            int vl1Num = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vl1.getId());
            int vl2Num = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vl2.getId());
            double vb1 = vl1.getNominalV();
            double vb2 = vl2.getNominalV();
            double zb2 = vb2 * vb2 / AmplConstants.SB;
            double r = twt.getR() / zb2;
            double x = twt.getX() / zb2;
            double g1;
            double g2;
            double b1;
            double b2;
            if (config.isSpecificCompatibility()) {
                g1 = twt.getG() * zb2 / 2;
                g2 = g1;
                b1 = twt.getB() * zb2 / 2;
                b2 = b1;
            } else {
                g1 = twt.getG() * zb2;
                g2 = 0;
                b1 = twt.getB() * zb2;
                b2 = 0;
            }

            double ratedU1 = twt.getRatedU1();
            double ratedU2 = twt.getRatedU2();
            double ratio = ratedU2 / vb2 / (ratedU1 / vb1);
            RatioTapChanger rtc = twt.getRatioTapChanger();
            PhaseTapChanger ptc = twt.getPhaseTapChanger();
            int rtcNum = rtc != null ? mapper.getInt(AmplSubset.RATIO_TAP_CHANGER, twt.getId()) : -1;
            int ptcNum = ptc != null ? mapper.getInt(AmplSubset.PHASE_TAP_CHANGER, twt.getId()) : -1;
            formatter.writeCell(num)
                .writeCell(bus1Num)
                .writeCell(bus2Num)
                .writeCell(-1)
                .writeCell(vl1Num)
                .writeCell(vl2Num)
                .writeCell(r)
                .writeCell(x)
                .writeCell(g1)
                .writeCell(g2)
                .writeCell(b1)
                .writeCell(b2)
                .writeCell(ratio)
                .writeCell(rtcNum)
                .writeCell(ptcNum)
                .writeCell(t1.getP())
                .writeCell(t2.getP())
                .writeCell(t1.getQ())
                .writeCell(t2.getQ())
                .writeCell(getPermanentLimit(twt.getCurrentLimits1()))
                .writeCell(getPermanentLimit(twt.getCurrentLimits2()))
                .writeCell(false) // TODO to update
                .writeCell(faultNum)
                .writeCell(actionNum)
                .writeCell(id)
                .writeCell(twt.getName());
            addExtensions(num, twt);
        }
    }

    private void writeThreeWindingsTransformers(AmplExportContext context, TableFormatter formatter) throws IOException {
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
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
            int num3wt = mapper.getInt(AmplSubset.THREE_WINDINGS_TRANSFO, twt.getId());
            int num1 = mapper.getInt(AmplSubset.BRANCH, id1);
            int num2 = mapper.getInt(AmplSubset.BRANCH, id2);
            int num3 = mapper.getInt(AmplSubset.BRANCH, id3);
            int vl1Num = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vl1.getId());
            int vl2Num = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vl2.getId());
            int vl3Num = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vl3.getId());
            String bus1Id = getBusId(bus1);
            int bus1Num = getBusNum(bus1);
            String bus2Id = getBusId(bus2);
            int bus2Num = getBusNum(bus2);
            String bus3Id = getBusId(bus3);
            int bus3Num = getBusNum(bus3);
            double vb1 = vl1.getNominalV();
            double vb2 = vl2.getNominalV();
            double vb3 = vl3.getNominalV();
            double zb1 = vb1 * vb1 / AmplConstants.SB;
            double zb2 = vb2 * vb2 / AmplConstants.SB;
            double zb3 = vb3 * vb3 / AmplConstants.SB;
            double r1 = twt.getLeg1().getR() / zb1;
            double x1 = twt.getLeg1().getX() / zb1;
            double g1 = twt.getLeg1().getG() * zb1;
            double b1 = twt.getLeg1().getB() * zb1;
            double r2 = twt.getLeg2().getR() / zb2;
            double x2 = twt.getLeg2().getX() / zb2;
            double r3 = twt.getLeg3().getR() / zb3;
            double x3 = twt.getLeg3().getX() / zb3;
            double ratedU1 = twt.getLeg1().getRatedU();
            double ratedU2 = twt.getLeg2().getRatedU();
            double ratedU3 = twt.getLeg3().getRatedU();
            double ratio2 =  ratedU1 / ratedU2;
            double ratio3 =  ratedU1 / ratedU3;
            RatioTapChanger rtc2 = twt.getLeg2().getRatioTapChanger();
            RatioTapChanger rtc3 = twt.getLeg2().getRatioTapChanger();
            int rtc2Num = rtc2 != null ? mapper.getInt(AmplSubset.RATIO_TAP_CHANGER, id2) : -1;
            int rtc3Num = rtc3 != null ? mapper.getInt(AmplSubset.RATIO_TAP_CHANGER, id3) : -1;

            int middleVlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, getThreeWindingsTransformerMiddleVoltageLevelId(twt));
            String middleBusId = getThreeWindingsTransformerMiddleBusId(twt);
            int middleBusNum = mapper.getInt(AmplSubset.BUS, middleBusId);

            if (!isOnlyMainCc() || isBusExported(context, middleBusId) || isBusExported(context, bus1Id)) {
                formatter.writeCell(num1)
                    .writeCell(middleBusNum)
                    .writeCell(bus1Num)
                    .writeCell(num3wt)
                    .writeCell(middleVlNum)
                    .writeCell(vl1Num)
                    .writeCell(r1)
                    .writeCell(x1)
                    .writeCell(g1)
                    .writeCell(0.0)
                    .writeCell(b1)
                    .writeCell(0.0)
                    .writeCell(1f) // ratio is one at primary leg
                    .writeCell(-1)
                    .writeCell(-1)
                    .writeCell(Double.NaN)
                    .writeCell(t1.getP())
                    .writeCell(Double.NaN)
                    .writeCell(t1.getQ())
                    .writeCell(Double.NaN)
                    .writeCell(getPermanentLimit(twt.getLeg1().getCurrentLimits()))
                    .writeCell(false)
                    .writeCell(faultNum)
                    .writeCell(actionNum)
                    .writeCell(id1)
                    .writeCell("");
                addExtensions(num1, twt);
            }
            if (!isOnlyMainCc() || isBusExported(context, middleBusId) || isBusExported(context, bus2Id)) {
                formatter.writeCell(num2)
                    .writeCell(bus2Num)
                    .writeCell(middleBusNum)
                    .writeCell(num3wt)
                    .writeCell(vl2Num)
                    .writeCell(middleVlNum)
                    .writeCell(r2)
                    .writeCell(x2)
                    .writeCell(0.0)
                    .writeCell(0.0)
                    .writeCell(0.0)
                    .writeCell(0.0)
                    .writeCell(ratio2)
                    .writeCell(rtc2Num)
                    .writeCell(-1)
                    .writeCell(t2.getP())
                    .writeCell(Double.NaN)
                    .writeCell(t2.getQ())
                    .writeCell(Double.NaN)
                    .writeCell(getPermanentLimit(twt.getLeg2().getCurrentLimits()))
                    .writeCell(Double.NaN)
                    .writeCell(false)
                    .writeCell(faultNum)
                    .writeCell(actionNum)
                    .writeCell(id2)
                    .writeCell("");
                addExtensions(num2, twt);
            }
            if (!isOnlyMainCc() || isBusExported(context, middleBusId) || isBusExported(context, bus3Id)) {
                formatter.writeCell(num3)
                    .writeCell(bus3Num)
                    .writeCell(middleBusNum)
                    .writeCell(num3wt)
                    .writeCell(vl3Num)
                    .writeCell(middleVlNum)
                    .writeCell(r3)
                    .writeCell(x3)
                    .writeCell(0.0)
                    .writeCell(0.0)
                    .writeCell(0.0)
                    .writeCell(0.0)
                    .writeCell(ratio3)
                    .writeCell(rtc3Num)
                    .writeCell(-1)
                    .writeCell(t3.getP())
                    .writeCell(Double.NaN)
                    .writeCell(t3.getQ())
                    .writeCell(Double.NaN)
                    .writeCell(getPermanentLimit(twt.getLeg3().getCurrentLimits()))
                    .writeCell(Double.NaN)
                    .writeCell(false)
                    .writeCell(faultNum)
                    .writeCell(actionNum)
                    .writeCell(id3)
                    .writeCell("");
                addExtensions(num3, twt);
            }
        }
    }

    private void writeDanglingLines(AmplExportContext context, TableFormatter formatter) throws IOException {
        for (DanglingLine dl : network.getDanglingLines()) {
            Terminal t = dl.getTerminal();
            Bus bus1 = AmplUtil.getBus(t);
            String bus1Id = getBusId(bus1);
            int bus1Num = getBusNum(bus1);
            String middleBusId = getDanglingLineMiddleBusId(dl);
            int middleBusNum = mapper.getInt(AmplSubset.BUS, middleBusId);
            if (isOnlyMainCc() && !(isBusExported(context, bus1Id) || isBusExported(context, middleBusId))) {
                continue;
            }
            VoltageLevel vl = t.getVoltageLevel();
            String middleVlId = getDanglingLineMiddleVoltageLevelId(dl);
            context.voltageLevelIdsToExport.add(vl.getId());
            context.voltageLevelIdsToExport.add(middleVlId);
            String id = dl.getId();
            int num = mapper.getInt(AmplSubset.BRANCH, id);
            int vl1Num = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vl.getId());
            int middleVlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, middleVlId);
            double vb = vl.getNominalV();
            double zb = vb * vb / AmplConstants.SB;
            double p1 = t.getP();
            double q1 = t.getQ();
            SV sv = new SV(p1, q1, bus1 != null ? bus1.getV() : Double.NaN, bus1 != null ? bus1.getAngle() : Double.NaN).otherSide(dl);
            double p2 = sv.getP();
            double q2 = sv.getQ();
            double patl = getPermanentLimit(dl.getCurrentLimits());
            formatter.writeCell(num)
                .writeCell(bus1Num)
                .writeCell(middleBusNum)
                .writeCell(-1)
                .writeCell(vl1Num)
                .writeCell(middleVlNum)
                .writeCell(dl.getR() / zb)
                .writeCell(dl.getX() / zb)
                .writeCell(dl.getG() / 2 * zb)
                .writeCell(dl.getG() / 2 * zb)
                .writeCell(dl.getB() / 2 * zb)
                .writeCell(dl.getB() / 2 * zb)
                .writeCell(1f)  // constant ratio
                .writeCell(-1) // no ratio tap changer
                .writeCell(-1) // no phase tap changer
                .writeCell(p1)
                .writeCell(p2)
                .writeCell(q1)
                .writeCell(q2)
                .writeCell(patl)
                .writeCell(patl)
                .writeCell(false)
                .writeCell(faultNum)
                .writeCell(actionNum)
                .writeCell(id)
                .writeCell(dl.getName());
            addExtensions(num, dl);
        }
    }

    private String getBusId(Bus bus) {
        return bus == null ? null : bus.getId();
    }

    private int getBusNum(Bus bus) {
        return bus == null ? -1 : mapper.getInt(AmplSubset.BUS, bus.getId());
    }

    private void writeTapChangerTable() throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("_network_tct", "txt", append), StandardCharsets.UTF_8);
             TableFormatter formatter = new AmplDatTableFormatter(writer,
                                                                  getTableTitle("Tap changer table"),
                                                                  AmplConstants.INVALID_FLOAT_VALUE,
                                                                  !append,
                                                                  AmplConstants.LOCALE,
                                                                  new Column("num"),
                                                                  new Column("tap"),
                                                                  new Column("var ratio"),
                                                                  new Column("x (pu)"),
                                                                  new Column("angle (rad)"),
                                                                  new Column(FAULT),
                                                                  new Column(config.getActionType().getLabel()))) {

            writeTwoWindingsTransformerTapChangerTable(formatter);

            writeThreeWindingsTransformerTapChangerTable(formatter);
        }
    }

    private void writeTwoWindingsTransformerTapChangerTable(TableFormatter formatter) throws IOException {
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            Terminal t2 = twt.getTerminal2();
            double vb2 = t2.getVoltageLevel().getNominalV();
            double zb2 = vb2 * vb2 / AmplConstants.SB;

            RatioTapChanger rtc = twt.getRatioTapChanger();
            if (rtc != null) {
                String id = twt.getId() + "_ratio_table";
                writeRatioTapChanger(formatter, id, zb2, twt.getX(), rtc);
            }

            PhaseTapChanger ptc = twt.getPhaseTapChanger();
            if (ptc != null) {
                String id = twt.getId() + "_phase_table";
                writePhaseTapChanger(formatter, id, zb2, twt.getX(), ptc);

            }
        }
    }

    private void writeThreeWindingsTransformerTapChangerTable(TableFormatter formatter) throws IOException {
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            RatioTapChanger rtc2 = twt.getLeg2().getRatioTapChanger();
            if (rtc2 != null) {
                String id = twt.getId() + "_leg2_ratio_table";

                Terminal t2 = twt.getLeg2().getTerminal();
                double vb2 = t2.getVoltageLevel().getNominalV();
                double zb2 = vb2 * vb2 / AmplConstants.SB;

                writeRatioTapChanger(formatter, id, zb2, twt.getLeg2().getX(), rtc2);
            }

            RatioTapChanger rtc3 = twt.getLeg3().getRatioTapChanger();
            if (rtc3 != null) {
                String id = twt.getId() + "_leg3_ratio_table";

                Terminal t3 = twt.getLeg3().getTerminal();
                double vb3 = t3.getVoltageLevel().getNominalV();
                double zb3 = vb3 * vb3 / AmplConstants.SB;

                writeRatioTapChanger(formatter, id, zb3, twt.getLeg3().getX(), rtc3);
            }
        }
    }

    private void writeRatioTapChanger(TableFormatter formatter, String id, double zb2, double reactance, RatioTapChanger rtc) throws IOException {
        int num = mapper.getInt(AmplSubset.TAP_CHANGER_TABLE, id);

        for (int position = rtc.getLowTapPosition(); position <= rtc.getHighTapPosition(); position++) {
            RatioTapChangerStep step = rtc.getStep(position);
            double x = reactance * (1 + step.getX() / 100) / zb2;
            formatter.writeCell(num)
                .writeCell(position - rtc.getLowTapPosition() + 1)
                .writeCell(step.getRho())
                .writeCell(x)
                .writeCell(0.0)
                .writeCell(faultNum)
                .writeCell(actionNum);
        }
    }

    private void writePhaseTapChanger(TableFormatter formatter, String id, double zb2, double reactance, PhaseTapChanger ptc) throws IOException {
        int num = mapper.getInt(AmplSubset.TAP_CHANGER_TABLE, id);

        for (int position = ptc.getLowTapPosition(); position <= ptc.getHighTapPosition(); position++) {
            PhaseTapChangerStep step = ptc.getStep(position);
            double x = reactance * (1 + step.getX() / 100) / zb2;
            formatter.writeCell(num)
                .writeCell(position - ptc.getLowTapPosition() + 1)
                .writeCell(step.getRho())
                .writeCell(x)
                .writeCell(Math.toRadians(step.getAlpha()))
                .writeCell(faultNum)
                .writeCell(actionNum);
        }
    }

    private void writeRatioTapChanger(TableFormatter formatter, String rtcId,
                                      RatioTapChanger rtc, String tcsId) throws IOException {
        int rtcNum = mapper.getInt(AmplSubset.RATIO_TAP_CHANGER, rtcId);
        int tcsNum = mapper.getInt(AmplSubset.TAP_CHANGER_TABLE, tcsId);
        formatter.writeCell(rtcNum)
             .writeCell(rtc.getTapPosition() - rtc.getLowTapPosition() + 1)
             .writeCell(tcsNum)
             .writeCell(rtc.hasLoadTapChangingCapabilities() && rtc.isRegulating());
        if (config.isExportRatioTapChangerVoltageTarget()) {
            formatter.writeCell(rtc.getTargetV());
        }
        formatter.writeCell(faultNum)
                .writeCell(actionNum)
                .writeCell(rtcId);
    }

    private void writeRatioTapChangers() throws IOException {
        List<Column> columns = new ArrayList<>(8);
        columns.add(new Column("num"));
        columns.add(new Column("tap"));
        columns.add(new Column("table"));
        columns.add(new Column("onLoad"));
        if (config.isExportRatioTapChangerVoltageTarget()) {
            columns.add(new Column("targetV"));
        }
        columns.add(new Column(FAULT));
        columns.add(new Column(config.getActionType().getLabel()));
        columns.add(new Column("id"));
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("_network_rtc", "txt", append), StandardCharsets.UTF_8);
             TableFormatter formatter = new AmplDatTableFormatter(writer,
                                                                  getTableTitle("Ratio tap changers"),
                                                                  AmplConstants.INVALID_FLOAT_VALUE,
                                                                  !append,
                                                                  AmplConstants.LOCALE,
                                                                  columns.toArray(new Column[columns.size()]))) {
            for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
                RatioTapChanger rtc = twt.getRatioTapChanger();
                if (rtc != null) {
                    String rtcId = twt.getId();
                    String tcsId = twt.getId() + "_ratio_table";
                    writeRatioTapChanger(formatter, rtcId, rtc, tcsId);
                }
            }
            for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
                RatioTapChanger rtc2 = twt.getLeg2().getRatioTapChanger();
                if (rtc2 != null) {
                    String rtc2Id = twt.getId() + AmplConstants.LEG2_SUFFIX;
                    String tcs2Id = twt.getId() + "_leg2_ratio_table";
                    writeRatioTapChanger(formatter, rtc2Id, rtc2, tcs2Id);
                }
                RatioTapChanger rtc3 = twt.getLeg3().getRatioTapChanger();
                if (rtc3 != null) {
                    String rtc3Id = twt.getId() + AmplConstants.LEG3_SUFFIX;
                    String tcs3Id = twt.getId() + "_leg3_ratio_table";
                    writeRatioTapChanger(formatter, rtc3Id, rtc3, tcs3Id);
                }
            }
        }
    }

    private void writePhaseTapChangers() throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("_network_ptc", "txt", append), StandardCharsets.UTF_8);
             TableFormatter formatter = new AmplDatTableFormatter(writer,
                                                                  getTableTitle("Phase tap changers"),
                                                                  AmplConstants.INVALID_FLOAT_VALUE,
                                                                  !append,
                                                                  AmplConstants.LOCALE,
                                                                  new Column("num"),
                                                                  new Column("tap"),
                                                                  new Column("table"),
                                                                  new Column(FAULT),
                                                                  new Column(config.getActionType().getLabel()),
                                                                  new Column("id"))) {
            for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
                PhaseTapChanger ptc = twt.getPhaseTapChanger();
                if (ptc != null) {
                    String ptcId = twt.getId();
                    int num = mapper.getInt(AmplSubset.PHASE_TAP_CHANGER, ptcId);
                    String tcsId = twt.getId() + "_phase_table";
                    int tcsNum = mapper.getInt(AmplSubset.TAP_CHANGER_TABLE, tcsId);
                    formatter.writeCell(num)
                             .writeCell(ptc.getTapPosition() - ptc.getLowTapPosition() + 1)
                             .writeCell(tcsNum)
                             .writeCell(faultNum)
                             .writeCell(actionNum)
                             .writeCell(ptcId);
                }
            }
        }
    }

    private boolean exportLoad(AmplExportContext context, String busId) {
        switch (config.getExportScope()) {
            case ALL:
            case ONLY_MAIN_CC_AND_CONNECTABLE_GENERATORS_AND_SHUNTS_AND_ALL_LOADS:
                return true;
            case ONLY_MAIN_CC:
            case ONLY_MAIN_CC_AND_CONNECTABLE_GENERATORS_AND_SHUNTS:
                return isBusExported(context, busId);
            default:
                throw new AssertionError();
        }
    }

    private void writeLoads(AmplExportContext context) throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("_network_loads", "txt", append), StandardCharsets.UTF_8);
             TableFormatter formatter = new AmplDatTableFormatter(writer,
                                                                  getTableTitle("Loads"),
                                                                  AmplConstants.INVALID_FLOAT_VALUE,
                                                                  !append,
                                                                  AmplConstants.LOCALE,
                                                                  new Column("num"),
                                                                  new Column("bus"),
                                                                  new Column(SUBSTATION),
                                                                  new Column("p0 (MW)"),
                                                                  new Column("q0 (MVar)"),
                                                                  new Column(FAULT),
                                                                  new Column(config.getActionType().getLabel()),
                                                                  new Column("id"),
                                                                  new Column(DESCRIPTION),
                                                                  new Column("p (MW)"),
                                                                  new Column("q (MVar)"))) {
            List<String> skipped = new ArrayList<>();
            for (Load l : network.getLoads()) {
                Terminal t = l.getTerminal();
                Bus bus = AmplUtil.getBus(t);
                String busId = null;
                int busNum = -1;
                if (bus != null) {
                    busId = bus.getId();
                    busNum = mapper.getInt(AmplSubset.BUS, bus.getId());
                }
                if (!exportLoad(context, busId)) {
                    skipped.add(l.getId());
                    continue;
                }
                String id = l.getId();
                context.loadsToExport.add(id);
                int num = mapper.getInt(AmplSubset.LOAD, id);
                int vlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, t.getVoltageLevel().getId());
                formatter.writeCell(num)
                         .writeCell(busNum)
                         .writeCell(vlNum)
                         .writeCell(l.getP0())
                         .writeCell(l.getQ0())
                         .writeCell(faultNum)
                         .writeCell(actionNum)
                         .writeCell(id)
                         .writeCell(l.getName())
                         .writeCell(t.getP())
                         .writeCell(t.getQ());
                addExtensions(num, l);
            }
            for (DanglingLine dl : network.getDanglingLines()) {
                String middleBusId = getDanglingLineMiddleBusId(dl);
                String id = dl.getId();
                int num = mapper.getInt(AmplSubset.LOAD, id);
                int busNum = mapper.getInt(AmplSubset.BUS, middleBusId);
                if (!exportLoad(context, middleBusId)) {
                    skipped.add(dl.getId());
                    continue;
                }
                String middleVlId = getDanglingLineMiddleVoltageLevelId(dl);
                int vlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, middleVlId);
                formatter.writeCell(num)
                         .writeCell(busNum)
                         .writeCell(vlNum)
                         .writeCell(dl.getP0())
                         .writeCell(dl.getQ0())
                         .writeCell(faultNum)
                         .writeCell(actionNum)
                         .writeCell(dl.getId() + "_load")
                         .writeCell("")
                         .writeCell(dl.getTerminal().getP())
                         .writeCell(dl.getTerminal().getQ());
            }
            if (!skipped.isEmpty()) {
                LOGGER.trace("Skip loads {} because not connected and not connectable", skipped);
            }
        }
    }

    private boolean exportGeneratorOrShunt(AmplExportContext context, String busId, String conBusId) {
        switch (config.getExportScope()) {
            case ALL:
                return true;
            case ONLY_MAIN_CC:
                return isBusExported(context, busId);
            case ONLY_MAIN_CC_AND_CONNECTABLE_GENERATORS_AND_SHUNTS:
            case ONLY_MAIN_CC_AND_CONNECTABLE_GENERATORS_AND_SHUNTS_AND_ALL_LOADS:
                return isBusExported(context, conBusId);
            default:
                throw new AssertionError();
        }
    }

    private void writeShunts(AmplExportContext context) throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("_network_shunts", "txt", append), StandardCharsets.UTF_8);
             TableFormatter formatter = new AmplDatTableFormatter(writer,
                                                                  getTableTitle("Shunts"),
                                                                  AmplConstants.INVALID_FLOAT_VALUE,
                                                                  !append,
                                                                  AmplConstants.LOCALE,
                                                                  new Column("num"),
                                                                  new Column("bus"),
                                                                  new Column(CON_BUS),
                                                                  new Column(SUBSTATION),
                                                                  new Column("minB (pu)"),
                                                                  new Column("maxB (pu)"),
                                                                  new Column("inter. points"),
                                                                  new Column("b (pu)"),
                                                                  new Column(FAULT),
                                                                  new Column(config.getActionType().getLabel()),
                                                                  new Column("id"),
                                                                  new Column(DESCRIPTION),
                                                                  new Column(ACTIVE_POWER),
                                                                  new Column(REACTIVE_POWER),
                                                                  new Column("sections count"))) {
            List<String> skipped = new ArrayList<>();
            for (ShuntCompensator sc : network.getShuntCompensators()) {
                Terminal t = sc.getTerminal();
                Bus bus = AmplUtil.getBus(t);
                String busId = null;
                int busNum = -1;
                if (bus != null) {
                    busId = bus.getId();
                    busNum = mapper.getInt(AmplSubset.BUS, bus.getId());
                }
                int conBusNum = -1;
                // take connectable bus if exists
                String conBusId = null;
                Bus conBus = AmplUtil.getConnectableBus(t);
                if (conBus != null) {
                    conBusId = conBus.getId();
                    conBusNum = mapper.getInt(AmplSubset.BUS, conBus.getId());
                }
                if (!exportGeneratorOrShunt(context, busId, conBusId)) {
                    skipped.add(sc.getId());
                    continue;
                }
                String id = sc.getId();
                int num = mapper.getInt(AmplSubset.SHUNT, id);
                int vlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, t.getVoltageLevel().getId());
                double vb = t.getVoltageLevel().getNominalV();
                double zb = vb * vb / AmplConstants.SB;
                double b1 = 0;
                double b2 = sc.getMaximumB() * zb;
                double minB = Math.min(b1, b2);
                double maxB = Math.max(b1, b2);
                double b = sc.getCurrentB() * zb;
                int points = sc.getMaximumSectionCount() < 1 ? 0 : sc.getMaximumSectionCount() - 1;
                formatter.writeCell(num)
                         .writeCell(busNum)
                         .writeCell(conBusNum != -1 ? conBusNum : busNum)
                         .writeCell(vlNum)
                         .writeCell(minB)
                         .writeCell(maxB)
                         .writeCell(points)
                         .writeCell(b)
                         .writeCell(faultNum)
                         .writeCell(actionNum)
                         .writeCell(id)
                         .writeCell(sc.getName())
                         .writeCell(t.getP())
                         .writeCell(t.getQ())
                         .writeCell(sc.getCurrentSectionCount());
                addExtensions(num, sc);
            }
            if (!skipped.isEmpty()) {
                LOGGER.trace("Skip shunts {} because not connected and not connectable", skipped);
            }
        }
    }

    private void writeStaticVarCompensators() throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("_network_static_var_compensators", "txt", append), StandardCharsets.UTF_8);
             TableFormatter formatter = new AmplDatTableFormatter(writer,
                                                                  getTableTitle("Static VAR compensators"),
                                                                  AmplConstants.INVALID_FLOAT_VALUE,
                                                                  !append,
                                                                  AmplConstants.LOCALE,
                                                                  new Column("num"),
                                                                  new Column("bus"),
                                                                  new Column(CON_BUS),
                                                                  new Column(SUBSTATION),
                                                                  new Column("minB (pu)"),
                                                                  new Column("maxB (pu)"),
                                                                  new Column(V_REGUL),
                                                                  new Column(TARGET_V),
                                                                  new Column(FAULT),
                                                                  new Column(config.getActionType().getLabel()),
                                                                  new Column("id"),
                                                                  new Column(DESCRIPTION),
                                                                  new Column(ACTIVE_POWER),
                                                                  new Column(REACTIVE_POWER))) {
            List<String> skipped = new ArrayList<>();
            for (StaticVarCompensator svc : network.getStaticVarCompensators()) {

                String id = svc.getId();
                int num = mapper.getInt(AmplSubset.STATIC_VAR_COMPENSATOR, id);

                Terminal t = svc.getTerminal();

                int busNum = AmplUtil.getBusNum(mapper, t);

                int conBusNum = AmplUtil.getConnectableBusNum(mapper, t);

                double vlSet = svc.getVoltageSetPoint();
                double vb = t.getVoltageLevel().getNominalV();
                double zb = vb * vb / AmplConstants.SB; //Base impedance

                int vlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, t.getVoltageLevel().getId());
                formatter.writeCell(num)
                        .writeCell(busNum)
                        .writeCell(conBusNum)
                        .writeCell(vlNum)
                        .writeCell(svc.getBmin() * zb)
                        .writeCell(svc.getBmax() * zb)
                        .writeCell(svc.getRegulationMode().equals(RegulationMode.VOLTAGE))
                        .writeCell(vlSet / vb)
                        .writeCell(faultNum)
                        .writeCell(actionNum)
                        .writeCell(id)
                        .writeCell(svc.getName())
                        .writeCell(t.getP())
                        .writeCell(t.getQ());
                addExtensions(num, svc);
            }
            if (!skipped.isEmpty()) {
                LOGGER.trace("Skip static VAR compensators {} because not connected and not connectable", skipped);
            }
        }
    }

    private void writeGenerators(AmplExportContext context) throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("_network_generators", "txt", append), StandardCharsets.UTF_8);
             TableFormatter formatter = new AmplDatTableFormatter(writer,
                                                                  getTableTitle("Generators"),
                                                                  AmplConstants.INVALID_FLOAT_VALUE,
                                                                  !append,
                                                                  AmplConstants.LOCALE,
                                                                  new Column("num"),
                                                                  new Column("bus"),
                                                                  new Column(CON_BUS),
                                                                  new Column(SUBSTATION),
                                                                  new Column("minP (MW)"),
                                                                  new Column(MAXP),
                                                                  new Column("minQmaxP (MVar)"),
                                                                  new Column("minQminP (MVar)"),
                                                                  new Column("maxQmaxP (MVar)"),
                                                                  new Column("maxQminP (MVar)"),
                                                                  new Column(V_REGUL),
                                                                  new Column(TARGET_V),
                                                                  new Column("targetP (MW)"),
                                                                  new Column("targetQ (MVar)"),
                                                                  new Column(FAULT),
                                                                  new Column(config.getActionType().getLabel()),
                                                                  new Column("id"),
                                                                  new Column(DESCRIPTION),
                                                                  new Column(ACTIVE_POWER),
                                                                  new Column(REACTIVE_POWER))) {
            List<String> skipped = new ArrayList<>();
            for (Generator g : network.getGenerators()) {
                Terminal t = g.getTerminal();
                Bus bus = AmplUtil.getBus(t);
                String busId = null;
                int busNum = -1;
                if (bus != null) {
                    busId = bus.getId();
                    busNum = mapper.getInt(AmplSubset.BUS, bus.getId());
                }
                String conBusId = null;
                int conBusNum = -1;
                // take connectable bus if exists
                Bus conBus = AmplUtil.getConnectableBus(t);
                if (conBus != null) {
                    conBusId = conBus.getId();
                    conBusNum = mapper.getInt(AmplSubset.BUS, conBus.getId());
                }
                if (!exportGeneratorOrShunt(context, busId, conBusId)) {
                    skipped.add(g.getId());
                    continue;
                }
                String id = g.getId();
                context.generatorIdsToExport.add(id);
                int num = mapper.getInt(AmplSubset.GENERATOR, id);
                int vlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, t.getVoltageLevel().getId());
                double minP = g.getMinP();
                double maxP = g.getMaxP();
                double vb = t.getVoltageLevel().getNominalV();
                formatter.writeCell(num)
                         .writeCell(busNum)
                         .writeCell(conBusNum != -1 ? conBusNum : busNum)
                         .writeCell(vlNum)
                         .writeCell(minP)
                         .writeCell(maxP)
                         .writeCell(g.getReactiveLimits().getMinQ(maxP))
                         .writeCell(g.getReactiveLimits().getMinQ(minP))
                         .writeCell(g.getReactiveLimits().getMaxQ(maxP))
                         .writeCell(g.getReactiveLimits().getMaxQ(minP))
                         .writeCell(g.isVoltageRegulatorOn())
                         .writeCell(g.getTargetV() / vb)
                         .writeCell(g.getTargetP())
                         .writeCell(g.getTargetQ())
                         .writeCell(faultNum)
                         .writeCell(actionNum)
                         .writeCell(id)
                         .writeCell(g.getName())
                         .writeCell(t.getP())
                         .writeCell(t.getQ());
                addExtensions(num, g);
            }
            if (!skipped.isEmpty()) {
                LOGGER.trace("Skip generators {} because not connected and not connectable", skipped);
            }
        }
    }

    private void writeTemporaryCurrentLimits(CurrentLimits limits, TableFormatter formatter, String branchId, boolean side1, String sideId) throws IOException {
        int branchNum = mapper.getInt(AmplSubset.BRANCH, branchId);
        for (TemporaryLimit tl : limits.getTemporaryLimits()) {
            String limitId = branchId + "_" + sideId + "_" + tl.getAcceptableDuration();
            int limitNum = mapper.getInt(AmplSubset.TEMPORARY_CURRENT_LIMIT, limitId);
            formatter.writeCell(limitNum)
                    .writeCell(branchNum)
                    .writeCell(side1 ? 1 : 2)
                    .writeCell(tl.getValue())
                    .writeCell(tl.getAcceptableDuration())
                    .writeCell(faultNum)
                    .writeCell(actionNum);
        }
    }

    private void writeCurrentLimits() throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("_network_limits", "txt", append), StandardCharsets.UTF_8);
             TableFormatter formatter = new AmplDatTableFormatter(writer,
                                                                  getTableTitle("Temporary current limits"),
                                                                  AmplConstants.INVALID_FLOAT_VALUE,
                                                                  !append,
                                                                  AmplConstants.LOCALE,
                                                                  new Column("num"),
                                                                  new Column("branch"),
                                                                  new Column("side"),
                                                                  new Column("limit (A)"),
                                                                  new Column("accept. duration (s)"),
                                                                  new Column(FAULT),
                                                                  new Column(config.getActionType().getLabel()))) {

            writeBranchCurrentLimits(formatter);

            writeThreeWindingsTransformerCurrentLimits(formatter);

            writeDanglingLineCurrentLimits(formatter);
        }
    }

    private void writeBranchCurrentLimits(TableFormatter formatter) throws IOException {
        for (Branch branch : network.getBranches()) {
            String branchId = branch.getId();
            if (branch.getCurrentLimits1() != null) {
                writeTemporaryCurrentLimits(branch.getCurrentLimits1(), formatter, branchId, true, "_1_");
            }
            if (branch.getCurrentLimits2() != null) {
                writeTemporaryCurrentLimits(branch.getCurrentLimits2(), formatter, branchId, false, "_2_");
            }
        }
    }

    private void writeThreeWindingsTransformerCurrentLimits(TableFormatter formatter) throws IOException {
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            if (twt.getLeg1().getCurrentLimits() != null) {
                String branchId = twt.getId() + AmplConstants.LEG1_SUFFIX;
                writeTemporaryCurrentLimits(twt.getLeg1().getCurrentLimits(), formatter, branchId, false, "");
            }
            if (twt.getLeg2().getCurrentLimits() != null) {
                String branchId = twt.getId() + AmplConstants.LEG2_SUFFIX;
                writeTemporaryCurrentLimits(twt.getLeg2().getCurrentLimits(), formatter,  branchId, true, "");
            }
            if (twt.getLeg3().getCurrentLimits() != null) {
                String branchId = twt.getId() + AmplConstants.LEG3_SUFFIX;
                writeTemporaryCurrentLimits(twt.getLeg3().getCurrentLimits(), formatter,  branchId, true, "");
            }
        }
    }

    private void writeDanglingLineCurrentLimits(TableFormatter formatter) throws IOException {
        for (DanglingLine dl : network.getDanglingLines()) {
            String branchId = dl.getId();
            if (dl.getCurrentLimits() != null) {
                writeTemporaryCurrentLimits(dl.getCurrentLimits(), formatter, branchId, true, "");
            }
        }
    }

    private void writeHvdcLines() throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("_network_hvdc", "txt", append), StandardCharsets.UTF_8);
             TableFormatter formatter = new AmplDatTableFormatter(writer,
                                                                  getTableTitle("HVDC lines"),
                                                                  AmplConstants.INVALID_FLOAT_VALUE,
                                                                  !append,
                                                                  AmplConstants.LOCALE,
                                                                  new Column("num"),
                                                                  new Column("type"),
                                                                  new Column("converterStation1"),
                                                                  new Column("converterStation2"),
                                                                  new Column("r (ohm)"),
                                                                  new Column("nomV (KV)"),
                                                                  new Column("convertersMode"),
                                                                  new Column("targetP (MW)"),
                                                                  new Column(MAXP),
                                                                  new Column(FAULT),
                                                                  new Column(config.getActionType().getLabel()),
                                                                  new Column("id"),
                                                                  new Column(DESCRIPTION))) {
            for (HvdcLine hvdcLine : network.getHvdcLines()) {
                String id = hvdcLine.getId();
                int num = mapper.getInt(AmplSubset.HVDC_LINE, id);
                HvdcType type = hvdcLine.getConverterStation1().getHvdcType();
                AmplSubset subset = type.equals(HvdcType.VSC) ? AmplSubset.VSC_CONVERTER_STATION : AmplSubset.LCC_CONVERTER_STATION;
                formatter.writeCell(num)
                        .writeCell(type.equals(HvdcType.VSC) ? 1 : 2)
                        .writeCell(mapper.getInt(subset, hvdcLine.getConverterStation1().getId()))
                        .writeCell(mapper.getInt(subset, hvdcLine.getConverterStation2().getId()))
                        .writeCell(hvdcLine.getR())
                        .writeCell(hvdcLine.getNominalV())
                        .writeCell(hvdcLine.getConvertersMode().name())
                        .writeCell(hvdcLine.getActivePowerSetpoint())
                        .writeCell(hvdcLine.getMaxP())
                        .writeCell(faultNum)
                        .writeCell(actionNum)
                        .writeCell(id)
                        .writeCell(hvdcLine.getName());
                addExtensions(num, hvdcLine);
            }
        }
    }

    private HashMap<String, HvdcLine> getHvdcLinesMap() {
        HashMap<String, HvdcLine> lineMap = new HashMap<>();
        network.getHvdcLines().forEach(line -> {
            if (line.getConverterStation1() != null) {
                lineMap.put(line.getConverterStation1().getId(), line);
            }
            if (line.getConverterStation2() != null) {
                lineMap.put(line.getConverterStation2().getId(), line);
            }
        });
        return lineMap;
    }

    private void writeLccConverterStations() throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("_network_lcc_converter_stations", "txt", append), StandardCharsets.UTF_8);
             TableFormatter formatter = new AmplDatTableFormatter(writer,
                                                                  getTableTitle("LCC Converter Stations"),
                                                                  AmplConstants.INVALID_FLOAT_VALUE,
                                                                  !append,
                                                                  AmplConstants.LOCALE,
                                                                  new Column("num"),
                                                                  new Column("bus"),
                                                                  new Column(CON_BUS),
                                                                  new Column(SUBSTATION),
                                                                  new Column("lossFactor (%PDC)"),
                                                                  new Column("powerFactor"),
                                                                  new Column(FAULT),
                                                                  new Column(config.getActionType().getLabel()),
                                                                  new Column("id"),
                                                                  new Column(DESCRIPTION),
                                                                  new Column(ACTIVE_POWER),
                                                                  new Column(REACTIVE_POWER))) {

            for (HvdcConverterStation hvdcStation : network.getHvdcConverterStations()) {
                if (hvdcStation.getHvdcType().equals(HvdcType.LCC)) {
                    Terminal t = hvdcStation.getTerminal();
                    int busNum = AmplUtil.getBusNum(mapper, t);
                    int conBusNum = AmplUtil.getConnectableBusNum(mapper, t);

                    LccConverterStation lccStation = (LccConverterStation) hvdcStation;
                    int vlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, t.getVoltageLevel().getId());

                    int num = mapper.getInt(AmplSubset.LCC_CONVERTER_STATION, lccStation.getId());

                    formatter.writeCell(num)
                            .writeCell(busNum)
                            .writeCell(conBusNum != -1 ? conBusNum : busNum)
                            .writeCell(vlNum)
                            .writeCell(lccStation.getLossFactor())
                            .writeCell(lccStation.getPowerFactor())
                            .writeCell(faultNum)
                            .writeCell(actionNum)
                            .writeCell(lccStation.getId())
                            .writeCell(lccStation.getName())
                            .writeCell(t.getP())
                            .writeCell(t.getQ());
                    addExtensions(num, lccStation);
                }
            }
        }
    }

    private void writeVscConverterStations() throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream("_network_vsc_converter_stations", "txt", append), StandardCharsets.UTF_8);
             TableFormatter formatter = new AmplDatTableFormatter(writer,
                                                                  getTableTitle("VSC Converter Stations"),
                                                                  AmplConstants.INVALID_FLOAT_VALUE,
                                                                  !append,
                                                                  AmplConstants.LOCALE,
                                                                  new Column("num"),
                                                                  new Column("bus"),
                                                                  new Column(CON_BUS),
                                                                  new Column(SUBSTATION),
                                                                  new Column("minP (MW)"),
                                                                  new Column(MAXP),
                                                                  new Column("minQmaxP (MVar)"),
                                                                  new Column("minQ0 (MVar)"),
                                                                  new Column("minQminP (MVar)"),
                                                                  new Column("maxQmaxP (MVar)"),
                                                                  new Column("maxQ0 (MVar)"),
                                                                  new Column("maxQminP (MVar)"),
                                                                  new Column(V_REGUL),
                                                                  new Column(TARGET_V),
                                                                  new Column("targetQ (MVar)"),
                                                                  new Column("lossFactor (%PDC)"),
                                                                  new Column(FAULT),
                                                                  new Column(config.getActionType().getLabel()),
                                                                  new Column("id"),
                                                                  new Column(DESCRIPTION),
                                                                  new Column(ACTIVE_POWER),
                                                                  new Column(REACTIVE_POWER))) {

            HashMap<String, HvdcLine> lineMap = getHvdcLinesMap();

            for (HvdcConverterStation hvdcStation : network.getHvdcConverterStations()) {
                if (hvdcStation.getHvdcType().equals(HvdcType.VSC)) {
                    String id = hvdcStation.getId();
                    Terminal t = hvdcStation.getTerminal();
                    int busNum = AmplUtil.getBusNum(mapper, t);
                    int conBusNum = AmplUtil.getConnectableBusNum(mapper, t);

                    double maxP = lineMap.get(id) != null ? lineMap.get(id).getMaxP() : Double.NaN;

                    VscConverterStation vscStation = (VscConverterStation) hvdcStation;
                    int vlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, t.getVoltageLevel().getId());
                    double vlSet = vscStation.getVoltageSetpoint();
                    double vb = t.getVoltageLevel().getNominalV();
                    double minP = -maxP;

                    int num = mapper.getInt(AmplSubset.VSC_CONVERTER_STATION, vscStation.getId());
                    formatter.writeCell(num)
                            .writeCell(busNum)
                            .writeCell(conBusNum != -1 ? conBusNum : busNum)
                            .writeCell(vlNum)
                            .writeCell(minP)
                            .writeCell(maxP)
                            .writeCell(vscStation.getReactiveLimits().getMinQ(maxP))
                            .writeCell(vscStation.getReactiveLimits().getMinQ(0))
                            .writeCell(vscStation.getReactiveLimits().getMinQ(minP))
                            .writeCell(vscStation.getReactiveLimits().getMaxQ(maxP))
                            .writeCell(vscStation.getReactiveLimits().getMaxQ(0))
                            .writeCell(vscStation.getReactiveLimits().getMaxQ(minP))
                            .writeCell(vscStation.isVoltageRegulatorOn())
                            .writeCell(vlSet / vb)
                            .writeCell(vscStation.getReactivePowerSetpoint())
                            .writeCell(vscStation.getLossFactor())
                            .writeCell(faultNum)
                            .writeCell(actionNum)
                            .writeCell(vscStation.getId())
                            .writeCell(vscStation.getName())
                            .writeCell(t.getP())
                            .writeCell(t.getQ());
                    addExtensions(num, vscStation);
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
        writeLoads(context);
        writeShunts(context);
        writeStaticVarCompensators();
        writeSubstations();
        writeVscConverterStations();
        writeLccConverterStations();
        writeHvdcLines();
        exportExtensions();
    }
}
