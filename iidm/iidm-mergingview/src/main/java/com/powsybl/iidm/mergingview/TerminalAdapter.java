/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Terminal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TerminalAdapter extends AbstractAdapter<Terminal> implements Terminal, MultiVariantObject {

    class BusBreakerViewAdapter extends AbstractAdapter<Terminal.BusBreakerView> implements Terminal.BusBreakerView {

        BusBreakerViewAdapter(final BusBreakerView delegate, final MergingViewIndex index) {
            super(delegate, index);
        }

        @Override
        public Bus getBus() {
            return getBus(getDelegate().getBus());
        }

        @Override
        public Bus getConnectableBus() {
            return getBus(getDelegate().getConnectableBus());
        }

        @Override
        public void setConnectableBus(final String busId) {
            getDelegate().setConnectableBus(busId);
        }

        @Override
        public void moveConnectable(String busId, boolean connected) {
            throw new PowsyblException("Not implemented");
        }

        private Bus getBus(Bus bus) {
            return getVoltageLevel().getBusBreakerView().getBus(bus);
        }
    }

    private BusBreakerViewAdapter busBreakerView;

    class BusViewAdapter extends AbstractAdapter<Terminal.BusView> implements Terminal.BusView {

        BusViewAdapter(final BusView delegate, final MergingViewIndex index) {
            super(delegate, index);
        }

        @Override
        public Bus getBus() {
            return getBus(getDelegate().getBus());
        }

        @Override
        public Bus getConnectableBus() {
            return getBus(getDelegate().getConnectableBus());
        }

        private Bus getBus(Bus bus) {
            return getVoltageLevel().getBusView().getBus(bus);
        }
    }

    private BusViewAdapter busView;

    static class NodeBreakerViewAdapter extends AbstractAdapter<Terminal.NodeBreakerView> implements Terminal.NodeBreakerView {

        NodeBreakerViewAdapter(Terminal.NodeBreakerView delegate, MergingViewIndex index) {
            super(delegate, index);
        }

        // -------------------------------
        // Simple delegated methods ------
        // -------------------------------
        @Override
        public int getNode() {
            return getDelegate().getNode();
        }

        @Override
        public void moveConnectable(int node, String voltageLevelId) {
            throw new PowsyblException("Not implemented");
        }
    }

    private NodeBreakerViewAdapter nodeBreakerView;

    private final Map<String, Integer> connectedComponentNumber = new HashMap<>();

    private final Map<String, Integer> synchronousComponentNumber = new HashMap<>();

    TerminalAdapter(final Terminal delegate, final MergingViewIndex index) {
        super(delegate, index);
        busBreakerView = new BusBreakerViewAdapter(getDelegate().getBusBreakerView(), index);
        busView = new BusViewAdapter(getDelegate().getBusView(), index);
        nodeBreakerView = new NodeBreakerViewAdapter(getDelegate().getNodeBreakerView(), index);
    }

    @Override
    public AbstractVoltageLevelAdapter getVoltageLevel() {
        return getIndex().getVoltageLevel(getDelegate().getVoltageLevel());
    }

    @Override
    public Terminal.NodeBreakerView getNodeBreakerView() {
        return nodeBreakerView;
    }

    @Override
    public Terminal.BusBreakerView getBusBreakerView() {
        return busBreakerView;
    }

    @Override
    public Terminal.BusView getBusView() {
        return busView;
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public Connectable getConnectable() {
        return getIndex().getConnectable(getDelegate().getConnectable());
    }

    @Override
    public double getP() {
        return getDelegate().getP();
    }

    @Override
    public Terminal setP(final double p) {
        getDelegate().setP(p);
        return this;
    }

    @Override
    public double getQ() {
        return getDelegate().getQ();
    }

    @Override
    public Terminal setQ(final double q) {
        getDelegate().setQ(q);
        return this;
    }

    @Override
    public double getI() {
        return getDelegate().getI();
    }

    @Override
    public boolean connect() {
        boolean connected = getDelegate().connect();
        if (connected) {
            getVoltageLevel().invalidateCache();
        }
        return connected;
    }

    @Override
    public boolean disconnect() {
        boolean disconnected = getDelegate().disconnect();
        if (disconnected) {
            getVoltageLevel().invalidateCache();
        }
        return disconnected;
    }

    @Override
    public boolean isConnected() {
        return getDelegate().isConnected();
    }

    @Override
    public void cloneVariant(String sourceVariantId, List<String> targetVariantIds) {
        for (String targetVariantId : targetVariantIds) {
            connectedComponentNumber.put(targetVariantId, connectedComponentNumber.get(sourceVariantId));
            synchronousComponentNumber.put(targetVariantId, synchronousComponentNumber.get(sourceVariantId));
        }
    }

    @Override
    public void removeVariant(String variantId) {
        connectedComponentNumber.remove(variantId);
        synchronousComponentNumber.remove(variantId);
    }

    int getConnectedComponentNumber() {
        String variantId = getIndex().getView().getVariantManager().getWorkingVariantId();
        return connectedComponentNumber.get(variantId);
    }

    void setConnectedComponentNumber(int connectedComponentNumber) {
        String variantId = getIndex().getView().getVariantManager().getWorkingVariantId();
        this.connectedComponentNumber.put(variantId, connectedComponentNumber);
    }

    int getSynchronousComponentNumber() {
        String variantId = getIndex().getView().getVariantManager().getWorkingVariantId();
        return synchronousComponentNumber.get(variantId);
    }

    void setSynchronousComponentNumber(int synchronousComponentNumber) {
        String variantId = getIndex().getView().getVariantManager().getWorkingVariantId();
        this.synchronousComponentNumber.put(variantId, synchronousComponentNumber);
    }

    @Override
    public void traverse(final TopologyTraverser traverser) {
        getDelegate().traverse(new TopologyTraverserAdapter(traverser, getIndex()));
    }
}
