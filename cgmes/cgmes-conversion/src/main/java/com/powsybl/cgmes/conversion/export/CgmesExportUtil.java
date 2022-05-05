/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.export.CgmesExportContext.ModelDescription;
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
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

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
    private static final DecimalFormat SCIENFIFIC_FORMAT = new DecimalFormat("0.####E0", DOUBLE_FORMAT_SYMBOLS);

    public static String format(double value) {
        return DOUBLE_FORMAT.format(Double.isNaN(value) ? 0.0 : value);
    }

    public static String scientificFormat(double value) {
        return SCIENFIFIC_FORMAT.format(Double.isNaN(value) ? 0.0 : value);
    }

    public static String format(int value) {
        return String.valueOf(value);
    }

    public static String format(boolean value) {
        return String.valueOf(value);
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
        modelDescription.setId(modelId);
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
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.PROFILE);
        writer.writeCharacters(modelDescription.getProfile());
        writer.writeEndElement();
        writer.writeStartElement(MD_NAMESPACE, CgmesNames.MODELING_AUTHORITY_SET);
        writer.writeCharacters(modelDescription.getModelingAuthoritySet());
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private static String toRdfId(String id) {
        // Handling ids: if received id is not prefixed by "_", add it to make it a valid RDF:Id
        // We have to be careful with "resource" and "about" references, and apply the same conversions
        return id.startsWith("_") ? id : "_" + id;
    }

    private static String toMasterResourceId(String id) {
        // Handling ids: if received id is prefixed by "_", remove it. Assuming it was added to comply with URN rules
        return id.startsWith("_") ? id.substring(1) : id;
    }

    public static void writeStartId(String className, String id, boolean writeMasterResourceId, String cimNamespace, XMLStreamWriter writer)  throws XMLStreamException {
        writer.writeStartElement(cimNamespace, className);
        // Writing mRID was optional in CIM 16, but is required since CIM 100
        // Only classes extending IdentifiedObject have an mRID
        // points of tables and curve data objects do not have mRID, although they have an RDF:ID
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ID, toRdfId(id));
        if (writeMasterResourceId) {
            writer.writeStartElement(cimNamespace, "IdentifiedObject.mRID");
            writer.writeCharacters(toMasterResourceId(id));
            writer.writeEndElement();
        }
    }

    public static void writeStartIdName(String className, String id, String name, String cimNamespace, XMLStreamWriter writer)  throws XMLStreamException {
        writeStartId(className, id, true, cimNamespace, writer);
        writer.writeStartElement(cimNamespace, CgmesNames.NAME);
        writer.writeCharacters(name);
        writer.writeEndElement();
    }

    public static void writeReference(String refName, String referredId, String cimNamespace, XMLStreamWriter writer) throws XMLStreamException {
        writer.writeEmptyElement(cimNamespace, refName);
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.RESOURCE, "#" + toRdfId(referredId));
    }

    public static void writeStartAbout(String className, String id, String cimNamespace, XMLStreamWriter writer)  throws XMLStreamException {
        writer.writeStartElement(cimNamespace, className);
        writer.writeAttribute(RDF_NAMESPACE, CgmesNames.ABOUT, "#" + toRdfId(id));
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
                return "ConformLoad";
            }
            // NonConform load if fixed part is non-zero and variable part is all zero
            if (loadDetail.getVariableActivePower() == 0 && loadDetail.getVariableReactivePower() == 0
                    && (loadDetail.getFixedActivePower() != 0 || loadDetail.getFixedReactivePower() != 0)) {
                return "NonConformLoad";
            }
        }
        LOG.warn("It is not possible to determine the type of load");
        return "EnergyConsumer";
    }

    /**
     * @deprecated Use {@link #getTerminalSequenceNumber(Terminal)} instead
     */
    @Deprecated(since = "4.9.0", forRemoval = true)
    public static int getTerminalSide(Terminal t, Connectable<?> c) {
        // There is no need to provide the connectable explicitly, it must always be the one associated with the terminal
        if (c != t.getConnectable()) {
            throw new PowsyblException("Wrong connectable in getTerminalSide : " + c.getId());
        }
        return getTerminalSequenceNumber(t);
    }

    public static int getTerminalSequenceNumber(Terminal t) {
        Connectable<?> c = t.getConnectable();
        if (c.getTerminals().size() == 1) {
            return 1;
        } else {
            if (c instanceof Injection) {
                // An injection should have only one terminal
            } else if (c instanceof Branch) {
                switch (((Branch<?>) c).getSide(t)) {
                    case ONE:
                        return 1;
                    case TWO:
                        return 2;
                    default:
                        throw new AssertionError("Incorrect branch side " + ((Branch<?>) c).getSide(t));
                }
            } else if (c instanceof ThreeWindingsTransformer) {
                switch (((ThreeWindingsTransformer) c).getSide(t)) {
                    case ONE:
                        return 1;
                    case TWO:
                        return 2;
                    case THREE:
                        return 3;
                    default:
                        throw new AssertionError("Incorrect three-windings transformer side " + ((ThreeWindingsTransformer) c).getSide(t));
                }
            } else {
                throw new PowsyblException("Unexpected Connectable instance: " + c.getClass());
            }
        }
        return 0;
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

    private static final Logger LOG = LoggerFactory.getLogger(CgmesExportUtil.class);

    public static String getTerminalId(Terminal t) {
        String aliasType;
        Connectable<?> c = t.getConnectable();
        if (c instanceof DanglingLine) {
            aliasType = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "Terminal_Network";
        } else {
            int sequenceNumber = getTerminalSequenceNumber(t);
            aliasType = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + sequenceNumber;
        }
        Optional<String> terminalId = c.getAliasFromType(aliasType);
        if (terminalId.isEmpty()) {
            LOG.error("Alias for type {} not found in connectable {}", aliasType, t.getConnectable().getId());
            throw new PowsyblException("Alias for type " + aliasType + " not found in connectable " + t.getConnectable().getId());
        }
        return terminalId.get();
    }
}
