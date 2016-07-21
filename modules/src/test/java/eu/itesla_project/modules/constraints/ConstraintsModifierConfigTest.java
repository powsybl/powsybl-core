/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.constraints;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.FileSystem;
import java.util.Arrays;
import java.util.List;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.nio.file.ShrinkWrapFileSystems;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.itesla_project.commons.io.InMemoryPlatformConfig;
import eu.itesla_project.commons.io.MapModuleConfig;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.security.LimitViolation;
import eu.itesla_project.modules.security.LimitViolationType;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class ConstraintsModifierConfigTest {

    private FileSystem fileSystem;
    private InMemoryPlatformConfig platformConfig;
    private Network network;
    private List<LimitViolation> violations;

    @Before
    public void setUp() throws Exception {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        fileSystem = ShrinkWrapFileSystems.newFileSystem(archive);
        platformConfig = new InMemoryPlatformConfig(fileSystem);
        network = ConstraintsModifierTestUtils.getNetwork();
        violations = ConstraintsModifierTestUtils.getViolations(network);
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    public void testNoConfig() throws Exception {
        ConstraintsModifierConfig config = ConstraintsModifierConfig.load(platformConfig);
        checkValues(config, ConstraintsModifierConfig.DEFAULT_COUNTRY, ConstraintsModifierConfig.DEFAULT_VIOLATION_TYPES);
    }

    @Test
    public void testLoadConfig() throws Exception {
        Country country = Country.FR;
        LimitViolationType violationType = LimitViolationType.CURRENT;
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("constraintsModifier");
        moduleConfig.setStringListProperty("country", Arrays.asList(country.name()));
        moduleConfig.setStringListProperty("violationsTypes", Arrays.asList(violationType.name()));
        ConstraintsModifierConfig config = ConstraintsModifierConfig.load(platformConfig);
        checkValues(config, country, Arrays.asList(violationType));
    }

    private void checkValues(ConstraintsModifierConfig config, Country expectedCountry, List<LimitViolationType> expectedViolationTypes) {
        assertEquals(expectedCountry, config.getCountry());
        assertArrayEquals(expectedViolationTypes.toArray(), config.getViolationsTypes().toArray());
        for(LimitViolation violation : violations) {
            assertTrue(config.isInAreaOfInterest(violation, network));
        }
    }

}
