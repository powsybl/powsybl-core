package com.powsybl.security.action;

import com.powsybl.contingency.contingency.list.identifier.IdBasedNetworkElementIdentifier;
import com.powsybl.contingency.contingency.list.identifier.NetworkElementIdentifier;

import java.util.Collections;
import java.util.List;

public class DanglingLineActionBuilder extends AbstractLoadActionBuilder<DanglingLineAction, DanglingLineActionBuilder> {

    public DanglingLineActionBuilder withDanglingLineId(String danglingLineId) {
        this.networkElementIdentifiers = Collections.singletonList(new IdBasedNetworkElementIdentifier(danglingLineId));
        return this;
    }

    public DanglingLineActionBuilder withDanglingLineIdentifiers(List<NetworkElementIdentifier> networkElementIdentifiers) {
        this.networkElementIdentifiers = networkElementIdentifiers;
        return this;
    }

    public DanglingLineAction build() {
        if (relativeValue == null) {
            throw new IllegalArgumentException("For a load action, relativeValue must be provided");
        }
        if (activePowerValue == null && reactivePowerValue == null) {
            throw new IllegalArgumentException("For a load action, activePowerValue or reactivePowerValue must be provided");
        }
        return new DanglingLineAction(id, networkElementIdentifiers, relativeValue, activePowerValue, reactivePowerValue);
    }
}
