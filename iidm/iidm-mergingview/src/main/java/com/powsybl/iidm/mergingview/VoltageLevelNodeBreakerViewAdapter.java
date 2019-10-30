/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.VoltageLevel.NodeBreakerView;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class VoltageLevelNodeBreakerViewAdapter extends AbstractAdapter<VoltageLevel.NodeBreakerView> implements VoltageLevel.NodeBreakerView {

    VoltageLevelNodeBreakerViewAdapter(final NodeBreakerView delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public Terminal getTerminal(final int node) {
        return getIndex().getTerminal(getDelegate().getTerminal(node));
    }

    @Override
    public Terminal getTerminal1(final String switchId) {
        return getIndex().getTerminal(getDelegate().getTerminal1(switchId));
    }

    @Override
    public Terminal getTerminal2(final String switchId) {
        return getIndex().getTerminal(getDelegate().getTerminal2(switchId));
    }

    @Override
    public Switch getSwitch(final String switchId) {
        return getIndex().getSwitch(getDelegate().getSwitch(switchId));
    }

    @Override
    public Iterable<Switch> getSwitches() {
        return getSwitchStream().collect(Collectors.toList());
    }

    @Override
    public Stream<Switch> getSwitchStream() {
        return getDelegate().getSwitchStream().map(getIndex()::getSwitch);
    }

    @Override
    public Iterable<BusbarSection> getBusbarSections() {
        return getBusbarSectionStream().collect(Collectors.toList());
    }

    @Override
    public Stream<BusbarSection> getBusbarSectionStream() {
        return getDelegate().getBusbarSectionStream().map(getIndex()::getBusbarSection);
    }

    @Override
    public BusbarSection getBusbarSection(final String id) {
        return getIndex().getBusbarSection(getDelegate().getBusbarSection(id));
    }

    @Override
    public VoltageLevel.NodeBreakerView.SwitchAdder newSwitch() {
        return new VoltageLevelNodeBreakerViewSwitchAdderAdapter(getDelegate().newSwitch(), getIndex());
    }

    @Override
    public VoltageLevel.NodeBreakerView.SwitchAdder newBreaker() {
        return new VoltageLevelNodeBreakerViewSwitchAdderAdapter(getDelegate().newBreaker(), getIndex());
    }

    @Override
    public VoltageLevel.NodeBreakerView.SwitchAdder newDisconnector() {
        return new VoltageLevelNodeBreakerViewSwitchAdderAdapter(getDelegate().newDisconnector(), getIndex());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public int getNodeCount() {
        return getDelegate().getNodeCount();
    }

    @Override
    public int[] getNodes() {
        return getDelegate().getNodes();
    }

    @Override
    public VoltageLevel.NodeBreakerView setNodeCount(final int count) {
        getDelegate().setNodeCount(count);
        return this;
    }

    @Override
    public int getInternalConnectionCount() {
        return getDelegate().getInternalConnectionCount();
    }

    @Override
    public Iterable<InternalConnection> getInternalConnections() {
        return getDelegate().getInternalConnections();
    }

    @Override
    public Stream<InternalConnection> getInternalConnectionStream() {
        return getDelegate().getInternalConnectionStream();
    }

    @Override
    public int getNode1(final String switchId) {
        return getDelegate().getNode1(switchId);
    }

    @Override
    public int getNode2(final String switchId) {
        return getDelegate().getNode2(switchId);
    }

    @Override
    public int getSwitchCount() {
        return  getDelegate().getSwitchCount();
    }

    @Override
    public int getBusbarSectionCount() {
        return getDelegate().getBusbarSectionCount();
    }

    @Override
    public InternalConnectionAdder newInternalConnection() {
        return getDelegate().newInternalConnection();
    }

    @Override
    public BusbarSectionAdder newBusbarSection() {
        return new BusbarSectionAdderAdapter(getDelegate().newBusbarSection(), getIndex());
    }

    // ------------------------------
    // Not implemented methods ------
    // ------------------------------
    @Override
    public void removeSwitch(final String switchId) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public void traverse(final int node, final Traverser traverser) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
