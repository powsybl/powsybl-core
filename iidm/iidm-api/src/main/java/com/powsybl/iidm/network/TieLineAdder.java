/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface TieLineAdder extends IdentifiableAdder<TieLine, TieLineAdder> {

    TieLineAdder setDanglingLine1(String dl1Id);

    TieLineAdder setDanglingLine2(String dl2Id);

    @Override
    TieLine add();

}
