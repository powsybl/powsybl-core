/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.TwoWindingsTransformer;

/**
 * @author Jérémy LABOUS <jlabous at silicom.fr>
 */
public class TwoWindingsTransformerPhaseAngleClockAdderImpl
    extends AbstractExtensionAdder<TwoWindingsTransformer, TwoWindingsTransformerPhaseAngleClock>
    implements TwoWindingsTransformerPhaseAngleClockAdder {

    private int phaseAngleClock = -1;

    protected TwoWindingsTransformerPhaseAngleClockAdderImpl(TwoWindingsTransformer extendable) {
        super(extendable);
    }

    @Override
    protected TwoWindingsTransformerPhaseAngleClock createExtension(TwoWindingsTransformer extendable) {
        return new TwoWindingsTransformerPhaseAngleClockImpl(extendable, phaseAngleClock);
    }

    @Override
    public TwoWindingsTransformerPhaseAngleClockAdder withPhaseAngleClock(int phaseAngleClock) {
        this.phaseAngleClock = phaseAngleClock;
        return this;
    }
}
