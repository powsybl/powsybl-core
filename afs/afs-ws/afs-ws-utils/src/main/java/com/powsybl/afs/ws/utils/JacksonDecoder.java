/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.json.JsonUtil;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import java.io.IOException;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class JacksonDecoder<T> implements Decoder.Text<T> {

    private final ObjectMapper objectMapper = JsonUtil.createObjectMapper();

    private Class<T> type;

    protected JacksonDecoder(Class<T> type) {
        this.type = Objects.requireNonNull(type);
    }

    @Override
    public T decode(String s) throws DecodeException {
        try {
            return objectMapper.readValue(s, type);
        } catch (IOException e) {
            throw new DecodeException(s, "Decoding error", e);
        }
    }

    @Override
    public boolean willDecode(String s) {
        return true;
    }

    @Override
    public void init(EndpointConfig endpointConfig) {
        // nothing to do
    }

    @Override
    public void destroy() {
        // nothing to do
    }
}
