/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.util.trove.TBooleanArrayList;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.iidm.network.impl.AbstractMultiVariantIdentifiableExtension;
import gnu.trove.list.array.TFloatArrayList;

import java.util.List;

/**
 * @author Ghiles Abdellah <ghiles.abdellah at rte-france.com>
 */
public class ActivePowerControlImpl<T extends Injection<T>> extends AbstractMultiVariantIdentifiableExtension<T>
        implements ActivePowerControl<T> {

    private final TBooleanArrayList participate;

    private final TFloatArrayList droop;
    private final TFloatArrayList shortPF;
    private final TFloatArrayList normalPF;
    private final TFloatArrayList longPF;

    private final List<TFloatArrayList> allTFloatArrayLists;

    public ActivePowerControlImpl(T component,
                                  boolean participate,
                                  float droop,
                                  float shortPF,
                                  float normalPF,
                                  float longPF) {
        super(component);
        int variantArraySize = getVariantManagerHolder().getVariantManager().getVariantArraySize();
        this.participate = new TBooleanArrayList(variantArraySize);
        this.droop = new TFloatArrayList(variantArraySize);
        this.shortPF = new TFloatArrayList(variantArraySize);
        this.normalPF = new TFloatArrayList(variantArraySize);
        this.longPF = new TFloatArrayList(variantArraySize);
        this.allTFloatArrayLists = List.of(this.droop, this.shortPF, this.normalPF, this.longPF);
        for (int i = 0; i < variantArraySize; i++) {
            this.participate.add(participate);
            this.droop.add(droop);
            this.shortPF.add(shortPF);
            this.normalPF.add(normalPF);
            this.longPF.add(longPF);
        }
    }

    public boolean isParticipate() {
        return participate.get(getVariantIndex());
    }

    public void setParticipate(boolean participate) {
        this.participate.set(getVariantIndex(), participate);
    }

    public float getDroop() {
        return droop.get(getVariantIndex());
    }

    public void setDroop(float droop) {
        this.droop.set(getVariantIndex(), droop);
    }

    public float getShortPF() {
        return shortPF.get(getVariantIndex());
    }

    public void setShortPF(float shortPF) {
        this.shortPF.set(getVariantIndex(), shortPF);
    }

    public float getNormalPF() {
        return normalPF.get(getVariantIndex());
    }

    public void setNormalPF(float normalPF) {
        this.normalPF.set(getVariantIndex(), normalPF);
    }

    public float getLongPF() {
        return longPF.get(getVariantIndex());
    }

    public void setLongPF(float longPF) {
        this.longPF.set(getVariantIndex(), longPF);

    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        participate.ensureCapacity(participate.size() + number);
        allTFloatArrayLists.forEach(fl -> fl.ensureCapacity(fl.size() + number));
        for (int i = 0; i < number; ++i) {
            participate.add(participate.get(sourceIndex));
            allTFloatArrayLists.forEach(fl -> fl.add(fl.get(sourceIndex)));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        participate.remove(participate.size() - number, number);
        allTFloatArrayLists.forEach(fl -> fl.remove(fl.size() - number, number));
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
            allTFloatArrayLists.forEach(fl -> fl.set(index, fl.get(sourceIndex)));
        }
    }
}
