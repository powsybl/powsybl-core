package com.powsybl.action;

public class DanglingLineActionBuilder extends AbstractLoadActionBuilder<DanglingLineAction, DanglingLineActionBuilder> {

    public DanglingLineActionBuilder withDanglingLineId(String danglingLineId) {
        this.elementId = danglingLineId;
        return this;
    }

    public DanglingLineAction build() {
        if (relativeValue == null) {
            throw new IllegalArgumentException("For a load action, relativeValue must be provided");
        }
        if (activePowerValue == null && reactivePowerValue == null) {
            throw new IllegalArgumentException("For a load action, activePowerValue or reactivePowerValue must be provided");
        }
        return new DanglingLineAction(id, elementId, relativeValue, activePowerValue, reactivePowerValue);
    }
}
