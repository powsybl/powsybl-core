/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class SelectiveStudyFeederResult {
    private String busId;
    private String connectableId;
    private double feederThreePhaseCurrent;
    private double feederThreePhaseVoltage;
    private ValuesOnThreePhases feederCurrentPerPhase;
    private ValuesOnThreePhases feederVoltagePerPhase;
    private double voltageDrop;

}
