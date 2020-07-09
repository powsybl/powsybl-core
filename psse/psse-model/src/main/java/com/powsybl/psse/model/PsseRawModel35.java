/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model;

import java.util.List;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class PsseRawModel35 extends PsseRawModel {

    public PsseRawModel35(PsseCaseIdentification caseIdentification) {
        super(caseIdentification);
    }

    @Override
    public void addLoads(List<PsseLoad> loads) {
        // loads.forEach(load -> assertTrue(load instanceof PsseLoad35));
        loads.forEach(load -> {
            if (load instanceof PsseLoad35) {
                System.err.printf("es PsseLoad35 %n");
            } else {
                System.err.printf("es PsseLoad %n");
            }
        });
        getLoads().addAll(loads);
    }
}
