/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.reducer;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ActivePowerControlAdder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class DefaultNetworkReducer extends AbstractNetworkReducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultNetworkReducer.class);

    private final ReductionOptions options;

    private final List<NetworkReducerObserver> observers = new ArrayList<>();

    public DefaultNetworkReducer(NetworkPredicate predicate, ReductionOptions options) {
        this(predicate, options, Collections.emptyList());
    }

    public DefaultNetworkReducer(NetworkPredicate predicate, ReductionOptions options, NetworkReducerObserver... observers) {
        this(predicate, options, Arrays.asList(observers));
    }

    public DefaultNetworkReducer(NetworkPredicate predicate, ReductionOptions options, List<NetworkReducerObserver> observers) {
        super(predicate);
        this.options = Objects.requireNonNull(options);
        this.observers.addAll(Objects.requireNonNull(observers));
    }

    @Override
    protected void reduce(Substation substation) {
        substation.remove();
        observers.forEach(o -> o.substationRemoved(substation));
    }

    @Override
    protected void reduce(VoltageLevel voltageLevel) {
        voltageLevel.remove();
        observers.forEach(o -> o.voltageLevelRemoved(voltageLevel));
    }

    @Override
    protected void reduce(Line line) {
        Terminal terminal1 = line.getTerminal1();
        Terminal terminal2 = line.getTerminal2();
        VoltageLevel vl1 = terminal1.getVoltageLevel();
        VoltageLevel vl2 = terminal2.getVoltageLevel();

        if (test(vl1)) {
            reduce(line, vl1, terminal1);
        } else if (test(vl2)) {
            reduce(line, vl2, terminal2);
        } else {
            line.remove();
        }

        observers.forEach(o -> o.lineRemoved(line));
    }

    @Override
    protected void reduce(TieLine tieLine) {
        Terminal terminal1 = tieLine.getTerminal1();
        Terminal terminal2 = tieLine.getTerminal2();
        VoltageLevel vl1 = terminal1.getVoltageLevel();
        VoltageLevel vl2 = terminal2.getVoltageLevel();

        // just remove the tie line if one of its voltage levels has to be removed
        if (!test(vl1) || !test(vl2)) {
            tieLine.remove();
        }

        observers.forEach(o -> o.tieLineRemoved(tieLine));
    }

    @Override
    protected void reduce(TwoWindingsTransformer transformer) {
        Terminal terminal1 = transformer.getTerminal1();
        Terminal terminal2 = transformer.getTerminal2();
        VoltageLevel vl1 = terminal1.getVoltageLevel();
        VoltageLevel vl2 = terminal2.getVoltageLevel();

        if (test(vl1)) {
            replaceTransformerByLoad(transformer, vl1, terminal1);
        } else if (test(vl2)) {
            replaceTransformerByLoad(transformer, vl2, terminal2);
        } else {
            transformer.remove();

        }

        observers.forEach(o -> o.transformerRemoved(transformer));
    }

    @Override
    protected void reduce(ThreeWindingsTransformer transformer) {
        Terminal terminal1 = transformer.getLeg1().getTerminal();
        Terminal terminal2 = transformer.getLeg2().getTerminal();
        Terminal terminal3 = transformer.getLeg3().getTerminal();
        VoltageLevel vl1 = terminal1.getVoltageLevel();
        VoltageLevel vl2 = terminal2.getVoltageLevel();
        VoltageLevel vl3 = terminal3.getVoltageLevel();

        if (test(vl1) ^ test(vl2) ^ test(vl3)) {
            VoltageLevel vlToKeep;
            Terminal terminal;
            if (test(vl1)) {
                vlToKeep = vl1;
                terminal = terminal1;
            } else {
                vlToKeep = test(vl2) ? vl2 : vl3;
                terminal = test(vl2) ? terminal2 : terminal3;
            }
            replaceTransformerByLoad(transformer, vlToKeep, terminal);
        } else if (!(test(vl1) || test(vl2) || test(vl3))) {
            transformer.remove();
        } else {
            throw new UnsupportedOperationException("Keeping only 2 legs of the 3 windings transformer " + transformer.getId() +
                    " is not possible : the third one should have also been kept by reduction.");
        }
        observers.forEach(o -> o.transformerRemoved(transformer));
    }

    @Override
    protected void reduce(HvdcLine hvdcLine) {
        Terminal terminal1 = hvdcLine.getConverterStation1().getTerminal();
        Terminal terminal2 = hvdcLine.getConverterStation2().getTerminal();
        VoltageLevel vl1 = terminal1.getVoltageLevel();
        VoltageLevel vl2 = terminal2.getVoltageLevel();
        HvdcConverterStation<?> station1 = hvdcLine.getConverterStation1();
        HvdcConverterStation<?> station2 = hvdcLine.getConverterStation2();
        if (test(vl1)) {
            replaceHvdcLine(hvdcLine, vl1, terminal1, station1);
        } else if (test(vl2)) {
            replaceHvdcLine(hvdcLine, vl2, terminal2, station2);
        } else {
            hvdcLine.remove();
            station1.remove();
            station2.remove();
        }
        observers.forEach(o -> o.hvdcLineRemoved(hvdcLine));
    }

    private void reduce(Line line, VoltageLevel vl, Terminal terminal) {
        if (options.isWithDanglingLines()) {
            replaceLineByDanglingLine(line, vl, terminal);
        } else {
            replaceLineByLoad(line, vl, terminal);
        }
    }

    private void replaceLineByLoad(Line line, VoltageLevel vl, Terminal terminal) {
        Load load = replaceConnectableByLoad(line, vl, terminal);
        observers.forEach(o -> o.lineReplaced(line, load));
    }

    private void replaceLineByDanglingLine(Line line, VoltageLevel vl, Terminal terminal) {
        TwoSides side = line.getSide(terminal);

        DanglingLineAdder dlAdder = vl.newDanglingLine()
                .setId(line.getId())
                .setName(line.getOptionalName().orElse(null))
                .setR(line.getR() / 2)
                .setX(line.getX() / 2)
                .setB(side == TwoSides.ONE ? line.getB1() : line.getB2())
                .setG(side == TwoSides.ONE ? line.getG1() : line.getG2())
                .setP0(checkP(terminal))
                .setQ0(checkQ(terminal))
                .setHasShuntAdmittanceLineEquivalentModel(false);
        fillNodeOrBus(dlAdder, terminal);

        double p = terminal.getP();
        double q = terminal.getQ();
        line.remove();

        DanglingLine dl = dlAdder.add();
        dl.getTerminal()
                .setP(p)
                .setQ(q);

        observers.forEach(o -> o.lineReplaced(line, dl));
    }

    private void replaceTransformerByLoad(TwoWindingsTransformer transformer, VoltageLevel vl, Terminal terminal) {
        Load load = replaceConnectableByLoad(transformer, vl, terminal);
        observers.forEach(o -> o.transformerReplaced(transformer, load));
    }

    private void replaceTransformerByLoad(ThreeWindingsTransformer transformer, VoltageLevel vl, Terminal terminal) {
        Load load = replaceConnectableByLoad(transformer, vl, terminal);
        observers.forEach(o -> o.transformerReplaced(transformer, load));
    }

    private Load replaceConnectableByLoad(Connectable<?> connectable, VoltageLevel vl, Terminal terminal) {
        LoadAdder loadAdder = vl.newLoad()
                .setId(connectable.getId())
                .setName(connectable.getOptionalName().orElse(null))
                .setLoadType(LoadType.FICTITIOUS)
                .setP0(checkP(terminal))
                .setQ0(checkQ(terminal));
        fillNodeOrBus(loadAdder, terminal);

        double p = terminal.getP();
        double q = terminal.getQ();
        connectable.remove();

        Load load = loadAdder.add();
        load.getTerminal()
                .setP(p)
                .setQ(q);

        return load;
    }

    private void replaceHvdcLine(HvdcLine hvdcLine, VoltageLevel vl, Terminal terminal, HvdcConverterStation<?> station) {
        if (station.getHvdcType() == HvdcConverterStation.HvdcType.VSC) {
            VscConverterStation vscStation = (VscConverterStation) station;
            if (vscStation.isVoltageRegulatorOn()) {
                replaceHvdcLineByGenerator(hvdcLine, vl, terminal, vscStation);
            } else {
                replaceHvdcLineByLoad(hvdcLine, vl, terminal);
            }
        } else {
            replaceHvdcLineByLoad(hvdcLine, vl, terminal);
        }
    }

    private void replaceHvdcLineByLoad(HvdcLine hvdcLine, VoltageLevel vl, Terminal terminal) {
        LoadAdder loadAdder = vl.newLoad()
                .setId(hvdcLine.getId())
                .setName(hvdcLine.getOptionalName().orElse(null))
                .setLoadType(LoadType.FICTITIOUS)
                .setP0(checkP(terminal))
                .setQ0(checkQ(terminal));
        fillNodeOrBus(loadAdder, terminal);

        double p = terminal.getP();
        double q = terminal.getQ();
        HvdcConverterStation<?> converter1 = hvdcLine.getConverterStation1();
        HvdcConverterStation<?> converter2 = hvdcLine.getConverterStation2();
        hvdcLine.remove();
        converter1.remove();
        converter2.remove();

        Load load = loadAdder.add();
        load.getTerminal()
                .setP(p)
                .setQ(q);
        observers.forEach(o -> o.hvdcLineReplaced(hvdcLine, load));
    }

    private void replaceHvdcLineByGenerator(HvdcLine hvdcLine, VoltageLevel vl, Terminal terminal, VscConverterStation station) {
        double maxP = hvdcLine.getMaxP();
        GeneratorAdder genAdder = vl.newGenerator()
                .setId(hvdcLine.getId())
                .setName(hvdcLine.getOptionalName().orElse(null))
                .setEnergySource(EnergySource.OTHER)
                .setVoltageRegulatorOn(true)
                .setMaxP(maxP)
                .setMinP(-maxP)
                .setTargetP(-checkP(terminal))
                .setTargetV(station.getVoltageSetpoint());
        fillNodeOrBus(genAdder, terminal);

        double p = terminal.getP();
        double q = terminal.getQ();
        ReactiveLimits stationLimits = station.getReactiveLimits();

        HvdcConverterStation<?> converter1 = hvdcLine.getConverterStation1();
        HvdcConverterStation<?> converter2 = hvdcLine.getConverterStation2();
        hvdcLine.remove();
        converter1.remove();
        converter2.remove();

        Generator generator = genAdder.add();
        generator.getTerminal()
                .setP(p)
                .setQ(q);

        if (stationLimits != null) {
            if (stationLimits.getKind() == ReactiveLimitsKind.MIN_MAX) {
                MinMaxReactiveLimits minMaxLimits = (MinMaxReactiveLimits) stationLimits;
                generator.newMinMaxReactiveLimits()
                        .setMinQ(minMaxLimits.getMinQ())
                        .setMaxQ(minMaxLimits.getMaxQ())
                        .add();
            } else if (stationLimits.getKind() == ReactiveLimitsKind.CURVE) {
                ReactiveCapabilityCurve reactiveCurve = (ReactiveCapabilityCurve) stationLimits;
                ReactiveCapabilityCurveAdder curveAdder = generator.newReactiveCapabilityCurve();
                reactiveCurve.getPoints().forEach(point ->
                    curveAdder.beginPoint()
                            .setP(point.getP())
                            .setMinQ(point.getMinQ())
                            .setMaxQ(point.getMaxQ())
                            .endPoint()
                );
                curveAdder.add();
            }
        }

        generator.newExtension(ActivePowerControlAdder.class).withParticipate(false).add();

        observers.forEach(o -> o.hvdcLineReplaced(hvdcLine, generator));
    }

    private static void fillNodeOrBus(InjectionAdder<?, ?> adder, Terminal terminal) {
        if (terminal.getVoltageLevel().getTopologyKind() == TopologyKind.NODE_BREAKER) {
            adder.setNode(terminal.getNodeBreakerView().getNode());
        } else {
            if (terminal.isConnected()) {
                adder.setBus(terminal.getBusBreakerView().getBus().getId());
            }
            adder.setConnectableBus(terminal.getBusBreakerView().getConnectableBus().getId());
        }
    }

    private static double checkP(Terminal terminal) {
        if (!terminal.isConnected()) {
            return 0.0;
        }
        if (Double.isNaN(terminal.getP())) {
            String connectableId = terminal.getConnectable().getId();
            String voltageLevelId = terminal.getVoltageLevel().getId();
            LOGGER.warn("The active power of '{}' ({}) is undefined -> set to 0.0", connectableId, voltageLevelId);
            return 0.0;
        }
        return terminal.getP();
    }

    private static double checkQ(Terminal terminal) {
        if (!terminal.isConnected()) {
            return 0.0;
        }
        if (Double.isNaN(terminal.getQ())) {
            String connectableId = terminal.getConnectable().getId();
            String voltageLevelId = terminal.getVoltageLevel().getId();
            LOGGER.warn("The reactive power of '{}' ({}) is undefined -> set to 0.0", connectableId, voltageLevelId);
            return 0.0;
        }
        return terminal.getQ();
    }
}
