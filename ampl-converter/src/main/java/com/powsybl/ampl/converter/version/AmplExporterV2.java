package com.powsybl.ampl.converter.version;

import com.powsybl.ampl.converter.*;
import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.TableFormatter;
import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.ConnectedComponents;
import com.powsybl.iidm.network.util.SV;

import java.io.IOException;
import java.util.List;

import static com.powsybl.ampl.converter.AmplConstants.*;

public class AmplExporterV2 extends BasicAmplExporter {

    private static int otherScNum = Integer.MAX_VALUE;

    public AmplExporterV2(AmplExportConfig config, Network network, StringToIntMapper<AmplSubset> mapper, int variantIndex, int faultNum, int actionNum) {
        super(config, network, mapper, variantIndex, faultNum, actionNum);
    }

    public static AmplExportVersion.Factory getFactory() {
        return AmplExporterV2::new;
    }

    @Override
    public String getExporterId() {
        return AmplExportVersion.V2_0.getExporterId();
    }

    @Override
    public List<Column> getBusesColumns() {
        return List.of(new Column(VARIANT),
                new Column(NUM),
                new Column(SUBSTATION),
                new Column("cc"),
                new Column("sc"),
                new Column("v (pu)"),
                new Column("theta (rad)"),
                new Column("p (MW)"),
                new Column("q (MVar)"),
                new Column(FAULT),
                new Column(config.getActionType().getLabel()),
                new Column(ID));
    }

