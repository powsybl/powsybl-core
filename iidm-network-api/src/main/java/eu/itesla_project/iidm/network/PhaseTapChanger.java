/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network;

/**
 * A phase tap changer that is associated to a transformer to control the phase.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface PhaseTapChanger extends TapChanger<PhaseTapChanger, PhaseTapChangerStep> {

    /**
     * Get the threshold current in A.
     * <p>
     * Depends on the working state.
     * @see StateManager
     */
    float getThresholdI();

    /**
     * Set the threshold current in A.
     * <p>
     * Depends on the working state.
     * @see StateManager
     */
    PhaseTapChanger setThresholdI(float thresholdI);

}
