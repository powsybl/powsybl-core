/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.solver;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public enum KinsolSolveStatus {
    KIN_SUCCESS,
    KIN_INITIAL_GUESS_OK,
    KIN_STEP_LT_STPTOL,
    KIN_MEM_NULL,
    KIN_ILL_INPUT,
    KIN_NO_MALLOC,
    KIN_MEM_FAIL,
    KIN_LINESEARCH_NONCONV,
    KIN_MAXITER_REACHED,
    KIN_MXNEWT_5X_EXCEEDED,
    KIN_LINESEARCH_BCFAIL,
    KIN_LINSOLV_NO_RECOVERY,
    KIN_LINIT_FAIL,
    KIN_LSETUP_FAIL,
    KIN_LSOLVE_FAIL,
    KIN_SYSFUNC_FAIL,
    KIN_FIRST_SYSFUNC_ERR,
    KIN_REPTD_SYSFUNC_ERR
}
