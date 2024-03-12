package com.powsybl.security.action;

import com.powsybl.contingency.contingency.list.identifier.IdBasedNetworkElementIdentifier;
import com.powsybl.contingency.contingency.list.identifier.NetworkElementIdentifier;

import java.util.Collections;
import java.util.List;

public class LoadActionBuilder extends AbstractLoadActionBuilder<LoadAction, LoadActionBuilder> {

    public LoadActionBuilder withLoadId(String loadId) {
        this.networkElementIdentifiers = Collections.singletonList(new IdBasedNetworkElementIdentifier(loadId));
        return this;
    }

    public LoadActionBuilder withLoadIdentifiers(List<NetworkElementIdentifier> networkElementIdentifiers) {
        this.networkElementIdentifiers = networkElementIdentifiers;
        return this;
    }

    public LoadAction build() {
        if (relativeValue == null) {
            throw new IllegalArgumentException("For a load action, relativeValue must be provided");
        }
        if (activePowerValue == null && reactivePowerValue == null) {
            throw new IllegalArgumentException("For a load action, activePowerValue or reactivePowerValue must be provided");
        }
        return new LoadAction(id, networkElementIdentifiers, relativeValue, activePowerValue, reactivePowerValue);
    }
}
