/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.afs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.Country;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;
import com.powsybl.security.json.SecurityAnalysisJsonModule;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.TreeSet;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SubjectInfoExtensionTest {

    @Test
    public void test() throws IOException {
        SubjectInfoExtension extension = new SubjectInfoExtension(new TreeSet<>(Arrays.asList(Country.FR, Country.BE)),
                                                                  new TreeSet<>(Arrays.asList(225d, 400d)));
        assertEquals(Sets.newHashSet(225d, 400d), extension.getNominalVoltages());
        assertEquals(Sets.newHashSet(Country.FR, Country.BE), extension.getCountries());

        LimitViolation violation = new LimitViolation("s", LimitViolationType.HIGH_VOLTAGE, 300, 1, 400);
        violation.addExtension(SubjectInfoExtension.class, extension);

        ObjectMapper mapper = JsonUtil.createObjectMapper()
                .registerModule(new SecurityAnalysisJsonModule());
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(violation);
        String jsonRef = String.join(System.lineSeparator(),
                "{",
                "  \"subjectId\" : \"s\",",
                "  \"limitType\" : \"HIGH_VOLTAGE\",",
                "  \"limit\" : 300.0,",
                "  \"limitReduction\" : 1.0,",
                "  \"value\" : 400.0,",
                "  \"extensions\" : {",
                "    \"SubjectInfo\" : {",
                "      \"countries\" : [ \"BE\", \"FR\" ],",
                "      \"nominalVoltages\" : [ 225.0, 400.0 ]",
                "    }",
                "  }",
                "}");
        assertEquals(jsonRef, json);
        LimitViolation violation2 = mapper.readValue(json, LimitViolation.class);
        assertEquals("s", violation2.getSubjectId());
        assertEquals(LimitViolationType.HIGH_VOLTAGE, violation2.getLimitType());
        assertEquals(300, violation2.getLimit(), 0);
        assertEquals(1f, violation2.getLimitReduction(), 0f);
        assertEquals(400, violation2.getValue(), 0);
        SubjectInfoExtension extension2 = violation2.getExtension(SubjectInfoExtension.class);
        assertNotNull(extension2);
        assertEquals("SubjectInfo", extension2.getName());
        assertSame(violation2, extension2.getExtendable());
        assertEquals(Sets.newHashSet(225d, 400d), extension2.getNominalVoltages());
        assertEquals(Sets.newHashSet(Country.FR, Country.BE), extension2.getCountries());
    }
}
