package com.powsybl.action;

public class LoadActionBuilder extends AbstractLoadActionBuilder<LoadAction, LoadActionBuilder> {

    public LoadActionBuilder withLoadId(String loadId) {
        return withNetworkElementId(loadId);
    }

    @Override
    public String getType() {
        return LoadAction.NAME;
    }

    @Override
    public LoadAction build() {
        if (this.getRelativeValue() == null) {
            throw new IllegalArgumentException("For a load action, relativeValue must be provided");
        }
        if (this.getActivePowerValue() == null && this.getReactivePowerValue() == null) {
            throw new IllegalArgumentException("For a load action, activePowerValue or reactivePowerValue must be provided");
        }
        return new LoadAction(this.getId(), this.getElementId(), this.getRelativeValue(), this.getActivePowerValue(),
            this.getReactivePowerValue());
    }
}
