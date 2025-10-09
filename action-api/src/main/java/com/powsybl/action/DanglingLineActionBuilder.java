package com.powsybl.action;

public class DanglingLineActionBuilder extends AbstractLoadActionBuilder<DanglingLineAction, DanglingLineActionBuilder> {

    public DanglingLineActionBuilder withDanglingLineId(String danglingLineId) {
        return withNetworkElementId(danglingLineId);
    }

    @Override
    public String getType() {
        return DanglingLineAction.NAME;
    }

    @Override
    public DanglingLineAction build() {
        if (this.getRelativeValue() == null) {
            throw new IllegalArgumentException("For a load action, relativeValue must be provided");
        }
        if (this.getActivePowerValue() == null && this.getReactivePowerValue() == null) {
            throw new IllegalArgumentException("For a load action, activePowerValue or reactivePowerValue must be provided");
        }
        return new DanglingLineAction(this.getId(), this.getElementId(), this.getRelativeValue(), this.getActivePowerValue(),
            this.getReactivePowerValue());
    }
}
