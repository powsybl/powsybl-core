/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.eurostag.export;

import eu.itesla_project.commons.ITeslaException;
import eu.itesla_project.eurostag.network.*;
import eu.itesla_project.eurostag.network.io.EsgWriter;
import eu.itesla_project.iidm.network.*;
import eu.itesla_project.iidm.network.util.Identifiables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EurostagEchExport {

    private static final Logger LOGGER = LoggerFactory.getLogger(EurostagEchExport.class);

    /**
     * epsilon value for conductance
     */
    public static final float G_EPSILON = 0.00001f;

    /**
     * epsilon value for susceptance
     */
    public static final float B_EPSILON = 0.00001f;

    private static final String XNODE_V_PROPERTY = "xnode_v";
    private static final String XNODE_ANGLE_PROPERTY = "xnode_angle";

    private final Network network;
    private final EurostagEchExportConfig config;
    private final BranchParallelIndexes parallelIndexes;
    private final EurostagDictionary dictionary;

    public EurostagEchExport(Network network, EurostagEchExportConfig config, BranchParallelIndexes parallelIndexes, EurostagDictionary dictionary) {
        this.network = Objects.requireNonNull(network);
        this.config = Objects.requireNonNull(config);
        this.parallelIndexes = Objects.requireNonNull(parallelIndexes);
        this.dictionary = Objects.requireNonNull(dictionary);
    }

    public EurostagEchExport(Network network, EurostagEchExportConfig config) {
        this.network = Objects.requireNonNull(network);
        this.config = config;
        this.parallelIndexes = BranchParallelIndexes.build(network, config);
        this.dictionary = EurostagDictionary.create(network, parallelIndexes, config);
    }

    public EurostagEchExport(Network network) {
        this(network, new EurostagEchExportConfig());
    }

    private void createAreas(EsgNetwork esgNetwork) {
        esgNetwork.addArea(new EsgArea(new Esg2charName(EchUtil.FAKE_AREA), EsgArea.Type.AC));
        for (Country c : network.getCountries()) {
            esgNetwork.addArea(new EsgArea(new Esg2charName(c.toString()), EsgArea.Type.AC));
        }
    }

    private EsgNode createNode(String busId, String countryIsoCode, float nominalV, float v, float angle, boolean slackBus) {
        return new EsgNode(new Esg2charName(countryIsoCode),
                new Esg8charName(dictionary.getEsgId(busId)),
                nominalV,
                Float.isNaN(v) ? 1f : v / nominalV,
                Float.isNaN(angle) ? 0f : angle,
                slackBus);
    }

    private EsgNode createNode(String busId, VoltageLevel vl, float v, float angle, boolean slackBus) {
        return createNode(busId, vl.getSubstation().getCountry().name(), vl.getNominalV(), v, angle, slackBus);
    }

    private void createNodes(EsgNetwork esgNetwork) {
        esgNetwork.addNode(createNode(EchUtil.FAKE_NODE_NAME1, EchUtil.FAKE_AREA, 380f, 380f, 0f, false));
        esgNetwork.addNode(createNode(EchUtil.FAKE_NODE_NAME2, EchUtil.FAKE_AREA, 380f, 380f, 0f, false));
        Bus sb = EchUtil.selectSlackbus(network, config);
        if (sb == null) {
            throw new RuntimeException("Stack bus not found");
        }
        LOGGER.debug("Slack bus: {} ({})", sb, sb.getVoltageLevel().getId());
        for (Bus b : Identifiables.sort(EchUtil.getBuses(network, config))) {
            esgNetwork.addNode(createNode(b.getId(), b.getVoltageLevel(), b.getV(), b.getAngle(), sb == b));
        }
        for (DanglingLine dl : Identifiables.sort(network.getDanglingLines())) {
            Properties properties = dl.getProperties();
            String strV = properties.getProperty(XNODE_V_PROPERTY);
            String strAngle = properties.getProperty(XNODE_ANGLE_PROPERTY);
            float v = strV != null ? Float.parseFloat(strV) : Float.NaN;
            float angle = strAngle != null ? Float.parseFloat(strAngle) : Float.NaN;
            esgNetwork.addNode(createNode(EchUtil.getBusId(dl), dl.getTerminal().getVoltageLevel(), v, angle, false));
        }
    }

    private static EsgBranchConnectionStatus getStatus(ConnectionBus bus1, ConnectionBus bus2) {
        if (!bus1.isConnected() && !bus2.isConnected()) {
            return EsgBranchConnectionStatus.OPEN_AT_BOTH_SIDES;
        } else if (bus1.isConnected() && bus2.isConnected()) {
            return EsgBranchConnectionStatus.CLOSED_AT_BOTH_SIDE;
        } else {
            return bus1.isConnected() ? EsgBranchConnectionStatus.OPEN_AT_RECEIVING_SIDE
                    : EsgBranchConnectionStatus.OPEN_AT_SENDING_SIDE;
        }
    }

    private void createCouplingDevices(EsgNetwork esgNetwork) {
        for (VoltageLevel vl : Identifiables.sort(network.getVoltageLevels())) {
            for (Switch sw : Identifiables.sort(EchUtil.getSwitches(vl, config))) {
                Bus bus1 = EchUtil.getBus1(vl, sw.getId(), config);
                Bus bus2 = EchUtil.getBus2(vl, sw.getId(), config);
                esgNetwork.addCouplingDevice(new EsgCouplingDevice(new EsgBranchName(new Esg8charName(dictionary.getEsgId(bus1.getId())),
                        new Esg8charName(dictionary.getEsgId(bus2.getId())),
                        parallelIndexes.getParallelIndex(sw.getId()))
                        , sw.isOpen() ? EsgCouplingDevice.ConnectionStatus.OPEN : EsgCouplingDevice.ConnectionStatus.CLOSED));
            }
        }
    }

    private EsgLine createLine(String id, ConnectionBus bus1, ConnectionBus bus2, float nominalV, float r, float x, float g,
                               float b, EsgGeneralParameters parameters) {
        EsgBranchConnectionStatus status = getStatus(bus1, bus2);
        float rate = parameters.getSnref();
        float vnom2 = (float) Math.pow(nominalV, 2);
        float rb = r * parameters.getSnref() / vnom2;
        float rxb = x * parameters.getSnref() / vnom2;
        float gs = g / parameters.getSnref() * vnom2;
        float bs = b / parameters.getSnref() * vnom2;
        return new EsgLine(new EsgBranchName(new Esg8charName(dictionary.getEsgId(bus1.getId())),
                new Esg8charName(dictionary.getEsgId(bus2.getId())),
                parallelIndexes.getParallelIndex(id)),
                status, rb, rxb, gs, bs, rate);
    }

    private EsgDissymmetricalBranch createDissymmetricalBranch(String id, ConnectionBus bus1, ConnectionBus bus2,
                                                               float nominalV, float r, float x, float g1, float b1, float g2, float b2,
                                                               EsgGeneralParameters parameters) {
        EsgBranchConnectionStatus status = getStatus(bus1, bus2);
        float rate = parameters.getSnref();
        float vnom2 = (float) Math.pow(nominalV, 2);
        float rb = (r * parameters.getSnref()) / vnom2;
        float rxb = (x * parameters.getSnref()) / vnom2;
        float gs1 = (g1 / parameters.getSnref()) * vnom2;
        float bs1 = (b1 / parameters.getSnref()) * vnom2;
        float gs2 = (g2 / parameters.getSnref()) * vnom2;
        float bs2 = (b2 / parameters.getSnref()) * vnom2;
        return new EsgDissymmetricalBranch(new EsgBranchName(new Esg8charName(bus1.getId()),
                new Esg8charName(bus2.getId()),
                parallelIndexes.getParallelIndex(id)),
                status, rb / 2, rxb / 2, gs1, bs1, rate, rb / 2, rxb / 2, gs2, bs2);
    }

    private void createLines(EsgNetwork esgNetwork, EsgGeneralParameters parameters) {
        for (Line l : Identifiables.sort(network.getLines())) {
            ConnectionBus bus1 = ConnectionBus.fromTerminal(l.getTerminal1(), config, EchUtil.FAKE_NODE_NAME1);
            ConnectionBus bus2 = ConnectionBus.fromTerminal(l.getTerminal2(), config, EchUtil.FAKE_NODE_NAME2);
            // if the admittance are the same in the both side of PI line model
            if (Math.abs(l.getG1() - l.getG2()) < G_EPSILON && Math.abs(l.getB1() - l.getB2()) < B_EPSILON) {
                //...create a simple line
                esgNetwork.addLine(createLine(l.getId(), bus1, bus2, l.getTerminal1().getVoltageLevel().getNominalV(),
                        l.getR(), l.getX(), l.getG1(), l.getB1(), parameters));
            } else {
                // create a dissymmetrical branch
                esgNetwork.addDissymmetricalBranch(createDissymmetricalBranch(l.getId(), bus1, bus2, l.getTerminal1().getVoltageLevel().getNominalV(),
                        l.getR(), l.getX(), l.getG1(), l.getB1(), l.getG2(), l.getB2(), parameters));
            }
        }
        for (DanglingLine dl : Identifiables.sort(network.getDanglingLines())) {
            ConnectionBus bus1 = ConnectionBus.fromTerminal(dl.getTerminal(), config, EchUtil.FAKE_NODE_NAME1);
            ConnectionBus bus2 = new ConnectionBus(true, EchUtil.getBusId(dl));
            esgNetwork.addLine(createLine(dl.getId(), bus1, bus2, dl.getTerminal().getVoltageLevel().getNominalV(),
                    dl.getR(), dl.getX(), dl.getG() / 2, dl.getB() / 2, parameters));
        }
    }

    private EsgDetailedTwoWindingTransformer.Tap createTap(TwoWindingsTransformer twt, int iplo, float rho, float dr, float dx,
                                                           float dephas, float rate, EsgGeneralParameters parameters) {
        float nomiU2 = twt.getTerminal2().getVoltageLevel().getNominalV();
        float rho_i = twt.getRatedU2() / twt.getRatedU1() * rho;
        float uno1 = nomiU2 / rho_i;
        float uno2 = nomiU2;
        float r = twt.getR() * (1 + dr / 100.0f);
        float x = twt.getX() * (1 + dx / 100.0f);

        //...mTrans.getR() = Get the nominal series resistance specified in Ω at the secondary voltage side.
        float zb2 = (float) (Math.pow(nomiU2, 2) / parameters.getSnref());
        float rpu2 = r / zb2;  //...total line resistance  [p.u.](Base snref)
        float xpu2 = x / zb2;  //...total line reactance   [p.u.](Base snref)

        //...leakage impedance [%] (base rate)
        float ucc;
        if (xpu2 < 0) {
            ucc = xpu2 * 100f * rate / parameters.getSnref();
        } else {
            float zpu2 = (float) Math.hypot(rpu2, xpu2);
            ucc = zpu2 * 100f * rate / parameters.getSnref();
        }

        return new EsgDetailedTwoWindingTransformer.Tap(iplo, dephas, uno1, uno2, ucc);
    }

    private void createTransformers(EsgNetwork esgNetwork, EsgGeneralParameters parameters) {
        for (TwoWindingsTransformer twt : Identifiables.sort(network.getTwoWindingsTransformers())) {
            ConnectionBus bus1 = ConnectionBus.fromTerminal(twt.getTerminal1(), config, EchUtil.FAKE_NODE_NAME1);
            ConnectionBus bus2 = ConnectionBus.fromTerminal(twt.getTerminal2(), config, EchUtil.FAKE_NODE_NAME2);
            EsgBranchConnectionStatus status = getStatus(bus1, bus2);

            //...IIDM gives no rate value. we take rate = 100 MVA But we have to find the corresponding pcu, pfer ...
            float rate = 100.f;

            //**************************
            //*** LOSSES COMPUTATION *** (Record 1)
            //**************************

            float nomiU2 = twt.getTerminal2().getVoltageLevel().getNominalV();

            //...mTrans.getR() = Get the nominal series resistance specified in Ω at the secondary voltage side.
            float Rpu2 = (twt.getR() * parameters.getSnref()) / nomiU2 / nomiU2;  //...total line resistance  [p.u.](Base snref)
            float Gpu2 = (twt.getG() / parameters.getSnref()) * nomiU2 * nomiU2;  //...semi shunt conductance [p.u.](Base snref)
            float Bpu2 = (twt.getB() / parameters.getSnref()) * nomiU2 * nomiU2;  //...semi shunt susceptance [p.u.](Base snref)

            //...changing base snref -> base rate to compute losses
            float pcu = Rpu2 * rate * 100f / parameters.getSnref();                  //...base rate (100F -> %)
            float pfer = 10000f * (Gpu2 / rate) * (parameters.getSnref() / 100f);  //...base rate
            float modgb = (float) Math.sqrt(Math.pow(Gpu2, 2.f) + Math.pow(Bpu2, 2.f));
            float cmagn = 10000 * (modgb / rate) * (parameters.getSnref() / 100f);  //...magnetizing current [% base rate]
            float esat = 1.f;

            //***************************
            // *** TAP TRANSFORMATION *** (Record 2)
            //***************************

            EsgDetailedTwoWindingTransformer.RegulatingMode regulatingMode = EsgDetailedTwoWindingTransformer.RegulatingMode.NOT_REGULATING;
            Esg8charName zbusr = null; //...regulated node name (if empty, no tap change)
            float voltr = Float.NaN;
            int ktpnom; //...nominal tap number is not available in IIDM. Take th median plot by default
            int ktap8;  //...initial tap position (tap number) (Ex: 10)
            List<EsgDetailedTwoWindingTransformer.Tap> taps = new ArrayList<>();

            RatioTapChanger rtc = twt.getRatioTapChanger();
            PhaseTapChanger ptc = twt.getPhaseTapChanger();
            if (rtc != null && ptc == null) {
                if (rtc.isRegulating()) {
                    ConnectionBus regulatingBus = ConnectionBus.fromTerminal(rtc.getRegulationTerminal(), config, null);
                    if (regulatingBus.getId() != null) {
                        regulatingMode = EsgDetailedTwoWindingTransformer.RegulatingMode.VOLTAGE;
                        zbusr = new Esg8charName(dictionary.getEsgId(regulatingBus.getId()));
                    }
                }
                voltr = rtc.getTargetV();
                ktap8 = rtc.getTapPosition() - rtc.getLowTapPosition() + 1;
                ktpnom = rtc.getStepCount() / 2 + 1;
                for (int p = rtc.getLowTapPosition(); p <= rtc.getHighTapPosition(); p++) {
                    int iplo = p - rtc.getLowTapPosition() + 1;
                    taps.add(createTap(twt, iplo, rtc.getStep(p).getRho(), rtc.getStep(p).getR(), rtc.getStep(p).getX(), 0f, rate, parameters));
                }

            } else if (ptc != null && rtc == null) {
                if (ptc.getRegulationMode() == PhaseTapChanger.RegulationMode.CURRENT_LIMITER && ptc.isRegulating()) {
                    String regulbus = EchUtil.getBus(ptc.getRegulationTerminal(), config).getId();
                    if (regulbus.equals(bus1.getId())) {
                        regulatingMode = EsgDetailedTwoWindingTransformer.RegulatingMode.ACTIVE_FLUX_SIDE_1;
                    }
                    if (regulbus.equals(bus2.getId())) {
                        regulatingMode = EsgDetailedTwoWindingTransformer.RegulatingMode.ACTIVE_FLUX_SIDE_2;
                    }
                    if (regulatingMode == EsgDetailedTwoWindingTransformer.RegulatingMode.NOT_REGULATING) {
                        throw new ITeslaException("Phase transformer " + twt.getId() + " has an unknown regulated node");
                    }
                }
                ktap8 = ptc.getTapPosition() - ptc.getLowTapPosition() + 1;
                ktpnom = ptc.getStepCount() / 2 + 1;
                for (int p = ptc.getLowTapPosition(); p <= ptc.getHighTapPosition(); p++) {
                    int iplo = p - ptc.getLowTapPosition() + 1;
                    taps.add(createTap(twt, iplo, ptc.getStep(p).getRho(), ptc.getStep(p).getR(), ptc.getStep(p).getX(), ptc.getStep(p).getAlpha(), rate, parameters));
                }
            } else if (rtc == null && ptc == null) {
                ktap8 = 1;
                ktpnom = 1;
                taps.add(createTap(twt, 1, 1f, 0f, 0f, 0f, rate, parameters));
            } else {
                throw new RuntimeException("Transformer " + twt.getId() + "  with voltage and phase tap changer not yet supported");
            }

            float pregmin = Float.NaN; //...?
            float pregmax = Float.NaN; //...?

            EsgDetailedTwoWindingTransformer esgTransfo = new EsgDetailedTwoWindingTransformer(
                    new EsgBranchName(new Esg8charName(dictionary.getEsgId(bus1.getId())),
                            new Esg8charName(dictionary.getEsgId(bus2.getId())),
                            parallelIndexes.getParallelIndex(twt.getId())),
                    status,
                    cmagn,
                    rate,
                    pcu,
                    pfer,
                    esat,
                    ktpnom,
                    ktap8,
                    zbusr,
                    voltr,
                    pregmin,
                    pregmax,
                    regulatingMode);

            //***************************
            // *** TAP TRANSFORMATION *** (Record 3)
            //***************************

            esgTransfo.getTaps().addAll(taps);

            esgNetwork.addDetailedTwoWindingTransformer(esgTransfo);
        }

        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            throw new AssertionError("TODO");
        }
    }

    private EsgLoad createLoad(ConnectionBus bus, String loadId, float p0, float q0) {
        EsgConnectionStatus status = bus.isConnected() ? EsgConnectionStatus.CONNECTED : EsgConnectionStatus.NOT_CONNECTED;
        return new EsgLoad(status, new Esg8charName(dictionary.getEsgId(loadId)),
                new Esg8charName(dictionary.getEsgId(bus.getId())),
                0f, 0f, p0, 0f, 0f, q0);
    }

    private void createLoads(EsgNetwork esgNetwork) {
        for (Load l : Identifiables.sort(network.getLoads())) {
            ConnectionBus bus = ConnectionBus.fromTerminal(l.getTerminal(), config, EchUtil.FAKE_NODE_NAME1);
            esgNetwork.addLoad(createLoad(bus, l.getId(), l.getP0(), l.getQ0()));
        }
        for (DanglingLine dl : Identifiables.sort(network.getDanglingLines())) {
            ConnectionBus bus = new ConnectionBus(true, EchUtil.getBusId(dl));
            esgNetwork.addLoad(createLoad(bus, EchUtil.getLoadId(dl), dl.getP0(), dl.getQ0()));
        }
    }

    private void createGenerators(EsgNetwork esgNetwork) {
        for (Generator g : Identifiables.sort(network.getGenerators())) {
            ConnectionBus bus = ConnectionBus.fromTerminal(g.getTerminal(), config, EchUtil.FAKE_NODE_NAME1);
            EsgConnectionStatus status = bus.isConnected() ? EsgConnectionStatus.CONNECTED : EsgConnectionStatus.NOT_CONNECTED;
            float pgen = g.getTargetP();
            float qgen = g.getTargetQ();
            float pgmin = g.getMinP();
            float pgmax = g.getMaxP();
            float qgmin = config.isNoGeneratorMinMaxQ() ? -9999 : g.getReactiveLimits().getMinQ(pgen);
            float qgmax = config.isNoGeneratorMinMaxQ() ? 9999 : g.getReactiveLimits().getMaxQ(pgen);
            EsgRegulatingMode mode = g.isVoltageRegulatorOn() && g.getTargetV() >= 0.1
                    ? EsgRegulatingMode.REGULATING : EsgRegulatingMode.NOT_REGULATING;
            float vregge = g.isVoltageRegulatorOn() ? g.getTargetV() : Float.NaN;
            float qgensh = 1.f;

            Bus regulatingBus = g.getRegulatingTerminal().getBusBreakerView().getConnectableBus();

            esgNetwork.addGenerator(new EsgGenerator(new Esg8charName(dictionary.getEsgId(g.getId())),
                    new Esg8charName(dictionary.getEsgId(bus.getId())),
                    pgmin, pgen, pgmax, qgmin, qgen, qgmax, mode, vregge,
                    new Esg8charName(dictionary.getEsgId(regulatingBus.getId())),
                    qgensh, status));
        }
    }

    private void createBanks(EsgNetwork esgNetwork) {
        for (ShuntCompensator sc : Identifiables.sort(network.getShunts())) {
            ConnectionBus bus = ConnectionBus.fromTerminal(sc.getTerminal(), config, EchUtil.FAKE_NODE_NAME1);
            //...number of steps in service
            int ieleba = bus.isConnected() ? sc.getCurrentSectionCount() : 0; // not really correct, because it can be connected with zero section, EUROSTAG should be modified...
            float plosba = 0.f; // no active lost in the iidm shunt compensator
            float vnom = sc.getTerminal().getVoltageLevel().getNominalV();
            float rcapba = vnom * vnom * sc.getbPerSection();
            int imaxba = sc.getMaximumSectionCount();
            EsgCapacitorOrReactorBank.RegulatingMode xregba = EsgCapacitorOrReactorBank.RegulatingMode.NOT_REGULATING;
            esgNetwork.addCapacitorsOrReactorBanks(new EsgCapacitorOrReactorBank(new Esg8charName(dictionary.getEsgId(sc.getId())),
                    new Esg8charName(dictionary.getEsgId(bus.getId())),
                    ieleba, plosba, rcapba, imaxba, xregba));
        }
    }

    private void createStaticVarCompensators(EsgNetwork esgNetwork) {
        for (StaticVarCompensator svc : Identifiables.sort(network.getStaticVarCompensators())) {
            ConnectionBus bus = ConnectionBus.fromTerminal(svc.getTerminal(), config, EchUtil.FAKE_NODE_NAME1);
            Esg8charName znamsvc = new Esg8charName(dictionary.getEsgId(svc.getId()));
            EsgConnectionStatus xsvcst = bus.isConnected() ? EsgConnectionStatus.CONNECTED : EsgConnectionStatus.NOT_CONNECTED;
            Esg8charName znodsvc = new Esg8charName(dictionary.getEsgId(bus.getId()));
            float factor = (float) Math.pow(svc.getTerminal().getVoltageLevel().getNominalV() / 100.0, 2);
            float bmin = svc.getBmin() * factor;
            float binit = svc.getReactivePowerSetPoint();
            float bmax = svc.getBmax() * factor;
            EsgRegulatingMode xregsvc = (svc.getRegulationMode() == StaticVarCompensator.RegulationMode.VOLTAGE) ? EsgRegulatingMode.REGULATING : EsgRegulatingMode.NOT_REGULATING;
            float vregsvc = svc.getVoltageSetPoint();
            float qsvsch = 1.0f;
            esgNetwork.addStaticVarCompensator(
                    new EsgStaticVarCompensator(znamsvc, xsvcst, znodsvc, bmin, binit, bmax, xregsvc, vregsvc, qsvsch));
        }
    }

    public EsgNetwork createNetwork(EsgGeneralParameters parameters) {

        EsgNetwork esgNetwork = new EsgNetwork();

        // areas
        createAreas(esgNetwork);

        // nodes
        createNodes(esgNetwork);

        // coupling devices
        createCouplingDevices(esgNetwork);

        // lines
        createLines(esgNetwork, parameters);

        // transformers
        createTransformers(esgNetwork, parameters);

        // loads
        createLoads(esgNetwork);

        // generators
        createGenerators(esgNetwork);

        // shunts
        createBanks(esgNetwork);

        // static VAR compensators
        createStaticVarCompensators(esgNetwork);

        return esgNetwork;
    }

    public void write(Writer writer, EsgGeneralParameters parameters) throws IOException {
        EsgNetwork esgNetwork = createNetwork(parameters);
        new EsgWriter(esgNetwork, parameters).write(writer, network.getId() + "/" + network.getStateManager().getWorkingStateId());
    }

    public void write(Writer writer) throws IOException {
        write(writer, new EsgGeneralParameters());
    }

    public void write(Path file, EsgGeneralParameters parameters) throws IOException {
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            write(writer, parameters);
        }
    }

    public void write(Path file) throws IOException {
        write(file, new EsgGeneralParameters());
    }

}
