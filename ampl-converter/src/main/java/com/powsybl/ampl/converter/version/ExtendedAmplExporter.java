/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.converter.version;

import com.powsybl.ampl.converter.AmplConstants;
import com.powsybl.ampl.converter.AmplExportConfig;
import com.powsybl.ampl.converter.AmplSubset;
import com.powsybl.ampl.converter.AmplUtil;
import com.powsybl.ampl.converter.util.NetworkUtil;
import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.TableFormatter;
import com.powsybl.commons.io.table.TableFormatterHelper;
import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.iidm.network.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.powsybl.ampl.converter.AmplConstants.*;

/**
 * 1st extension of BasicAmplExporter, associated with AMPL version 1.1 (exporter id).
 * The extension adds:
 *  - A synchronous component number in the bus tables.
 *  - A slack bus boolean in the bus table.
 *  - R, G and B characteristics in tap tables.
 *  - The regulated bus in generator and static var compensator tables.
 *
 * @author Nicolas PIERRE {@literal <nicolas.pierre at artelys.com>}
 * @author Pierre ARVY {@literal <pierre.arvy at artelys.com>}
 */
public class ExtendedAmplExporter extends BasicAmplExporter {

    private static final int SYNCHRONOUS_COMPONENT_COLUMN_INDEX = 4;
    private static final int SLACK_BUS_COLUMN_INDEX = 9;
    private static final int TAP_CHANGER_R_COLUMN_INDEX = 4;
    private static final int TAP_CHANGER_G_COLUMN_INDEX = 6;
    private static final int TAP_CHANGER_B_COLUMN_INDEX = 7;
    private static final int GENERATOR_V_REGUL_BUS_COLUMN_INDEX = 14;
    private static final int STATIC_VAR_COMPENSATOR_V_REGUL_BUS_COLUMN_INDEX = 8;

    private int otherScNum = Integer.MAX_VALUE;

    public ExtendedAmplExporter(AmplExportConfig config,
                                Network network,
                                StringToIntMapper<AmplSubset> mapper,
                                int variantIndex, int faultNum, int actionNum) {
        super(config, network, mapper, variantIndex, faultNum, actionNum);
    }

    private record ImpedanceAndAdmittance(double r, double x, double g, double b) { }

    @Override
    public List<Column> getBusesColumns() {
        List<Column> busesColumns = new ArrayList<>(super.getBusesColumns());
        // add synchronous component column
        busesColumns.add(SYNCHRONOUS_COMPONENT_COLUMN_INDEX, new Column("sc"));
        // add slack bus column
        busesColumns.add(SLACK_BUS_COLUMN_INDEX, new Column("slack bus"));
        return busesColumns;
    }

    @Override
    public List<Column> getTapChangerTableColumns() {
        List<Column> tapChangerTableColumns = new ArrayList<>(super.getTapChangerTableColumns());
        // add r, g and b columns
        tapChangerTableColumns.add(TAP_CHANGER_R_COLUMN_INDEX, new Column("r (pu)"));
        tapChangerTableColumns.add(TAP_CHANGER_G_COLUMN_INDEX, new Column("g (pu)"));
        tapChangerTableColumns.add(TAP_CHANGER_B_COLUMN_INDEX, new Column("b (pu)"));
        return tapChangerTableColumns;
    }

    @Override
    public List<Column> getGeneratorsColumns() {
        List<Column> generatorsColumns = new ArrayList<>(super.getGeneratorsColumns());
        // add column for voltage regulated bus
        generatorsColumns.add(GENERATOR_V_REGUL_BUS_COLUMN_INDEX, new Column(V_REGUL_BUS));
        return generatorsColumns;
    }

    @Override
    public List<Column> getStaticVarCompensatorColumns() {
        List<Column> svcColumns = new ArrayList<>(super.getStaticVarCompensatorColumns());
        // add column for voltage regulated bus
        svcColumns.add(STATIC_VAR_COMPENSATOR_V_REGUL_BUS_COLUMN_INDEX, new Column(V_REGUL_BUS));
        return svcColumns;
    }

    @Override
    public void addAdditionalCellsBusesColumns(TableFormatterHelper formatterHelper, Bus b) {
        formatterHelper.addCell(b.getSynchronousComponent().getNum(), SYNCHRONOUS_COMPONENT_COLUMN_INDEX);
        formatterHelper.addCell(NetworkUtil.isSlackBus(b), SLACK_BUS_COLUMN_INDEX);
    }

