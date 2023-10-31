package com.powsybl.security.detectors.criterion.network;

public interface NetworkElementCriterion {

    enum NetworkElementCriterionType {
        BRANCH,
        BUS
    }

    NetworkElementCriterionType getNetworkElementCriterionType();

}
