/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.rulesdb.fs;

import com.google.common.collect.Sets;
import eu.itesla_project.modules.rules.RuleAttributeSet;
import eu.itesla_project.modules.rules.RuleId;
import eu.itesla_project.modules.rules.SecurityRule;
import eu.itesla_project.simulation.securityindexes.SecurityIndexId;
import eu.itesla_project.simulation.securityindexes.SecurityIndexType;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.nio.file.ShrinkWrapFileSystems;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalFileSystemRulesDbClientTest {

    private FileSystem fileSystem;
    private Path dbDir;
    private LocalFileSystemRulesDbClient rulesDbClient;

    @Before
    public void setUp() throws Exception {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        fileSystem = ShrinkWrapFileSystems.newFileSystem(archive);
        dbDir = fileSystem.getPath("db");
        SecurityRuleSerializerLoaderMock loader = new SecurityRuleSerializerLoaderMock();
        loader.addSerializer(new SecurityRuleSerializerMock());
        rulesDbClient = new LocalFileSystemRulesDbClient(dbDir, loader);
    }

    @After
    public void tearDown() throws Exception {
        rulesDbClient.close();
        fileSystem.close();
    }

    @Test
    public void listWorkflowsTest() {
        Assert.assertTrue(rulesDbClient.listWorkflows().isEmpty());
        rulesDbClient.updateRule(new SecurityRuleMock(new RuleId(RuleAttributeSet.MONTE_CARLO, new SecurityIndexId("fault1", SecurityIndexType.TSO_OVERLOAD)), "workflow-0"));
        Assert.assertEquals(rulesDbClient.listWorkflows(), Arrays.asList("workflow-0"));
        rulesDbClient.updateRule(new SecurityRuleMock(new RuleId(RuleAttributeSet.MONTE_CARLO, new SecurityIndexId("fault1", SecurityIndexType.TSO_OVERLOAD)), "workflow-1"));
        Assert.assertEquals(rulesDbClient.listWorkflows(), Arrays.asList("workflow-0", "workflow-1"));
    }

    @Test
    public void listRulesTest() {
        Assert.assertTrue(rulesDbClient.listWorkflows().isEmpty());
        Assert.assertTrue(rulesDbClient.listRules("workflow-0", RuleAttributeSet.MONTE_CARLO).isEmpty());
        RuleId ruleId1 = new RuleId(RuleAttributeSet.MONTE_CARLO, new SecurityIndexId("fault1", SecurityIndexType.TSO_OVERLOAD));
        RuleId ruleId2 = new RuleId(RuleAttributeSet.MONTE_CARLO, new SecurityIndexId("fault2", SecurityIndexType.TSO_OVERLOAD));
        rulesDbClient.updateRule(new SecurityRuleMock(ruleId1, "workflow-0"));
        rulesDbClient.updateRule(new SecurityRuleMock(ruleId2, "workflow-0"));
        Assert.assertEquals(rulesDbClient.listRules("workflow-0", RuleAttributeSet.MONTE_CARLO), Arrays.asList(ruleId1, ruleId2));
    }

    @Test
    public void getRulesTest() {
        RuleId ruleId1 = new RuleId(RuleAttributeSet.MONTE_CARLO, new SecurityIndexId("fault1", SecurityIndexType.TSO_OVERLOAD));
        SecurityRuleMock rule1 = new SecurityRuleMock(ruleId1, "workflow-0");
        rulesDbClient.updateRule(rule1);
        List<SecurityRule> rules1Back = rulesDbClient.getRules("workflow-0", RuleAttributeSet.MONTE_CARLO, "fault1", SecurityIndexType.TSO_OVERLOAD);
        Assert.assertTrue(rules1Back.size() == 1);
        Assert.assertTrue(rules1Back.get(0).getId().equals(ruleId1));
        Assert.assertTrue(rules1Back.get(0).getWorkflowId().equals("workflow-0"));
        List<SecurityRule> rules2Back = rulesDbClient.getRules("workflow-0", RuleAttributeSet.WORST_CASE, "fault1", SecurityIndexType.TSO_OVERLOAD);
        Assert.assertTrue(rules2Back.isEmpty());
    }

    @Test
    public void getRulesWithWildCardTest() {
        SecurityRuleMock rule1 = new SecurityRuleMock(new RuleId(RuleAttributeSet.MONTE_CARLO, new SecurityIndexId("fault1", SecurityIndexType.TSO_OVERLOAD)), "workflow-0");
        SecurityRuleMock rule2 = new SecurityRuleMock(new RuleId(RuleAttributeSet.MONTE_CARLO, new SecurityIndexId("fault1", SecurityIndexType.SMALLSIGNAL)), "workflow-0");
        SecurityRuleMock rule3 = new SecurityRuleMock(new RuleId(RuleAttributeSet.WORST_CASE, new SecurityIndexId("fault1", SecurityIndexType.TSO_OVERLOAD)), "workflow-0");
        SecurityRuleMock rule4 = new SecurityRuleMock(new RuleId(RuleAttributeSet.WORST_CASE, new SecurityIndexId("fault1", SecurityIndexType.SMALLSIGNAL)), "workflow-0");
        rulesDbClient.updateRule(rule1);
        rulesDbClient.updateRule(rule2);
        rulesDbClient.updateRule(rule3);
        rulesDbClient.updateRule(rule4);
        Assert.assertEquals(Sets.newHashSet(rule1, rule3), new HashSet<>(rulesDbClient.getRules("workflow-0", null, "fault1", SecurityIndexType.TSO_OVERLOAD)));
        Assert.assertEquals(Sets.newHashSet(rule1, rule2), new HashSet<>(rulesDbClient.getRules("workflow-0", RuleAttributeSet.MONTE_CARLO, "fault1", null)));
        try {
            rulesDbClient.getRules("workflow-0", RuleAttributeSet.MONTE_CARLO, null, SecurityIndexType.TSO_OVERLOAD);
            Assert.fail();
        } catch (Exception e) {
        }
        try {
            rulesDbClient.getRules(null, RuleAttributeSet.MONTE_CARLO, "fault1", SecurityIndexType.TSO_OVERLOAD);
            Assert.fail();
        } catch (Exception e) {
        }
    }
}