/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Load;

/**
 * @author Jean-Baptiste Heyberger {@literal <jbheyberger at gmail.com>}
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface LoadAsymmetrical extends Extension<Load> {

    String NAME = "loadAsymmetrical";

    @Override
    default String getName() {
        return NAME;
    }

    LoadConnectionType getConnectionType();

    LoadAsymmetrical setConnectionType(LoadConnectionType connectionType);

    /**
     * DeltaPa is used to extend the modelling of load P0 for load with a phase asymmetry. Using that attribute makes P0
     * the balanced part of the power of the load. DeltaPa models the additional unbalanced active power part on phase A.
     * Therefore the complete load to be taken into account in calculations for phase A is Pa = P0 + deltaPa
     */
    double getDeltaPa();

    LoadAsymmetrical setDeltaPa(double deltaPa);

    /**
     * DeltaPb is used to extend the modelling of load P0 for load with a phase asymmetry. Using that attribute makes P0
     * the balanced part of the active power of the load. DeltaPb models the additional unbalanced active power part on phase B.
     * Therefore the complete load to be taken into account in calculations for phase B is Pb = P0 + deltaPb
     */
    double getDeltaPb();

    LoadAsymmetrical setDeltaPb(double deltaPb);

    /**
     * DeltaPc is used to extend the modelling of load P0 for load with a phase asymmetry. Using that attribute makes P0
     * the balanced part of the active power of the load. DeltaPc models the additional unbalanced active power part on phase C.
     * Therefore the complete load to be taken into account in calculations for phase C is Pc = P0 + deltaPc
     */
    double getDeltaPc();

    LoadAsymmetrical setDeltaPc(double deltaPc);

    /**
     * DeltaQa is used to extend the modelling of load Q0 for load with a phase asymmetry. Using that attribute makes Q0
     * the balanced part of the reactive power of the load. DeltaQa models the additional unbalanced reactive power part on phase A.
     * Therefore the complete load to be taken into account in calculations for phase A is Qa = Q0 + deltaQa
     */
    double getDeltaQa();

    LoadAsymmetrical setDeltaQa(double deltaQa);

    /**
     * DeltaQb is used to extend the modelling of load Q0 for load with a phase asymmetry. Using that attribute makes Q0
     * the balanced part of the reactive power of the load. DeltaQb models the additional unbalanced reactive power part on phase B.
     * Therefore the complete load to be taken into account in calculations for phase B is Qb = Q0 + deltaQb
     */
    double getDeltaQb();

    LoadAsymmetrical setDeltaQb(double deltaQb);

    /**
     * DeltaQc is used to extend the modelling of load Q0 for load with a phase asymmetry. Using that attribute makes Q0
     * the balanced part of the reactive power of the load. DeltaQc models the additional unbalanced reactive power part on phase C.
     * Therefore the complete load to be taken into account in calculations for phase C is Qc = Q0 + deltaQc
     */
    double getDeltaQc();

    LoadAsymmetrical setDeltaQc(double deltaQc);
}
