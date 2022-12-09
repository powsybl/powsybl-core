/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.merging;

import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.modification.AbstractNetworkModification;
import com.powsybl.iidm.modification.topology.TopologyModificationUtils;
import com.powsybl.iidm.network.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.powsybl.iidm.modification.topology.TopologyModificationUtils.addLoadingLimits;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class SplitTieLine extends AbstractNetworkModification {

    private final String tieLineId;
    private final Set<String> aliasTypes1 = new HashSet<>();
    private final Set<String> aliasTypes2 = new HashSet<>();
    private final Branch.Side defaultSideForAliases;

    SplitTieLine(String tieLineId, Set<String> aliasTypes1, Set<String> aliasTypes2, Branch.Side defaultSideForAliases) {
        this.tieLineId = Objects.requireNonNull(tieLineId);
        this.aliasTypes1.addAll(Objects.requireNonNull(aliasTypes1));
        this.aliasTypes2.addAll(Objects.requireNonNull(aliasTypes2));
        this.defaultSideForAliases = Objects.requireNonNull(defaultSideForAliases);
    }

    @Override
    public void apply(Network network, boolean throwException, ComputationManager computationManager, Reporter reporter) {
        Line line = network.getLine(tieLineId);
        if (line == null) {
            return;
        }
        if (!(line instanceof TieLine)) {
            return;
        }
        TieLine tl = (TieLine) line;
        DanglingLineAdder adder1 = createDanglingLineAdder(tl.getHalf1()).setUcteXnodeCode(tl.getUcteXnodeCode());
        DanglingLineAdder adder2 = createDanglingLineAdder(tl.getHalf2()).setUcteXnodeCode(tl.getUcteXnodeCode());
        connect(adder1, tl.getTerminal1());
        connect(adder2, tl.getTerminal2());
        TopologyModificationUtils.LoadingLimitsBags dl1Limits = new TopologyModificationUtils.LoadingLimitsBags(() -> tl.getActivePowerLimits(Branch.Side.ONE),
            () -> tl.getApparentPowerLimits(Branch.Side.ONE),
            () -> tl.getCurrentLimits(Branch.Side.ONE));
        TopologyModificationUtils.LoadingLimitsBags dl2Limits = new TopologyModificationUtils.LoadingLimitsBags(() -> tl.getActivePowerLimits(Branch.Side.TWO),
            () -> tl.getApparentPowerLimits(Branch.Side.TWO),
            () -> tl.getCurrentLimits(Branch.Side.TWO));
        Map<String, String> properties = tl.getPropertyNames().stream().collect(Collectors.toMap(name -> name, tl::getProperty));
        Map<String, String> aliases1 = new HashMap<>();
        Map<String, String> aliases2 = new HashMap<>();
        fillAliases(aliases1, aliases2, tl);
        tl.remove();
        DanglingLine dl1 = adder1.add();
        DanglingLine dl2 = adder2.add();
        properties.forEach(dl1::setProperty);
        properties.forEach(dl2::setProperty);
        aliases1.forEach((alias, type) -> {
            if (type != null) {
                dl1.addAlias(alias, type);
            } else {
                dl1.addAlias(alias);
            }
        });
        aliases2.forEach((alias, type) -> {
            if (type != null) {
                dl2.addAlias(alias, type);
            } else {
                dl2.addAlias(alias);
            }
        });
        addLoadingLimits(dl1, dl1Limits);
        addLoadingLimits(dl2, dl2Limits);
        // NB:
        // - extensions are not handled
        // - no generation is created
    }

    private static DanglingLineAdder createDanglingLineAdder(TieLine.HalfLine halfLine) {
        return halfLine.getBoundary().getNetworkSideVoltageLevel().newDanglingLine()
                .setId(halfLine.getId())
                .setEnsureIdUnicity(true)
                .setName(halfLine.getName())
                .setFictitious(halfLine.isFictitious())
                .setR(halfLine.getR())
                .setX(halfLine.getX())
                .setG(halfLine.getG1() + halfLine.getG2())
                .setB(halfLine.getB1() + halfLine.getB2())
                .setP0(Double.isNaN(halfLine.getBoundary().getP()) ? 0 : -halfLine.getBoundary().getP())
                .setQ0(Double.isNaN(halfLine.getBoundary().getQ()) ? 0 : -halfLine.getBoundary().getQ());
    }

    private static void connect(DanglingLineAdder adder, Terminal terminal) {
        TopologyKind topologyKind = terminal.getVoltageLevel().getTopologyKind();
        if (topologyKind == TopologyKind.NODE_BREAKER) {
            adder.setNode(terminal.getNodeBreakerView().getNode());
        } else if (topologyKind == TopologyKind.BUS_BREAKER) {
            adder.setConnectableBus(terminal.getBusBreakerView().getConnectableBus().getId())
                    .setBus(Optional.ofNullable(terminal.getBusBreakerView().getBus()).map(Identifiable::getId).orElse(null));
        } else {
            throw new AssertionError("Unexpected topology kind: " + topologyKind);
        }
    }

    private void fillAliases(Map<String, String> aliases1, Map<String, String> aliases2, TieLine tl) {
        for (String alias : tl.getAliases()) {
            Optional<String> type = tl.getAliasType(alias);
            if (type.isPresent()) {
                if (aliasTypes1.contains(type.get())) {
                    aliases1.put(alias, type.get());
                } else if (aliasTypes2.contains(type.get())) {
                    aliases2.put(alias, type.get());
                } else {
                    fillAliases(aliases1, aliases2, alias, type.get());
                }
            } else {
                fillAliases(aliases1, aliases2, alias, null);
            }
        }
    }

    private void fillAliases(Map<String, String> aliases1, Map<String, String> aliases2, String alias, String type) {
        if (defaultSideForAliases == Branch.Side.ONE) {
            aliases1.put(alias, type);
        } else {
            aliases2.put(alias, type);
        }
    }
}
