/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.json;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.loadflow.LoadFlowResult;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.it>
 */
public class LoadFlowResultJsonTest extends AbstractConverterTest {

    private static LoadFlowResult create() {
        return new LoadFlowResult() {
            @Override
            public boolean isOk() {
                return true;
            }

            @Override
            public Map<String, String> getMetrics() {
                Map<String, String> metrics = new HashMap<>();
                metrics.put("nbiter", "4");
                metrics.put("dureeCalcul", "0.02");
                metrics.put("cause", "0");
                metrics.put("contraintes", "0");
                metrics.put("statut", "OK");
                metrics.put("csprMarcheForcee", "0");
                return metrics;
            }

            @Override
            public String getLogs() {
                return "";
            }
        };
    }

    @Test
    public void roundTripTest() throws IOException {
        roundTripTest(create(), LoadFlowResultSerializer::write, LoadFlowResultDeserializer::read, "/LoadFlowResult.json");
    }

    @Test
    public void handleErrorTest() throws IOException {
        try {
            LoadFlowResultDeserializer.read(getClass().getResourceAsStream("/LoadFlowResultError.json"));
            Assert.fail();
        } catch (AssertionError ignored) {
        }
    }

}
