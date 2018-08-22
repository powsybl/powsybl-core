/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import com.powsybl.commons.json.JsonUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Metadata parsing issue when there is a tag with name "name".
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NameTagBugTest {

    @Test
    public void test() {
        String jsonRef = String.join(System.lineSeparator(),
                "{",
                "  \"name\" : \"ts1\",",
                "  \"dataType\" : \"DOUBLE\",",
                "  \"tags\" : [ {",
                "    \"name\" : \"foo\"",
                "  } ],",
                "  \"regularIndex\" : {",
                "    \"startTime\" : 1420070400000,",
                "    \"endTime\" : 1420074000000,",
                "    \"spacing\" : 900000",
                "  }",
                "}");
        TimeSeriesMetadata metadata = JsonUtil.parseJson(jsonRef, TimeSeriesMetadata::parseJson);
        String json = JsonUtil.toJson(metadata::writeJson);
        assertEquals(jsonRef, json);
    }
}
