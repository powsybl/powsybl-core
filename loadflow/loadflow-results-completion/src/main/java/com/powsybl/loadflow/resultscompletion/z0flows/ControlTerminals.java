/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.resultscompletion.z0flows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.DanglingLine.Generation;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.ReactiveLimits;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.StaticVarCompensator.RegulationMode;
import com.powsybl.iidm.network.extensions.CoordinatedReactiveControl;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VscConverterStation;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class ControlTerminals {

    public enum ControlType {
        ACTIVE, REACTIVE
    }

    public ControlTerminals(List<Z0Vertex> z0Vertices, ControlType type) {
        this.z0Vertices = z0Vertices;
        this.type = type;
        this.controlTerminals = findControlTerminals();
    }

    public void distribute(double balance) {
        checkAndFixPercents();
        double residue = -balance; // the opposite
        List<ControlTerminal> nonSaturated = findValidControlTerminals(residue);
        while (residue != 0.0 && !nonSaturated.isEmpty()) {
            double totalPercent = calculateTotalPercent(nonSaturated);
            distributeResidue(nonSaturated, residue, totalPercent);

            residue = calculateNextStepResidueByFixingOverlimitControls(nonSaturated);
            nonSaturated = findValidControlTerminals(residue);
        }
        // update values
        controlTerminals.stream().filter(ct -> !Double.isNaN(ct.finalValue))
            .forEach(controlTerminal -> updateTerminal(controlTerminal.terminal, type, controlTerminal.finalValue));
    }

    private void checkAndFixPercents() {
        if (controlTerminals.stream().anyMatch(controlTerminal -> controlTerminal.percent > 0.0)) {
            return;
        }
        controlTerminals.forEach(controlTerminal -> controlTerminal.percent = 1.0);
    }

    List<ControlTerminal> findValidControlTerminals(double residue) {
        return controlTerminals.stream().filter(ct -> isValid(ct, residue)).collect(Collectors.toList());
    }

    private static boolean isValid(ControlTerminal controlTerminal, double residue) {
        double value = Double.isNaN(controlTerminal.finalValue) ? controlTerminal.initialValue : controlTerminal.finalValue;
        boolean saturated = residue > 0.0 ? value >= controlTerminal.max : value <= controlTerminal.min;
        return !saturated && controlTerminal.percent > 0.0;
    }

    private static double calculateTotalPercent(List<ControlTerminal> controlTerminals) {
        double total = 0.0;
        for (ControlTerminal controlTerminal : controlTerminals) {
            total += controlTerminal.percent;
        }
        return total;
    }

    private static void distributeResidue(List<ControlTerminal> controlTerminals, double residue, double totalPercent) {
        double total = totalPercent == 0.0 ? 1.0 : totalPercent; // never should happen
        for (ControlTerminal controlTerminal : controlTerminals) {
            double value = Double.isNaN(controlTerminal.finalValue) ? controlTerminal.initialValue : controlTerminal.finalValue;
            controlTerminal.finalValue = value + residue * controlTerminal.percent / total;
        }
    }

    private static double calculateNextStepResidueByFixingOverlimitControls(List<ControlTerminal> controlTerminals) {
        double residue = 0.0;
        for (ControlTerminal controlTerminal : controlTerminals) {
            if (controlTerminal.finalValue < controlTerminal.min) {
                residue += controlTerminal.finalValue = controlTerminal.min;
                controlTerminal.finalValue = controlTerminal.min;
            } else if (controlTerminal.finalValue > controlTerminal.max) {
                residue += controlTerminal.finalValue - controlTerminal.max;
                controlTerminal.finalValue = controlTerminal.max;
            }
        }
        return residue;
    }

    private static void updateTerminal(Terminal terminal, ControlType type, double value) {
        if (type == ControlType.ACTIVE) {
            terminal.setP(value);
        } else {
            terminal.setQ(value);
        }
    }

    private static final class ControlTerminal {
        private final Terminal terminal;
        private final double initialValue;
        private final double min;
        private final double max;
        private double percent;
        private double finalValue;

        private ControlTerminal(Terminal terminal, double initialValue, double min, double max, double percent) {
            this.terminal = terminal;
            this.initialValue = initialValue;
            this.min = min;
            this.max = max;
            this.percent = percent;
            this.finalValue = Double.NaN;
        }
    }

    private List<ControlTerminal> findControlTerminals() {
        return type == ControlType.ACTIVE ? findActiveControlTerminals() : findReactiveControlTerminals();
    }

    // Only controls associated to buses are considered
    private List<ControlTerminal> findActiveControlTerminals() {
        List<ControlTerminal> pControlTerminals = new ArrayList<>();

        for (Z0Vertex z0Vertex : z0Vertices) {
            if (z0Vertex.getBus() != null) {
                pControlTerminals.addAll(findActiveControlTerminalsBus(z0Vertex.getBus()));
            }
        }
        return pControlTerminals;
    }

    // Only controls associated to buses and danglingLines are considered
    private List<ControlTerminal> findReactiveControlTerminals() {
        List<ControlTerminal> qControlTerminals = new ArrayList<>();

        for (Z0Vertex z0Vertex : z0Vertices) {
            if (z0Vertex.getBus() != null) {
                qControlTerminals.addAll(findReactiveControlTerminalsBus(z0Vertex.getBus()));
            } else if (z0Vertex.getDanglingLine() != null) {
                qControlTerminals.addAll(findReactiveControlTerminalsDanglingLine(z0Vertex.getDanglingLine()));
            }
        }
        return qControlTerminals;
    }

    private List<ControlTerminal> findActiveControlTerminalsBus(Bus bus) {
        List<ControlTerminal> pControlTerminals = new ArrayList<>();

        bus.getConnectedTerminals().forEach(terminal -> {
            if (terminal.getConnectable().getType() == IdentifiableType.GENERATOR) {
                createActiveControlTerminal((Generator) terminal.getConnectable(), terminal).ifPresent(pControlTerminals::add);
            }
        });

        return pControlTerminals;
    }

    // Only continuous controls are considered
    private List<ControlTerminal> findReactiveControlTerminalsBus(Bus bus) {
        List<ControlTerminal> qControlTerminals = new ArrayList<>();

        bus.getConnectedTerminals().forEach(terminal -> {
            if (terminal.getConnectable().getType() == IdentifiableType.GENERATOR) {
                createReactiveControlTerminal((Generator) terminal.getConnectable(), terminal).ifPresent(qControlTerminals::add);
            } else if (terminal.getConnectable().getType() == IdentifiableType.STATIC_VAR_COMPENSATOR) {
                createReactiveControlTerminal((StaticVarCompensator) terminal.getConnectable(), terminal).ifPresent(qControlTerminals::add);
            } else if (terminal.getConnectable().getType() == IdentifiableType.HVDC_CONVERTER_STATION
                && terminal.getConnectable() instanceof VscConverterStation) {
                createReactiveControlTerminal((VscConverterStation) terminal.getConnectable(), terminal).ifPresent(qControlTerminals::add);
            }
        });

        return qControlTerminals;
    }

    private List<ControlTerminal> findReactiveControlTerminalsDanglingLine(DanglingLine dl) {
        Generation generation = dl.getGeneration();
        if (generation == null || !generation.isVoltageRegulationOn() || generation.getReactiveLimits() == null) {
            return Collections.<ControlTerminal>emptyList();
        }
        ReactiveLimits reactiveLimits = generation.getReactiveLimits();
        double targetP = generation.getTargetP();
        double q = Double.isNaN(dl.getTerminal().getQ()) ? dl.getQ0() - generation.getTargetQ() : dl.getTerminal().getQ();
        return Collections.singletonList(new ControlTerminal(dl.getTerminal(), q,
            -reactiveLimits.getMaxQ(targetP) + dl.getQ0(), -reactiveLimits.getMinQ(targetP) + dl.getQ0(), 1.0));
    }

    // Criteria of the terminal values is used
    // Value is opposite to target and min and max are in concordance with target
    private static Optional<ControlTerminal> createActiveControlTerminal(Generator generator, Terminal terminal) {
        return Optional.of(new ControlTerminal(terminal, terminal.getP(), -generator.getMaxP(), -generator.getMinP(), 0.0));
    }

    private static Optional<ControlTerminal> createReactiveControlTerminal(Generator generator, Terminal terminal) {
        ReactiveLimits reactiveLimits = generator.getReactiveLimits();
        if (!generator.isVoltageRegulatorOn() || reactiveLimits == null) {
            return Optional.empty();
        }
        double targetP = -terminal.getP();

        CoordinatedReactiveControl extension = generator.getExtension(CoordinatedReactiveControl.class);
        double qPercent = extension != null ? extension.getQPercent() : 0.0;

        return Optional.of(new ControlTerminal(terminal, terminal.getQ(), -reactiveLimits.getMaxQ(targetP),
            -reactiveLimits.getMinQ(targetP), qPercent));
    }

    private static Optional<ControlTerminal> createReactiveControlTerminal(StaticVarCompensator svc, Terminal terminal) {
        if (!svc.getRegulationMode().equals(RegulationMode.VOLTAGE)) {
            return Optional.empty();
        }
        Bus bus = terminal.getBusView().getBus();
        double v2 = bus.getV() * bus.getV();
        return Optional.of(new ControlTerminal(terminal, terminal.getQ(), -svc.getBmax() * v2, -svc.getBmin() * v2, 0.0));
    }

    private static Optional<ControlTerminal> createReactiveControlTerminal(VscConverterStation vsc, Terminal terminal) {
        if (vsc.isVoltageRegulatorOn() && vsc.getReactiveLimits() != null) {
            ReactiveLimits reactiveLimits = vsc.getReactiveLimits();
            double targetP = -terminal.getP();

            return Optional.of(new ControlTerminal(terminal, terminal.getQ(), -reactiveLimits.getMaxQ(targetP), -reactiveLimits.getMinQ(targetP), 0.0));
        }
        return Optional.empty();
    }

    private final ControlType type;
    private final List<Z0Vertex> z0Vertices;
    private final List<ControlTerminal> controlTerminals;
}