    @Override
    public void addAdditionalCellsThreeWindingsTranformersMiddleBusesColumns(TableFormatterHelper formatterHelper,
                                                                             ThreeWindingsTransformer twt,
                                                                             int middleCcNum) {
        formatterHelper.addCell(getThreeWindingsTransformerMiddleBusSCNum(twt), SYNCHRONOUS_COMPONENT_COLUMN_INDEX);
        formatterHelper.addCell(false, SLACK_BUS_COLUMN_INDEX);
    }

    private int getThreeWindingsTransformerMiddleBusSCNum(ThreeWindingsTransformer twt) {
        Terminal t1 = twt.getLeg1().getTerminal();
        Terminal t2 = twt.getLeg2().getTerminal();
        Terminal t3 = twt.getLeg3().getTerminal();
        Bus b1 = AmplUtil.getBus(t1);
        Bus b2 = AmplUtil.getBus(t2);
        Bus b3 = AmplUtil.getBus(t3);
        int middleScNum;
        if (b1 != null) {
            middleScNum = b1.getSynchronousComponent().getNum();
        } else if (b2 != null) {
            middleScNum = b2.getSynchronousComponent().getNum();
        } else if (b3 != null) {
            middleScNum = b3.getSynchronousComponent().getNum();
        } else {
            middleScNum = otherScNum--;
        }

        return middleScNum;
    }

    @Override
    public void addAdditionalCellsDanglingLineMiddleBuses(TableFormatterHelper formatterHelper, DanglingLine dl,
                                                          int middleCcNum) {
        formatterHelper.addCell(getDanglingLineMiddleBusSCNum(dl), SYNCHRONOUS_COMPONENT_COLUMN_INDEX);
        formatterHelper.addCell(false, SLACK_BUS_COLUMN_INDEX);
    }

    private int getDanglingLineMiddleBusSCNum(DanglingLine dl) {
        Bus b = AmplUtil.getBus(dl.getTerminal());
        return b != null ? b.getSynchronousComponent().getNum() : otherScNum--;
    }

    @Override
    public void addAdditionalCellsTieLineMiddleBuses(TableFormatterHelper formatterHelper, TieLine tieLine,
                                                     int xNodeCcNum) {
        formatterHelper.addCell(getTieLineMiddleBusSCNum(tieLine), SYNCHRONOUS_COMPONENT_COLUMN_INDEX);
        formatterHelper.addCell(false, SLACK_BUS_COLUMN_INDEX);
    }

    private int getTieLineMiddleBusSCNum(TieLine tieLine) {
        Terminal t1 = tieLine.getDanglingLine1().getTerminal();
        Terminal t2 = tieLine.getDanglingLine2().getTerminal();
        Bus b1 = AmplUtil.getBus(t1);
        Bus b2 = AmplUtil.getBus(t2);
        int xNodeScNum;
        if (b1 != null) {
            xNodeScNum = b1.getSynchronousComponent().getNum();
        } else if (b2 != null) {
            xNodeScNum = b2.getSynchronousComponent().getNum();
        } else {
            xNodeScNum = otherScNum--;
        }
        return xNodeScNum;
    }

    @Override
    public void writeTwoWindingsTransformerTapChangerTableToFormatter(TableFormatter formatter,
                                                                      TwoWindingsTransformer twt) throws IOException {
        Terminal t2 = twt.getTerminal2();
        double vb2 = t2.getVoltageLevel().getNominalV();
        double zb2 = vb2 * vb2 / AmplConstants.SB;
        ImpedanceAndAdmittance transformer = new ImpedanceAndAdmittance(twt.getR(), twt.getX(),
            twt.getG(), twt.getB());
        RatioTapChanger rtc = twt.getRatioTapChanger();
        if (rtc != null) {
            String id = twt.getId() + RATIO_TABLE_SUFFIX;
            writeRatioTapChanger(formatter, id, zb2, transformer, rtc);
        }

        PhaseTapChanger ptc = twt.getPhaseTapChanger();
        if (ptc != null) {
            String id = twt.getId() + PHASE_TABLE_SUFFIX;
            writePhaseTapChanger(formatter, id, zb2, transformer, ptc);
        }
    }

    @Override
    public void writeThreeWindingsTransformerTapChangerTableToFormatter(TableFormatter formatter,
                                                                        ThreeWindingsTransformer twt) throws IOException {
        int legNumber = 0;
        for (ThreeWindingsTransformer.Leg leg : twt.getLegs()) {
            legNumber++;
            RatioTapChanger rtc = leg.getRatioTapChanger();
            double vb = twt.getRatedU0();
            double zb = vb * vb / AmplConstants.SB;
            ImpedanceAndAdmittance transformer = new ImpedanceAndAdmittance(leg.getR(), leg.getX(),
                leg.getG(), leg.getB());
            if (rtc != null) {
                String id = twt.getId() + "_leg" + legNumber + RATIO_TABLE_SUFFIX;
                writeRatioTapChanger(formatter, id, zb, transformer, rtc);
            }
            PhaseTapChanger ptc = leg.getPhaseTapChanger();
            if (ptc != null) {
                String id = twt.getId() + "_leg" + legNumber + PHASE_TABLE_SUFFIX;
                writePhaseTapChanger(formatter, id, zb, transformer, ptc);
            }
        }
    }

