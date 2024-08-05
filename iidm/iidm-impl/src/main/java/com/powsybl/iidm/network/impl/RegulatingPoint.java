/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.util.trove.TBooleanArrayList;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.StaticVarCompensator;
import gnu.trove.list.array.TIntArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
class RegulatingPoint implements MultiVariantObject {

    private static final Logger LOG = LoggerFactory.getLogger(RegulatingPoint.class);

    private final String regulatedEquipmentId;
    private final Supplier<TerminalExt> localTerminalSupplier;
    private boolean useVoltageRegulation;
    private TerminalExt regulatingTerminal = null;

    // attributes depending on the variant

    private final TBooleanArrayList regulating;
    private final TIntArrayList regulationMode;

    RegulatingPoint(String regulatedEquipmentId, Supplier<TerminalExt> localTerminalSupplier, int variantArraySize, boolean regulating, boolean useVoltageRegulation) {
        this.regulatedEquipmentId = regulatedEquipmentId;
        this.localTerminalSupplier = localTerminalSupplier;
        this.useVoltageRegulation = useVoltageRegulation;
        this.regulating = new TBooleanArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.regulating.add(regulating);
        }
        this.regulationMode = null;
    }

    RegulatingPoint(String regulatedEquipmentId, Supplier<TerminalExt> localTerminalSupplier, int variantArraySize, int regulationMode, boolean useVoltageRegulation) {
        this.regulatedEquipmentId = regulatedEquipmentId;
        this.localTerminalSupplier = localTerminalSupplier;
        this.useVoltageRegulation = useVoltageRegulation;
        this.regulationMode = new TIntArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.regulationMode.add(regulationMode);
        }
        this.regulating = null;
    }

    void setRegulatingTerminal(TerminalExt regulatingTerminal) {
        if (this.regulatingTerminal != null) {
            this.regulatingTerminal.removeRegulatingPoint(this, false);
        }
        this.regulatingTerminal = regulatingTerminal != null ? regulatingTerminal : localTerminalSupplier.get();
        if (this.regulatingTerminal != null) {
            this.regulatingTerminal.setAsRegulatingPoint(this);
        }
    }

    TerminalExt getRegulatingTerminal() {
        return regulatingTerminal;
    }

    boolean setRegulating(int index, boolean regulating) {
        return setRegulating(index, regulating, false);
    }

    boolean setRegulating(int index, boolean regulating, boolean dryRun) {
        if (dryRun) {
            return this.regulating.get(index);
        }
        return this.regulating.set(index, regulating);
    }

    void setUseVoltageRegulation(boolean useVoltageRegulation, boolean dryRun) {
        if (!dryRun) {
            this.useVoltageRegulation = useVoltageRegulation;
        }
    }

    boolean isRegulating(int index) {
        return regulating.get(index);
    }

    int setRegulationMode(int index, int regulationMode) {
        return this.regulationMode.set(index, regulationMode);
    }

    int getRegulationMode(int index) {
        return regulationMode.get(index);
    }

    void removeRegulatingTerminal(boolean dryRun) {
        Objects.requireNonNull(regulatingTerminal);
        TerminalExt localTerminal = localTerminalSupplier.get();
        if (localTerminal != null && useVoltageRegulation) { // if local voltage regulation, we keep the regulating status, and re-locate the regulation at the regulated equipment
            Bus bus = regulatingTerminal.getBusView().getBus();
            Bus localBus = localTerminal.getBusView().getBus();
            if (bus != null && bus == localBus) {
                LOG.warn("Connectable {} was a local voltage regulation point for {}. Regulation point is re-located at {}.", regulatingTerminal.getConnectable().getId(),
                        regulatedEquipmentId, regulatedEquipmentId);
                if (!dryRun) {
                    regulatingTerminal = localTerminal;
                }
                return;
            }
        }
        LOG.warn("Connectable {} was a regulation point for {}. Regulation is deactivated", regulatingTerminal.getConnectable().getId(), regulatedEquipmentId);
        if (!dryRun) {
            regulatingTerminal = localTerminal;
            if (regulating != null) {
                regulating.fill(0, regulating.size(), false);
            }
            if (regulationMode != null) {
                regulationMode.fill(0, regulationMode.size(), StaticVarCompensator.RegulationMode.OFF.ordinal());
            }
        }
    }

    void remove() {
        remove(false);
    }

    void remove(boolean dryRun) {
        if (regulatingTerminal != null) {
            regulatingTerminal.removeRegulatingPoint(this, dryRun);
        }
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        if (regulating != null) {
            regulating.ensureCapacity(regulating.size() + number);
        }
        if (regulationMode != null) {
            regulationMode.ensureCapacity(regulationMode.size() + number);
        }
        for (int i = 0; i < number; i++) {
            if (regulating != null) {
                regulating.add(regulating.get(sourceIndex));
            }
            if (regulationMode != null) {
                regulationMode.add(regulationMode.get(sourceIndex));
            }
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        if (regulating != null) {
            regulating.remove(regulating.size() - number, number);
        }
        if (regulationMode != null) {
            regulationMode.remove(regulationMode.size() - number, number);
        }
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        // nothing to do
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            if (regulating != null) {
                regulating.set(index, regulating.get(sourceIndex));
            }
            if (regulationMode != null) {
                regulationMode.set(index, regulationMode.get(sourceIndex));
            }
        }
    }
}
