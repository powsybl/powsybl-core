/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class ReportNodeJsonModule extends SimpleModule {

    public ReportNodeJsonModule() {
        addDeserializer(ReportNode.class, new ReportRootDeserializer());
        addDeserializer(TypedValue.class, new ReportRootDeserializer.TypedValueDeserializer());
        addSerializer(ReportNode.class, new ReportRootSerializer());
        addSerializer(TypedValue.class, new ReportRootSerializer.TypedValueSerializer());
    }
}
