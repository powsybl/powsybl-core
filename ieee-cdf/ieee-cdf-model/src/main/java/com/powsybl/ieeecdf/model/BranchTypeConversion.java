/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model;

import com.univocity.parsers.conversions.ObjectConversion;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class BranchTypeConversion extends ObjectConversion<IeeeCdfBranch.Type> {

    @Override
    protected IeeeCdfBranch.Type fromString(String str) {
        String trimmedStr = str.trim();
        return trimmedStr.isEmpty() ? null : IeeeCdfBranch.Type.values()[Integer.parseInt(trimmedStr)];
    }

    @Override
    public String revert(IeeeCdfBranch.Type type) {
        return type == null ? "" : Integer.toString(type.ordinal());
    }
}
