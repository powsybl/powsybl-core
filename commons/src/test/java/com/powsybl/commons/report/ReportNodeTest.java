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
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Optional;

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
                .withLocaleMessageTemplate("rootTemplate", ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
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
                .withLocaleMessageTemplate("child", ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
                .withSeverity("Very important custom severity")
                .add();
        assertEquals("Child message with parent value filename and own severity 'Very important custom severity'", child.getMessage());
        assertEquals(1, root.getChildren().size());

        roundTripTest(root, ReportNodeSerializer::write, ReportNodeDeserializer::read, "/testValuesReportNode.json");
    }

    @Test
    void testPostponedValues() throws IOException {
        ReportNode root = ReportNode.newRootReportNode()
                .withLocaleMessageTemplate("rootTemplate", ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
                .build();
        ReportNode child = root.newReportNode()
                .withLocaleMessageTemplate("child", ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
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
                .withLocaleMessageTemplate("simpleRootTemplate", ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
                .build();

        ReportNode otherRoot = ReportNode.newRootReportNode()
                .withLocaleMessageTemplate("includedRoot", ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
                .build();
        ReportNode otherRootChild = otherRoot.newReportNode()
                .withLocaleMessageTemplate("includedChild", ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
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
        assertEquals(((ReportNodeImpl) root).getTreeContext(), ((ReportNodeImpl) otherRoot).getTreeContextRef().get());

        // Other root is not root anymore and can therefore not be added again
        PowsyblException e4 = assertThrows(PowsyblException.class, () -> root.include(otherRoot));
        assertEquals("Cannot include non-root reportNode", e4.getMessage());

        ReportNode child = root.newReportNode()
                .withLocaleMessageTemplate("simpleChild", ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
                .add();
        PowsyblException e5 = assertThrows(PowsyblException.class, () -> root.include(child));
        assertEquals("Cannot include non-root reportNode", e5.getMessage());

        ReportNode yetAnotherRoot = ReportNode.newRootReportNode()
                .withLocaleMessageTemplate("newRootAboveAll", ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
                .build();
        yetAnotherRoot.include(root);
        assertEquals(((ReportNodeImpl) root).getTreeContext(), ((ReportNodeImpl) yetAnotherRoot).getTreeContextRef().get());
        assertEquals(((ReportNodeImpl) root).getTreeContext(), ((ReportNodeImpl) otherRoot).getTreeContextRef().get());

        roundTripTest(yetAnotherRoot, ReportNodeSerializer::write, ReportNodeDeserializer::read, "/testIncludeReportNode.json");
    }

    @Test
    void testAddCopy() throws IOException {
        ReportNode root = ReportNode.newRootReportNode()
                .withLocaleMessageTemplate("rootWithValue", ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
                .withTypedValue("value", 2.3203, "ROOT_VALUE")
                .build();
        root.newReportNode()
                .withLocaleMessageTemplate("existingChild", ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
                .add();

        ReportNode otherRoot = ReportNode.newRootReportNode()
                .withLocaleMessageTemplate("otherRoot", ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
                .withTypedValue("value", -915.3, "ROOT_VALUE")
                .build();
        otherRoot.newReportNode()
                .withLocaleMessageTemplate("childNotCopied", ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
                .add();
        ReportNode childToCopy = otherRoot.newReportNode()
                .withLocaleMessageTemplate("childToCopy", ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
                .add();
        childToCopy.newReportNode()
                .withLocaleMessageTemplate("grandChild", ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
                .add();

        root.addCopy(childToCopy);
        assertEquals(2, otherRoot.getChildren().size()); // the copied message is not removed
        assertEquals(2, root.getChildren().size());

        ReportNode childCopied = root.getChildren().get(1);
        assertEquals(childToCopy.getMessageKey(), childCopied.getMessageKey());
        assertEquals(((ReportNodeImpl) root).getTreeContext(), ((ReportNodeImpl) childCopied).getTreeContextRef().get());

        // Two limitations of copy current implementation, due to the current ReportNode serialization
        // 1. the inherited values are not kept
        assertNotEquals(childToCopy.getMessage(), childCopied.getMessage());
        // 2. the dictionary contains all the keys from the copied reportNode tree (even the ones from non-copied reportNodes)
        assertEquals(6, ((ReportNodeImpl) root).getTreeContext().getDictionary().size());

        Path serializedReport = tmpDir.resolve("tmp.json");
        ReportNodeSerializer.write(root, serializedReport);
        ComparisonUtils.assertTxtEquals(getClass().getResourceAsStream("/testCopyReportNode.json"), Files.newInputStream(serializedReport));
    }

    @Test
    void testAddCopyCornerCases() {
        ReportNode root = ReportNode.newRootReportNode()
                .withLocaleMessageTemplate("rootWithValue", ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
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
                .withLocaleMessageTemplate("rootChild", ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
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
                .withLocaleMessageTemplate("rootTemplate", ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
                .build();
        assertHasNoTimeStamp(root1);

        // No locale set and no timestamp pattern set
        ReportNode child1 = root1.newReportNode()
                .withLocaleMessageTemplate("child", ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
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
                .withDefaultTimestampPattern(customPattern1)
                .withTimestamp()
                .withLocaleMessageTemplate("rootTemplate", ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
                .build();
        assertHasTimeStamp(root2, customPatternFormatter);

        // Child does not inherit timestamp enabled (but still contains the value as inherited!)
        ReportNode child2 = root2.newReportNode()
                .withMessageTemplate("child", "Child message")
                .add();
        assertHasNoTimeStamp(child2);

        // Both default timestamp pattern and locale set
        Locale customLocale = Locale.ITALIAN;
        DateTimeFormatter customPatternAndLocaleFormatter1 = DateTimeFormatter.ofPattern(customPattern1, customLocale);
        ReportNode root3 = ReportNode.newRootReportNode()
                .withDefaultTimestampPattern(customPattern1)
                .withLocale(customLocale)
                .withTimestamp()
                .withLocaleMessageTemplate("rootTemplate", ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
                .build();
        assertHasTimeStamp(root3, customPatternAndLocaleFormatter1);

        // Child inherits timestamp pattern and locale
        ReportNode child3 = root3.newReportNode()
                .withMessageTemplate("child", "Child message")
                .withTimestamp()
                .add();
        assertHasTimeStamp(child3, customPatternAndLocaleFormatter1);

        // Child might override timestamp pattern
        String customPattern2 = "eeee dd MMMM yyyy HH:mm:ss XXX";
        DateTimeFormatter customPatternAndLocaleFormatter2 = DateTimeFormatter.ofPattern(customPattern2, customLocale);
        ReportNode child4 = root3.newReportNode()
                .withMessageTemplate("child", "Child message")
                .withTimestamp(customPattern2)
                .add();
        assertHasTimeStamp(child4, customPatternAndLocaleFormatter2);

        // with Locale set but no timestamp pattern
        DateTimeFormatter noPatternAndLocaleFormatter = DateTimeFormatter.ofPattern(ReportConstants.DEFAULT_TIMESTAMP_PATTERN, customLocale);
        ReportNode root4 = ReportNode.newRootReportNode()
                .withLocale(customLocale)
                .withMessageTemplate("rootTemplate", "Root message")
                .withTimestamp()
                .build();
        assertHasTimeStamp(root4, noPatternAndLocaleFormatter);
    }

    @Test
    void testLocaleAndi18n() {
        final String bundleName = "i18n.reports";

        // Without giving a locale => default one is en_US
        ReportNode rootReportEnglish = ReportNode.newRootReportNode()
                .withLocaleMessageTemplate("rootWithValue", bundleName)
                .withUntypedValue("value", 4)
                .build();
        // translation should fall back to default properties as the key is not defined in the reports_en_US.properties
        assertEquals("Root message with value 4", rootReportEnglish.getMessage());
        assertEquals(Locale.US, rootReportEnglish.getTreeContext().getLocale());

        // With french locale
        ReportNode rootReportFrench = ReportNode.newRootReportNode()
                .withLocale(Locale.FRENCH)
                .withLocaleMessageTemplate("rootWithValue", bundleName)
                .withUntypedValue("value", 4)
                .build();
        // translation should be from the reports_fr.properties file
        assertEquals("Message racine avec la valeur 4", rootReportFrench.getMessage());
        assertEquals(Locale.FRENCH, rootReportFrench.getTreeContext().getLocale());

        // Test giving the specific France locale
        ReportNode rootReportFrance = ReportNode.newRootReportNode()
                .withLocale(Locale.FRANCE)
                .withLocaleMessageTemplate("rootWithValue", bundleName)
                .withUntypedValue("value", 4)
                .build();
        // translation should be from the reports_fr.properties file as the key is not defined in the reports_fr_FR.properties
        assertEquals("Message racine avec la valeur 4", rootReportFrance.getMessage());
        assertEquals(Locale.FRANCE, rootReportFrance.getTreeContext().getLocale());
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
