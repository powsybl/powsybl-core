/*
 * Copyright (c) 2019-2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model.conversion;

import com.powsybl.ieeecdf.model.IeeeCdfBranch;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class BranchSideConversion {

    private BranchSideConversion() {
        // Utility class
    }

    public static IeeeCdfBranch.Side fromString(String str) {
        return IeeeCdfBranch.Side.values()[Integer.parseInt(str.trim())];
    }

    public static String revert(IeeeCdfBranch.Side side) {
        return side == null ? "" : Integer.toString(side.ordinal());
    }
}
