/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class ReporterModelJsonModule extends SimpleModule {

    public ReporterModelJsonModule() {
        addDeserializer(ReporterModel.class, new ReporterModelDeserializer());
        addDeserializer(TypedValue.class, new ReporterModelDeserializer.TypedValueDeserializer());
        addSerializer(ReporterModel.class, new ReporterModelSerializer());
        addSerializer(TypedValue.class, new ReporterModelSerializer.TypedValueSerializer());
    }
}
