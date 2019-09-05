/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.stream.Stream;

import org.apache.commons.io.output.NullWriter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.util.TwtTestData;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class Transformers3WValidationTest extends AbstractValidationTest {

    private TwtTestData twtValidationData;

    @Before
    public void setUp() {
        twtValidationData = new TwtTestData();
    }

    @Test
    public void checkTwts() {
        assertFalse(Transformers3WValidation.checkTransformer(twtValidationData.get3WTransformer(), strictConfig, NullWriter.NULL_WRITER));
        strictConfig.setThreshold(.3);
        assertTrue(Transformers3WValidation.checkTransformer(twtValidationData.get3WTransformer(), strictConfig, NullWriter.NULL_WRITER));
        // check NaN values
        twtValidationData.setNanLeg1P();
        assertFalse(Transformers3WValidation.checkTransformer(twtValidationData.get3WTransformer(), strictConfig, NullWriter.NULL_WRITER));
        strictConfig.setOkMissingValues(true);
        assertTrue(Transformers3WValidation.checkTransformer(twtValidationData.get3WTransformer(), strictConfig, NullWriter.NULL_WRITER));
    }

    @Test
    public void checkNetworkTwts() {
        Network network = Mockito.mock(Network.class);
        Mockito.when(network.getId()).thenReturn("network");
        Mockito.when(network.getThreeWindingsTransformerStream()).thenAnswer(dummy -> Stream.of(twtValidationData.get3WTransformer()));

        assertFalse(Transformers3WValidation.checkTransformers(network, strictConfig, NullWriter.NULL_WRITER));
        strictConfig.setThreshold(.3);
        assertTrue(Transformers3WValidation.checkTransformers(network, strictConfig, NullWriter.NULL_WRITER));
    }

}
