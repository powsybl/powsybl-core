/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum UcteElementStatus {

    /**
     * 0: real element in operation (R, X only positive values permitted).
     */
    REAL_ELEMENT_IN_OPERATION(0),

    /**
     * 8: real element out of operation (R, X only positive values permitted).
     */
    REAL_ELEMENT_OUT_OF_OPERATION(8),

    /**
     * 1: equivalent element in operation.
     */
    EQUIVALENT_ELEMENT_IN_OPERATION(1),

    /**
     * 9: equivalent element out of operation.
     */
    EQUIVALENT_ELEMENT_OUT_OF_OPERATION(9),

    /**
     * 2: busbar coupler in operation (definition: R=0, X=0, B=0).
     */
    BUSBAR_COUPLER_IN_OPERATION(2),

    /**
     * 7: busbar coupler out of operation (definition: R=0, X=0, B=0).
     */
    BUSBAR_COUPLER_OUT_OF_OPERATION(7);

    private final int code;

    UcteElementStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static UcteElementStatus fromCode(int code) {
        switch (code) {
            case 0: return REAL_ELEMENT_IN_OPERATION;
            case 8: return REAL_ELEMENT_OUT_OF_OPERATION;
            case 1: return EQUIVALENT_ELEMENT_IN_OPERATION;
            case 9: return EQUIVALENT_ELEMENT_OUT_OF_OPERATION;
            case 2: return BUSBAR_COUPLER_IN_OPERATION;
            case 7: return BUSBAR_COUPLER_OUT_OF_OPERATION;
            default: throw new IllegalArgumentException("Unknow element status code " + code);
        }
    }
}
