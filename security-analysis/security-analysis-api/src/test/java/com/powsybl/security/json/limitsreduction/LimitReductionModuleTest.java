/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json.limitsreduction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.LimitType;
import com.powsybl.iidm.network.util.criterion.*;
import com.powsybl.security.limitsreduction.LimitReductionDefinitionList;
import com.powsybl.security.limitsreduction.LimitReductionDefinitionList.LimitReductionDefinition;
import com.powsybl.security.limitsreduction.criterion.duration.AllTemporaryDurationCriterion;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class LimitReductionModuleTest extends AbstractSerDeTest {

    private static final ObjectMapper MAPPER = JsonUtil.createObjectMapper().registerModule(new LimitReductionModule());
    private static final ObjectWriter WRITER = MAPPER.writerWithDefaultPrettyPrinter();

    @Test
    void roundTripTest() throws IOException {
        LimitReductionDefinition definition1 = new LimitReductionDefinition(LimitType.CURRENT)
                .setLimitReduction(0.9)
                .setNetworkElementCriteria(new NetworkElementIdListCriterion(Set.of("NHV1_NHV2_1")),
                        new LineCriterion().setSingleNominalVoltageCriterion(new SingleNominalVoltageCriterion(
                                new SingleNominalVoltageCriterion.VoltageInterval(350., 410., true, false))),
                        new TwoWindingsTransformerCriterion().setSingleCountryCriterion(new SingleCountryCriterion(List.of(Country.FR, Country.BE))),
                        new ThreeWindingsTransformerCriterion().setSingleCountryCriterion(new SingleCountryCriterion(List.of(Country.FR, Country.BE))))
                .setContingencyContexts(ContingencyContext.specificContingency("contingency1"), ContingencyContext.none());
                //.setDurationCriteria(new PermanentDurationCriterion());
        LimitReductionDefinition definition2 = new LimitReductionDefinition(LimitType.APPARENT_POWER)
                .setLimitReduction(0.5)
                .setNetworkElementCriteria(new NetworkElementIdListCriterion(Set.of("NHV1_NHV2_2")))
                .setDurationCriteria(new AllTemporaryDurationCriterion());
        LimitReductionDefinitionList definitionList = new LimitReductionDefinitionList()
                .setLimitReductionDefinitions(List.of(definition1));
                //.setLimitReductionDefinitions(List.of(definition1, definition2));

        roundTripTest(definitionList, LimitReductionModuleTest::write,
                LimitReductionModuleTest::read,
                "/LimitReductions.json");
    }

    private static <T> void write(T object, Path jsonFile) {
        Objects.requireNonNull(object);
        Objects.requireNonNull(jsonFile);

        try (OutputStream os = Files.newOutputStream(jsonFile)) {
            WRITER.writeValue(os, object);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static LimitReductionDefinitionList read(Path jsonFile) {
        return read(jsonFile, LimitReductionDefinitionList.class);
    }

    private static <T> T read(Path jsonFile, Class<T> clazz) {
        try (InputStream is = Files.newInputStream(jsonFile)) {
            return MAPPER.readValue(is, clazz);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
