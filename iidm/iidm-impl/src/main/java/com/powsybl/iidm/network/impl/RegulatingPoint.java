/**
 * Copyright (c) 2023-2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.util.fastutil.ExtendedBooleanArrayList;
import com.powsybl.commons.util.fastutil.ExtendedIntArrayList;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
class RegulatingPoint implements MultiVariantObject, Referrer<Terminal> {

    private static final Logger LOG = LoggerFactory.getLogger(RegulatingPoint.class);

    private final String regulatedEquipmentId;
    private final Supplier<TerminalExt> localTerminalSupplier;
    private final boolean useVoltageRegulation;
    private final int offRegulationMode; // mode to be used on regulating terminal removal
    private TerminalExt regulatingTerminal;

    // attributes depending on the variant

    private final ExtendedBooleanArrayList regulating;
    private final ExtendedIntArrayList regulationMode;

    RegulatingPoint(String regulatedEquipmentId, Supplier<TerminalExt> localTerminalSupplier, int variantArraySize, boolean regulating, boolean useVoltageRegulation) {
        this.regulatedEquipmentId = regulatedEquipmentId;
        this.localTerminalSupplier = localTerminalSupplier;
        this.useVoltageRegulation = useVoltageRegulation;
        this.regulating = new ExtendedBooleanArrayList(variantArraySize, regulating);
        this.regulationMode = null;
        this.offRegulationMode = -1;
    }

    RegulatingPoint(String regulatedEquipmentId, Supplier<TerminalExt> localTerminalSupplier, int variantArraySize, int regulationMode, boolean regulating, int offRegulationMode, boolean useVoltageRegulation) {
        this.regulatedEquipmentId = regulatedEquipmentId;
        this.localTerminalSupplier = localTerminalSupplier;
        this.useVoltageRegulation = useVoltageRegulation;
        this.regulationMode = new ExtendedIntArrayList(variantArraySize, regulationMode);
        this.offRegulationMode = offRegulationMode;
        this.regulating = new ExtendedBooleanArrayList(variantArraySize, regulating);
    }

    RegulatingPoint(String regulatedEquipmentId, Supplier<TerminalExt> localTerminalSupplier, int variantArraySize, int regulationMode, int offRegulationMode, boolean useVoltageRegulation) {
        this.regulatedEquipmentId = regulatedEquipmentId;
        this.localTerminalSupplier = localTerminalSupplier;
        this.useVoltageRegulation = useVoltageRegulation;
        this.regulationMode = new ExtendedIntArrayList(variantArraySize, regulationMode);
        this.offRegulationMode = offRegulationMode;
        this.regulating = null;
    }

    void setRegulatingTerminal(TerminalExt regulatingTerminal) {
        if (this.regulatingTerminal != null) {
            this.regulatingTerminal.getReferrerManager().unregister(this);
            this.regulatingTerminal = null;
        }
        if (regulatingTerminal != null) {
            this.regulatingTerminal = regulatingTerminal;
            this.regulatingTerminal.getReferrerManager().register(this);
        }
    }

    TerminalExt getRegulatingTerminal() {
        return regulatingTerminal != null ? regulatingTerminal : localTerminalSupplier.get();
    }

    boolean setRegulating(int index, boolean regulating) {
        if (this.regulating == null) {
            // TODO: throw exception?
            throw new IllegalStateException("Regulating point is not initialized");
        }
        return this.regulating.set(index, regulating);
    }

    boolean isRegulating(int index) {
        if (regulating == null) {
            // TODO: throw exception?
            throw new IllegalStateException("Regulating point is not initialized");
        }
        return regulating.getBoolean(index);
    }

    int setRegulationMode(int index, int regulationMode) {
        if (this.regulationMode == null) {
            // TODO: throw exception?
            throw new IllegalStateException("Regulation mode is not initialized");
        }
        return this.regulationMode.set(index, regulationMode);
    }

    int getRegulationMode(int index) {
        if (this.regulationMode == null) {
            // TODO: throw exception?
            throw new IllegalStateException("Regulation mode is not initialized");
        }
        return regulationMode.getInt(index);
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        if (regulating != null) {
            regulating.growAndFill(number, regulating.getBoolean(sourceIndex));
        }
        if (regulationMode != null) {
            regulationMode.growAndFill(number, regulationMode.getInt(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        if (regulating != null) {
            regulating.removeElements(number);
        }
        if (regulationMode != null) {
            regulationMode.removeElements(number);
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
                regulating.set(index, regulating.getBoolean(sourceIndex));
            }
            if (regulationMode != null) {
                regulationMode.set(index, regulationMode.getInt(sourceIndex));
            }
        }
    }

    void remove() {
        if (regulatingTerminal != null) {
            regulatingTerminal.getReferrerManager().unregister(this);
        }
    }

    @Override
    public void onReferencedRemoval(Terminal removedTerminal) {
        TerminalExt oldRegulatingTerminal = regulatingTerminal;
        TerminalExt localTerminal = localTerminalSupplier.get();
        if (localTerminal != null && useVoltageRegulation) { // if local voltage regulation, we keep the regulating status, and re-locate the regulation at the regulated equipment
            Bus bus = regulatingTerminal.getBusView().getBus();
            Bus localBus = localTerminal.getBusView().getBus();
            if (bus != null && bus == localBus) {
                LOG.warn("Connectable {} was a local voltage regulation point for {}. Regulation point is re-located at {}.", regulatingTerminal.getConnectable().getId(),
                        regulatedEquipmentId, regulatedEquipmentId);
                regulatingTerminal = localTerminal;
                return;
            } else {
                regulatingTerminal = null;
            }
        } else {
            regulatingTerminal = null;
        }
        LOG.warn("Connectable {} was a regulation point for {}. Regulation is deactivated", oldRegulatingTerminal.getConnectable().getId(), regulatedEquipmentId);
        if (regulating != null) {
            regulating.fill(0, regulating.size(), false);
        }
        if (regulationMode != null) {
            regulationMode.fill(0, regulationMode.size(), this.offRegulationMode);
        }
    }

    @Override
    public void onReferencedReplacement(Terminal oldReferenced, Terminal newReferenced) {
        if (regulatingTerminal == oldReferenced) {
            regulatingTerminal = (TerminalExt) newReferenced;
        }
    }
}
