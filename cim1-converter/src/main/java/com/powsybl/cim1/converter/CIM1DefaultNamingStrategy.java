/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cim1.converter;

import cim1.model.IdentifiedObject;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class CIM1DefaultNamingStrategy implements CIM1NamingStrategy {

    @Override
    public String getId(IdentifiedObject object) {
        return object.getId();
    }

    @Override
    public String getName(IdentifiedObject object) {
        return object.getName();
    }

    @Override
    public String getCimId(String id) {
        return id;
    }
}
