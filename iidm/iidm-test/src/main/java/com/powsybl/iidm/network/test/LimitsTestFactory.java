package com.powsybl.iidm.network.test;

import com.powsybl.iidm.network.CurrentLimitsAdder;

public final class LimitsTestFactory {

    private LimitsTestFactory() {
    }

    public static void createLimits(CurrentLimitsAdder limitsAdder) {
        limitsAdder.setPermanentLimit(1000f)
                .beginTemporaryLimit()
                .setName("20'")
                .setAcceptableDuration(20 * 60)
                .setValue(1100f)
                .endTemporaryLimit()
                .add();
    }

}
