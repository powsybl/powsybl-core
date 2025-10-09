/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.extensions.LoadAsymmetrical;
import com.powsybl.iidm.network.extensions.LoadConnectionType;

import java.util.Objects;

/**
 * This class is used as an extension of a "classical" balanced direct load
 * we store here the deltas of power that will build the unblalanced loads. The reference is the positive sequence load stored in "Load".
 *
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class LoadAsymmetricalImpl extends AbstractExtension<Load> implements LoadAsymmetrical {

    private LoadConnectionType connectionType;

    private double deltaPa;
    private double deltaQa;
    private double deltaPb;
    private double deltaQb;
    private double deltaPc;
    private double deltaQc;

    public LoadAsymmetricalImpl(LoadConnectionType connectionType, Load load, double deltaPa, double deltaQa, double deltaPb, double deltaQb, double deltaPc, double deltaQc) {
        super(load);
        this.connectionType = connectionType;
        this.deltaPa = deltaPa;
        this.deltaPb = deltaPb;
        this.deltaPc = deltaPc;
        this.deltaQa = deltaQa;
        this.deltaQb = deltaQb;
        this.deltaQc = deltaQc;
    }

    @Override
    public LoadConnectionType getConnectionType() {
        return connectionType;
    }

    @Override
    public LoadAsymmetricalImpl setConnectionType(LoadConnectionType connectionType) {
        this.connectionType = Objects.requireNonNull(connectionType);
        return this;
    }

    @Override
    public double getDeltaPa() {
        return deltaPa;
    }

    @Override
    public LoadAsymmetricalImpl setDeltaPa(double deltaPa) {
        this.deltaPa = deltaPa;
        return this;
    }

    @Override
    public double getDeltaPb() {
        return deltaPb;
    }

    @Override
    public LoadAsymmetricalImpl setDeltaPb(double deltaPb) {
        this.deltaPb = deltaPb;
        return this;
    }

    @Override
    public double getDeltaPc() {
        return deltaPc;
    }

    @Override
    public LoadAsymmetricalImpl setDeltaPc(double deltaPc) {
        this.deltaPc = deltaPc;
        return this;
    }

    @Override
    public double getDeltaQa() {
        return deltaQa;
    }

    @Override
    public LoadAsymmetricalImpl setDeltaQa(double deltaQa) {
        this.deltaQa = deltaQa;
        return this;
    }

    @Override
    public double getDeltaQb() {
        return deltaQb;
    }

    @Override
    public LoadAsymmetricalImpl setDeltaQb(double deltaQb) {
        this.deltaQb = deltaQb;
        return this;
    }

    @Override
    public double getDeltaQc() {
        return deltaQc;
    }

    @Override
    public LoadAsymmetricalImpl setDeltaQc(double deltaQc) {
        this.deltaQc = deltaQc;
        return this;
    }
}
