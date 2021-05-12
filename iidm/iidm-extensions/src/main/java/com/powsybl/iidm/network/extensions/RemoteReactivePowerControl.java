package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Terminal;

public interface RemoteReactivePowerControl extends Extension<Generator> {

    void setRemoteQ(double q);

    double getRemoteQ();

    void setRegulatingTerminal(Terminal t);

    Terminal getRegulatingTerminal();
}
