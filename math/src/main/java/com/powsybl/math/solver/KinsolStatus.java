/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.solver;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public enum KinsolStatus {
    KIN_SUCCESS(0),
    KIN_INITIAL_GUESS_OK(1),
    KIN_STEP_LT_STPTOL(2),
    KIN_MEM_NULL(-1),
    KIN_ILL_INPUT(-2),
    KIN_NO_MALLOC(-3),
    KIN_MEM_FAIL(-4),
    KIN_LINESEARCH_NONCONV(-5),
    KIN_MAXITER_REACHED(-6),
    KIN_MXNEWT_5X_EXCEEDED(-7),
    KIN_LINESEARCH_BCFAIL(-8),
    KIN_LINSOLV_NO_RECOVERY(-9),
    KIN_LINIT_FAIL(-10),
    KIN_LSETUP_FAIL(-11),
    KIN_LSOLVE_FAIL(-12),
    KIN_SYSFUNC_FAIL(-13),
    KIN_FIRST_SYSFUNC_ERR(-14),
    KIN_REPTD_SYSFUNC_ERR(-15);

    private final int value;

    KinsolStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    static KinsolStatus fromValue(int value) {
        for (KinsolStatus status : KinsolStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new KinsolException("Unkown solver status value: " + value);
    }
}
