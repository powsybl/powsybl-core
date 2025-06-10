/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.test.ComparisonUtils;
import com.powsybl.commons.test.PowsyblCoreTestReportResourceBundle;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Optional;

import static com.powsybl.commons.test.PowsyblCoreTestReportResourceBundle.TEST_BASE_NAME;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class ReportNodeTest extends AbstractSerDeTest {

    private static final String ALL_VALUES_MESSAGE_FORMATTED = """
            Root message
            doubleUntyped: 4.3
            doubleTyped: 4.4
            floatUntyped: -1.5
            floatTyped: 0.6
            intUntyped: 4
            intTyped: -2
            longUntyped: 5
            longTyped: -3
            booleanUntyped: true
            booleanTyped: false
            stringUntyped: value
            stringTyped: filename
            severity: INFO""";

    @Test
    void testValues() throws IOException {
        ReportNode root = ReportNode.newRootReportNode()
                .withResourceBundles(TEST_BASE_NAME)
                .withMessageTemplate("rootTemplate")
                .withUntypedValue("doubleUntyped", 4.3)
                .withTypedValue("doubleTyped", 4.4, TypedValue.ACTIVE_POWER)
                .withUntypedValue("floatUntyped", -1.5f)
                .withTypedValue("floatTyped", 0.6f, TypedValue.IMPEDANCE)
                .withUntypedValue("intUntyped", 4)
                .withTypedValue("intTyped", -2, "count")
                .withUntypedValue("longUntyped", 5L)
                .withTypedValue("longTyped", -3L, "count")
                .withUntypedValue("booleanUntyped", true)
                .withTypedValue("booleanTyped", false, "protected")
                .withUntypedValue("stringUntyped", "value")
                .withTypedValue("stringTyped", "filename", TypedValue.FILENAME)
                .withSeverity(TypedValue.INFO_SEVERITY)
                .build();
        assertEquals(ALL_VALUES_MESSAGE_FORMATTED, root.getMessage());

        ReportNode child = root.newReportNode()
                .withMessageTemplate("child")
                .withSeverity("Very important custom severity")
                .add();
        assertEquals("Child message with parent value filename and own severity 'Very important custom severity'", child.getMessage());
        assertEquals(1, root.getChildren().size());

        roundTripTest(root, ReportNodeSerializer::write, ReportNodeDeserializer::read, "/testValuesReportNode.json");
    }

    @Test
    void testPostponedValues() throws IOException {
        ReportNode root = ReportNode.newRootReportNode()
                .withResourceBundles(TEST_BASE_NAME)
                .withMessageTemplate("rootTemplate")
                .build();
        ReportNode child = root.newReportNode()
                .withMessageTemplate("child")
                .withSeverity("Overridden custom severity")
                .add();
        assertEquals("Child message with parent value ${stringTyped} and own severity 'Overridden custom severity'", child.getMessage());

        // postponed values added
        root.addUntypedValue("doubleUntyped", 4.3)
                .addTypedValue("doubleTyped", 4.4, TypedValue.ACTIVE_POWER)
                .addUntypedValue("floatUntyped", -1.5f)
                .addTypedValue("floatTyped", 0.6f, TypedValue.IMPEDANCE)
                .addUntypedValue("intUntyped", 4)
                .addTypedValue("intTyped", -2, "count")
                .addUntypedValue("longUntyped", 5L)
                .addTypedValue("longTyped", -3L, "count")
                .addUntypedValue("booleanUntyped", true)
                .addTypedValue("booleanTyped", false, "protected")
                .addUntypedValue("stringUntyped", "value")
                .addTypedValue("stringTyped", "filename", TypedValue.FILENAME)
                .addSeverity(TypedValue.INFO_SEVERITY);
        assertEquals(ALL_VALUES_MESSAGE_FORMATTED, root.getMessage());

        // child reportNode also inherits the postponed added values
        assertEquals("Child message with parent value filename and own severity 'Overridden custom severity'", child.getMessage());

        // postponed overriding severity
        child.addSeverity("Very important custom severity");
        assertEquals("Child message with parent value filename and own severity 'Very important custom severity'", child.getMessage());

        Path report = tmpDir.resolve("report.json");
        ReportNodeSerializer.write(root, report);
        ComparisonUtils.assertTxtEquals(getClass().getResourceAsStream("/testValuesReportNode.json"), Files.readString(report));
    }

    @Test
    void testInclude() throws IOException {
        ReportNode root = ReportNode.newRootReportNode()
                .withResourceBundles(TEST_BASE_NAME)
                .withMessageTemplate("simpleRootTemplate")
                .build();

        ReportNode otherRoot = ReportNode.newRootReportNode()
                .withResourceBundles(TEST_BASE_NAME)
                .withMessageTemplate("includedRoot")
                .build();
        ReportNode otherRootChild = otherRoot.newReportNode()
                .withMessageTemplate("includedChild")
                .add();

        PowsyblException e1 = assertThrows(PowsyblException.class, () -> root.include(ReportNode.NO_OP));
        PowsyblException e2 = assertThrows(PowsyblException.class, () -> root.include(root));
        PowsyblException e3 = assertThrows(PowsyblException.class, () -> otherRootChild.include(otherRoot));
        assertEquals("Cannot mix implementations of ReportNode, included reportNode should be/extend ReportNodeImpl", e1.getMessage());
        assertEquals("The given reportNode cannot be included as it is the root of the reportNode", e2.getMessage());
        assertEquals("The given reportNode cannot be included as it is the root of the reportNode", e3.getMessage());

        root.include(otherRoot);
        assertEquals(1, root.getChildren().size());
        assertEquals(otherRoot, root.getChildren().get(0));
        assertEquals(root.getTreeContext(), ((ReportNodeImpl) otherRoot).getTreeContextRef().get());

        // Other root is not root anymore and can therefore not be added again
        PowsyblException e4 = assertThrows(PowsyblException.class, () -> root.include(otherRoot));
        assertEquals("Cannot include non-root reportNode", e4.getMessage());

        ReportNode child = root.newReportNode()
                .withMessageTemplate("simpleChild")
                .add();
        PowsyblException e5 = assertThrows(PowsyblException.class, () -> root.include(child));
        assertEquals("Cannot include non-root reportNode", e5.getMessage());

        ReportNode yetAnotherRoot = ReportNode.newRootReportNode()
                .withResourceBundles(TEST_BASE_NAME)
                .withMessageTemplate("newRootAboveAll")
                .build();
        yetAnotherRoot.include(root);
        assertEquals(root.getTreeContext(), ((ReportNodeImpl) yetAnotherRoot).getTreeContextRef().get());
        assertEquals(root.getTreeContext(), ((ReportNodeImpl) otherRoot).getTreeContextRef().get());

        roundTripTest(yetAnotherRoot, ReportNodeSerializer::write, ReportNodeDeserializer::read, "/testIncludeReportNode.json");
    }

    @Test
    void testAddCopy() throws IOException {
        ReportNode root = ReportNode.newRootReportNode()
                .withResourceBundles(TEST_BASE_NAME)
                .withMessageTemplate("rootWithValue")
                .withTypedValue("value", 2.3203, "ROOT_VALUE")
                .build();
        root.newReportNode()
                .withMessageTemplate("existingChild")
                .add();

        ReportNode otherRoot = ReportNode.newRootReportNode()
                .withResourceBundles(TEST_BASE_NAME)
                .withMessageTemplate("otherRoot")
                .withTypedValue("value", -915.3, "ROOT_VALUE")
                .build();
        otherRoot.newReportNode()
                .withMessageTemplate("childNotCopied")
                .add();
        ReportNode childToCopy = otherRoot.newReportNode()
                .withMessageTemplate("childToCopy")
                .add();
        childToCopy.newReportNode()
                .withMessageTemplate("grandChild")
                .add();

        root.addCopy(childToCopy);
        assertEquals(2, otherRoot.getChildren().size()); // the copied message is not removed
        assertEquals(2, root.getChildren().size());

        ReportNode childCopied = root.getChildren().get(1);
        assertEquals(childToCopy.getMessageKey(), childCopied.getMessageKey());
        assertEquals(root.getTreeContext(), ((ReportNodeImpl) childCopied).getTreeContextRef().get());

        // Two limitations of copy current implementation, due to the current ReportNode serialization
        // 1. the inherited values are not kept
        assertNotEquals(childToCopy.getMessage(), childCopied.getMessage());
        // 2. the dictionary contains all the keys from the copied reportNode tree (even the ones from non-copied reportNodes)
        assertEquals(6, root.getTreeContext().getDictionary().size());

        Path serializedReport = tmpDir.resolve("tmp.json");
        ReportNodeSerializer.write(root, serializedReport);
        ComparisonUtils.assertTxtEquals(getClass().getResourceAsStream("/testCopyReportNode.json"), Files.newInputStream(serializedReport));
    }

    @Test
    void testAddCopyCornerCases() {
        ReportNode root = ReportNode.newRootReportNode()
                .withResourceBundles(TEST_BASE_NAME)
                .withMessageTemplate("rootWithValue")
                .withTypedValue("value", 2.3203, "ROOT_VALUE")
                .build();

        // Corner case: copying oneself
        // there's no limitation on this with current implementation
        // this leads to: root
        //                 |___ root
        root.addCopy(root);

        assertEquals(root.getMessage(), root.getChildren().get(0).getMessage());

        // Corner case: copying an ancestor
        // there's also no limitation on this with current implementation
        // this leads to: root
        //                 |___ root
        //                 |___ rootChild
        //                        |___root
        //                             |___ root
        //                             |___ rootChild
        ReportNode rootChild = root.newReportNode()
                .withMessageTemplate("rootChild")
                .add();
        rootChild.addCopy(root);

        ReportNode rootGrandChild = rootChild.getChildren().get(0);
        ReportNode rootGreatGrandChild1 = rootGrandChild.getChildren().get(0);
        ReportNode rootGreatGrandChild2 = rootGrandChild.getChildren().get(1);
        assertEquals(root.getMessage(), rootGrandChild.getMessage());
        assertEquals(root.getMessage(), rootGreatGrandChild1.getMessage());
        assertEquals(rootChild.getMessage(), rootGreatGrandChild2.getMessage());
    }

    @Test
    void testDictionaryEnd() {
        ReportNode report = ReportNodeDeserializer.read(getClass().getResourceAsStream("/testDictionaryEnd.json"));
        assertEquals("Root message", report.getMessage());
        assertEquals("Child message", report.getChildren().get(0).getMessage());
    }

    @Test
    void testTimestamps() {
        DateTimeFormatter defaultDateTimeFormatter = DateTimeFormatter.ofPattern(
                ReportConstants.DEFAULT_TIMESTAMP_PATTERN, ReportConstants.DEFAULT_LOCALE);

        ReportNode root1 = ReportNode.newRootReportNode()
                .withResourceBundles(TEST_BASE_NAME)
                .withMessageTemplate("rootTemplate")
                .build();
        assertHasNoTimeStamp(root1);

        // No locale set and no timestamp pattern set
        ReportNode child1 = root1.newReportNode()
                .withMessageTemplate("child")
                .withTimestamp()
                .add();
        assertHasTimeStamp(child1, defaultDateTimeFormatter);

        Path report = tmpDir.resolve("report");
        ReportNodeSerializer.write(root1, report);
        ReportNode rootRead = ReportNodeDeserializer.read(report);
        assertHasNoTimeStamp(rootRead);
        assertHasTimeStamp(rootRead.getChildren().get(0), defaultDateTimeFormatter);

        // Default timestamp pattern set but no locale set
        String customPattern1 = "dd MMMM yyyy HH:mm:ss XXX";
        DateTimeFormatter customPatternFormatter = DateTimeFormatter.ofPattern(customPattern1, ReportConstants.DEFAULT_LOCALE);
        ReportNode root2 = ReportNode.newRootReportNode()
                .withResourceBundles(TEST_BASE_NAME)
                .withDefaultTimestampPattern(customPattern1)
                .withTimestamp()
                .withMessageTemplate("rootTemplate")
                .build();
        assertHasTimeStamp(root2, customPatternFormatter);

        // Child does not inherit timestamp enabled (but still contains the value as inherited!)
        ReportNode child2 = root2.newReportNode()
                .withMessageTemplate("child")
                .add();
        assertHasNoTimeStamp(child2);

        // Both default timestamp pattern and locale set
        Locale customLocale = Locale.ITALIAN;
        DateTimeFormatter customPatternAndLocaleFormatter1 = DateTimeFormatter.ofPattern(customPattern1, customLocale);
        ReportNode root3 = ReportNode.newRootReportNode()
                .withResourceBundles(TEST_BASE_NAME)
                .withLocale(customLocale)
                .withDefaultTimestampPattern(customPattern1)
                .withTimestamp()
                .withMessageTemplate("rootTemplate")
                .build();
        assertHasTimeStamp(root3, customPatternAndLocaleFormatter1);

        // Child inherits timestamp pattern and locale
        ReportNode child3 = root3.newReportNode()
                .withMessageTemplate("simpleChild")
                .withTimestamp()
                .add();
        assertHasTimeStamp(child3, customPatternAndLocaleFormatter1);

        // Child might override timestamp pattern
        String customPattern2 = "eeee dd MMMM yyyy HH:mm:ss XXX";
        DateTimeFormatter customPatternAndLocaleFormatter2 = DateTimeFormatter.ofPattern(customPattern2, customLocale);
        ReportNode child4 = root3.newReportNode()
                .withMessageTemplate("simpleChild")
                .withTimestamp(customPattern2)
                .add();
        assertHasTimeStamp(child4, customPatternAndLocaleFormatter2);

        // with Locale set but no timestamp pattern
        DateTimeFormatter noPatternAndLocaleFormatter = DateTimeFormatter.ofPattern(ReportConstants.DEFAULT_TIMESTAMP_PATTERN, customLocale);
        ReportNode root4 = ReportNode.newRootReportNode()
                .withResourceBundles(TEST_BASE_NAME)
                .withLocale(customLocale)
                .withMessageTemplate("simpleRootTemplate")
                .withTimestamp()
                .build();
        assertHasTimeStamp(root4, noPatternAndLocaleFormatter);
    }

    @Test
    void testMissingKey() {
        // Without giving a locale
        ReportNode report1 = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblCoreTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("unknown.key")
                .build();
        // translation should fall back to default properties as the key is not defined in the reports_en_US.properties
        assertEquals("Cannot find message template with key: 'unknown.key'", report1.getMessage());

        // With Locale.FRENCH
        ReportNode report2 = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblCoreTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withLocale(Locale.FRENCH)
                .withMessageTemplate("unknown.key")
                .build();
        // translation should fall back to default properties as the key is not defined in the reports_en_US.properties
        assertEquals("Template de message non trouvé pour la clé 'unknown.key'", report2.getMessage());
    }

    @Test
    void testLocaleAndi18n() {
        // Without giving a locale => default one is en_US
        ReportNode rootReportEnglish = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblCoreTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplateProvider(new BundleMessageTemplateProvider(TEST_BASE_NAME))
                .withMessageTemplate("rootWithValue")
                .withUntypedValue("value", 4)
                .build();
        // translation should fall back to default properties as the key is not defined in the reports_en_US.properties
        assertEquals("Root message with value 4", rootReportEnglish.getMessage());
        assertEquals(Locale.US, rootReportEnglish.getTreeContext().getLocale());

        // With french locale
        ReportNode rootReportFrench = ReportNode.newRootReportNode()
                .withResourceBundles(TEST_BASE_NAME)
                .withLocale(Locale.FRENCH)
                .withMessageTemplate("rootWithValue")
                .withUntypedValue("value", 4)
                .build();
        // translation should be from the reports_fr.properties file
        assertEquals("Message racine avec la valeur 4", rootReportFrench.getMessage());
        assertEquals(Locale.FRENCH, rootReportFrench.getTreeContext().getLocale());

        // Test giving the specific France locale
        ReportNode rootReportFrance = ReportNode.newRootReportNode()
                .withResourceBundles(TEST_BASE_NAME)
                .withLocale(Locale.FRANCE)
                .withMessageTemplate("rootWithValue")
                .withUntypedValue("value", 4)
                .build();
        // translation should be from the reports_fr.properties file as the key is not defined in the reports_fr_FR.properties
        assertEquals("Message racine avec la valeur 4", rootReportFrance.getMessage());
        assertEquals(Locale.FRANCE, rootReportFrance.getTreeContext().getLocale());
    }

    @Test
    void testAllBundlesFromClasspath() {
        ReportNode root = ReportNode.newRootReportNode()
                .withAllResourceBundlesFromClasspath()
                .withMessageTemplate("core.iidm.modification.voltageLevelRemoved")
                .withTypedValue("vlId", "vl1", TypedValue.ID)
                .build();
        assertEquals("Voltage level vl1 removed", root.getMessage());

        ReportNode child = root.newReportNode()
                .withMessageTemplate("simpleChild")
                .add();
        assertEquals("Child message", child.getMessage());
    }

    @Test
    void testMissingBundleName() {
        ReportNodeBuilder reportNodeBuilder = ReportNode.newRootReportNode();
        PowsyblException e = assertThrows(PowsyblException.class, reportNodeBuilder::withResourceBundles);
        assertEquals("bundleBaseNames must not be empty", e.getMessage());
    }

    private static void assertHasNoTimeStamp(ReportNode root1) {
        assertFalse(root1.getValues().containsKey(ReportConstants.TIMESTAMP_KEY));
    }

    private static void assertHasTimeStamp(ReportNode root, DateTimeFormatter dateTimeFormatter) {
        Optional<TypedValue> timestamp = root.getValue(ReportConstants.TIMESTAMP_KEY);
        assertTrue(timestamp.isPresent());
        assertInstanceOf(String.class, timestamp.get().getValue());
        try {
            ZonedDateTime.parse((String) timestamp.get().getValue(), dateTimeFormatter);
        } catch (DateTimeParseException e) {
            fail();
        }
    }
}
