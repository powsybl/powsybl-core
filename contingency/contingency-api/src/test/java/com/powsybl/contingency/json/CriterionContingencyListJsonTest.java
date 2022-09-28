/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.CriterionContingencyList;
import com.powsybl.contingency.contingency.list.criterion.CountryCriterion;
import com.powsybl.contingency.contingency.list.criterion.Criterion;
import com.powsybl.contingency.contingency.list.criterion.NominalVoltageCriterion;
import com.powsybl.contingency.contingency.list.criterion.PropertyCriterion;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class CriterionContingencyListJsonTest extends AbstractConverterTest {

    private static CriterionContingencyList create() {
        List<Criterion> criteria = new ArrayList<>();
        criteria.add(new CountryCriterion(null, "FR", "BE"));
        criteria.add(new NominalVoltageCriterion(null,
                new NominalVoltageCriterion.VoltageInterval(200.0, 230.0, true, true),
                new NominalVoltageCriterion.VoltageInterval(380.0, 420.0, true, true),
                null));
        criteria.add(new PropertyCriterion("property", "val1"));
        return new CriterionContingencyList("list1", "LINE", criteria);
    }

    @Test
    public void roundTripTest() throws IOException {
        roundTripTest(create(), CriterionContingencyListJsonTest::write, CriterionContingencyListJsonTest::readContingencyList,
                "/criterionContingencyList.json");
    }

    private static CriterionContingencyList readContingencyList(Path jsonFile) {
        return read(jsonFile, CriterionContingencyList.class);
    }

    private static <T> T read(Path jsonFile, Class<T> clazz) {
        Objects.requireNonNull(jsonFile);

        try (InputStream is = Files.newInputStream(jsonFile)) {
            ObjectMapper objectMapper = JsonUtil.createObjectMapper();
            ContingencyJsonModule module = new ContingencyJsonModule();
            objectMapper.registerModule(module);

            return (T) objectMapper.readValue(is, clazz);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static <T> void write(T object, Path jsonFile) {
        Objects.requireNonNull(object);
        Objects.requireNonNull(jsonFile);

        try (OutputStream os = Files.newOutputStream(jsonFile)) {
            ObjectMapper mapper = JsonUtil.createObjectMapper();
            ContingencyJsonModule module = new ContingencyJsonModule();
            mapper.registerModule(module);

            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
            writer.writeValue(os, object);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
