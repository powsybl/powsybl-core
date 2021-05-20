package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;

/**
 * @author Bertrand Rix <bertrand.rix at artelys.com>
 */
public class RemoteReactivePowerControlImpl extends AbstractExtension<Generator> implements RemoteReactivePowerControl {

    double targetQ;

    Terminal regulatingTerminal;

    boolean enabled;

    public RemoteReactivePowerControlImpl(double targetQ, Terminal regulatingTerminal, boolean enabled) {
        this.targetQ = targetQ;
        this.regulatingTerminal = regulatingTerminal;
        this.enabled = enabled;
    }

    @Override
    public void setTargetQ(double targetQ) {
        this.targetQ = targetQ;
    }

    @Override
    public double getTargetQ() {
        return targetQ;
    }

    @Override
    public void setRegulatingTerminal(Terminal regulatingTerminal) {
        this.regulatingTerminal = regulatingTerminal;
    }

    @Override
    public Terminal getRegulatingTerminal() {
        return regulatingTerminal;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
