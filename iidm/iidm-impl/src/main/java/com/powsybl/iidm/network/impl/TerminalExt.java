/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Terminal;
import com.powsybl.math.graph.TraverseResult;

import java.util.Set;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
interface TerminalExt extends Terminal, MultiVariantObject {

    interface BusBreakerViewExt extends BusBreakerView {

        @Override BusExt getBus();

    }

    interface BusViewExt extends BusView {

        @Override BusExt getBus();

    }

    @Override
    AbstractConnectable getConnectable();

    void setConnectable(AbstractConnectable connectable);

    @Override VoltageLevelExt getVoltageLevel();

    void setVoltageLevel(VoltageLevelExt voltageLevel);

    @Override BusBreakerViewExt getBusBreakerView();

    @Override BusViewExt getBusView();

    void setNum(int num);

    /**
     * Traverse from this terminal using the given topology traverser, knowing that the terminals in the given
     * set have already been visited.
     * @return false if the traverser has to stop, meaning that a {@link TraverseResult#TERMINATE_TRAVERSER}
     * has been returned from the traverser, true otherwise
     */
    boolean traverse(TopologyTraverser traverser, Set<Terminal> visitedTerminals);

    String getConnectionInfo();

    void removeAsRegulationPoint();

    void remove();

    void setAsRegulatingPoint(RegulatingPoint rp);

    void removeRegulatingPoint(RegulatingPoint rp);
}
