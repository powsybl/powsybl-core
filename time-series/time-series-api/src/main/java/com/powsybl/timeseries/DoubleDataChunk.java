/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import java.nio.DoubleBuffer;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface DoubleDataChunk extends DataChunk<DoublePoint, DoubleDataChunk> {

    void fillBuffer(DoubleBuffer buffer, int timeSeriesOffset);

    void fillBuffer(BigDoubleBuffer buffer, long timeSeriesOffset);
}
