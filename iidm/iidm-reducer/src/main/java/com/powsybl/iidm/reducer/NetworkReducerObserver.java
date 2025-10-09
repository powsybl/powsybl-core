/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.reducer;

import com.powsybl.iidm.network.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public interface NetworkReducerObserver {

    void substationRemoved(Substation substation);

    void voltageLevelRemoved(VoltageLevel voltageLevel);

    void lineReplaced(Line line, Injection injection);

    void lineRemoved(Line line);

    void tieLineRemoved(TieLine tieLine);

    void transformerReplaced(TwoWindingsTransformer transformer, Injection injection);

    void transformerRemoved(TwoWindingsTransformer transformer);

    void transformerReplaced(ThreeWindingsTransformer transformer, Injection injection);

    void transformerRemoved(ThreeWindingsTransformer transformer);

    void hvdcLineReplaced(HvdcLine hvdcLine, Injection injection);

    void hvdcLineRemoved(HvdcLine hvdcLine);

}
