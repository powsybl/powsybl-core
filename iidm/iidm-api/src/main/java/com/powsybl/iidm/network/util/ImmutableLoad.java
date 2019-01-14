/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.LoadType;
import com.powsybl.iidm.network.Terminal;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ImmutableLoad extends AbstractImmutableIdentifiable<Load> implements Load {

    public ImmutableLoad(Load identifiable) {
        super(identifiable);
    }

    public static ImmutableLoad ofNullable(Load l) {
        return null == l ? null : new ImmutableLoad(l);
    }

    @Override
    public LoadType getLoadType() {
        return identifiable.getLoadType();
    }

    @Override
    public Load setLoadType(LoadType loadType) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getP0() {
        return identifiable.getP0();
    }

    @Override
    public Load setP0(double p0) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getQ0() {
        return identifiable.getQ0();
    }

    @Override
    public Load setQ0(double q0) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public Terminal getTerminal() {
        return ImmutableTerminal.ofNullable(identifiable.getTerminal());
    }

    @Override
    public ConnectableType getType() {
        return identifiable.getType();
    }

    @Override
    public List<? extends Terminal> getTerminals() {
        return identifiable.getTerminals().stream().map(ImmutableTerminal::ofNullable).collect(Collectors.toList());
    }

    @Override
    public void remove() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }
}
