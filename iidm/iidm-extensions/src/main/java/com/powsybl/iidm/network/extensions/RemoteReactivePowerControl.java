package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Terminal;

public interface RemoteReactivePowerControl extends Extension<Generator> {
    @Override
    default String getName() {
        return "generatorRemoteReactivePowerControl";
    }

    void setTargetQ(double targetQ);

    double getTargetQ();

    void setRegulatingTerminal(Terminal t);

    Terminal getRegulatingTerminal();

    void setEnabled(boolean enabled);

    boolean isEnabled();
}
