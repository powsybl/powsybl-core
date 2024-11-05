/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.ShortIdDictionary;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
abstract class AbstractTopologyModel implements TopologyModel {

    public static final int DEFAULT_NODE_INDEX_LIMIT = 1000;

    public static final int NODE_INDEX_LIMIT = loadNodeIndexLimit(PlatformConfig.defaultConfig());

    protected final VoltageLevelExt voltageLevel;

    protected AbstractTopologyModel(VoltageLevelExt voltageLevel) {
        this.voltageLevel = Objects.requireNonNull(voltageLevel);
    }

    protected static int loadNodeIndexLimit(PlatformConfig platformConfig) {
        return platformConfig
                .getOptionalModuleConfig("iidm")
                .map(moduleConfig -> moduleConfig.getIntProperty("node-index-limit", DEFAULT_NODE_INDEX_LIMIT))
                .orElse(DEFAULT_NODE_INDEX_LIMIT);
    }

    protected NetworkImpl getNetwork() {
        return voltageLevel.getNetwork();
    }

    protected static void addNextTerminals(TerminalExt otherTerminal, List<TerminalExt> nextTerminals) {
        Objects.requireNonNull(otherTerminal);
        Objects.requireNonNull(nextTerminals);
        Connectable<?> otherConnectable = otherTerminal.getConnectable();
        if (otherConnectable instanceof Branch<?> branch) {
            if (branch.getTerminal1() == otherTerminal) {
                nextTerminals.add((TerminalExt) branch.getTerminal2());
            } else if (branch.getTerminal2() == otherTerminal) {
                nextTerminals.add((TerminalExt) branch.getTerminal1());
            } else {
                throw new IllegalStateException();
            }
        } else if (otherConnectable instanceof ThreeWindingsTransformer ttc) {
            if (ttc.getLeg1().getTerminal() == otherTerminal) {
                nextTerminals.add((TerminalExt) ttc.getLeg2().getTerminal());
                nextTerminals.add((TerminalExt) ttc.getLeg3().getTerminal());
            } else if (ttc.getLeg2().getTerminal() == otherTerminal) {
                nextTerminals.add((TerminalExt) ttc.getLeg1().getTerminal());
                nextTerminals.add((TerminalExt) ttc.getLeg3().getTerminal());
            } else if (ttc.getLeg3().getTerminal() == otherTerminal) {
                nextTerminals.add((TerminalExt) ttc.getLeg1().getTerminal());
                nextTerminals.add((TerminalExt) ttc.getLeg2().getTerminal());
            } else {
                throw new IllegalStateException();
            }
        }
    }

    public void invalidateCache() {
        invalidateCache(false);
    }

    public abstract Iterable<Terminal> getTerminals();

    public abstract Stream<Terminal> getTerminalStream();

    public abstract VoltageLevelExt.NodeBreakerViewExt getNodeBreakerView();

    public abstract VoltageLevelExt.BusBreakerViewExt getBusBreakerView();

    public abstract VoltageLevelExt.BusViewExt getBusView();

    public abstract Iterable<Switch> getSwitches();

    public abstract int getSwitchCount();

    public abstract TopologyKind getTopologyKind();

    public abstract void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex);

    public abstract void reduceVariantArraySize(int number);

    public abstract void deleteVariantArrayElement(int index);

    public abstract void allocateVariantArrayElement(int[] indexes, int sourceIndex);

    protected abstract void removeTopology();

    public abstract void printTopology();

    public abstract void printTopology(PrintStream out, ShortIdDictionary dict);

    public abstract void exportTopology(Path file) throws IOException;

    public abstract void exportTopology(Writer writer);

    public abstract void exportTopology(Writer writer, Random random);
}
