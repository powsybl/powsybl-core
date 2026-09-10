package com.powsybl.iidm.network.impl;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
abstract class AbstractConnectableAdder<T extends AbstractConnectableAdder<T>> extends AbstractIdentifiableAdder<T> {

    private boolean equivalent;

    public T setEquivalent(boolean equivalent) {
        this.equivalent = equivalent;
        return (T) this;
    }

    protected boolean isEquivalent() {
        return equivalent;
    }

}
