/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.HvdcLineAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.util.Identifiables;

import java.util.Objects;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class HvdcLineAdderAdapter implements HvdcLineAdder {

    private String id;

    private boolean ensureIdUnicity = false;

    private String name;

    private boolean fictitious;

    private double r;

    private HvdcLine.ConvertersMode convertersMode;

    private double nominalV;

    private double maxP;

    private double activePowerSetpoint;

    private String converterStationId1;

    private String converterStationId2;

    private MergingViewIndex index;

    HvdcLineAdderAdapter(final MergingViewIndex index) {
        this.index = Objects.requireNonNull(index, "merging view index is null");
    }

    @Override
    public HvdcLine add() {
        final Network n1 = checkAndGetNetwork1();
        final Network n2 = checkAndGetNetwork2();
        if (n1 != n2) {
            throw new PowsyblException("HvdcLine creation between two networks is not allowed");
        }
        checkAndSetUniqueId();
        return index.getHvdcLine(addLine(n1));
    }

    private Network checkAndGetNetwork1() {
        if (Objects.isNull(converterStationId1)) {
            throw new PowsyblException("Side 1 converter station is not set");
        }
        final Network network = index.getNetwork(n -> n.getHvdcConverterStation(converterStationId1) != null);
        if (Objects.isNull(network)) {
            throw new PowsyblException("Side 1 converter station '" + converterStationId1 + "' not found");
        }
        return network;
    }

    private Network checkAndGetNetwork2() {
        if (Objects.isNull(converterStationId2)) {
            throw new PowsyblException("Side 2 converter station is not set");
        }
        final Network network = index.getNetwork(n -> n.getHvdcConverterStation(converterStationId2) != null);
        if (Objects.isNull(network)) {
            throw new PowsyblException("Side 2 converter station '" + converterStationId2 + "' not found");
        }
        return network;
    }

    private void checkAndSetUniqueId() {
        if (this.id == null) {
            throw new PowsyblException(getClass().getSimpleName() + " id is not set");
        }
        if (ensureIdUnicity) {
            setId(Identifiables.getUniqueId(id, index::contains));
        } else {
            // Check Id is unique in all merging view
            if (index.contains(id)) {
                throw new PowsyblException("The network already contains an object 'HvdcLine' with the id '"
                        + id
                        + "'");
            }
        }
    }

    private HvdcLine addLine(final Network network) {
        return network.newHvdcLine()
                          .setId(id)
                          .setName(name)
                          .setEnsureIdUnicity(ensureIdUnicity)
                          .setFictitious(fictitious)
                          .setR(r)
                          .setConvertersMode(convertersMode)
                          .setNominalV(nominalV)
                          .setMaxP(maxP)
                          .setActivePowerSetpoint(activePowerSetpoint)
                          .setConverterStationId1(converterStationId1)
                          .setConverterStationId2(converterStationId2)
                      .add();
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public HvdcLineAdder setId(final String id) {
        this.id = id;
        return this;
    }

    @Override
    public HvdcLineAdder setName(final String name) {
        this.name = name;
        return this;
    }

    @Override
    public HvdcLineAdder setEnsureIdUnicity(final boolean ensureIdUnicity) {
        this.ensureIdUnicity = ensureIdUnicity;
        return this;
    }

    @Override
    public HvdcLineAdder setFictitious(final boolean fictitious) {
        this.fictitious = fictitious;
        return this;
    }

    @Override
    public HvdcLineAdder setConverterStationId2(String converterStationId2) {
        this.converterStationId2 = converterStationId2;
        return this;
    }

    @Override
    public HvdcLineAdder setConverterStationId1(String converterStationId1) {
        this.converterStationId1 = converterStationId1;
        return this;
    }

    @Override
    public HvdcLineAdder setMaxP(double maxP) {
        this.maxP = maxP;
        return this;
    }

    @Override
    public HvdcLineAdder setActivePowerSetpoint(double activePowerSetpoint) {
        this.activePowerSetpoint = activePowerSetpoint;
        return this;
    }

    @Override
    public HvdcLineAdder setNominalV(double nominalV) {
        this.nominalV = nominalV;
        return this;
    }

    @Override
    public HvdcLineAdder setConvertersMode(HvdcLine.ConvertersMode convertersMode) {
        this.convertersMode = convertersMode;
        return this;
    }

    @Override
    public HvdcLineAdder setR(double r) {
        this.r = r;
        return this;
    }
}
