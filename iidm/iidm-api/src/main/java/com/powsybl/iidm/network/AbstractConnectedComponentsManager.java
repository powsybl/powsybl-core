package com.powsybl.iidm.network;

import gnu.trove.list.array.TIntArrayList;

import java.util.Map;

public abstract class AbstractConnectedComponentsManager<C extends Component> extends AbstractComponentsManager<C> {

    protected AbstractConnectedComponentsManager(Network network) {
        super(network);
    }

    @Override
    protected void fillAdjacencyList(Map<String, Integer> id2num, TIntArrayList[] adjacencyList) {
        super.fillAdjacencyList(id2num, adjacencyList);
        for (HvdcLine line : network.getHvdcLines()) {
            Bus bus1 = line.getConverterStation1().getTerminal().getBusView().getBus();
            Bus bus2 = line.getConverterStation2().getTerminal().getBusView().getBus();
            addToAdjacencyList(bus1, bus2, id2num, adjacencyList);
        }
    }

    @Override
    protected String getComponentLabel() {
        return "Connected";
    }
}