    @Override
    public void writeBusesColumnsToFormatter(TableFormatter formatter, Bus b) throws IOException {
        int ccNum = ConnectedComponents.getCcNum(b);
        int scNum = b.getSynchronousComponent().getNum();
        String id = b.getId();
        VoltageLevel vl = b.getVoltageLevel();
        int num = mapper.getInt(AmplSubset.BUS, id);
        int vlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, vl.getId());
        double nomV = vl.getNominalV();
        double v = b.getV() / nomV;
        double theta = Math.toRadians(b.getAngle());
        formatter.writeCell(variantIndex)
                .writeCell(num)
                .writeCell(vlNum)
                .writeCell(ccNum)
                .writeCell(scNum)
                .writeCell(v)
                .writeCell(theta)
                .writeCell(b.getP())
                .writeCell(b.getQ())
                .writeCell(faultNum)
                .writeCell(actionNum)
                .writeCell(id);
    }

    @Override
    public void writeThreeWindingsTranformersMiddleBusesColumnsToFormatter(TableFormatter formatter,
                                                                           ThreeWindingsTransformer twt,
                                                                           int middleCcNum) throws IOException {
        String middleBusId = AmplUtil.getThreeWindingsTransformerMiddleBusId(twt);
        String middleVlId = AmplUtil.getThreeWindingsTransformerMiddleVoltageLevelId(twt);
        int middleBusNum = mapper.getInt(AmplSubset.BUS, middleBusId);
        int middleVlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, middleVlId);

        int middleScNum = getThreeWindingsTransformerMiddleBusSCNum(twt);
        double v = twt.getProperty("v") == null ? Double.NaN :
                Double.parseDouble(twt.getProperty("v")) / twt.getRatedU0();
        double angle = twt.getProperty("angle") == null ? Double.NaN :
                Math.toRadians(Double.parseDouble(twt.getProperty("angle")));

        formatter.writeCell(variantIndex)
                .writeCell(middleBusNum)
                .writeCell(middleVlNum)
                .writeCell(middleCcNum)
                .writeCell(middleScNum)
                .writeCell(v)
                .writeCell(angle)
                .writeCell(0.0)
                .writeCell(0.0)
                .writeCell(faultNum)
                .writeCell(actionNum)
                .writeCell(middleBusId);
    }

    private static int getThreeWindingsTransformerMiddleBusSCNum(ThreeWindingsTransformer twt) {
        Terminal t1 = twt.getLeg1().getTerminal();
        Terminal t2 = twt.getLeg2().getTerminal();
        Terminal t3 = twt.getLeg3().getTerminal();
        Bus b1 = AmplUtil.getBus(t1);
        Bus b2 = AmplUtil.getBus(t2);
        Bus b3 = AmplUtil.getBus(t3);
        int middleScNum;
        if (b1 != null) {
            middleScNum = b1.getSynchronousComponent().getNum();;
        } else if (b2 != null) {
            middleScNum = b2.getSynchronousComponent().getNum();
        } else if (b3 != null) {
            middleScNum = b3.getSynchronousComponent().getNum();
        } else {
            middleScNum = otherScNum--;
        }

        return middleScNum;
    }

    private static int getDanglingLineMiddleBusComponentNum(DanglingLine dl) {
        Bus b = AmplUtil.getBus(dl.getTerminal());
        return b != null ? b.getSynchronousComponent().getNum() : otherScNum--;
    }

    @Override
    public void writeDanglingLineMiddleBusesToFormatter(TableFormatter formatter, DanglingLine dl,
                                                        int middleCcNum) throws IOException {
        Terminal t = dl.getTerminal();
        Bus b = AmplUtil.getBus(dl.getTerminal());
        String middleBusId = AmplUtil.getDanglingLineMiddleBusId(dl);
        String middleVlId = AmplUtil.getDanglingLineMiddleVoltageLevelId(dl);
        int middleBusNum = mapper.getInt(AmplSubset.BUS, middleBusId);
        int middleVlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, middleVlId);
        SV sv = new SV(t.getP(), t.getQ(), b != null ? b.getV() : Double.NaN, b != null ? b.getAngle() : Double.NaN,
                TwoSides.ONE).otherSide(
                dl, true);
        double nomV = t.getVoltageLevel().getNominalV();
        double v = sv.getU() / nomV;
        double theta = Math.toRadians(sv.getA());
        int middleScNum = getDanglingLineMiddleBusComponentNum(dl);
        formatter.writeCell(variantIndex)
                .writeCell(middleBusNum)
                .writeCell(middleVlNum)
                .writeCell(middleCcNum)
                .writeCell(middleScNum)
                .writeCell(v)
                .writeCell(theta)
                .writeCell(0.0) // 0 MW injected at dangling line internal bus
                .writeCell(0.0) // 0 MVar injected at dangling line internal bus
                .writeCell(faultNum)
                .writeCell(actionNum)
                .writeCell(middleBusId);
    }

    private static int getTieLineMiddleBusComponentNum(TieLine tieLine) {
        Terminal t1 = tieLine.getDanglingLine1().getTerminal();
        Terminal t2 = tieLine.getDanglingLine2().getTerminal();
        Bus b1 = AmplUtil.getBus(t1);
        Bus b2 = AmplUtil.getBus(t2);
        int xNodeCcNum;
        if (b1 != null) {
            xNodeCcNum = b1.getSynchronousComponent().getNum();
        } else if (b2 != null) {
            xNodeCcNum = b2.getSynchronousComponent().getNum();
        } else {
            xNodeCcNum = otherScNum--;
        }

        return xNodeCcNum;
    }

    @Override
    public void writeTieLineMiddleBusesToFormatter(TableFormatter formatter, TieLine tieLine,
                                                   int xNodeCcNum) throws IOException {
        String xNodeBusId = AmplUtil.getXnodeBusId(tieLine);
        int xNodeBusNum = mapper.getInt(AmplSubset.BUS, xNodeBusId);
        int xNodeVlNum = mapper.getInt(AmplSubset.VOLTAGE_LEVEL, AmplUtil.getXnodeVoltageLevelId(tieLine));
        int xNodeScNum = getTieLineMiddleBusComponentNum(tieLine);
        formatter.writeCell(variantIndex)
                .writeCell(xNodeBusNum)
                .writeCell(xNodeVlNum)
                .writeCell(xNodeCcNum)
                .writeCell(xNodeScNum)
                .writeCell(Float.NaN)
                .writeCell(Double.NaN)
                .writeCell(0.0)
                .writeCell(0.0)
                .writeCell(faultNum)
                .writeCell(actionNum)
                .writeCell(xNodeBusId);
    }

//    @Override
//    void writeSlackBusToFormatter(TableFormatter formatter, Bus slackBus) throws IOException {
//        String id = slackBus.getId();
//        int num = mapper.getInt(AmplSubset.BUS, id);
//        formatter.writeCell(VARIANT)
//                .writeCell(num)
//                .writeCell(slackBus.get)
//    }

}
