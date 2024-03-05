package com.powsybl.security.action;

public class DanglingLineActionBuilder extends AbstractLoadActionBuilder<DanglingLineAction> {
    public DanglingLineAction build() {
        if (relativeValue == null) {
            throw new IllegalArgumentException("For a load action, relativeValue must be provided");
        }
        if (activePowerValue == null && reactivePowerValue == null) {
            throw new IllegalArgumentException("For a load action, activePowerValue or reactivePowerValue must be provided");
        }
        return new DanglingLineAction(id, loadId, relativeValue, activePowerValue, reactivePowerValue);
    }
}