    private void writeRatioTapChanger(TableFormatter formatter, String id, double zb2,
                                      ImpedanceAndAdmittance transformerZandY,
                                      RatioTapChanger rtc) throws IOException {
        int num = getMapper().getInt(AmplSubset.TAP_CHANGER_TABLE, id);

        for (int position = rtc.getLowTapPosition(); position <= rtc.getHighTapPosition(); position++) {
            RatioTapChangerStep step = rtc.getStep(position);
            ImpedanceAndAdmittance stepCharacteristics = new ImpedanceAndAdmittance(step.getR(), step.getX(), step.getG(), step.getB());
            writeTapChanger(formatter, new TapChangerParametersForWriter(num, position, rtc.getLowTapPosition(), zb2, transformerZandY, stepCharacteristics, step.getRho(), 0));
        }
    }

    private void writePhaseTapChanger(TableFormatter formatter, String id, double zb2,
                                      ImpedanceAndAdmittance transformerZandY,
                                      PhaseTapChanger ptc) throws IOException {
        int num = getMapper().getInt(AmplSubset.TAP_CHANGER_TABLE, id);

        for (int position = ptc.getLowTapPosition(); position <= ptc.getHighTapPosition(); position++) {
            PhaseTapChangerStep step = ptc.getStep(position);
            ImpedanceAndAdmittance stepCharacteristics = new ImpedanceAndAdmittance(step.getR(), step.getX(), step.getG(), step.getB());
            writeTapChanger(formatter, new TapChangerParametersForWriter(num, position, ptc.getLowTapPosition(), zb2, transformerZandY, stepCharacteristics, step.getRho(), Math.toRadians(step.getAlpha())));
        }
    }

    private record TapChangerParametersForWriter(int num, int stepPosition, int lowTapPosition, double zb2,
                                                 ImpedanceAndAdmittance transformer, ImpedanceAndAdmittance step, double rho, double alpha) { }

    private void writeTapChanger(TableFormatter formatter, TapChangerParametersForWriter parametersForWriter) throws IOException {
        double rNorm = parametersForWriter.transformer.r * (1 + parametersForWriter.step.r / 100) / parametersForWriter.zb2;
        double xNorm = parametersForWriter.transformer.x * (1 + parametersForWriter.step.x / 100) / parametersForWriter.zb2;
        double gNorm = parametersForWriter.transformer.g * (1 + parametersForWriter.step.g / 100) * parametersForWriter.zb2;
        double bNorm = parametersForWriter.transformer.b * (1 + parametersForWriter.step.b / 100) * parametersForWriter.zb2;
        formatter.writeCell(getVariantIndex())
            .writeCell(parametersForWriter.num)
            .writeCell(parametersForWriter.stepPosition - parametersForWriter.lowTapPosition + 1)
            .writeCell(parametersForWriter.rho)
            .writeCell(rNorm)
            .writeCell(xNorm)
            .writeCell(gNorm)
            .writeCell(bNorm)
            .writeCell(parametersForWriter.alpha)
            .writeCell(getFaultNum())
            .writeCell(getActionNum());
    }

    @Override
    public void addAdditionalCellsGenerator(TableFormatterHelper formatterHelper, Generator gen) {
        int regulatingBusNum = gen.isVoltageRegulatorOn() && gen.getRegulatingTerminal().isConnected() ?
            getMapper().getInt(AmplSubset.BUS, gen.getRegulatingTerminal().getBusView().getBus().getId()) : -1;
        formatterHelper.addCell(regulatingBusNum, GENERATOR_V_REGUL_BUS_COLUMN_INDEX);
    }

    @Override
    public void addAdditionalCellsStaticVarCompensator(TableFormatterHelper formatterHelper,
                                                       StaticVarCompensator svc) {
        boolean voltageRegulation = svc.getRegulationMode().equals(StaticVarCompensator.RegulationMode.VOLTAGE);
        int regulatingBusNum = voltageRegulation && svc.getRegulatingTerminal().isConnected() ?
            getMapper().getInt(AmplSubset.BUS, svc.getRegulatingTerminal().getBusView().getBus().getId()) : -1;

        // Cell to add
        formatterHelper.addCell(regulatingBusNum, STATIC_VAR_COMPENSATOR_V_REGUL_BUS_COLUMN_INDEX);
    }

}
