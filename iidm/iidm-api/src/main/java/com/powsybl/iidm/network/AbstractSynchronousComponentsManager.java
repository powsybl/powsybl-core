package com.powsybl.iidm.network;

public abstract class AbstractSynchronousComponentsManager<C extends Component> extends AbstractComponentsManager<C> {

    protected AbstractSynchronousComponentsManager(Network network) {
        super(network);
    }

    @Override
    protected String getComponentLabel() {
        return "Synchronous";
    }
}
