/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.extensions.LoadAsymmetrical;
import com.powsybl.iidm.network.extensions.LoadAsymmetricalAdder;

/**
 * @author Jean-Baptiste Heyberger <jbheyberger at gmail.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LoadAsymmetricalAdderImpl extends AbstractExtensionAdder<Load, LoadAsymmetrical> implements LoadAsymmetricalAdder {

    private double deltaPa = 0;
    private double deltaQa = 0;
    private double deltaPb = 0;
    private double deltaQb = 0;
    private double deltaPc = 0;
    private double deltaQc = 0;

    public LoadAsymmetricalAdderImpl(Load load) {
        super(load);
    }

    @Override
    public Class<? super LoadAsymmetrical> getExtensionClass() {
        return LoadAsymmetrical.class;
    }

    @Override
    protected LoadAsymmetricalImpl createExtension(Load load) {
        return new LoadAsymmetricalImpl(load, deltaPa, deltaQa, deltaPb, deltaQb, deltaPc, deltaQc);
    }

    public LoadAsymmetricalAdderImpl withDeltaPa(double deltaPa) {
        this.deltaPa = deltaPa;
        return this;
    }

    public LoadAsymmetricalAdderImpl withDeltaQa(double deltaQa) {
        this.deltaQa = deltaQa;
        return this;
    }

    public LoadAsymmetricalAdderImpl withDeltaPb(double deltaPb) {
        this.deltaPb = deltaPb;
        return this;
    }

    public LoadAsymmetricalAdderImpl withDeltaQb(double deltaQb) {
        this.deltaQb = deltaQb;
        return this;
    }

    public LoadAsymmetricalAdderImpl withDeltaPc(double deltaPc) {
        this.deltaPc = deltaPc;
        return this;
    }

    public LoadAsymmetricalAdderImpl withDeltaQc(double deltaQc) {
        this.deltaQc = deltaQc;
        return this;
    }
}
