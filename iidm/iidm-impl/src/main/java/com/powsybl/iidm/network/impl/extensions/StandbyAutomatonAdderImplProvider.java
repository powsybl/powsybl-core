package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.extensions.StandbyAutomaton;

/**
 * @author Jérémy Labous <jlabous at silicom.fr>
 */
@AutoService(ExtensionAdderProvider.class)
public class StandbyAutomatonAdderImplProvider
        implements ExtensionAdderProvider<StaticVarCompensator, StandbyAutomaton, StandbyAutomatonAdderImpl> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public Class<StandbyAutomatonAdderImpl> getAdderClass() {
        return StandbyAutomatonAdderImpl.class;
    }

    @Override
    public StandbyAutomatonAdderImpl newAdder(StaticVarCompensator extendable) {
        return new StandbyAutomatonAdderImpl(extendable);
    }
}
