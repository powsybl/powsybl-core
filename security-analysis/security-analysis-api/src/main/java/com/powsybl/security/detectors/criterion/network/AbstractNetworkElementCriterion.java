package com.powsybl.security.detectors.criterion.network;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractNetworkElementCriterion {
    private Set<String> networkElementIds = new HashSet<>();

    protected AbstractNetworkElementCriterion(Set<String> networkElementIds) {
        this.networkElementIds = networkElementIds;
    }

    public Set<String> getNetworkElementIds() {
        return networkElementIds;
    }

}
