package com.powsybl.cgmes.conversion.elements.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.ThreeWindingsTransformer;

public class PhaseAngleClocksExtension extends AbstractExtension<ThreeWindingsTransformer> {

    public PhaseAngleClocksExtension(int clock1, int clock2, int clock3) {
        this.clock1 = clock1;
        this.clock2 = clock2;
        this.clock3 = clock3;
    }

    public int clock1() {
        return clock1;
    }

    public int clock2() {
        return clock2;
    }

    public int clock3() {
        return clock3;
    }

    @Override
    public String getName() {
        return "PhaseAngleClocks";
    }

    private final int clock1;
    private final int clock2;
    private final int clock3;
}
