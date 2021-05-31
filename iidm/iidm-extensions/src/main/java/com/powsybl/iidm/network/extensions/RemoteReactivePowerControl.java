package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Terminal;

public interface RemoteReactivePowerControl extends Extension<Generator> {
    @Override
    default String getName() {
        return "generatorRemoteReactivePowerControl";
    }

    /**
     * Get the reactive power target in MVar, at the remote regulating terminal. It is not the local target of the generator.
     */
    double getTargetQ();

    /**
     * Get the regulating terminal where the reactive power should be controlled. The regulating terminal should be the
     * terminal of a branch or the terminal of a dangling line that will be merged later. It is not the regulating terminal
     * of the generator used for voltage control that is less restrictive.
     */
    Terminal getRegulatingTerminal();

    /**
     * Says if the control is active or not.
     */
    boolean isEnabled();
}
