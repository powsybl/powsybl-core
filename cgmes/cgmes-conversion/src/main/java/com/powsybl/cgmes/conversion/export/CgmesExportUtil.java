/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.export.CgmesExportContext.ModelDescription;
import com.powsybl.cgmes.extensions.CgmesTapChanger;
import com.powsybl.cgmes.extensions.CgmesTapChangers;
import com.powsybl.cgmes.extensions.CgmesTapChangersAdder;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.LoadDetail;
import com.powsybl.iidm.network.util.LinkData;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.powsybl.cgmes.model.CgmesNamespace.MD_NAMESPACE;
import static com.powsybl.cgmes.model.CgmesNamespace.RDF_NAMESPACE;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public final class CgmesExportUtil {

    private CgmesExportUtil() {
    }

    // Avoid trailing zeros and format always using US locale

    private static final DecimalFormatSymbols DOUBLE_FORMAT_SYMBOLS = new DecimalFormatSymbols(Locale.US);
    private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("0.##############", DOUBLE_FORMAT_SYMBOLS);
    private static final DecimalFormat SCIENTIFIC_FORMAT = new DecimalFormat("0.####E0", DOUBLE_FORMAT_SYMBOLS);

    private static final Pattern CIM_MRID_PATTERN = Pattern.compile("(?i)[a-f\\d]{8}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{4}-[a-f\\d]{12}");
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
        if (value == Double.MAX_VALUE || value == -Double.MAX_VALUE) {
            return scientificFormat(value, defaultValue);
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

    public static String getUniqueId() {
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

    public static void writeModelDescription(XMLStreamWriter writer, ModelDescription modelDescription, CgmesExportContext context) throws XMLStreamException {
        writer.writeStartElement(MD_NAMESPACE, "FullModel");
        String modelId = "urn:uuid:" + CgmesExportUtil.getUniqueId();
        modelDescription.setIds(modelId);
        context.updateDependencies();
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ABOUT, modelId);
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.SCENARIO_TIME);
        writer.writeCharacters(ISODateTimeFormat.dateTimeNoMillis().withZoneUTC().print(context.getScenarioTime()));
        writer.writeEndElement();
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.CREATED);
        writer.writeCharacters(ISODateTimeFormat.dateTimeNoMillis().withZoneUTC().print(DateTime.now()));
        writer.writeEndElement();
        if (modelDescription.getDescription() != null) {
            writer.writeStartElement(MD_NAMESPACE, CgmesNames.DESCRIPTION);
            writer.writeCharacters(modelDescription.getDescription());
            writer.writeEndElement();
        }
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.VERSION);
        writer.writeCharacters(format(modelDescription.getVersion()));
        writer.writeEndElement();
        for (String dependency : modelDescription.getDependencies()) {
            writer.writeEmptyElement(MD_NAMESPACE, CgmesNames.DEPENDENT_ON);
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, dependency);
        }
        if (modelDescription.getSupersedes() != null) {
            writer.writeEmptyElement(MD_NAMESPACE, CgmesNames.SUPERSEDES);
            writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, modelDescription.getSupersedes());
        }
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.PROFILE);
        writer.writeCharacters(modelDescription.getProfile());
        writer.writeEndElement();
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
        if (writeMasterResourceId) {
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

    public static Complex complexVoltage(double r, double x, double g, double b,
                                         double v, double angle, double p, double q) {
        LinkData.BranchAdmittanceMatrix adm = LinkData.calculateBranchAdmittance(r, x, 1.0, 0.0, 1.0, 0.0,
                new Complex(g * 0.5, b * 0.5), new Complex(g * 0.5, b * 0.5));
        Complex v1 = ComplexUtils.polar2Complex(v, Math.toRadians(angle));
        Complex s1 = new Complex(p, q);
        return (s1.conjugate().divide(v1.conjugate()).subtract(adm.y11().multiply(v1))).divide(adm.y12());
    }

    public static String loadClassName(LoadDetail loadDetail) {
        if (loadDetail != null) {
            // Conform load if fixed part is zero and variable part is non-zero
            if (loadDetail.getFixedActivePower() == 0 && loadDetail.getFixedReactivePower() == 0
                    && (loadDetail.getVariableActivePower() != 0 || loadDetail.getVariableReactivePower() != 0)) {
                return CgmesNames.CONFORM_LOAD;
            } else if (loadDetail.getVariableActivePower() == 0 && loadDetail.getVariableReactivePower() == 0
                    && (loadDetail.getFixedActivePower() != 0 || loadDetail.getFixedReactivePower() != 0)) {  // NonConform load if fixed part is non-zero and variable part is all zero
                return CgmesNames.NONCONFORM_LOAD;
            } else {
                return CgmesNames.NONCONFORM_LOAD;
            }
        }
        LOG.warn("It is not possible to determine the type of load");
        return CgmesNames.ENERGY_CONSUMER;
    }

    public static String switchClassname(SwitchKind kind) {
        switch (kind) {
            case BREAKER:
                return "Breaker";
            case DISCONNECTOR:
                return "Disconnector";
            case LOAD_BREAK_SWITCH:
                return "LoadBreakSwitch";
        }
        LOG.warn("It is not possible to determine the type of switch from kind {}", kind);
        return "Switch";
    }

    public static int getTerminalSequenceNumber(Terminal t, List<DanglingLine> boundaryDanglingLines) {
        Connectable<?> c = t.getConnectable();
        if (c.getTerminals().size() == 1) {
            if (c instanceof DanglingLine dl && !boundaryDanglingLines.contains(dl)) {
                // TODO(Luma) Export tie line components instead of a single equipment
                // If this dangling line is part of a tie line we will be exporting the tie line as a single equipment
                // We need to return the proper terminal of the single tie line that will be exported
                // When we change the export and write the two dangling lines as separate equipment,
                // then we should always return 1 and forget about special case
                return dl.getTieLine().map(tl -> tl.getDanglingLine1() == dl ? 1 : 2).orElse(1);
            }
            return 1;
        } else {
            if (c instanceof Branch) {
                switch (((Branch<?>) c).getSide(t)) {
                    case ONE:
                        return 1;
                    case TWO:
                        return 2;
                    default:
                        throw new IllegalStateException("Incorrect branch side " + ((Branch<?>) c).getSide(t));
                }
            } else if (c instanceof ThreeWindingsTransformer twt) {
                switch (twt.getSide(t)) {
                    case ONE:
                        return 1;
                    case TWO:
                        return 2;
                    case THREE:
                        return 3;
                    default:
                        throw new IllegalStateException("Incorrect three-windings transformer side " + twt.getSide(t));
                }
            } else {
                throw new PowsyblException("Unexpected Connectable instance: " + c.getClass());
            }
        }
    }

    public static boolean isConverterStationRectifier(HvdcConverterStation<?> converterStation) {
        if (converterStation.getHvdcLine().getConvertersMode().equals(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)) {
            if (converterStation.getHvdcLine().getConverterStation1().equals(converterStation)) {
                return true;
            }
        } else {
            if (converterStation.getHvdcLine().getConverterStation2().equals(converterStation)) {
                return true;
            }
        }
        return false;
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
    static void addUpdateCgmesTapChangerExtension(TwoWindingsTransformer twt) {
        twt.getOptionalRatioTapChanger().ifPresent(rtc -> addTapChangerExtension(twt, getTapChangerId(twt, CgmesNames.RATIO_TAP_CHANGER), regulatingControlIsDefined(rtc)));
        twt.getOptionalPhaseTapChanger().ifPresent(ptc -> addTapChangerExtension(twt, getTapChangerId(twt, CgmesNames.PHASE_TAP_CHANGER), regulatingControlIsDefined(ptc)));
    }

    static void addUpdateCgmesTapChangerExtension(ThreeWindingsTransformer twt) {
        twt.getLeg1().getOptionalRatioTapChanger().ifPresent(rtc -> addTapChangerExtension(twt, getTapChangerId(twt, CgmesNames.RATIO_TAP_CHANGER, 1), regulatingControlIsDefined(rtc)));
        twt.getLeg1().getOptionalPhaseTapChanger().ifPresent(ptc -> addTapChangerExtension(twt, getTapChangerId(twt, CgmesNames.PHASE_TAP_CHANGER, 1), regulatingControlIsDefined(ptc)));

        twt.getLeg2().getOptionalRatioTapChanger().ifPresent(rtc -> addTapChangerExtension(twt, getTapChangerId(twt, CgmesNames.RATIO_TAP_CHANGER, 2), regulatingControlIsDefined(rtc)));
        twt.getLeg2().getOptionalPhaseTapChanger().ifPresent(ptc -> addTapChangerExtension(twt, getTapChangerId(twt, CgmesNames.PHASE_TAP_CHANGER, 2), regulatingControlIsDefined(ptc)));

        twt.getLeg3().getOptionalRatioTapChanger().ifPresent(rtc -> addTapChangerExtension(twt, getTapChangerId(twt, CgmesNames.RATIO_TAP_CHANGER, 3), regulatingControlIsDefined(rtc)));
        twt.getLeg3().getOptionalPhaseTapChanger().ifPresent(ptc -> addTapChangerExtension(twt, getTapChangerId(twt, CgmesNames.PHASE_TAP_CHANGER, 3), regulatingControlIsDefined(ptc)));
    }

    // If we had alias only for tc1, it will be at end 1
    // If we had alias for tc1 and tc2, tc2 has been moved to end 1 and combined with tc1, tc1 id will be used
    // If we only had tc at end 2, it has been moved to end 1 but the id is recorded at end2, tc2 id will be used
    private static <C extends Connectable<C>> String getTapChangerId(C twt, String cgmesTapChangerTag) {
        String aliasType1 = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + cgmesTapChangerTag + 1;
        Optional<String> optionalTapChangerId1 = twt.getAliasFromType(aliasType1);
        if (optionalTapChangerId1.isPresent()) {
            return optionalTapChangerId1.get();
        } else {
            String aliasType2 = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + cgmesTapChangerTag + 2;
            Optional<String> optionalTapChangerId2 = twt.getAliasFromType(aliasType2);
            if (optionalTapChangerId2.isEmpty()) {
                // We create a new id always at end 1
                String newTapChangerId = CgmesExportUtil.getUniqueId();
                twt.addAlias(newTapChangerId, aliasType1);
                return newTapChangerId;
            } else {
                return optionalTapChangerId2.get();
            }
        }
    }

    private static <C extends Connectable<C>> String getTapChangerId(C twt, String cgmesTapChangerTag, int endNumber) {
        String aliasType = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + cgmesTapChangerTag + endNumber;
        Optional<String> optionalTapChangerId = twt.getAliasFromType(aliasType);
        if (optionalTapChangerId.isEmpty()) {
            String newTapChangerId = CgmesExportUtil.getUniqueId();
            twt.addAlias(newTapChangerId, aliasType);
            return newTapChangerId;
        } else {
            return optionalTapChangerId.get();
        }
    }

    static boolean regulatingControlIsDefined(RatioTapChanger rtc) {
        return !Double.isNaN(rtc.getTargetV())
                && !Double.isNaN(rtc.getTargetDeadband())
                && rtc.getRegulationTerminal() != null;
    }

    static boolean regulatingControlIsDefined(PhaseTapChanger ptc) {
        return ptc.getRegulationMode() != PhaseTapChanger.RegulationMode.FIXED_TAP
                && !Double.isNaN(ptc.getRegulationValue())
                && !Double.isNaN(ptc.getTargetDeadband())
                && ptc.getRegulationTerminal() != null;
    }

    private static <C extends Connectable<C>> void addTapChangerExtension(C twt, String tapChangerId, boolean regulatingControlIsDefined) {
        if (!regulatingControlIsDefined) {
            return;
        }
        CgmesTapChangers<C> cgmesTapChangers = twt.getExtension(CgmesTapChangers.class);
        if (cgmesTapChangers == null) {
            twt.newExtension(CgmesTapChangersAdder.class).add();
            cgmesTapChangers = twt.getExtension(CgmesTapChangers.class);
        }
        CgmesTapChanger cgmesTapChanger = cgmesTapChangers.getTapChanger(tapChangerId);
        if (cgmesTapChanger == null) {
            cgmesTapChanger = cgmesTapChangers.newTapChanger()
                    .setId(tapChangerId)
                    .setControlId(CgmesExportUtil.getUniqueId())
                    .add();
        }
        if (cgmesTapChanger.getControlId() == null) {
            cgmesTapChanger.setControlId(CgmesExportUtil.getUniqueId());
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
            int sequenceNumber = getTerminalSequenceNumber(t, Collections.emptyList()); // never a dangling line here
            aliasType = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + sequenceNumber;
        }
        return context.getNamingStrategy().getCgmesIdFromAlias(c, aliasType);
    }

    public static List<DanglingLine> getBoundaryDanglingLines(Network network) {
        return network.getBoundaryElements().stream()
                .filter(DanglingLine.class::isInstance)
                .map(DanglingLine.class::cast)
                .collect(Collectors.toList());
    }

    public static boolean isEquivalentShuntWithZeroSectionCount(Connectable<?> c) {
        if (c instanceof ShuntCompensator shuntCompensator) {
            return "true".equals(c.getProperty(Conversion.PROPERTY_IS_EQUIVALENT_SHUNT))
                    && shuntCompensator.getSectionCount() == 0;
        }
        return false;
    }
}
