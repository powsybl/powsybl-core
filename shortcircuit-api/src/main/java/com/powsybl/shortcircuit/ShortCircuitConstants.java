/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public final class ShortCircuitConstants {

    private ShortCircuitConstants() {
    }

    public enum StudyType {
        SYSTEMATIC_STUDY,
        SELECTIVE_STUDY,
    }

    public enum SelectiveStudyType {
        BUS_STUDY,
        BRANCH_STUDY,
    }

    public enum ImpedanceConnection {
        SERIES,
        PARALLEL,
    }

    public static final boolean SUBTRANS_STUDY = false;
    public static final boolean WITH_FEEDER_RESULT = true;
    public static final StudyType DEFAULT_STUDY_TYPE = StudyType.SYSTEMATIC_STUDY;
    public static final ImpedanceConnection DEFAULT_IMPEDANCE_CONNECTION = ImpedanceConnection.SERIES;
}
