/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.detectors;

/**
 * Permanently or temporarily admissible limit violations,
 * as defined in the ENTSO-E operation handbook.
 *
 * @see <a href="https://www.entsoe.eu/fileadmin/user_upload/_library/publications/entsoe/Operation_Handbook/Policy_3_final.pdf">Policy 3 of ENTSO-E operation handbook</a>
 */
public enum LoadingLimitType {
    /**
     * Permanently Admissible Transmission Loading.
     */
    PATL,
    /**
     * Temporary Admissible Transmission Loading.
     */
    TATL
}
