package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;

/**
 * @author Bertrand Rix <bertrand.rix at artelys.com>
 */
public class RemoteReactivePowerControlImpl implements RemoteReactivePowerControl {
    double remoteQ;

    Terminal regulatingTerminal;

    Generator extendedGenerator;

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Generator getExtendable() {
        return extendedGenerator;
    }

    @Override
    public void setExtendable(final Generator extendable) {
        extendedGenerator = extendable;
    }

    @Override
    public void setRemoteQ(double q) {
        remoteQ = q;
    }

    @Override
    public double getRemoteQ() {
        return remoteQ;
    }

    @Override
    public void setRegulatingTerminal(final Terminal t) {
        regulatingTerminal = t;
    }

    @Override
    public Terminal getRegulatingTerminal() {
        return regulatingTerminal;
    }
}
