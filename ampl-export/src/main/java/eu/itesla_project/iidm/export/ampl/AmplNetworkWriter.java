/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.export.ampl;

import eu.itesla_project.commons.util.StringToIntMapper;
import eu.itesla_project.iidm.datasource.DataSource;
import eu.itesla_project.commons.io.table.Column;
import eu.itesla_project.commons.io.table.TableFormatter;
import eu.itesla_project.iidm.export.ampl.util.AmplDatTableFormatter;
import eu.itesla_project.iidm.network.*;
import eu.itesla_project.iidm.network.CurrentLimits.TemporaryLimit;
import eu.itesla_project.iidm.network.util.ConnectedComponents;
import eu.itesla_project.iidm.network.util.SV;
import eu.itesla_project.merge.MergeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AmplNetworkWriter implements AmplConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmplNetworkWriter.class);

    private static final String XNODE_COUNTRY_NAME = "XNODE";

    private final Network network;

    private final int faultNum;

    private final int curativeActionNum;

    private final DataSource dataSource;

    private final boolean append;

    private final StringToIntMapper<AmplSubset> mapper; // id mapper

    private final AmplExportConfig config;

    private static class AmplExportContext {

        private int otherCcNum = Integer.MAX_VALUE;

        public final Set<String> busIdsToExport = new HashSet<>();

        public final Set<String> voltageLevelIdsToExport = new HashSet<>();

        public final Set<String> generatorIdsToExport = new HashSet<>();

        public final Set<String> loadsToExport = new HashSet<>();

    }

    public AmplNetworkWriter(Network network, DataSource dataSource, int faultNum, int curativeActionNum,
                boolean append, StringToIntMapper<AmplSubset> mapper, AmplExportConfig config) {
        this.network = network;
        this.faultNum = faultNum;
        this.curativeActionNum = curativeActionNum;
        this.dataSource = dataSource;
        this.append = append;
        this.mapper = mapper;
        this.config = config;
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

    private static String getDanglingLineMiddleBusId(DanglingLine dl) {
        return dl.getId(); // same id as the dangling line
    }

    private static String getDanglingLineMiddleVoltageLevelId(DanglingLine dl) {
        return dl.getId(); // same id as the dangling line
    }

    private void writeSubstations(AmplExportContext context) throws IOException {
        try (TableFormatter formatter = new AmplDatTableFormatter(
                new OutputStreamWriter(dataSource.newOutputStream("_network_substations", "txt", append), StandardCharsets.UTF_8),
                getTableTitle("Substations"),
                INVALID_FLOAT_VALUE,
                !append,
                LOCALE,
                new Column("num"),
                new Column("horizon"),
                new Column("reference date distance (minutes)"),
                new Column("nomV (KV)"),
                new Column("minV (pu)"),
                new Column("maxV (pu)"),
                new Column("fault"),
                new Column("curative"),
                new Column("country"),
                new Column("id"),
                new Column("description"))) {
            for (VoltageLevel vl : network.getVoltageLevels()) {
//                if (!context.voltageLevelIdsToExport.contains(vl.getId())) {
//                    continue;
//                }
                int num = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vl.getId());
                float nomV = vl.getNominalV();
                float minV = vl.getLowVoltageLimit() / nomV;
                float maxV = vl.getHighVoltageLimit() / nomV;
                formatter.writeCell(num)
                         .writeCell(MergeUtil.getHorizon(vl))
                         .writeCell(MergeUtil.getDateDistanceToReference(vl))
                         .writeCell(nomV)
                         .writeCell(minV)
                         .writeCell(maxV)
                         .writeCell(faultNum)
                         .writeCell(curativeActionNum)
                         .writeCell(vl.getSubstation().getCountry().toString())
                         .writeCell(vl.getId())
                         .writeCell(vl.getName());

            }
            // voltage level associated to 3 windings transformers middle bus
            for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
                String vlId = getThreeWindingsTransformerMiddleVoltageLevelId(twt);
//                if (!context.voltageLevelIdsToExport.contains(vlId)) {
//                    continue;
//                }
                int num = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vlId);
                Terminal t1 = twt.getLeg1().getTerminal();
                VoltageLevel vl1 = t1.getVoltageLevel();
                formatter.writeCell(num)
                         .writeCell(MergeUtil.getHorizon(vl1))
                         .writeCell(MergeUtil.getDateDistanceToReference(vl1))
                         .writeCell(vl1.getNominalV())
                         .writeCell(Float.NaN)
                         .writeCell(Float.NaN)
                         .writeCell(faultNum)
                         .writeCell(curativeActionNum)
                         .writeCell(vl1.getSubstation().getCountry().toString())
                         .writeCell(vlId)
                         .writeEmptyCell();
            }
            // voltage level associated to dangling lines middle bus
            for (DanglingLine dl : network.getDanglingLines()) {
                String vlId = getDanglingLineMiddleVoltageLevelId(dl);
//                if (!context.voltageLevelIdsToExport.contains(vlId)) {
//                    continue;
//                }
                int num = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vlId);
                VoltageLevel vl = dl.getTerminal().getVoltageLevel();
                float nomV = vl.getNominalV();
                float minV = vl.getLowVoltageLimit() / nomV;
                float maxV = vl.getHighVoltageLimit() / nomV;
                formatter.writeCell(num)
                         .writeCell(MergeUtil.getHorizon(vl))
                         .writeCell(MergeUtil.getDateDistanceToReference(vl))
                         .writeCell(nomV)
                         .writeCell(minV)
                         .writeCell(maxV)
                         .writeCell(faultNum)
                         .writeCell(curativeActionNum)
                         .writeCell(vl.getSubstation().getCountry().toString())
                         .writeCell(dl.getId() + "_voltageLevel")
                         .writeEmptyCell();
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
                            .writeCell(MergeUtil.getHorizon(tieLine.getTerminal1().getVoltageLevel()))
                            .writeCell(MergeUtil.getDateDistanceToReference(tieLine.getTerminal1().getVoltageLevel()))
                            .writeCell(l.getTerminal1().getVoltageLevel().getNominalV())
                            .writeCell(Float.NaN)
                            .writeCell(Float.NaN)
                            .writeCell(faultNum)
                            .writeCell(curativeActionNum)
                            .writeCell(XNODE_COUNTRY_NAME)
                            .writeCell(AmplUtil.getXnodeBusId(tieLine) + "_voltageLevel")
                            .writeEmptyCell();
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

    private void writeBuses(AmplExportContext context) throws IOException {
        try (TableFormatter formatter = new AmplDatTableFormatter(
                new OutputStreamWriter(dataSource.newOutputStream("_network_buses", "txt", append), StandardCharsets.UTF_8),
                getTableTitle("Buses"),
                INVALID_FLOAT_VALUE,
                !append,
                LOCALE,
                new Column("num"),
                new Column("substation"),
                new Column("cc"),
                new Column("v (pu)"),
                new Column("theta (rad)"),
                new Column("p (MW)"),
                new Column("q (MVar)"),
                new Column("fault"),
                new Column("curative"))) {
            for (Bus b : AmplUtil.getBuses(network)) {
                int ccNum = ConnectedComponents.getCcNum(b);
                // skip buses not in the main connected component
                if (isOnlyMainCc() && ccNum != ConnectedComponent.MAIN_CC_NUM) {
                    continue;
                }
                String id = b.getId();
                VoltageLevel vl = b.getVoltageLevel();
                context.busIdsToExport.add(id);
                int num = mapper.getInt(AmplSubset.BUS, id);
                int vlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vl.getId());
                float nomV = vl.getNominalV();
                float v = b.getV() / nomV;
                float theta = (float) Math.toRadians(b.getAngle());
                formatter.writeCell(num)
                         .writeCell(vlNum)
                         .writeCell(ccNum)
                         .writeCell(v)
                         .writeCell(theta)
                         .writeCell(b.getP())
                         .writeCell(b.getQ())
                         .writeCell(faultNum)
                         .writeCell(curativeActionNum);
            }
            // 3 windings transformers middle bus
            for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
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
                // skip buses not in the main connected component
                if (isOnlyMainCc() && middleCcNum != ConnectedComponent.MAIN_CC_NUM) {
                    continue;
                }
                String middleBusId = getThreeWindingsTransformerMiddleBusId(twt);
                String middleVlId = getThreeWindingsTransformerMiddleVoltageLevelId(twt);
                context.busIdsToExport.add(middleBusId);
                int middleBusNum = mapper.getInt(AmplSubset.BUS, middleBusId);
                int middleVlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, middleVlId);
                formatter.writeCell(middleBusNum)
                         .writeCell(middleVlNum)
                         .writeCell(middleCcNum)
                         .writeCell(Float.NaN)
                         .writeCell(Float.NaN)
                         .writeCell(0f)
                         .writeCell(0f)
                         .writeCell(faultNum)
                         .writeCell(curativeActionNum);
            }
            // dangling line middle bus
            for (DanglingLine dl : network.getDanglingLines()) {
                Terminal t = dl.getTerminal();
                Bus b = AmplUtil.getBus(t);
                int middleCcNum;
                // if the connection bus of the dangling line is null or not in the main cc, the middle bus is
                // obviously not in the main cc
                if (b != null) {
                    middleCcNum = ConnectedComponents.getCcNum(b);
                } else {
                    middleCcNum = context.otherCcNum--;
                }
                // skip buses not in the main connected component
                if (isOnlyMainCc() && middleCcNum != ConnectedComponent.MAIN_CC_NUM) {
                    continue;
                }
                String middleBusId = getDanglingLineMiddleBusId(dl);
                String middleVlId = getDanglingLineMiddleVoltageLevelId(dl);
                context.busIdsToExport.add(middleBusId);
                int middleBusNum = mapper.getInt(AmplSubset.BUS, middleBusId);
                int middleVlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, middleVlId);
                SV sv = new SV(t.getP(), t.getQ(), b != null ? b.getV() : Float.NaN, b != null ? b.getAngle() : Float.NaN).otherSide(dl);
                float nomV = t.getVoltageLevel().getNominalV();
                float v = sv.getU() / nomV;
                float theta = (float) Math.toRadians(sv.getA());
                formatter.writeCell(middleBusNum)
                         .writeCell(middleVlNum)
                         .writeCell(middleCcNum)
                         .writeCell(v)
                         .writeCell(theta)
                         .writeCell(0f) // 0 MW injected at dangling line internal bus
                         .writeCell(0f) // 0 MVar injected at dangling line internal bus
                         .writeCell(faultNum)
                         .writeCell(curativeActionNum);
            }
            if (config.isExportXNodes()) {
                for (Line l : network.getLines()) {
                    if (!l.isTieLine()) {
                        continue;
                    }
                    TieLine tieLine = (TieLine) l;
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
                    // skip buses not in the main connected component
                    if (isOnlyMainCc() && xNodeCcNum != ConnectedComponent.MAIN_CC_NUM) {
                        continue;
                    }
                    String xNodeBusId = AmplUtil.getXnodeBusId(tieLine);
                    int xNodeBusNum = mapper.getInt(AmplSubset.BUS, xNodeBusId);
                    int xNodeVlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, AmplUtil.getXnodeVoltageLevelId(tieLine));
                    formatter.writeCell(xNodeBusNum)
                            .writeCell(xNodeVlNum)
                            .writeCell(xNodeCcNum)
                            .writeCell(Float.NaN)
                            .writeCell(Float.NaN)
                            .writeCell(0f)
                            .writeCell(0f)
                            .writeCell(faultNum)
                            .writeCell(curativeActionNum);
                }
            }
        }
    }

    private static float getPermanentLimit(CurrentLimits limits) {
        if (limits != null) {
            return limits.getPermanentLimit();
        }
        return Float.NaN;
    }

    private static boolean isBusExported(AmplExportContext context, String busId) {
        return busId != null && context.busIdsToExport.contains(busId);
    }

    private void writeBranches(AmplExportContext context) throws IOException {
        try (TableFormatter formatter = new AmplDatTableFormatter(
                new OutputStreamWriter(dataSource.newOutputStream("_network_branches", "txt", append), StandardCharsets.UTF_8),
                getTableTitle("Branches"),
                INVALID_FLOAT_VALUE,
                !append,
                LOCALE,
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
                new Column("fault"),
                new Column("curative"),
                new Column("id"),
                new Column("description"))) {
            for (Line l : network.getLines()) {
                Terminal t1 = l.getTerminal1();
                Terminal t2 = l.getTerminal2();
                Bus bus1 = AmplUtil.getBus(t1);
                Bus bus2 = AmplUtil.getBus(t2);
                if (bus1 != null && bus2 != null && bus1 == bus2) {
                    LOGGER.warn("Skipping line '{}' connected to the same bus at both sides", l.getId());
                    continue;
                }
                String bus1Id = null;
                int bus1Num = -1;
                if (bus1 != null) {
                    bus1Id = bus1.getId();
                    bus1Num = mapper.getInt(AmplSubset.BUS, bus1.getId());
                }
                String bus2Id = null;
                int bus2Num = -1;
                if (bus2 != null) {
                    bus2Id = bus2.getId();
                    bus2Num = mapper.getInt(AmplSubset.BUS, bus2.getId());
                }
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

                float vb = vl1.getNominalV();
                float zb = vb * vb / SB;

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
                            .writeCell(curativeActionNum)
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
                            .writeCell(curativeActionNum)
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
                            .writeCell(curativeActionNum)
                            .writeCell(id)
                            .writeCell(l.getName());
                }
            }
            for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
                Terminal t1 = twt.getTerminal1();
                Terminal t2 = twt.getTerminal2();
                Bus bus1 = AmplUtil.getBus(t1);
                Bus bus2 = AmplUtil.getBus(t2);
                if (bus1 != null && bus2 != null && bus1 == bus2) {
                    LOGGER.warn("Skipping transformer '{}' connected to the same bus at both sides", twt.getId());
                    continue;
                }
                String bus1Id = null;
                int bus1Num = -1;
                if (bus1 != null) {
                    bus1Id = bus1.getId();
                    bus1Num = mapper.getInt(AmplSubset.BUS, bus1.getId());
                }
                String bus2Id = null;
                int bus2Num = -1;
                if (bus2 != null) {
                    bus2Id = bus2.getId();
                    bus2Num = mapper.getInt(AmplSubset.BUS, bus2.getId());
                }
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
                float vb1 = vl1.getNominalV();
                float vb2 = vl2.getNominalV();
                float zb2 = vb2 * vb2 / SB;
                float r = twt.getR() / zb2;
                float x = twt.getX() / zb2;
                float g = twt.getG() * zb2;
                float b = twt.getB() * zb2;
                float ratedU1 = twt.getRatedU1();
                float ratedU2 = twt.getRatedU2();
                float ratio = ratedU2 / vb2 / (ratedU1 / vb1);
                RatioTapChanger rtc = twt.getRatioTapChanger();
                PhaseTapChanger ptc = twt.getPhaseTapChanger();
                int rtcNum = (rtc != null ? mapper.getInt(AmplSubset.RATIO_TAP_CHANGER, twt.getId()) : -1);
                int ptcNum = (ptc != null ? mapper.getInt(AmplSubset.PHASE_TAP_CHANGER, twt.getId()) : -1);
                formatter.writeCell(num)
                         .writeCell(bus1Num)
                         .writeCell(bus2Num)
                         .writeCell(-1)
                         .writeCell(vl1Num)
                         .writeCell(vl2Num)
                         .writeCell(r)
                         .writeCell(x)
                         .writeCell(g)
                         .writeCell(0)
                         .writeCell(b)
                         .writeCell(0)
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
                         .writeCell(curativeActionNum)
                         .writeCell(id)
                         .writeCell(twt.getName());
            }
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
                String id1 = twt.getId() + "_leg1";
                String id2 = twt.getId() + "_leg2";
                String id3 = twt.getId() + "_leg3";
                int num3wt = mapper.getInt(AmplSubset.THREE_WINDINGS_TRANSFO, twt.getId());
                int num1 = mapper.getInt(AmplSubset.BRANCH, id1);
                int num2 = mapper.getInt(AmplSubset.BRANCH, id2);
                int num3 = mapper.getInt(AmplSubset.BRANCH, id3);
                int vl1Num = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vl1.getId());
                int vl2Num = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vl2.getId());
                int vl3Num = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vl3.getId());
                String bus1Id = null;
                int bus1Num = -1;
                if (bus1 != null) {
                    bus1Id = bus1.getId();
                    bus1Num = mapper.getInt(AmplSubset.BUS, bus1.getId());
                }
                String bus2Id = null;
                int bus2Num = -1;
                if (bus2 != null) {
                    bus2Id = bus2.getId();
                    bus2Num = mapper.getInt(AmplSubset.BUS, bus2.getId());
                }
                String bus3Id = null;
                int bus3Num = -1;
                if (bus3 != null) {
                    bus3Id = bus3.getId();
                    bus3Num = mapper.getInt(AmplSubset.BUS, bus3.getId());
                }
                float vb1 = vl1.getNominalV();
                float vb2 = vl2.getNominalV();
                float vb3 = vl3.getNominalV();
                float zb1 = vb1 * vb1 / SB;
                float zb2 = vb2 * vb2 / SB;
                float zb3 = vb3 * vb3 / SB;
                float r1 = twt.getLeg1().getR() / zb1;
                float x1 = twt.getLeg1().getX() / zb1;
                float g1 = twt.getLeg1().getG() * zb1;
                float b1 = twt.getLeg1().getB() * zb1;
                float r2 = twt.getLeg2().getR() / zb2;
                float x2 = twt.getLeg2().getX() / zb2;
                float r3 = twt.getLeg3().getR() / zb3;
                float x3 = twt.getLeg3().getX() / zb3;
                float ratedU1 = twt.getLeg1().getRatedU();
                float ratedU2 = twt.getLeg2().getRatedU();
                float ratedU3 = twt.getLeg3().getRatedU();
                float ratio2 =  ratedU1 / ratedU2;
                float ratio3 =  ratedU1 / ratedU3;
                RatioTapChanger rtc2 = twt.getLeg2().getRatioTapChanger();
                RatioTapChanger rtc3 = twt.getLeg2().getRatioTapChanger();
                int rtc2Num = (rtc2 != null ? mapper.getInt(AmplSubset.RATIO_TAP_CHANGER, id2) : -1);
                int rtc3Num = (rtc3 != null ? mapper.getInt(AmplSubset.RATIO_TAP_CHANGER, id3) : -1);

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
                            .writeCell(0)
                            .writeCell(b1)
                            .writeCell(0)
                            .writeCell(1f) // ratio is one at primary leg
                            .writeCell(-1)
                            .writeCell(-1)
                            .writeCell(Float.NaN)
                            .writeCell(t1.getP())
                            .writeCell(Float.NaN)
                            .writeCell(t1.getQ())
                            .writeCell(Float.NaN)
                            .writeCell(getPermanentLimit(twt.getLeg1().getCurrentLimits()))
                            .writeCell(false)
                            .writeCell(faultNum)
                            .writeCell(curativeActionNum)
                            .writeCell(id1)
                            .writeEmptyCell();
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
                            .writeCell(0)
                            .writeCell(0)
                            .writeCell(0)
                            .writeCell(0)
                            .writeCell(ratio2)
                            .writeCell(rtc2Num)
                            .writeCell(-1)
                            .writeCell(t2.getP())
                            .writeCell(Float.NaN)
                            .writeCell(t2.getQ())
                            .writeCell(Float.NaN)
                            .writeCell(getPermanentLimit(twt.getLeg2().getCurrentLimits()))
                            .writeCell(Float.NaN)
                            .writeCell(false)
                            .writeCell(faultNum)
                            .writeCell(curativeActionNum)
                            .writeCell(id2)
                            .writeEmptyCell();
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
                            .writeCell(0)
                            .writeCell(0)
                            .writeCell(0)
                            .writeCell(0)
                            .writeCell(ratio3)
                            .writeCell(rtc3Num)
                            .writeCell(-1)
                            .writeCell(t3.getP())
                            .writeCell(Float.NaN)
                            .writeCell(t3.getQ())
                            .writeCell(Float.NaN)
                            .writeCell(getPermanentLimit(twt.getLeg3().getCurrentLimits()))
                            .writeCell(Float.NaN)
                            .writeCell(false)
                            .writeCell(faultNum)
                            .writeCell(curativeActionNum)
                            .writeCell(id3)
                            .writeEmptyCell();
                }
            }
            for (DanglingLine dl : network.getDanglingLines()) {
                Terminal t = dl.getTerminal();
                Bus bus1 = AmplUtil.getBus(t);
                String bus1Id = null;
                int bus1Num = -1;
                if (bus1 != null) {
                    bus1Id = bus1.getId();
                    bus1Num = mapper.getInt(AmplSubset.BUS, bus1Id);
                }
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
                float vb = vl.getNominalV();
                float zb = vb * vb / SB;
                float p1 = t.getP();
                float q1 = t.getQ();
                SV sv = new SV(p1, q1, bus1 != null ? bus1.getV() : Float.NaN, bus1 != null ? bus1.getAngle() : Float.NaN).otherSide(dl);
                float p2 = sv.getP();
                float q2 = sv.getQ();
                float patl = getPermanentLimit(dl.getCurrentLimits());
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
                         .writeCell(curativeActionNum)
                         .writeCell(id)
                         .writeCell(dl.getName());
            }
        }
    }

    private void writeTapChangerTable() throws IOException {
        try (TableFormatter formatter = new AmplDatTableFormatter(
                new OutputStreamWriter(dataSource.newOutputStream("_network_tct", "txt", append), StandardCharsets.UTF_8),
                getTableTitle("Tap changer table"),
                INVALID_FLOAT_VALUE,
                !append,
                LOCALE,
                new Column("num"),
                new Column("tap"),
                new Column("var ratio"),
                new Column("x (pu)"),
                new Column("angle (rad)"),
                new Column("fault"),
                new Column("curative"))) {
            for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
                Terminal t2 = twt.getTerminal2();
                float vb2 = t2.getVoltageLevel().getNominalV();
                float zb2 = vb2 * vb2 / SB;
                RatioTapChanger rtc = twt.getRatioTapChanger();
                if (rtc != null) {
                    String id = twt.getId() + "_ratio_table";
                    int num = mapper.getInt(AmplSubset.TAP_CHANGER_TABLE, id);

                    for (int position = rtc.getLowTapPosition(); position <= rtc.getHighTapPosition(); position++) {
                        RatioTapChangerStep step = rtc.getStep(position);
                        float x = twt.getX() * (1 + step.getX() / 100) / zb2;
                        formatter.writeCell(num)
                                 .writeCell(position - rtc.getLowTapPosition() + 1)
                                 .writeCell(step.getRho())
                                 .writeCell(x)
                                 .writeCell(0f)
                                 .writeCell(faultNum)
                                 .writeCell(curativeActionNum);
                    }
                }
                PhaseTapChanger ptc = twt.getPhaseTapChanger();
                if (ptc != null) {
                    String id = twt.getId() + "_phase_table";
                    int num = mapper.getInt(AmplSubset.TAP_CHANGER_TABLE, id);
                    for (int position = ptc.getLowTapPosition(); position <= ptc.getHighTapPosition(); position++) {
                        PhaseTapChangerStep step = ptc.getStep(position);
                        float x = twt.getX() * (1 + step.getX() / 100) / zb2;
                        formatter.writeCell(num)
                                 .writeCell(position - ptc.getLowTapPosition() + 1)
                                 .writeCell(step.getRho())
                                 .writeCell(x)
                                 .writeCell((float) Math.toRadians(step.getAlpha()))
                                 .writeCell(faultNum)
                                 .writeCell(curativeActionNum);
                    }
                }
            }
            for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
                Terminal t2 = twt.getLeg2().getTerminal();
                Terminal t3 = twt.getLeg3().getTerminal();
                float vb2 = t2.getVoltageLevel().getNominalV();
                float vb3 = t3.getVoltageLevel().getNominalV();
                float zb2 = vb2 * vb2 / SB;
                float zb3 = vb3 * vb3 / SB;
                RatioTapChanger rtc2 = twt.getLeg2().getRatioTapChanger();
                if (rtc2 != null) {
                    String id = twt.getId() + "_leg2_ratio_table";
                    int num = mapper.getInt(AmplSubset.TAP_CHANGER_TABLE, id);
                    for (int position = rtc2.getLowTapPosition(); position <= rtc2.getHighTapPosition(); position++) {
                        RatioTapChangerStep step = rtc2.getStep(position);
                        float x = twt.getLeg2().getX() * (1 + step.getX() / 100) / zb2;
                        formatter.writeCell(num)
                                 .writeCell(position - rtc2.getLowTapPosition() + 1)
                                 .writeCell(step.getRho())
                                 .writeCell(x)
                                 .writeCell(0f)
                                 .writeCell(faultNum)
                                 .writeCell(curativeActionNum);
                    }
                }
                RatioTapChanger rtc3 = twt.getLeg3().getRatioTapChanger();
                if (rtc3 != null) {
                    String id = twt.getId() + "_leg3_ratio_table";
                    int num = mapper.getInt(AmplSubset.TAP_CHANGER_TABLE, id);
                    for (int position = rtc3.getLowTapPosition(); position <= rtc3.getHighTapPosition(); position++) {
                        RatioTapChangerStep step = rtc3.getStep(position);
                        float x = twt.getLeg3().getX() * (1 + step.getX() / 100) / zb3;
                        formatter.writeCell(num)
                                 .writeCell(position - rtc3.getLowTapPosition() + 1)
                                 .writeCell(step.getRho())
                                 .writeCell(x)
                                 .writeCell(0f)
                                 .writeCell(faultNum)
                                 .writeCell(curativeActionNum);
                    }
                }
            }
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
                .writeCell(curativeActionNum)
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
        columns.add(new Column("fault"));
        columns.add(new Column("curative"));
        columns.add(new Column("id"));
        try (TableFormatter formatter = new AmplDatTableFormatter(
                new OutputStreamWriter(dataSource.newOutputStream("_network_rtc", "txt", append), StandardCharsets.UTF_8),
                getTableTitle("Ratio tap changers"),
                INVALID_FLOAT_VALUE,
                !append,
                LOCALE,
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
                    String rtc2Id = twt.getId() + "_leg2";
                    String tcs2Id = twt.getId() + "_leg2_ratio_table";
                    writeRatioTapChanger(formatter, rtc2Id, rtc2, tcs2Id);
                }
                RatioTapChanger rtc3 = twt.getLeg3().getRatioTapChanger();
                if (rtc3 != null) {
                    String rtc3Id = twt.getId() + "_leg3";
                    String tcs3Id = twt.getId() + "_leg3_ratio_table";
                    writeRatioTapChanger(formatter, rtc3Id, rtc3, tcs3Id);
                }
            }
        }
    }

    private void writePhaseTapChangers() throws IOException {
        try (TableFormatter formatter = new AmplDatTableFormatter(
                new OutputStreamWriter(dataSource.newOutputStream("_network_ptc", "txt", append), StandardCharsets.UTF_8),
                getTableTitle("Phase tap changers"),
                INVALID_FLOAT_VALUE,
                !append,
                LOCALE,
                new Column("num"),
                new Column("tap"),
                new Column("table"),
                new Column("fault"),
                new Column("curative"),
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
                             .writeCell(curativeActionNum)
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
        try (TableFormatter formatter = new AmplDatTableFormatter(
                new OutputStreamWriter(dataSource.newOutputStream("_network_loads", "txt", append), StandardCharsets.UTF_8),
                getTableTitle("Loads"),
                INVALID_FLOAT_VALUE,
                !append,
                LOCALE,
                new Column("num"),
                new Column("bus"),
                new Column("substation"),
                new Column("p (MW)"),
                new Column("q (MVar)"),
                new Column("fault"),
                new Column("curative"),
                new Column("id"),
                new Column("description"))) {
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
                         .writeCell(curativeActionNum)
                         .writeCell(id)
                         .writeCell(l.getName());
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
                         .writeCell(curativeActionNum)
                         .writeCell(dl.getId() + "_load")
                         .writeEmptyCell();
            }
            if (skipped.size() > 0) {
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
        try (TableFormatter formatter = new AmplDatTableFormatter(
                new OutputStreamWriter(dataSource.newOutputStream("_network_shunts", "txt", append), StandardCharsets.UTF_8),
                getTableTitle("Shunts"),
                INVALID_FLOAT_VALUE,
                !append,
                LOCALE,
                new Column("num"),
                new Column("bus"),
                new Column("con. bus"),
                new Column("substation"),
                new Column("minB (pu)"),
                new Column("maxB (pu)"),
                new Column("inter. points"),
                new Column("b (pu)"),
                new Column("fault"),
                new Column("curative"),
                new Column("id"),
                new Column("description"))) {
            List<String> skipped = new ArrayList<>();
            for (ShuntCompensator sc : network.getShunts()) {
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
                float vb = t.getVoltageLevel().getNominalV();
                float zb = vb * vb / SB;
                float b1 = 0;
                float b2 = sc.getMaximumB() * zb;
                float minB = Math.min(b1, b2);
                float maxB = Math.max(b1, b2);
                float b = sc.getCurrentB() * zb;
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
                         .writeCell(curativeActionNum)
                         .writeCell(id)
                         .writeCell(sc.getName());
            }
            if (skipped.size() > 0) {
                LOGGER.trace("Skip shunts {} because not connected and not connectable", skipped);
            }
        }
    }

    private void writeStaticVarCompensators(AmplExportContext context) throws IOException {
        try (TableFormatter formatter = new AmplDatTableFormatter(
                new OutputStreamWriter(dataSource.newOutputStream("_network_static_var_compensatiors", "txt", append), StandardCharsets.UTF_8),
                getTableTitle("Static VAR compensators"),
                INVALID_FLOAT_VALUE,
                !append,
                LOCALE)) {
            List<String> skipped = new ArrayList<>();
            for (StaticVarCompensator svc : network.getStaticVarCompensators()) {
                throw new UnsupportedOperationException(); // FIXME
            }
            if (skipped.size() > 0) {
                LOGGER.trace("Skip static VAR compensators {} because not connected and not connectable", skipped);
            }
        }
    }

    private void writeGenerators(AmplExportContext context) throws IOException {
        try (TableFormatter formatter = new AmplDatTableFormatter(
                new OutputStreamWriter(dataSource.newOutputStream("_network_generators", "txt", append), StandardCharsets.UTF_8),
                getTableTitle("Generators"),
                INVALID_FLOAT_VALUE,
                !append,
                LOCALE,
                new Column("num"),
                new Column("bus"),
                new Column("con. bus"),
                new Column("substation"),
                new Column("minP (MW)"),
                new Column("maxP (MW)"),
                new Column("minQmaxP (MVar)"),
                new Column("minQminP (MVar)"),
                new Column("maxQmaxP (MVar)"),
                new Column("maxQminP (MVar)"),
                new Column("v regul."),
                new Column("targetV (pu)"),
                new Column("targetP (MW)"),
                new Column("targetQ (MVar)"),
                new Column("fault"),
                new Column("curative"),
                new Column("id"),
                new Column("description"))) {
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
                float minP = g.getMinP();
                float maxP = g.getMaxP();
                float vb = t.getVoltageLevel().getNominalV();
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
                         .writeCell(curativeActionNum)
                         .writeCell(id)
                         .writeCell(g.getName());
            }
            if (skipped.size() > 0) {
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
                    .writeCell(curativeActionNum);
        }
    }

    private void writeTemporaryCurrentLimits() throws IOException {
        try (TableFormatter formatter = new AmplDatTableFormatter(
                new OutputStreamWriter(dataSource.newOutputStream("_network_limits", "txt", append), StandardCharsets.UTF_8),
                getTableTitle("Temporary current limits"),
                INVALID_FLOAT_VALUE,
                !append,
                LOCALE,
                new Column("num"),
                new Column("branch"),
                new Column("side"),
                new Column("limit (A)"),
                new Column("accept. duration (s)"),
                new Column("fault"),
                new Column("curative"))) {
            for (Line l : network.getLines()) {
                String branchId = l.getId();
                if (l.getCurrentLimits1() != null) {
                    writeTemporaryCurrentLimits(l.getCurrentLimits1(), formatter, branchId, true, "_1_");
                }
                if (l.getCurrentLimits2() != null) {
                    writeTemporaryCurrentLimits(l.getCurrentLimits2(), formatter, branchId, false, "_2_");
                }
            }
            for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
                String branchId = twt.getId();
                if (twt.getCurrentLimits1() != null) {
                    writeTemporaryCurrentLimits(twt.getCurrentLimits1(), formatter, branchId, true, "_1_");
                }
                if (twt.getCurrentLimits2() != null) {
                    writeTemporaryCurrentLimits(twt.getCurrentLimits2(), formatter, branchId, false, "_2_");
                }
            }
            for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
                if (twt.getLeg1().getCurrentLimits() != null) {
                    String branchId = twt.getId() + "_leg1";
                    writeTemporaryCurrentLimits(twt.getLeg1().getCurrentLimits(), formatter, branchId, false, "");
                }
                if (twt.getLeg2().getCurrentLimits() != null) {
                    String branchId = twt.getId() + "_leg2";
                    writeTemporaryCurrentLimits(twt.getLeg2().getCurrentLimits(), formatter,  branchId, true, "");
                }
                if (twt.getLeg3().getCurrentLimits() != null) {
                    String branchId = twt.getId() + "_leg3";
                    writeTemporaryCurrentLimits(twt.getLeg3().getCurrentLimits(), formatter,  branchId, true, "");
                }
            }
            for (DanglingLine dl : network.getDanglingLines()) {
                String branchId = dl.getId();
                if (dl.getCurrentLimits() != null) {
                    writeTemporaryCurrentLimits(dl.getCurrentLimits(), formatter, branchId, true, "");
                }
            }
        }
    }

    public void write() throws IOException {
        write(new AmplExportContext());
    }

    public void write(AmplExportContext context) throws IOException {
        writeBuses(context);
        writeTapChangerTable();
        writeRatioTapChangers();
        writePhaseTapChangers();
        writeBranches(context);
        writeTemporaryCurrentLimits();
        writeGenerators(context);
        writeLoads(context);
        writeShunts(context);
        writeStaticVarCompensators(context);
        writeSubstations(context);
    }

}
