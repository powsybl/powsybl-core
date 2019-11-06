/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.VoltageLevel.NodeBreakerView;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class VoltageLevelNodeBreakerViewAdapter extends AbstractAdapter<VoltageLevel.NodeBreakerView> implements VoltageLevel.NodeBreakerView {

    protected VoltageLevelNodeBreakerViewAdapter(final NodeBreakerView delegate, final MergingViewIndex index) {
        super(delegate, index);
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
    public VoltageLevelNodeBreakerViewAdapter setNodeCount(final int count) {
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
    public TerminalAdapter getTerminal(final int node) {
        return getIndex().getTerminal(getDelegate().getTerminal(node));
    }

    @Override
    public TerminalAdapter getTerminal1(final String switchId) {
        return getIndex().getTerminal(getDelegate().getTerminal1(switchId));
    }

    @Override
    public TerminalAdapter getTerminal2(final String switchId) {
        return getIndex().getTerminal(getDelegate().getTerminal2(switchId));
    }

    @Override
    public SwitchAdapter getSwitch(final String switchId) {
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
    public int getSwitchCount() {
        return  getDelegate().getSwitchCount();
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
    public int getBusbarSectionCount() {
        return getDelegate().getBusbarSectionCount();
    }

    @Override
    public BusbarSectionAdapter getBusbarSection(final String id) {
        return getIndex().getBusbarSection(getDelegate().getBusbarSection(id));
    }

    @Override
    public void traverse(final int node, final Traverser traverser) {
        getDelegate().traverse(node, traverser);
    }

    @Override
    public VoltageLevelNodeBreakerViewSwitchAdderAdapter newSwitch() {
        return new VoltageLevelNodeBreakerViewSwitchAdderAdapter(getDelegate().newSwitch(), getIndex());
    }

    @Override
    public InternalConnectionAdder newInternalConnection() {
        return getDelegate().newInternalConnection();
    }

    @Override
    public VoltageLevelNodeBreakerViewSwitchAdderAdapter newBreaker() {
        return new VoltageLevelNodeBreakerViewSwitchAdderAdapter(getDelegate().newBreaker(), getIndex());
    }

    @Override
    public VoltageLevelNodeBreakerViewSwitchAdderAdapter newDisconnector() {
        return new VoltageLevelNodeBreakerViewSwitchAdderAdapter(getDelegate().newDisconnector(), getIndex());
    }

    @Override
    public void removeSwitch(final String switchId) {
        getDelegate().removeSwitch(switchId);
    }

    @Override
    public BusbarSectionAdderAdapter newBusbarSection() {
        return new BusbarSectionAdderAdapter(getDelegate().newBusbarSection(), getIndex());
    }
}
