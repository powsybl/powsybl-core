/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.extensions.LinePosition;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
@AutoService(ExtensionAdderProvider.class)
public class LinePositionAdderImplProvider<I extends Identifiable<I>> implements
        ExtensionAdderProvider<I, LinePosition<I>, LinePositionAdderImpl<I>> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public String getExtensionName() {
        return LinePosition.NAME;
    }

    @Override
    public Class<LinePositionAdderImpl> getAdderClass() {
        return LinePositionAdderImpl.class;
    }

    @Override
    public LinePositionAdderImpl<I> newAdder(I line) {
        if (!(line instanceof Line) && !(line instanceof DanglingLine)) {
            throw new PowsyblException("Line position extension only supported for lines and dangling lines");
        }
        return new LinePositionAdderImpl<>(line);
    }
}
