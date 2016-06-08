/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.eurostag;

import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.ddb.eurostag.model.PowerFlow;
import eu.itesla_project.iidm.ddb.eurostag.model.StateVariable;
import eu.itesla_project.iidm.ddb.eurostag.model.TransformerModel;
import eu.itesla_project.iidm.network.*;
import eu.itesla_project.iidm.network.ReactiveCapabilityCurve.Point;
import eu.itesla_project.iidm.network.util.Identifiables;
import eu.itesla_project.iidm.network.util.SV;
import eu.itesla_project.loadflow.api.LoadFlow;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.loadflow.api.LoadFlowParameters;
import eu.itesla_project.loadflow.api.LoadFlowResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EurostagStepUpTransformerInserter {

    private static final Logger LOGGER = LoggerFactory.getLogger(EurostagStepUpTransformerInserter.class);

    private final static float SB = 100;
    private final static float ACCEPTABLE_VOLTAGE_DIFF = 0.1f;
    private final static float INFINITE_REACTIVE_LIMIT = 9999f;

    public enum InsertionStatus {
        OK("ok"),
        ALREADY_DONE("step-up transformer already in the network"),
        ID_NOT_FOUND("cim id not found in the dictionary"),
        DYNAMIC_DATA_NOT_FOUND(".tg file not found"),
        DYNAMIC_DATA_ERROR(".tg file containes errors");

        private final String label;

        InsertionStatus(String label) {
            this.label = label;
        }

        private String getLabel() {
            return label;
        }
    }

    public static class StateBefore {

        private final Map<String, Float> buses = new TreeMap<>();

        private final Map<String, PowerFlow> injections = new HashMap<>();

        public void debug(Network n) {
            for (TwoWindingsTransformer twt : n.getTwoWindingsTransformers()) {
                PowerFlow pf = injections.get(twt.getId());
                if (pf != null) {
                    double dp = pf.p - (Float.isNaN(twt.getTerminal1().getP()) ? 0 : twt.getTerminal1().getP());
                    double dq = pf.q - (Float.isNaN(twt.getTerminal1().getQ()) ? 0 : twt.getTerminal1().getQ());
                    if (dp > 1 || dq > 1) {
                        LOGGER.warn("Mismatch detected for {}: {},{}-> {},{}",
                                twt.getId(), pf.p, pf.q, twt.getTerminal1().getP(), twt.getTerminal1().getQ());
                    }
                }
            }

            // for debug
            for (Bus b : n.getBusBreakerView().getBuses()) {
                if (buses.containsKey(b.getId())) {
                    buses.put(b.getId(), Math.abs(buses.get(b.getId()) - b.getV()));
                }
            }

            List<Map.Entry<String, Float>> diffs = buses.entrySet().stream()
                    .filter(e -> e.getValue() > ACCEPTABLE_VOLTAGE_DIFF)
                    .sorted((o1, o2) -> Float.compare(o2.getValue(), o1.getValue()))
                    .collect(Collectors.toList());

            if (diffs.size() > 0) {
                float max = diffs.stream().max((o1, o2) -> Float.compare(o1.getValue(), o2.getValue())).get().getValue();
                LOGGER.warn("{} buses on {} with a voltage difference greater than {}, max is {}",
                        diffs.size(), buses.size(), ACCEPTABLE_VOLTAGE_DIFF, max);
                if (LOGGER.isTraceEnabled()) {
                    int displayLimit = 10;
                    LOGGER.trace("{} biggest voltage differences:", displayLimit);
                    diffs.stream().limit(displayLimit).forEach(e -> LOGGER.trace("{}: {} KV", e.getKey(), e.getValue()));
                }
            }
        }
    }

    private EurostagStepUpTransformerInserter() {
    }

    /**
     *  plvgen  plvload
     *  qlvgen  qlvload
     *    |      |
     *  ------------
     *       |
     *       O
     *       O
     *       |
     *  ------------
     *    |      |
     *  phvgen  phvload
     *  qhvgen  qhvload
     */
    private static StateVariable toLvGenPf(TransformerModel transformerModel, StateVariable hvGenSv, PowerFlow hvLoadPf, PowerFlow lvLoadPf) {
        double p1 = (hvGenSv.p + (hvLoadPf != null ? hvLoadPf.p : 0f));
        double q1 = (hvGenSv.q + (hvLoadPf != null ? hvLoadPf.q : 0f));
        StateVariable sv1 = new StateVariable(p1, q1, hvGenSv.u, hvGenSv.theta);
        StateVariable sv2 = transformerModel.toSv2(sv1);
        double pLvGen = -(sv2.p + (lvLoadPf != null ? lvLoadPf.p : 0f));
        double qLvGen = -(sv2.q + (lvLoadPf != null ? lvLoadPf.q : 0f));
        StateVariable sv3 = new StateVariable(pLvGen, qLvGen, sv2.u, sv2.theta);
//        LOGGER.trace("{} -> {}", sv1, sv3);
        return sv3;
    }

    private static TwoWindingsTransformer createTransformer(TG tg, Generator hvGen, VoltageLevel lvVl, Bus lvBus, float vbaseHvBdd, float vbaseLvBdd,
                                                            EurostagStepUpTransformerConfig config) {
        VoltageLevel hvVl = hvGen.getTerminal().getVoltageLevel();
        Substation s = hvVl.getSubstation();
        // create the transformer
        //   - side 1 -> high voltage
        //   - side 2 -> low voltage
        TwoWindingsTransformerAdder twta = s.newTwoWindingsTransformer()
                .setId(tg.fileName)
                .setVoltageLevel1(hvVl.getId())
                .setVoltageLevel2(lvVl.getId());
        // connect the high voltage side on the same bus/node than the generator even if it is disconnected that is
        // why we use connectable bus
        if (hvVl.getTopologyKind() == TopologyKind.BUS_BREAKER) {
            twta.setBus1(hvGen.getTerminal().getBusBreakerView().getConnectableBus().getId());
            twta.setConnectableBus1(hvGen.getTerminal().getBusBreakerView().getConnectableBus().getId());
        } else {
            twta.setNode1(hvGen.getTerminal().getNodeBreakerView().getNode());
        }
        // pcu ucc are given as %, hence the factor 100f
        float ucc = tg.t4x.ucc.get(tg.t4x.ktpnom - 1);
        float r_pu = config.isNoActiveLosses() ? 0f : tg.t4x.pcu / 100f * SB / tg.t4x.rate;
        float z_pu = ucc / 100f;
        float x_pu = (float) Math.sqrt(z_pu * z_pu - r_pu * r_pu) * SB / tg.t4x.rate;
        float uno1 = tg.t4x.uno1.get(tg.t4x.ktpnom - 1);
        float uno2 = tg.t4x.uno2.get(tg.t4x.ktpnom - 1);
        float zb2 = (float) (Math.pow(vbaseLvBdd, 2) / SB);
        float r = r_pu * zb2;
        float x = x_pu * zb2;
        float ratedU1 = Math.max(uno1, uno2);
        float ratedU2 = Math.min(uno1, uno2);
        TwoWindingsTransformer twt = twta
                .setBus2(lvBus.getId())
                .setConnectableBus2(lvBus.getId())
                .setRatedU1(ratedU1)
                .setRatedU2(ratedU2)
                .setR(r)
                .setX(x)
                .setG(0f)
                .setB(0f)
                .add();
        LOGGER.trace("Creating transformer '{}' (r_pu={}, x_pu={}, r={}, x={})", twt.getId(), r_pu, x_pu, r, x);
        if (tg.t4x.uno1.size() > 0) {
            for (float depha : tg.t4x.dephas) {
                if (depha != 0) {
                    throw new RuntimeException("A step up transformer is not supposed to shift phase");
                }
            }
            RatioTapChangerAdder rtca = twt.newRatioTapChanger()
                    .setLoadTapChangingCapabilities(false);
            float a = (ratedU2 / vbaseLvBdd) / (ratedU1 / vbaseHvBdd);
            for (int i = 0; i < tg.t4x.uno1.size(); i++) {
                float uno1_i = tg.t4x.uno1.get(i);
                float uno2_i = tg.t4x.uno2.get(i);
                float ucc_i = tg.t4x.ucc.get(i);
                float z_pu_i = ucc_i / 100f;
                float ratedU1_i = Math.max(uno1_i, uno2_i);
                float ratedU2_i = Math.min(uno1_i, uno2_i);
                float x_pu_i = (float) Math.sqrt(z_pu_i * z_pu_i - r_pu * r_pu) * SB / tg.t4x.rate;
                float x_i = x_pu_i * zb2;
                float a_i = (ratedU2_i / vbaseLvBdd) / (ratedU1_i / vbaseHvBdd);
                rtca.beginStep()
                        .setRho(a_i / a)
                        .setR(0f)
                        .setX(100f * (x_i - x) / x)
                        .setG(0f)
                        .setB(0f)
                        .endStep();
            }
            rtca.setCurrentStepPosition(tg.t4x.ktap8 - 1)
                    .add();
        }
        return twt;
    }

    private static void createAux(TG tg, IdDictionary auxDict,
                                  Network n, VoltageLevel lvVl, String lvBusId, VoltageLevel hvVl,
                                  Generator hvGen, Bus hvGenConnectableBus, PowerFlow lvAuxPf, PowerFlow hvAuxPf,
                                  TransformerModel transformerModel, EurostagStepUpTransformerConfig config) {
        if (config.isMoveAux() && tg.lh != null && tg.lh.znodlo.equals(lvBusId)) {

            for (String hvAuxId : auxDict.getAll(hvGen.getId())) {
                Load hvAux = n.getLoad(hvAuxId);
                if (hvAux != null && hvAux.getTerminal().getBusBreakerView().getConnectableBus() == hvGenConnectableBus) {
                    if (hvAux.getTerminal().getBusView().getBus() != null) {
                        if (!Float.isNaN(hvAux.getTerminal().getP())) {
                            hvAuxPf.p = hvAux.getTerminal().getP();
                        }
                        if (!Float.isNaN(hvAux.getTerminal().getQ())) {
                            hvAuxPf.q = hvAux.getTerminal().getQ();
                        }
                    }
                    hvAux.remove();
                    LOGGER.trace("Removing HV axiliary '{}' (p={}, q={})",
                            hvAux.getId(), hvAux.getTerminal().getP(), hvAux.getTerminal().getQ());
                    break;
                }
            }

            for (Load l : hvGenConnectableBus.getLoads()) {
                if (l.getLoadType() != LoadType.FICTITIOUS) {
                    LOGGER.warn("Load {} connected to high voltage bus, maybe this is an auxiliary and should be move to low level?",
                            l.getId());
                }
            }

            // transfer auxiliary load from hv to lv
            if (hvAuxPf.isValid()) {
                float v = hvGenConnectableBus.getV();
                if (Float.isNaN(v)) {
                    v = hvVl.getNominalV();
                }
                StateVariable auxSv = transformerModel.toSv2(new StateVariable(-hvAuxPf.p, -hvAuxPf.q, v, 0));
                lvAuxPf.p = auxSv.p;
                lvAuxPf.q = auxSv.q;
            }

            if (hvGen.getTerminal().getBusView().getBus() != null) { // it means that we can use the generator to balance the auxiliary difference
                if (tg.lh.pl > lvAuxPf.p) {
                    lvAuxPf.p = tg.lh.pl;
                }
                if (tg.lh.ql > lvAuxPf.q) {
                    lvAuxPf.q = tg.lh.ql;
                }
            }

            Load lvAux = lvVl.newLoad()
                    .setId(hvGen.getId() + "_AUX")
                    .setBus(lvBusId)
                    .setConnectableBus(lvBusId)
                    .setLoadType(LoadType.AUXILIARY)
                    .setP0((float) lvAuxPf.p)
                    .setQ0((float) lvAuxPf.q)
                    .add();
            lvAux.getTerminal()
                    .setP((float) lvAuxPf.p)
                    .setQ((float) lvAuxPf.q);

            LOGGER.trace("Creating LV axiliary '{}' (p={}, q={})",
                    lvAux.getId(), lvAuxPf.p, lvAuxPf.q);
        }
    }

    private static void fillGeneratorState(Generator g, StateVariable sv) {
        Terminal t = g.getTerminal();
        Bus b = t.getBusBreakerView().getBus();
        if (Float.isNaN(t.getP())) {
            sv.p = 0;
        } else {
            sv.p = t.getP();
        }
        if (Float.isNaN(t.getQ())) {
            sv.q = 0;
        } else {
            sv.q = t.getQ();
        }
        if (b != null && !Float.isNaN(b.getV()) && !Float.isNaN(b.getAngle())) { // generator is connected
            sv.u = b.getV();
            sv.theta = b.getAngle();
        } else {
            sv.u = t.getVoltageLevel().getNominalV();
            sv.theta = 0;
        }
        if (!sv.isValid()) {
            throw new RuntimeException("Invalid sv " + g.getId() + ": " + sv);
        }
    }

    private static Generator moveGenerator(Generator srcGen, StateVariable srcSv, VoltageLevel destVl, Bus destBus, boolean connected,
                                           Function<StateVariable, StateVariable> fct, EurostagStepUpTransformerConfig config) {

        fillGeneratorState(srcGen, srcSv);

        StateVariable destTargetSv = fct.apply(srcSv);

        float newTargetP = (float) -destTargetSv.p;
        float newTargetQ = (float) -destTargetSv.q;
        float newTargetV = (float) destTargetSv.u;

        destBus.setV((float) destTargetSv.u);
        destBus.setAngle((float) destTargetSv.theta);

        LOGGER.trace("Resizing set points of '{}', p0: {} -> {}, q0: {} -> {}, v0: {} -> {}",
                srcGen.getId(), -srcSv.p, newTargetP,
                -srcSv.q, newTargetQ, srcSv.u, newTargetV);

        float newMinP = (float) -fct.apply(new StateVariable(-srcGen.getMinP(), srcSv.q, srcSv.u, srcSv.theta)).p;
        float newMaxP = (float) -fct.apply(new StateVariable(-srcGen.getMaxP(), srcSv.q, srcSv.u, srcSv.theta)).p;

        LOGGER.trace("Resizing active limits of {} [{}, {}] -> [{}, {}]",
                srcGen.getId(), srcGen.getMinP(), srcGen.getMaxP(), newMinP, newMaxP);

        String srcGenId = srcGen.getId();
        String srcGenName = srcGen.getName();
        EnergySource srcGenEnergySource = srcGen.getEnergySource();
        boolean secGenVoltageRegulatorOn = srcGen.isVoltageRegulatorOn();

        // move the generator and fix the reactive limits
        srcGen.remove();

        Generator lvGen = destVl.newGenerator()
                .setId(srcGenId)
                .setName(srcGenName)
                .setBus(connected ? destBus.getId() : null) // connect the generator if it was connected at original level
                .setConnectableBus(destBus.getId())
                .setEnergySource(srcGenEnergySource)
                .setVoltageRegulatorOn(secGenVoltageRegulatorOn)
                .setMinP(newMinP)
                .setMaxP(newMaxP)
                .setTargetP(newTargetP)
                .setTargetV(newTargetV)
                .setTargetQ(newTargetQ)
                .add();

        if (srcGen.getReactiveLimits() != null) {
            switch (srcGen.getReactiveLimits().getKind()) {
                case MIN_MAX: {
                    MinMaxReactiveLimits limits = srcGen.getReactiveLimits(MinMaxReactiveLimits.class);
                    float newMinQ = config.isNoReactiveLimits() ? -INFINITE_REACTIVE_LIMIT : (float) -fct.apply(new StateVariable(srcSv.p, -limits.getMinQ(), srcSv.u, srcSv.theta)).q;
                    float newMaxQ = config.isNoReactiveLimits() ? INFINITE_REACTIVE_LIMIT : (float) -fct.apply(new StateVariable(srcSv.p, -limits.getMaxQ(), srcSv.u, srcSv.theta)).q;
                    LOGGER.trace("Resizing reactive limits of '{}': [{}, {}] -> [{}, {}]",
                            lvGen.getId(), limits.getMinQ(), limits.getMaxQ(), newMinQ, newMaxQ);
                    lvGen.newMinMaxReactiveLimits()
                            .setMinQ(newMinQ)
                            .setMaxQ(newMaxQ)
                            .add();
                    break;
                }
                case CURVE: {
                    ReactiveCapabilityCurve curve = srcGen.getReactiveLimits(ReactiveCapabilityCurve.class);
                    ReactiveCapabilityCurveAdder rcca = lvGen.newReactiveCapabilityCurve();
                    for (Point point : curve.getPoints()) {
                        float newP = (float) -fct.apply(new StateVariable(-point.getP(), 0, srcSv.u, srcSv.theta)).p;
                        float newMinQ = config.isNoReactiveLimits() ? -INFINITE_REACTIVE_LIMIT : (float) -fct.apply(new StateVariable(srcSv.p, -point.getMinQ(), srcSv.u, srcSv.theta)).q;
                        float newMaxQ = config.isNoReactiveLimits() ? INFINITE_REACTIVE_LIMIT : (float) -fct.apply(new StateVariable(srcSv.p, -point.getMaxQ(), srcSv.u, srcSv.theta)).q;
                        LOGGER.trace("Resizing reactive limits of '{}': [{}, {}] -> [{}, {}]",
                                lvGen.getId(), point.getMinQ(), point.getMaxQ(), newMinQ, newMaxQ);
                        rcca.beginPoint()
                                .setP(newP)
                                .setMinQ(newMinQ)
                                .setMaxQ(newMaxQ)
                                .endPoint();
                    }
                    rcca.add();
                    break;
                }

                default:
                    throw new AssertionError();
            }
        }

        return lvGen;
    }

    public static InsertionStatus insert(Generator hvGen, TG tg, IdDictionary auxDict, EurostagStepUpTransformerConfig config,
                                         StateBefore stateBefore) {
        Terminal hvT = hvGen.getTerminal();
        VoltageLevel hvVl = hvT.getVoltageLevel();
        Substation s = hvVl.getSubstation();
        Network n = s.getNetwork();
        float hvNomV = hvVl.getNominalV();
        Bus hvGenBus = hvT.getBusView().getBus();
        Bus hvGenConnectableBus = hvT.getBusBreakerView().getConnectableBus();
        if (hvGenConnectableBus == null) {
            throw new RuntimeException("Generator " + hvGen.getId() + " is not connectable");
        }

        // skip generators connected to low level voltage
        if (hvNomV < 25) {
            return InsertionStatus.ALREADY_DONE;
        }

        // create on the same substation the low voltage voltage level to move the
        // generator
        float vbase1 = tg.f1.vbase;
        float vbase2 = tg.f2.vbase;

        float vbaseHvBdd = Math.max(vbase1, vbase2);
        float vbaseLvBdd = Math.min(vbase1, vbase2);

        // JBH: without the following test we are creating step up TFO on a voltage
        // level inconsistent with what is expected in the .tg files
        if (hvNomV != vbaseHvBdd) {
            LOGGER.warn("Hight nominal voltage ({}) of step up transformer is inconsistent with the one ({}) of its generator '{}'",
                    vbaseHvBdd, hvNomV, hvGen.getId());
            return InsertionStatus.DYNAMIC_DATA_ERROR;
        }

        LOGGER.trace("Adding step up transformer to generator '{}' ('{}')", hvGen.getId(), tg.fileName);

        VoltageLevel lvVl = s.newVoltageLevel()
                .setId(hvGen.getId() + "_VL")
                .setNominalV(vbaseLvBdd)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        LOGGER.trace("Creating voltage level '{}' in substation '{}'", lvVl.getId(), s.getId());

        // create the low voltage bus
        String lvBusId = vbase1 > vbase2 ? tg.t4x.name2 : tg.t4x.name1;
        if (lvBusId.equals(tg.fileName)) { // Elia case
            lvBusId = lvBusId + "_bus";
        }
        Bus lvBus = lvVl.getBusBreakerView().newBus()
                .setId(lvBusId)
            .add();
        LOGGER.trace("Creating bus '{}' in voltage level '{}'", lvBusId, lvVl.getId());

        TwoWindingsTransformer twt = createTransformer(tg, hvGen, lvVl, lvBus, vbaseHvBdd, vbaseLvBdd, config);

        TransformerModel transformerModel = new TransformerModel(SV.getR(twt), SV.getX(twt), SV.getG(twt), SV.getB(twt), SV.getRatio(twt));

        // remove high voltage auxiliary and create another one at low
        // voltage according to Eurostag DDB
        PowerFlow lvAuxPf = new PowerFlow(0, 0);
        PowerFlow hvAuxPf = new PowerFlow(0, 0);
        createAux(tg, auxDict, n, lvVl, lvBusId, hvVl, hvGen, hvGenConnectableBus, lvAuxPf, hvAuxPf, transformerModel, config);

        LOGGER.trace("Moving generator '{}' from '{}' to '{}'", hvGen.getId(), hvVl.getId(), lvVl.getId());

        Function<StateVariable, StateVariable> fct = sv -> toLvGenPf(transformerModel, sv, hvAuxPf, lvAuxPf);

        StateVariable hvGenSv = new StateVariable();
        moveGenerator(hvGen, hvGenSv, lvVl, lvBus, hvGenBus != null, fct, config);

        stateBefore.injections.put(twt.getId(), new PowerFlow((float) (hvGenSv.p + hvAuxPf.p), (float) (hvGenSv.q + hvAuxPf.q)));

        return InsertionStatus.OK;
    }

    public static InsertionStatus insert(Generator g, Path ddbFile, IdDictionary auxDict, EurostagStepUpTransformerConfig config, StateBefore stateBefore) throws IOException {
        if (!Files.exists(ddbFile) && !Files.isRegularFile(ddbFile)) {
            throw new IllegalArgumentException(ddbFile + " must exist and be a file");
        }
        LOGGER.trace("Parsing {} ...", ddbFile);
        TG tg = TG.parse(ddbFile);
        tg.print();
        return insert(g, tg, auxDict, config, stateBefore);
    }

    public static InsertionStatus insert(Generator g, EurostagDDB ddb, IdDictionary genDict, IdDictionary auxDict, EurostagStepUpTransformerConfig config, StateBefore stateBefore) throws IOException {
        String ddbId = genDict.get(g.getId());
        if (ddbId == null || ddbId.trim().isEmpty()) {
            LOGGER.trace("CIM id '{}' not found in the dictionnary", g.getId());
            return InsertionStatus.ID_NOT_FOUND;
        }
        Path ddbFile = ddb.findGenerator(ddbId);
        if (ddbFile == null) {
            LOGGER.trace("Cannot find dynamic data for generator '{}'", ddbId);
            return InsertionStatus.DYNAMIC_DATA_NOT_FOUND;
        }
        return insert(g, ddbFile, auxDict, config, stateBefore);
    }

    private static void throwsUnexpectedTopology() {
        throw new RuntimeException("Unexpected stator substation topology");
    }

    private static void removeStepUpTransformersAlreadyPresents(Network n, List<String> statorVoltageLevels,
                                                                EurostagStepUpTransformerConfig config) {

        for (String lvVlId : statorVoltageLevels) {
            VoltageLevel lvVl = n.getVoltageLevel(lvVlId);
            if (lvVl == null) {
                continue;
            }

            for (Bus lvBus : lvVl.getBusBreakerView().getBuses()) {

                // check there is:
                //    - one two windings transformers
                //    - one ore more generators
                //    - zero ore more loads
                //    - nothing else!!!!!

                List<Load> auxLs = new ArrayList<>();
                List<TwoWindingsTransformer> twtLs = new ArrayList<>();
                List<Generator> genLs = new ArrayList<>();

                lvBus.visitConnectedOrConnectableEquipments(new AbstractTopologyVisitor() {
                    @Override
                    public void visitLoad(Load load) {
                        auxLs.add(load);
                    }


                    @Override
                    public void visitTwoWindingsTransformer(TwoWindingsTransformer transformer, TwoWindingsTransformer.Side side) {
                        twtLs.add(transformer);
                    }

                    @Override
                    public void visitGenerator(Generator generator) {
                        genLs.add(generator);
                    }

                    @Override
                    public void visitBusbarSection(BusbarSection section) {
                        throwsUnexpectedTopology();
                    }

                    @Override
                    public void visitDanglingLine(DanglingLine danglingLine) {
                        throwsUnexpectedTopology();
                    }

                    @Override
                    public void visitLine(Line line, Line.Side side) {
                        throwsUnexpectedTopology();
                    }

                    @Override
                    public void visitShuntCompensator(ShuntCompensator sc) {
                        throwsUnexpectedTopology();
                    }

                    @Override
                    public void visitThreeWindingsTransformer(ThreeWindingsTransformer transformer, ThreeWindingsTransformer.Side side) {
                        throwsUnexpectedTopology();
                    }
                });

                if (twtLs.isEmpty()) {
                    for (Generator g : genLs) {
                        g.remove();
                    }
                    for (Load aux : auxLs) {
                        aux.remove();
                    }
                    continue;
                }

                if (twtLs.size() != 1) {
                    throwsUnexpectedTopology();
                }

                TwoWindingsTransformer twt = twtLs.get(0);

                TransformerModel transformerModel = new TransformerModel(SV.getR(twt),
                                                                         SV.getX(twt),
                                                                         SV.getG(twt),
                                                                         SV.getB(twt),
                                                                         SV.getRatio(twt));

                VoltageLevel hvVl;
                Bus hvBus;
                if (twt.getTerminal1().getBusBreakerView().getConnectableBus() == lvBus) {
                    hvVl = twt.getTerminal2().getVoltageLevel();
                    hvBus = twt.getTerminal2().getBusBreakerView().getConnectableBus();
                } else if (twt.getTerminal2().getBusBreakerView().getConnectableBus() == lvBus) {
                    hvVl = twt.getTerminal1().getVoltageLevel();
                    hvBus = twt.getTerminal1().getBusBreakerView().getConnectableBus();
                } else {
                    throw new RuntimeException("Unexpected stator substation topology");
                }

                Function<StateVariable, StateVariable> fct = sv -> {
                    StateVariable otherSideSv;
                    if (twt.getTerminal2().getBusBreakerView().getConnectableBus() == lvBus) {
                        otherSideSv = transformerModel.toSv1(new StateVariable(-sv.p, -sv.q, sv.u, sv.theta));
                    } else if (twt.getTerminal1().getBusBreakerView().getConnectableBus() == lvBus) {
                        otherSideSv = transformerModel.toSv2(new StateVariable(-sv.p, -sv.q, sv.u, sv.theta));
                    } else {
                        throw new RuntimeException("Unexpected stator substation topology");
                    }
                    if (!Float.isNaN(hvBus.getV())) {
                        otherSideSv.u = hvBus.getV();
                    }
                    if (!Float.isNaN(hvBus.getAngle())) {
                        otherSideSv.theta = hvBus.getAngle();
                    }
                    return otherSideSv;
                };

                for (Generator lvGen : genLs) {
                    LOGGER.trace("Removing step up transformer '{}' of generator '{}'", twt.getId(), lvGen.getId());

                    boolean connected = twt.getTerminal1().isConnected() && twt.getTerminal2().isConnected() && lvGen.getTerminal().isConnected();

                    StateVariable lvGenSv = new StateVariable();
                    moveGenerator(lvGen, lvGenSv, hvVl, hvBus, connected, fct, config);
                }

                for (Load aux : auxLs) {

                    float v = lvBus.getV();
                    if (Float.isNaN(v)) {
                        v = lvVl.getNominalV();
                    }
                    float a = lvBus.getAngle();
                    if (Float.isNaN(a)) {
                        a = 0;
                    }

                    StateVariable hlSvAux = fct.apply(new StateVariable(-aux.getP0(), -aux.getQ0(), v, a));

                    float newP0 = (float) -hlSvAux.p;
                    float newQ0 = (float) -hlSvAux.q;

                    boolean connected = aux.getTerminal().getBusBreakerView().getBus() != null;

                    LOGGER.trace("Moving auxiliary '{}' to high level: (p: {} -> {}, q:{} -> {})",
                            aux.getId(), aux.getP0(), newP0, aux.getQ0(), newQ0);

                    LoadType loadType = aux.getLoadType();
                    if (loadType != LoadType.FICTITIOUS) {
                        loadType = LoadType.AUXILIARY;
                    }

                    aux.remove();

                    hvVl.newLoad()
                            .setId(aux.getId())
                            .setName(aux.getName())
                            .setLoadType(loadType)
                            .setBus(connected ? hvBus.getId() : null)
                            .setConnectableBus(hvBus.getId())
                            .setP0(newP0)
                            .setQ0(newQ0)
                            .add();
                }

                twt.remove();
            }
        }
    }

    public static void insert(Network n, LoadFlowFactory loadFlowFactory, ComputationManager computationManager, List<Path> ddbDirs,
                              IdDictionary genDict, IdDictionary auxDict, List<String> statorVoltageLevels, EurostagStepUpTransformerConfig config) throws Exception {
        long start = System.currentTimeMillis();

        LoadFlow loadFlow = loadFlowFactory.create(n, computationManager, 0);
        LoadFlowResult result = loadFlow.run(new LoadFlowParameters(LoadFlowParameters.VoltageInitMode.UNIFORM_VALUES));
//            if (LOGGER.isTraceEnabled()) {
//                LOGGER.trace("\n{}", result.getLogs());
//            }
        if (!result.isOk()) {
            throw new RuntimeException("Load flow diverged before stepup transformer expansion");
        }

        StateBefore stateBefore = new StateBefore();

        for (Bus b : n.getBusBreakerView().getBuses()) {
            stateBefore.buses.put(b.getId(), b.getV());
        }

        EurostagDDB ddb = new EurostagDDB(ddbDirs);
        Map<InsertionStatus, Set<String>> count = new EnumMap<>(InsertionStatus.class);
        for (InsertionStatus s : InsertionStatus.values()) {
            count.put(s, new TreeSet<>());
        }

        if (config.isRemoveAlreadyExistingStators()) {
            removeStepUpTransformersAlreadyPresents(n, statorVoltageLevels, config);

            result = loadFlow.run(new LoadFlowParameters(LoadFlowParameters.VoltageInitMode.UNIFORM_VALUES));
//            if (LOGGER.isTraceEnabled()) {
//                LOGGER.trace("\n{}", result.getLogs());
//            }
            if (!result.isOk()) {
                throw new RuntimeException("Load flow diverged after stepup transformer expansion");
            }
        }

        for (Generator g : Identifiables.sort(n.getGenerators())) {
            count.get(insert(g, ddb, genDict, auxDict, config, stateBefore)).add(g.getId());
        }
        LOGGER.info("{} step-up transformers added in {} ms", count.get(InsertionStatus.OK).size(), (System.currentTimeMillis() - start));

        int generatorsNotMoved = n.getGeneratorCount() - count.get(InsertionStatus.OK).size();
        if (generatorsNotMoved > 0) {
            LOGGER.warn("{} generators have not been moved for the following reasons:", generatorsNotMoved);
            count.entrySet().stream().filter(entry -> entry.getKey() != InsertionStatus.OK && entry.getValue().size() > 0)
                    .forEach(entry -> LOGGER.warn("    * {} ({})", entry.getKey().getLabel(), entry.getValue().size()));
        }
        LOGGER.debug("Detailed report:");
        for (Map.Entry<InsertionStatus, Set<String>> entry : count.entrySet()) {
            LOGGER.debug("    {}: {}", entry.getKey(), entry.getValue());
        }

        result = loadFlow.run(new LoadFlowParameters(LoadFlowParameters.VoltageInitMode.UNIFORM_VALUES));
//            if (LOGGER.isTraceEnabled()) {
//                LOGGER.trace("\n{}", result.getLogs());
//            }
        if (!result.isOk()) {
            throw new RuntimeException("Load flow diverged after stepup transformer expansion");
        }

        stateBefore.debug(n);
    }

    public static void insert(Network n, LoadFlowFactory loadFlowFactory, ComputationManager computationManager, List<Path> ddbDirs, IdDictionary genDict, IdDictionary auxDict, List<String> statorVoltageLevels) throws Exception {
        insert(n, loadFlowFactory, computationManager, ddbDirs, genDict, auxDict, statorVoltageLevels, EurostagStepUpTransformerConfig.load());
    }

    public static void insert(Network n, LoadFlowFactory loadFlowFactory, ComputationManager computationManager, Path ddbDir, Path genDictFile, Path auxDictFile, EurostagStepUpTransformerConfig config) throws Exception {
        insert(n, loadFlowFactory, computationManager, ddbDir, genDictFile, auxDictFile, null, config);
    }

    public static List<String> readStatorVoltageLevels(Path statorVoltageLevelsFile) throws IOException {
        List<String> statorVoltageLevels = new ArrayList<>();
        if (statorVoltageLevelsFile != null && Files.exists(statorVoltageLevelsFile)) {
            try (Stream<String> stream = Files.lines(statorVoltageLevelsFile)) {
                statorVoltageLevels.addAll(stream
                        .filter(line -> !line.trim().isEmpty())
                        .collect(Collectors.toList()));
            }
        }
        return statorVoltageLevels;
    }

    public static void insert(Network n, LoadFlowFactory loadFlowFactory, ComputationManager computationManager, Path ddbDir, Path genDictFile, Path auxDictFile, Path statorVoltageLevelsFile, EurostagStepUpTransformerConfig config) throws Exception {
        IdDictionary genDict = new IdDictionary();
        genDict.loadCsv(genDictFile, 0, 1);
        IdDictionary auxDict = new IdDictionary();
        auxDict.loadCsv(auxDictFile, 0, 1);
        List<String> statorVoltageLevels = readStatorVoltageLevels(statorVoltageLevelsFile);
        insert(n, loadFlowFactory, computationManager, Arrays.asList(ddbDir), genDict, auxDict, statorVoltageLevels, config);
    }

    public static void insert(Network n, LoadFlowFactory loadFlowFactory, ComputationManager computationManager, Path ddbDir, Path genDictFile, Path auxDictFile) throws Exception {
        insert(n, loadFlowFactory, computationManager, ddbDir, genDictFile, auxDictFile, null, EurostagStepUpTransformerConfig.load());
    }

    public static void insert(Network n, LoadFlowFactory loadFlowFactory, ComputationManager computationManager, Path ddbDir, Path genDictFile, Path auxDictFile, Path statorVoltageLevelsFile) throws Exception {
        insert(n, loadFlowFactory, computationManager, ddbDir, genDictFile, auxDictFile, statorVoltageLevelsFile, EurostagStepUpTransformerConfig.load());
    }

}
