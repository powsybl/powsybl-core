package com.powsybl.action;

public class BoundaryLineActionBuilder extends AbstractLoadActionBuilder<BoundaryLineAction, BoundaryLineActionBuilder> {

    public BoundaryLineActionBuilder withDanglingLineId(String danglingLineId) {
        return withNetworkElementId(danglingLineId);
    }

    @Override
    public String getType() {
        return BoundaryLineAction.NAME;
    }

    @Override
    public BoundaryLineAction build() {
        if (this.getRelativeValue() == null) {
            throw new IllegalArgumentException("For a load action, relativeValue must be provided");
        }
        if (this.getActivePowerValue() == null && this.getReactivePowerValue() == null) {
            throw new IllegalArgumentException("For a load action, activePowerValue or reactivePowerValue must be provided");
        }
        return new BoundaryLineAction(this.getId(), this.getElementId(), this.getRelativeValue(), this.getActivePowerValue(),
            this.getReactivePowerValue());
    }
}
