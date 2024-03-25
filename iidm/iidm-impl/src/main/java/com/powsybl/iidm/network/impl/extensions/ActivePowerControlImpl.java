/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.util.trove.TBooleanArrayList;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.impl.AbstractMultiVariantIdentifiableExtension;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.List;

/**
 * @author Ghiles Abdellah {@literal <ghiles.abdellah at rte-france.com>}
 */
public class ActivePowerControlImpl<T extends Injection<T>> extends AbstractMultiVariantIdentifiableExtension<T>
        implements ActivePowerControl<T> {

    private final TBooleanArrayList participate;

    private final TDoubleArrayList droop;
    private final TDoubleArrayList participationFactor;

    private final List<TDoubleArrayList> allTDoubleArrayLists;

    public ActivePowerControlImpl(T component,
                                  boolean participate,
                                  double droop,
                                  double participationFactor) {
        super(component);
        int variantArraySize = getVariantManagerHolder().getVariantManager().getVariantArraySize();
        this.participate = new TBooleanArrayList(variantArraySize);
        this.droop = new TDoubleArrayList(variantArraySize);
        this.participationFactor = new TDoubleArrayList(variantArraySize);
        this.allTDoubleArrayLists = List.of(this.droop, this.participationFactor);
        for (int i = 0; i < variantArraySize; i++) {
            this.participate.add(participate);
            this.droop.add(droop);
            this.participationFactor.add(participationFactor);
        }
    }

    public boolean isParticipate() {
        return participate.get(getVariantIndex());
    }

    public void setParticipate(boolean participate) {
        this.participate.set(getVariantIndex(), participate);
    }

    public double getDroop() {
        return droop.get(getVariantIndex());
    }

    public void setDroop(double droop) {
        this.droop.set(getVariantIndex(), droop);
    }

    public double getParticipationFactor() {
        return participationFactor.get(getVariantIndex());
    }

    public void setParticipationFactor(double participationFactor) {
        this.participationFactor.set(getVariantIndex(), participationFactor);
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        participate.ensureCapacity(participate.size() + number);
        allTDoubleArrayLists.forEach(dl -> dl.ensureCapacity(dl.size() + number));
        for (int i = 0; i < number; ++i) {
            participate.add(participate.get(sourceIndex));
            allTDoubleArrayLists.forEach(dl -> dl.add(dl.get(sourceIndex)));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        participate.remove(participate.size() - number, number);
        allTDoubleArrayLists.forEach(dl -> dl.remove(dl.size() - number, number));
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        // Does nothing
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            participate.set(index, participate.get(sourceIndex));
            droop.set(index, droop.get(sourceIndex));
            allTDoubleArrayLists.forEach(dl -> dl.set(index, dl.get(sourceIndex)));
        }
    }
}
