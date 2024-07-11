/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity;

import com.fasterxml.jackson.core.JsonToken;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.ContingencyContext;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class SensitivityFactorJsonReader implements SensitivityFactorReader {

    private final Path jsonFile;

    public SensitivityFactorJsonReader(Path jsonFile) {
        this.jsonFile = Objects.requireNonNull(jsonFile);
    }

    @Override
    public void read(Handler handler) {
        Objects.requireNonNull(handler);
        JsonUtil.parseJson(jsonFile, parser -> {
            try {
                var context = new SensitivityFactor.ParsingContext();
                JsonToken token;
                while ((token = parser.nextToken()) != null) {
                    if (token == JsonToken.FIELD_NAME) {
                        SensitivityFactor.parseJson(parser, context);
                    } else if (token == JsonToken.END_OBJECT) {
                        handler.onFactor(context.functionType, context.functionId, context.variableType, context.variableId, context.variableSet,
                                ContingencyContext.create(context.contingencyIds, context.contingencyContextType));
                        context.reset();
                    } else if (token == JsonToken.END_ARRAY) {
                        break;
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return null;
        });
    }
}
