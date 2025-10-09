package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.LoadingLimits;

import java.util.List;

public abstract class AbstractIdenticalLimitsTest {
    public boolean areLimitsIdentical(LoadingLimits limits1, LoadingLimits limits2) {
        boolean areIdentical = limits1.getPermanentLimit() == limits2.getPermanentLimit();

        List<LoadingLimits.TemporaryLimit> tempLimits1 = limits1.getTemporaryLimits().stream().toList();
        List<LoadingLimits.TemporaryLimit> tempLimits2 = limits2.getTemporaryLimits().stream().toList();

        if (areIdentical && tempLimits1.size() == tempLimits2.size()) {
            for (int i = 0; i < tempLimits1.size(); i++) {
                LoadingLimits.TemporaryLimit limit1 = tempLimits1.get(i);
                LoadingLimits.TemporaryLimit limit2 = tempLimits2.get(i);

                if (!limit1.getName().equals(limit2.getName()) ||
                        limit1.getAcceptableDuration() != limit2.getAcceptableDuration() ||
                        limit1.getValue() != limit2.getValue()) {
                    areIdentical = false;
                    break;
                }
            }
        } else {
            areIdentical = false;
        }

        return areIdentical;
    }
}
