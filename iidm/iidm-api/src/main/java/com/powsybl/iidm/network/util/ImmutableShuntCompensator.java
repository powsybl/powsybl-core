/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.Terminal;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ImmutableShuntCompensator extends AbstractImmutableIdentifiable<ShuntCompensator> implements ShuntCompensator {

    ImmutableShuntCompensator(ShuntCompensator identifiable) {
        super(identifiable);
    }

    static ImmutableShuntCompensator ofNullable(ShuntCompensator shunt) {
        return null == shunt ? null : new ImmutableShuntCompensator(shunt);
    }

    @Override
    public int getMaximumSectionCount() {
        return identifiable.getMaximumSectionCount();
    }

    @Override
    public ShuntCompensator setMaximumSectionCount(int maximumSectionCount) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public int getCurrentSectionCount() {
        return identifiable.getCurrentSectionCount();
    }

    @Override
    public ShuntCompensator setCurrentSectionCount(int currentSectionCount) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getbPerSection() {
        return identifiable.getbPerSection();
    }

    @Override
    public ShuntCompensator setbPerSection(double bPerSection) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public double getMaximumB() {
        return identifiable.getMaximumB();
    }

    @Override
    public double getCurrentB() {
        return identifiable.getCurrentB();
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
        return identifiable.getTerminals().stream().map(ImmutableTerminal::new).collect(Collectors.toList());
    }

    @Override
    public void remove() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }
}
