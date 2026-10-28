/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
module com.powsybl.math {
    requires com.google.common;
    requires transitive com.powsybl.commons;
    requires transitive commons.math3; // No Automatic-Module-Name
    requires ejml.core; // No Automatic-Module-Name
    requires transitive jama; // No Automatic-Module-Name
    requires org.scijava.nativelib;
    requires transitive org.slf4j;
    requires transitive trove4j; // No Automatic-Module-Name
    requires us.hebi.matlab.mat.mfl.core;
    requires us.hebi.matlab.mat.mfl.ejml;

    exports com.powsybl.math;
    exports com.powsybl.math.casting;
    exports com.powsybl.math.graph;
    exports com.powsybl.math.matrix;
    exports com.powsybl.math.matrix.serializer;
    exports com.powsybl.math.solver;
}
