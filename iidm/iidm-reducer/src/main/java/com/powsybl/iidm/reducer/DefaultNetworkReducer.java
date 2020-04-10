/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.reducer;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;

import java.util.*;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class DefaultNetworkReducer extends AbstractNetworkReducer {

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

        if (getPredicate().test(vl1)) {
            reduce(line, vl1, terminal1);
        } else if (getPredicate().test(vl2)) {
            reduce(line, vl2, terminal2);
        } else {
            line.remove();
        }

        observers.forEach(o -> o.lineRemoved(line));
    }

    @Override
    protected void reduce(TwoWindingsTransformer transformer) {
        Terminal terminal1 = transformer.getTerminal1();
        Terminal terminal2 = transformer.getTerminal2();
        VoltageLevel vl1 = terminal1.getVoltageLevel();
        VoltageLevel vl2 = terminal2.getVoltageLevel();

        if (getPredicate().test(vl1)) {
            replaceTransformerByLoad(transformer, vl1, terminal1);
        } else if (getPredicate().test(vl2)) {
            replaceTransformerByLoad(transformer, vl2, terminal2);
        } else {
            transformer.remove();

        }

        observers.forEach(o -> o.transformerRemoved(transformer));
    }

    @Override
    protected void reduce(ThreeWindingsTransformer transformer) {
        VoltageLevel vl1 = transformer.getLeg1().getTerminal().getVoltageLevel();
        VoltageLevel vl2 = transformer.getLeg2().getTerminal().getVoltageLevel();
        VoltageLevel vl3 = transformer.getLeg3().getTerminal().getVoltageLevel();
        if (getPredicate().test(vl1) || getPredicate().test(vl2) || getPredicate().test(vl3)) {
            throw new UnsupportedOperationException("Reduction of three-windings transformers is not supported");
        } else {
            transformer.remove();
        }

        observers.forEach(o -> o.transformerRemoved(transformer));
    }

    @Override
    protected void reduce(HvdcLine hvdcLine) {
        VoltageLevel vl1 = hvdcLine.getConverterStation1().getTerminal().getVoltageLevel();
        VoltageLevel vl2 = hvdcLine.getConverterStation2().getTerminal().getVoltageLevel();
        if (getPredicate().test(vl1) || getPredicate().test(vl2)) {
            throw new UnsupportedOperationException("Reduction of HVDC lines is not supported");
        } else {
            hvdcLine.remove();
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
        Load load = replaceBranchByLoad(line, vl, terminal);
        observers.forEach(o -> o.lineReplaced(line, load));
    }

    private void replaceLineByDanglingLine(Line line, VoltageLevel vl, Terminal terminal) {
        Branch.Side side = line.getSide(terminal);

        DanglingLineAdder dlAdder = vl.newDanglingLine()
                .setId(line.getId())
                .setName(line.getOptionalName().orElse(null))
                .setR(line.getR() / 2)
                .setX(line.getX() / 2)
                .setB(side == Branch.Side.ONE ? line.getB1() : line.getB2())
                .setG(side == Branch.Side.ONE ? line.getG1() : line.getG2())
                .setP0(checkP(terminal))
                .setQ0(checkQ(terminal));
        fillNodeOrBus(dlAdder, terminal);

        line.remove();

        DanglingLine dl = dlAdder.add();
        dl.getTerminal()
                .setP(terminal.getP())
                .setQ(terminal.getQ());

        observers.forEach(o -> o.lineReplaced(line, dl));
    }

    private void replaceTransformerByLoad(TwoWindingsTransformer transformer, VoltageLevel vl, Terminal terminal) {
        Load load = replaceBranchByLoad(transformer, vl, terminal);
        observers.forEach(o -> o.transformerReplaced(transformer, load));
    }

    private Load replaceBranchByLoad(Branch<?> branch, VoltageLevel vl, Terminal terminal) {
        LoadAdder loadAdder = vl.newLoad()
                .setId(branch.getId())
                .setName(branch.getOptionalName().orElse(null))
                .setLoadType(LoadType.FICTITIOUS)
                .setP0(checkP(terminal))
                .setQ0(checkQ(terminal));
        fillNodeOrBus(loadAdder, terminal);

        branch.remove();

        Load load = loadAdder.add();
        load.getTerminal()
                .setP(terminal.getP())
                .setQ(terminal.getQ());

        return load;
    }

    private static void fillNodeOrBus(InjectionAdder adder, Terminal terminal) {
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
            throw new PowsyblException("The active power of '" + connectableId + "' (" + voltageLevelId + ") is not set. Do you forget to compute the flows?");
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
            throw new PowsyblException("The reactive power of '" + connectableId + "' (" + voltageLevelId + ") is not set. Do you forget to compute the flows?");
        }
        return terminal.getQ();
    }
}
