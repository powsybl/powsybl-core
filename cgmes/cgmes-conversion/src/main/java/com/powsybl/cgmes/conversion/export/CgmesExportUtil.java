/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.export.elements.RegulatingControlEq;
import com.powsybl.cgmes.conversion.naming.CgmesObjectReference;
import com.powsybl.cgmes.conversion.naming.CgmesObjectReference.Part;
import com.powsybl.cgmes.extensions.CgmesTapChanger;
import com.powsybl.cgmes.extensions.CgmesTapChangers;
import com.powsybl.cgmes.extensions.CgmesTapChangersAdder;
import com.powsybl.cgmes.extensions.CgmesTopologyKind;
import com.powsybl.cgmes.model.CgmesMetadataModel;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.TypedValue;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.LoadDetail;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.ref;
import static com.powsybl.cgmes.conversion.naming.CgmesObjectReference.refTyped;
import static com.powsybl.cgmes.model.CgmesNames.*;
import static com.powsybl.cgmes.model.CgmesNamespace.MD_NAMESPACE;
import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public final class CgmesExportUtil {

    private CgmesExportUtil() {
    }

    public static final String REPORT_NODE_KEY_EXPORTED_CGMES_ID = "ExportedCgmesId";
    public static final String REPORT_VALUE_EXPORTED_CGMES_ID = "cgmesId";
    public static final String REPORT_VALUE_EXPORTED_CGMES_SUBSET = "cgmesSubset";
    public static final String REPORT_VALUE_EXPORTED_CGMES_NETWORK_ID = "networkId";

    // Avoid trailing zeros and format always using US locale

    private static final DecimalFormatSymbols DOUBLE_FORMAT_SYMBOLS = new DecimalFormatSymbols(Locale.US);
    private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("0.##############", DOUBLE_FORMAT_SYMBOLS);
    private static final DecimalFormat SCIENTIFIC_FORMAT = new DecimalFormat("0.######E0", DOUBLE_FORMAT_SYMBOLS);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyy-MM-dd'T'HH:mm:ssXXX").withZone(ZoneOffset.UTC);

    private static final Pattern CIM_MRID_PATTERN = Pattern.compile("(?i)_?[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}");
    private static final Pattern URN_UUID_PATTERN = Pattern.compile("(?i)urn:uuid:[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}");
    private static final Pattern ENTSOE_BD_EXCEPTIONS_PATTERN1 = Pattern.compile("(?i)[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{7}");
    private static final Pattern ENTSOE_BD_EXCEPTIONS_PATTERN2 = Pattern.compile("(?i)[a-f\\d]{8}[a-f\\d]{4}[a-f\\d]{4}[a-f\\d]{4}[a-f\\d]{12}");

    private static double fixValue(double value, double defaultValue) {
        return Double.isNaN(value) ? defaultValue : value;
    }

    public static String format(double value) {
        return format(value, 0.0); // disconnected equipment in general, a bit dangerous.
    }

    public static String format(double value, double defaultValue) {
        // Always use scientific format for extreme values
        if (value >= Float.MAX_VALUE || value <= -Float.MAX_VALUE) {
            // CIMXML expects xsd:float values
            float value1 = value >= Float.MAX_VALUE ? Float.MAX_VALUE : -Float.MAX_VALUE;
            return scientificFormat(value1, defaultValue);
        }
        return DOUBLE_FORMAT.format(fixValue(value, defaultValue));
    }

    public static String scientificFormat(double value) {
        return scientificFormat(value, 0.0); // disconnected equipment in general, a bit dangerous.
    }

    private static String scientificFormat(double value, double defaultValue) {
        return SCIENTIFIC_FORMAT.format(fixValue(value, defaultValue));
    }

    public static String format(int value) {
        return String.valueOf(value);
    }

    public static String format(boolean value) {
        return String.valueOf(value);
    }

    public static boolean isValidCimMasterRID(String id) {
        return CIM_MRID_PATTERN.matcher(id).matches()
                || URN_UUID_PATTERN.matcher(id).matches()
                || ENTSOE_BD_EXCEPTIONS_PATTERN1.matcher(id).matches()
                || ENTSOE_BD_EXCEPTIONS_PATTERN2.matcher(id).matches();
    }

    public static String getUniqueRandomId() {
        return UUID.randomUUID().toString();
    }

    public static void writeRdfRoot(String cimNamespace, String euPrefix, String euNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.setPrefix(euPrefix, euNamespace);
        writer.setPrefix("rdf", RDF_NAMESPACE);
        writer.setPrefix("cim", cimNamespace);
        writer.setPrefix("md", MD_NAMESPACE);
        writer.writeStartElement(RDF_NAMESPACE, "RDF");
        writer.writeNamespace(euPrefix, euNamespace);
        writer.writeNamespace("rdf", RDF_NAMESPACE);
        writer.writeNamespace("cim", cimNamespace);
        writer.writeNamespace("md", MD_NAMESPACE);
    }

    public static void initializeModelId(Network network, CgmesMetadataModel model, CgmesExportContext context) {
        // The ref to build a unique model id must contain:
        // the network, the subset (EQ, SSH, SV, ...), the time of the scenario, the version, the business process and the FULL_MODEL part
        // If we use name-based UUIDs this ensures that the UUID for the model will be specific enough
        CgmesObjectReference[] modelRef = {
            refTyped(network),
            ref(model.getSubset()),
            ref(DATE_TIME_FORMATTER.format(context.getScenarioTime())),
            ref(String.valueOf(model.getVersion())),
            ref(context.getBusinessProcess()),
            Part.FULL_MODEL};
        String modelId = "urn:uuid:" + context.getNamingStrategy().getCgmesId(modelRef);
        model.setId(modelId);
    }

    public static void writeModelDescription(Network network, CgmesSubset subset, XMLStreamWriter writer, CgmesMetadataModel modelDescription, CgmesExportContext context) throws XMLStreamException {
        if (modelDescription.getId() == null || modelDescription.getId().isEmpty()) {
            initializeModelId(network, modelDescription, context);
        }
        writer.writeStartElement(MD_NAMESPACE, "FullModel");
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ABOUT, modelDescription.getId());
        // Report the exported CGMES model identifiers
        context.getReportNode().newReportNode()
                .withMessageTemplate(REPORT_NODE_KEY_EXPORTED_CGMES_ID, "CGMES exported model identifier: ${cgmesId} for subset ${cgmesSubset} of network ${networkId}")
                .withTypedValue(REPORT_VALUE_EXPORTED_CGMES_ID, modelDescription.getId(), TypedValue.URN_UUID)
                .withTypedValue(REPORT_VALUE_EXPORTED_CGMES_SUBSET, subset.getIdentifier(), TypedValue.CGMES_SUBSET)
                .withTypedValue(REPORT_VALUE_EXPORTED_CGMES_NETWORK_ID, network.getId(), TypedValue.ID)
                .add();
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.SCENARIO_TIME);
        writer.writeCharacters(DATE_TIME_FORMATTER.format(context.getScenarioTime()));
        writer.writeEndElement();
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.CREATED);
        writer.writeCharacters(DATE_TIME_FORMATTER.format(ZonedDateTime.now()));
        writer.writeEndElement();
        if (modelDescription.getDescription() != null) {
            writer.writeStartElement(MD_NAMESPACE, CgmesNames.DESCRIPTION);
            writer.writeCharacters(modelDescription.getDescription());
            writer.writeEndElement();
        }
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.VERSION);
        writer.writeCharacters(format(modelDescription.getVersion()));
        writer.writeEndElement();
        for (String dependentOn : modelDescription.getDependentOn()) {
            writer.writeEmptyElement(MD_NAMESPACE, CgmesNames.DEPENDENT_ON);
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, dependentOn);
        }
        for (String supersedes : modelDescription.getSupersedes()) {
            writer.writeEmptyElement(MD_NAMESPACE, CgmesNames.SUPERSEDES);
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, supersedes);
        }
        for (String profile : modelDescription.getProfiles()) {
            writer.writeStartElement(MD_NAMESPACE, CgmesNames.PROFILE);
            writer.writeCharacters(profile);
            writer.writeEndElement();
        }
        if (subset == CgmesSubset.EQUIPMENT && context.getTopologyKind().equals(CgmesTopologyKind.NODE_BREAKER) && context.getCimVersion() < 100) {
            // From CGMES 3 EquipmentOperation is not required to write operational limits, connectivity nodes
            writer.writeStartElement(MD_NAMESPACE, CgmesNames.PROFILE);
            writer.writeCharacters(context.getCim().getProfileUri("EQ_OP"));
            writer.writeEndElement();
        }
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.MODELING_AUTHORITY_SET);
        writer.writeCharacters(modelDescription.getModelingAuthoritySet());
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static String toRdfId(String id, CgmesExportContext context) {
        // Handling ids: if received id is not prefixed by "_", add it to make it a valid RDF:Id
        // We have to be careful with "resource" and "about" references, and apply the same conversions
        // Encode IDs to be URL compatible (prevent issues when importing)
        return context.encode(id.startsWith("_") ? id : "_" + id);
    }

    private static String toMasterResourceId(String id, CgmesExportContext context) {
        // Handling ids: if received id is prefixed by "_", remove it. Assuming it was added to comply with URN rules
        return context.encode(id.startsWith("_") ? id.substring(1) : id);
    }

    public static void writeStartId(String className, String id, boolean writeMasterResourceId, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, className);
        // Writing mRID was optional in CIM 16, but is required since CIM 100
        // Only classes extending IdentifiedObject have an mRID
        // points of tables and curve data objects do not have mRID, although they have an RDF:ID
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, toRdfId(id, context));
        if (writeMasterResourceId && context.getCim().getVersion() >= 100) {
            writer.writeStartElement(cimNamespace, "IdentifiedObject.mRID");
            writer.writeCharacters(toMasterResourceId(id, context));
            writer.writeEndElement();
        }
    }

    public static void writeStartIdName(String className, String id, String name, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        writeStartId(className, id, true, cimNamespace, writer, context);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(name.length() > 32 ? name.substring(0, 32) : name); // name should not be longer than 32 characters
        writer.writeEndElement();
    }

    public static void writeReference(String refName, String referredId, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        writer.writeEmptyElement(cimNamespace, refName);
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + toRdfId(referredId, context));
    }

    public static void writeStartAbout(String className, String id, String cimNamespace, XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        writer.writeStartElement(cimNamespace, className);
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ABOUT, "#" + toRdfId(id, context));
    }

    public static String loadClassName(Load load) {
        String originalClassName = load.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS, "undefined");
        double p0 = load.getP0();
        LoadDetail loadDetail = load.getExtension(LoadDetail.class);
        if (originalClassName.equals(CgmesNames.ASYNCHRONOUS_MACHINE)
                || originalClassName.equals(CgmesNames.SV_INJECTION)
                || originalClassName.equals(CgmesNames.ENERGY_SOURCE) && p0 <= 0.0
                || originalClassName.equals(CgmesNames.ENERGY_CONSUMER) && p0 >= 0.0 && !isConformLoad(loadDetail) && !isNonConformLoad(loadDetail)
                || originalClassName.equals(CgmesNames.CONFORM_LOAD) && p0 >= 0.0 && !isNonConformLoad(loadDetail)
                || originalClassName.equals(CgmesNames.NONCONFORM_LOAD) && p0 >= 0.0 && !isConformLoad(loadDetail)
                || originalClassName.equals(CgmesNames.STATION_SUPPLY) && p0 >= 0.0 && !isConformLoad(loadDetail) && !isNonConformLoad(loadDetail)) {
            return originalClassName;
        }
        return calculatedLoadClassName(p0, loadDetail);
    }

    private static String calculatedLoadClassName(double p0, LoadDetail loadDetail) {
        // As negative loads are not allowed, they are modeled as energy source.
        // Note that negative loads can be the result of network reduction and could be modeled
        // as equivalent injections.
        return p0 < 0 ? CgmesNames.ENERGY_SOURCE : loadDetailClassName(loadDetail);
    }

    public static String loadDetailClassName(LoadDetail loadDetail) {
        if (loadDetail != null) {
            if (isConformLoad(loadDetail)) {
                return CgmesNames.CONFORM_LOAD;
            } else if (isNonConformLoad(loadDetail)) {
                return CgmesNames.NONCONFORM_LOAD;
            } else {
                return CgmesNames.NONCONFORM_LOAD;
            }
        }
        LOG.warn("It is not possible to determine the type of load");
        return CgmesNames.ENERGY_CONSUMER;
    }

    private static boolean isConformLoad(LoadDetail loadDetail) {
        // Conform load if fixed part is zero and variable part is non-zero
        return loadDetail != null
                && loadDetail.getFixedActivePower() == 0
                && loadDetail.getFixedReactivePower() == 0
                && (loadDetail.getVariableActivePower() != 0 || loadDetail.getVariableReactivePower() != 0);
    }

    private static boolean isNonConformLoad(LoadDetail loadDetail) {
        // NonConform load if fixed part is non-zero and variable part is all zero
        return loadDetail != null
                && loadDetail.getVariableActivePower() == 0
                && loadDetail.getVariableReactivePower() == 0
                && (loadDetail.getFixedActivePower() != 0 || loadDetail.getFixedReactivePower() != 0);
    }

    public static String switchClassname(SwitchKind kind) {
        return switch (kind) {
            case BREAKER -> "Breaker";
            case DISCONNECTOR -> "Disconnector";
            case LOAD_BREAK_SWITCH -> "LoadBreakSwitch";
        };
    }

    public static int getTerminalSequenceNumber(Terminal t) {
        Connectable<?> c = t.getConnectable();
        if (c.getTerminals().size() == 1) {
            return 1;
        } else {
            if (c instanceof Branch<?> branch) {
                return branch.getSide(t).getNum();
            } else if (c instanceof ThreeWindingsTransformer twt) {
                return twt.getSide(t).getNum();
            } else {
                throw new PowsyblException("Unexpected Connectable instance: " + c.getClass());
            }
        }
    }

    public static boolean isConverterStationRectifier(HvdcConverterStation<?> converterStation) {
        if (converterStation.getHvdcLine().getConvertersMode().equals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)) {
            return converterStation.getHvdcLine().getConverterStation1().equals(converterStation);
        } else {
            return converterStation.getHvdcLine().getConverterStation2().equals(converterStation);
        }
    }

    public static String converterClassName(HvdcConverterStation<?> converterStation) {
        if (converterStation instanceof LccConverterStation) {
            return "CsConverter";
        } else if (converterStation instanceof VscConverterStation) {
            return "VsConverter";
        } else {
            throw new PowsyblException("Invalid converter type");
        }
    }

    public static <C extends Connectable<C>> Optional<String> cgmesTapChangerType(C eq, String tcId) {
        CgmesTapChangers<C> cgmesTcs = eq.getExtension(CgmesTapChangers.class);
        if (cgmesTcs != null) {
            CgmesTapChanger cgmesTc = cgmesTcs.getTapChanger(tcId);
            if (cgmesTc != null) {
                return Optional.ofNullable(cgmesTc.getType());
            }
        }
        return Optional.empty();
    }

    public static <C extends Connectable<C>> void setCgmesTapChangerType(C eq, String tapChangerId, String type) {
        CgmesTapChangers<C> cgmesTcs = eq.getExtension(CgmesTapChangers.class);
        if (cgmesTcs != null) {
            CgmesTapChanger cgmesTc = cgmesTcs.getTapChanger(tapChangerId);
            if (cgmesTc != null) {
                cgmesTc.setType(type);
            }
        }
    }

    // tap changer is exported as it is modelled in IIDM, always at end 1
    static void addUpdateCgmesTapChangerExtension(TwoWindingsTransformer twt, CgmesExportContext context) {
        twt.getOptionalRatioTapChanger().ifPresent(rtc -> addTapChangerExtension(twt, RATIO_TAP_CHANGER, getTapChangerId(twt, RATIO_TAP_CHANGER, context), rtc.getTapPosition(), regulatingControlIsDefined(rtc), context));
        twt.getOptionalPhaseTapChanger().ifPresent(ptc -> addTapChangerExtension(twt, PHASE_TAP_CHANGER, getTapChangerId(twt, PHASE_TAP_CHANGER, context), ptc.getTapPosition(), regulatingControlIsDefined(ptc), context));
    }

    static void addUpdateCgmesTapChangerExtension(ThreeWindingsTransformer twt, CgmesExportContext context) {
        twt.getLeg1().getOptionalRatioTapChanger().ifPresent(rtc -> addTapChangerExtension(twt, RATIO_TAP_CHANGER, getTapChangerId(twt, RATIO_TAP_CHANGER, 1, context), rtc.getTapPosition(), regulatingControlIsDefined(rtc), context));
        twt.getLeg1().getOptionalPhaseTapChanger().ifPresent(ptc -> addTapChangerExtension(twt, PHASE_TAP_CHANGER, getTapChangerId(twt, PHASE_TAP_CHANGER, 1, context), ptc.getTapPosition(), regulatingControlIsDefined(ptc), context));

        twt.getLeg2().getOptionalRatioTapChanger().ifPresent(rtc -> addTapChangerExtension(twt, RATIO_TAP_CHANGER, getTapChangerId(twt, RATIO_TAP_CHANGER, 2, context), rtc.getTapPosition(), regulatingControlIsDefined(rtc), context));
        twt.getLeg2().getOptionalPhaseTapChanger().ifPresent(ptc -> addTapChangerExtension(twt, PHASE_TAP_CHANGER, getTapChangerId(twt, PHASE_TAP_CHANGER, 2, context), ptc.getTapPosition(), regulatingControlIsDefined(ptc), context));

        twt.getLeg3().getOptionalRatioTapChanger().ifPresent(rtc -> addTapChangerExtension(twt, RATIO_TAP_CHANGER, getTapChangerId(twt, RATIO_TAP_CHANGER, 3, context), rtc.getTapPosition(), regulatingControlIsDefined(rtc), context));
        twt.getLeg3().getOptionalPhaseTapChanger().ifPresent(ptc -> addTapChangerExtension(twt, PHASE_TAP_CHANGER, getTapChangerId(twt, PHASE_TAP_CHANGER, 3, context), ptc.getTapPosition(), regulatingControlIsDefined(ptc), context));
    }

    // If we had alias only for tc1, it will be at end 1
    // If we had alias for tc1 and tc2, tc2 has been moved to end 1 and combined with tc1, tc1 id will be used
    // If we only had tc at end 2, it has been moved to end 1 but the id is recorded at end2, tc2 id will be used
    private static <C extends Connectable<C>> String getTapChangerId(C twt, String cgmesTapChangerTag, CgmesExportContext context) {
        String aliasType1 = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + cgmesTapChangerTag + 1;
        Optional<String> optionalTapChangerId1 = twt.getAliasFromType(aliasType1);
        if (optionalTapChangerId1.isPresent()) {
            return optionalTapChangerId1.get();
        } else {
            String aliasType2 = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + cgmesTapChangerTag + 2;
            Optional<String> optionalTapChangerId2 = twt.getAliasFromType(aliasType2);
            if (optionalTapChangerId2.isEmpty()) {
                // We create a new id always at end 1
                Part ratioPhasePart = Objects.equals(cgmesTapChangerTag, RATIO_TAP_CHANGER) ? Part.RATIO_TAP_CHANGER : Part.PHASE_TAP_CHANGER;
                String newTapChangerId = context.getNamingStrategy().getCgmesId(refTyped(twt), ratioPhasePart);
                twt.addAlias(newTapChangerId, aliasType1);
                return newTapChangerId;
            } else {
                return optionalTapChangerId2.get();
            }
        }
    }

    private static <C extends Connectable<C>> String getTapChangerId(C twt, String cgmesTapChangerTag, int endNumber, CgmesExportContext context) {
        String aliasType = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + cgmesTapChangerTag + endNumber;
        Optional<String> optionalTapChangerId = twt.getAliasFromType(aliasType);
        if (optionalTapChangerId.isEmpty()) {
            Part ratioPhasePart = Objects.equals(cgmesTapChangerTag, RATIO_TAP_CHANGER) ? Part.RATIO_TAP_CHANGER : Part.PHASE_TAP_CHANGER;
            String newTapChangerId = context.getNamingStrategy().getCgmesId(refTyped(twt), ratioPhasePart);
            twt.addAlias(newTapChangerId, aliasType);
            return newTapChangerId;
        } else {
            return optionalTapChangerId.get();
        }
    }

    static boolean regulatingControlIsDefined(RatioTapChanger rtc) {
        return !Double.isNaN(rtc.getRegulationValue())
                && rtc.getRegulationTerminal() != null;
    }

    static boolean regulatingControlIsDefined(PhaseTapChanger ptc) {
        return ptc.getRegulationMode() != PhaseTapChanger.RegulationMode.FIXED_TAP
                && !Double.isNaN(ptc.getRegulationValue())
                && ptc.getRegulationTerminal() != null;
    }

    static boolean targetDeadbandIsDefined(double targetDeadband) {
        return !Double.isNaN(targetDeadband) && targetDeadband >= 0.0;
    }

    private static <C extends Connectable<C>> void addTapChangerExtension(C twt, String cgmesTapChangerTag, String tapChangerId, int tapPosition, boolean regulatingControlIsDefined, CgmesExportContext context) {

        CgmesTapChangers<C> cgmesTapChangers = twt.getExtension(CgmesTapChangers.class);
        if (cgmesTapChangers == null) {
            twt.newExtension(CgmesTapChangersAdder.class).add();
            cgmesTapChangers = twt.getExtension(CgmesTapChangers.class);
        }

        Part refTapChanger = Objects.equals(cgmesTapChangerTag, RATIO_TAP_CHANGER) ? Part.RATIO_TAP_CHANGER : Part.PHASE_TAP_CHANGER;

        CgmesTapChanger cgmesTapChanger = cgmesTapChangers.getTapChanger(tapChangerId);
        if (cgmesTapChanger == null) {
            cgmesTapChanger = cgmesTapChangers.newTapChanger()
                    .setId(tapChangerId)
                    .setStep(tapPosition)
                    .add();
        }
        if (regulatingControlIsDefined && cgmesTapChanger.getControlId() == null) {
            cgmesTapChanger.setControlId(context.getNamingStrategy().getCgmesId(ref(twt), refTapChanger, Part.REGULATING_CONTROL));
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(CgmesExportUtil.class);

    public static String getTerminalId(Terminal t, CgmesExportContext context) {
        String aliasType;
        Connectable<?> c = t.getConnectable();
        // For dangling lines terminal id is always stored at TERMINAL1 alias,
        // it doesn't matter if it is paired or not
        if (c instanceof DanglingLine) {
            aliasType = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1;
        } else {
            int sequenceNumber = getTerminalSequenceNumber(t);
            aliasType = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + sequenceNumber;
        }
        return context.getNamingStrategy().getCgmesIdFromAlias(c, aliasType);
    }

    public static List<DanglingLine> getBoundaryDanglingLines(Network network) {
        return network.getBoundaryElements().stream()
                .filter(DanglingLine.class::isInstance)
                .map(DanglingLine.class::cast)
                .sorted(Comparator.comparing(Identifiable::getId))
                .toList();
    }

    public static boolean isEquivalentShuntWithZeroSectionCount(Connectable<?> c) {
        if (c instanceof ShuntCompensator shuntCompensator) {
            return "true".equals(c.getProperty(Conversion.PROPERTY_IS_EQUIVALENT_SHUNT))
                    && shuntCompensator.getSectionCount() == 0;
        }
        return false;
    }

    static <I extends ReactiveLimitsHolder & Injection<I>> ReactiveCapabilityCurve obtainCurve(I i) {
        return i.getReactiveLimits().getKind().equals(ReactiveLimitsKind.CURVE) ? i.getReactiveLimits(ReactiveCapabilityCurve.class) : null;
    }

    // Original synchronous machine kind it is only preserved if it is compatible with the calculated synchronous machine kind
    // calculated synchronous machine kind is based on the present limits
    static <I extends ReactiveLimitsHolder & Injection<I>> String obtainSynchronousMachineKind(I i, double minP, double maxP, ReactiveCapabilityCurve curve) {
        String kind = i.getProperty(Conversion.PROPERTY_CGMES_SYNCHRONOUS_MACHINE_TYPE);
        String calculatedKind = CgmesExportUtil.obtainCalculatedSynchronousMachineKind(minP, maxP, curve, kind);
        if (kind == null) {
            return calculatedKind;
        } else if (calculatedKind.contains(kind)) {
            return kind;
        } else {
            LOG.warn("original synchronousMachineKind {} has been modified to {} according to the limits", kind, calculatedKind);
            return calculatedKind;
        }
    }

    // we cannot discriminate between generatorOrMotor and generatorOrCondenserOrMotor so,
    // we preserve the original kind if it is available
    static String obtainCalculatedSynchronousMachineKind(double minP, double maxP, ReactiveCapabilityCurve curve, String originalKind) {
        double min = curve != null ? curve.getMinP() : minP;
        double max = curve != null ? curve.getMaxP() : maxP;

        String kind;
        if (min > 0) {
            kind = "generator";
        } else if (max < 0) {
            kind = "motor";
        } else if (min == 0 && max == 0) {
            kind = "condenser";
        } else if (min == 0) {
            kind = "generatorOrCondenser";
        } else if (max == 0) {
            kind = "motorOrCondenser";
        } else {
            kind = originalKind != null && (originalKind.equals(GENERATOR_OR_MOTOR) || originalKind.equals("generatorOrCondenserOrMotor")) ? originalKind : "generatorOrCondenserOrMotor";
        }
        return kind;
    }

    public static boolean isValidVoltageSetpoint(double v) {
        return Double.isFinite(v) && v > 0;
    }

    public static boolean isValidReactivePowerSetpoint(double q) {
        return Double.isFinite(q);
    }

    public static String getGeneratorRegulatingControlMode(Generator generator, RemoteReactivePowerControl rrpc) {
        if (rrpc == null) {
            return RegulatingControlEq.REGULATING_CONTROL_VOLTAGE;
        }
        boolean enabledVoltageControl = generator.isVoltageRegulatorOn();
        boolean enabledReactivePowerControl = rrpc.isEnabled();

        if (enabledVoltageControl) {
            return RegulatingControlEq.REGULATING_CONTROL_VOLTAGE;
        } else if (enabledReactivePowerControl) {
            return RegulatingControlEq.REGULATING_CONTROL_REACTIVE_POWER;
        } else {
            boolean validVoltageSetpoint = isValidVoltageSetpoint(generator.getTargetV());
            boolean validReactiveSetpoint = isValidReactivePowerSetpoint(rrpc.getTargetQ());
            if (validReactiveSetpoint && !validVoltageSetpoint) {
                return RegulatingControlEq.REGULATING_CONTROL_REACTIVE_POWER;
            }
            return RegulatingControlEq.REGULATING_CONTROL_VOLTAGE;
        }
    }

    public static String getSvcMode(StaticVarCompensator svc) {
        if (svc.getRegulationMode().equals(StaticVarCompensator.RegulationMode.VOLTAGE)) {
            return RegulatingControlEq.REGULATING_CONTROL_VOLTAGE;
        } else if (svc.getRegulationMode().equals(StaticVarCompensator.RegulationMode.REACTIVE_POWER)) {
            return RegulatingControlEq.REGULATING_CONTROL_REACTIVE_POWER;
        } else {
            boolean validVoltageSetpoint = isValidVoltageSetpoint(svc.getVoltageSetpoint());
            boolean validReactiveSetpoint = isValidReactivePowerSetpoint(svc.getReactivePowerSetpoint());
            if (validReactiveSetpoint && !validVoltageSetpoint) {
                return RegulatingControlEq.REGULATING_CONTROL_REACTIVE_POWER;
            }
            return RegulatingControlEq.REGULATING_CONTROL_VOLTAGE;
        }
    }

    public static String getTcMode(RatioTapChanger rtc) {
        if (rtc.getRegulationMode() == null) {
            throw new PowsyblException("Regulation mode not defined for RTC.");
        }
        return switch (rtc.getRegulationMode()) {
            case VOLTAGE -> RegulatingControlEq.REGULATING_CONTROL_VOLTAGE;
            case REACTIVE_POWER -> RegulatingControlEq.REGULATING_CONTROL_REACTIVE_POWER;
        };
    }

    public static String getPhaseTapChangerRegulationMode(PhaseTapChanger ptc) {
        return switch (ptc.getRegulationMode()) {
            case CURRENT_LIMITER -> RegulatingControlEq.REGULATING_CONTROL_CURRENT_FLOW;
            case ACTIVE_POWER_CONTROL -> RegulatingControlEq.REGULATING_CONTROL_ACTIVE_POWER;
            default -> throw new PowsyblException("Unexpected regulation mode: " + ptc.getRegulationMode());
        };
    }

    public static boolean isMinusOrMaxValue(double value) {
        return value == -Double.MAX_VALUE || value == Double.MAX_VALUE;
    }
}
