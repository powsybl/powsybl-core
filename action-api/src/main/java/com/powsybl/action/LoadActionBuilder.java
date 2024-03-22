package com.powsybl.action;

public class LoadActionBuilder extends AbstractLoadActionBuilder<LoadAction, LoadActionBuilder> {

    public LoadActionBuilder withLoadId(String loadId) {
        this.elementId = loadId;
        return this;
    }

    @Override
    public LoadAction build() {
        if (relativeValue == null) {
            throw new IllegalArgumentException("For a load action, relativeValue must be provided");
        }
        if (activePowerValue == null && reactivePowerValue == null) {
            throw new IllegalArgumentException("For a load action, activePowerValue or reactivePowerValue must be provided");
        }
        return new LoadAction(id, elementId, relativeValue, activePowerValue, reactivePowerValue);
    }
}
