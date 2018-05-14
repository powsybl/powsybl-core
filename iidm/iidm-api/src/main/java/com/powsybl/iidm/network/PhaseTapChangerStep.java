/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface PhaseTapChangerStep extends TapChangerStep<PhaseTapChangerStep> {

    /**
     * Get the angle difference in degree.
     */
    double getAlpha();

    /**
     * Set the angle difference in degree.
     */
    PhaseTapChangerStep setAlpha(double alpha);

}
