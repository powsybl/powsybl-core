/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.eurostag;

import com.google.common.base.Strings;
import eu.itesla_project.contingency.Contingency;
import eu.itesla_project.contingency.ContingencyElement;
import eu.itesla_project.iidm.eurostag.export.EurostagDictionary;
import eu.itesla_project.iidm.network.*;
import eu.itesla_project.simulation.SimulationParameters;
import org.jboss.shrinkwrap.api.Domain;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.nio.file.ShrinkWrapFileSystems;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EurostagScenario {

    private static final DecimalFormatSymbols SYMBOLS = new DecimalFormatSymbols(Locale.US);

    private static final String PADDING_1 = " ";
    private static final String PADDING_5 = Strings.padEnd("", 5, ' ');
    private static final String PADDING_6 = Strings.padEnd("", 6, ' ');
    private static final String PADDING_8 = Strings.padEnd("", 8, ' ');
    private static final String PADDING_12 = Strings.padEnd("", 12, ' ');
    private static final String PADDING_15 = Strings.padEnd("", 15, ' ');

    private final SimulationParameters parameters;

    private final EurostagConfig config;

    public EurostagScenario(SimulationParameters parameters, EurostagConfig config) {
        this.parameters = parameters;
        this.config = config;
    }

    private static String format(double f, int width) {
        String s = new DecimalFormat(Strings.padEnd("#.0", width + 1, '#'), SYMBOLS).format(f);
        if (s.length() > width) {
            throw new RuntimeException("Impossible to format " + f + " to " + width + " characters");
        }
        return String.format("%" + width + "s", s);
    }

    private static void writeParamZone(BufferedWriter writer) throws IOException {
        writer.append("PARAM");
        writer.newLine();
        writer.append("         0  0  0");
        writer.newLine();
        writer.append(PADDING_8)
                .append(format(0.00001, 15))
                .append(format(0.0001, 15))
                .append(format(0.001, 15))
                .append(format(0.0001, 15))
                .append(format(0.0001, 15))
                .append("2");
        writer.newLine();
        writer.newLine();
    }

    public void writePreFaultSeq(BufferedWriter writer, String sacFileName) throws IOException {
        writer.append("HEADER")
                .append(PADDING_5)
                .append(new SimpleDateFormat("dd/MM/YY").format(new Date()))
                .append(" ")
                .append(EurostagUtil.VERSION);
        writer.newLine();
        writer.newLine();

        writeParamZone(writer);

        writer.append("TIME");
        writer.newLine();
        writer.append(PADDING_8)
                .append(format(config.getMinimumStep(), 15)) // minimum step used in the integration method
                .append(format(60, 15)) // maximum step used in the integration method
                .append(format(0, 15)); // initial time
        writer.newLine();
        writer.newLine();

        writer.append("EVENTS");
        writer.newLine();
        writer.append(format(parameters.getPreFaultSimulationStopInstant(), 8))
                .append(PADDING_1)
                .append("SAVE    ")
                .append(PADDING_1)
                .append(String.format("%32s", sacFileName))
                .append(String.format("%24s", ""))
                .append(String.format("%5s", 0));
        writer.newLine();

        writer.append(format(parameters.getPreFaultSimulationStopInstant(), 8))
                .append(PADDING_1)
                .append("STOP");
        writer.newLine();
        writer.newLine();
    }

    public void writeFaultSeq(BufferedWriter writer, Contingency contingency, Network network, EurostagDictionary dictionary) throws IOException {
        writer.append("HEADER     14/01/13 ").append(EurostagUtil.VERSION);
        writer.newLine();
        writer.newLine();

        writeParamZone(writer);

        writer.append("TIME");
        writer.newLine();
        writer.append(PADDING_8)
                .append(format(config.getMinimumStep(), 15))
                .append(format(60, 15))
                .append(format(0, 15));
        writer.newLine();
        writer.newLine();

        writer.append("EVENTS");
        writer.newLine();
        for (ContingencyElement element : contingency.getElements()) {
            switch (element.getType()) {
                case LINE: {
                    Line l = network.getLine(element.getId());
                    if (l == null) {
                        throw new RuntimeException("Line '" + element.getId() + "' not found");
                    }
                    VoltageLevel vl1 = l.getTerminal1().getVoltageLevel();
                    VoltageLevel vl2 = l.getTerminal2().getVoltageLevel();
                    double shortCircuitDistance = parameters.getBranchFaultShortCircuitDistance(contingency.getId(), element.getId());
                    if (shortCircuitDistance != 50) {
                        String shortCircuitSide = parameters.getBranchFaultShortCircuitSide(contingency.getId(), l.getId());
                        if (shortCircuitSide == null) {
                            throw new RuntimeException("Short circuit side has to be specified in detailed dynamic simulation configuration file when not at middle of the line");
                        }
                        if (shortCircuitSide.equals(vl1.getId())) {
                            // nothing to do
                        } else if (shortCircuitSide.equals(vl2.getId())) {
                            shortCircuitDistance = 100 - shortCircuitDistance; // invert the distance because in Eurostag it is always defined from sending node (terminal 1 in iidm)
                        } else {
                            throw new RuntimeException("Bad side definition " + shortCircuitSide + " for line " + l.getId()
                                    + ": " + vl1.getId() + " or " + vl2.getId());
                        }
                    }
                    double shortCircuitDuration = parameters.getBranchFaultShortCircuitDuration(contingency.getId(), l.getId());
                    String esgId = dictionary.getEsgId(l.getId());
                    writer.append(String.format("%8s", parameters.getFaultEventInstant()))
                            .append(PADDING_1)
                            .append("FAULTONL")
                            .append(PADDING_1)
                            .append(esgId)
                            .append(PADDING_1)
                            .append("   ") // empty: permanent fault, FUG: transitory fault
                            .append(PADDING_6)
                            .append(format(shortCircuitDistance, 8)) // short circuit distance from sending node
                            .append(PADDING_1)
                            .append(format(parameters.getBranchFaultResistance(), 8)) // fault resistance
                            .append(PADDING_1)
                            .append(format(parameters.getBranchFaultReactance(), 8)) // fault reactance
                            .append(PADDING_1)
                            .append("   0.");
                    writer.newLine();
                    writer.append(format(parameters.getFaultEventInstant() + shortCircuitDuration, 8))
                            .append(PADDING_1)
                            .append("BRANC OP")
                            .append(PADDING_1)
                            .append(esgId)
                            .append(" S          1                           0.");
                    writer.newLine();
                    writer.append(format(parameters.getFaultEventInstant() + shortCircuitDuration, 8))
                            .append(PADDING_1)
                            .append("BRANC OP")
                            .append(PADDING_1)
                            .append(esgId)
                            .append(" R          1                           0.");
                    writer.newLine();
                }
                break;

                case GENERATOR: {
                    Generator g = network.getGenerator(element.getId());
                    if (g == null) {
                        throw new RuntimeException("Generator '" + element.getId() + "' not found");
                    }
                    Bus bus = g.getTerminal().getBusBreakerView().getConnectableBus();
                    double shortCircuitDuration = parameters.getGeneratorFaultShortCircuitDuration(contingency.getId(), g.getId());
                    String esgId = dictionary.getEsgId(bus.getId());
                    writer.append(format(parameters.getFaultEventInstant(), 8))
                            .append(PADDING_1)
                            .append("FAULTATN")
                            .append(PADDING_1)
                            .append(esgId)
                            .append(PADDING_12)
                            .append("   ") // empty: permanent fault, FUG: transitory fault
                            .append(PADDING_15)
                            .append(format(parameters.getGeneratorFaultResistance(), 8)) // fault resistance
                            .append(PADDING_1)
                            .append(format(parameters.getGeneratorFaultReactance(), 8)) // fault reactance
                            .append(PADDING_1)
                            .append("   0.");
                    writer.newLine();
                    writer.append(format(parameters.getFaultEventInstant() + shortCircuitDuration, 8))
                            .append(PADDING_1)
                            .append("CLEARB  ")
                            .append(PADDING_1)
                            .append(esgId)
                            .append("                                                   0.                   0.");
                    writer.newLine();
                }
                break;

                default:
                    throw new AssertionError();
            }
        }

        writer.append(format(parameters.getPostFaultSimulationStopInstant(), 8))
                .append(PADDING_1)
                .append("STOP");
        writer.newLine();
        writer.newLine();

        //debug section, pro wp43 indexes
        writer.append("DEBUG");
        writer.newLine();
        writer.append("        103 1");
        writer.newLine();
        writer.append("         -1");
        writer.newLine();
    }

    public GenericArchive writeFaultSeqArchive(List<Contingency> contingencies, Network network, EurostagDictionary dictionary, Function<Integer, String> seqFileNameFct) throws IOException {
        return writeFaultSeqArchive(ShrinkWrap.createDomain(), contingencies, network, dictionary, seqFileNameFct);
    }

    public GenericArchive writeFaultSeqArchive(Domain domain, List<Contingency> contingencies, Network network, EurostagDictionary dictionary, Function<Integer, String> seqFileNameFct) throws IOException {
        if ((contingencies == null) || (contingencies.isEmpty())) {
            throw new RuntimeException("contingencies list is empty, cannot write .seq scenario files");
        }
        GenericArchive archive = domain.getArchiveFactory().create(GenericArchive.class);
        try (FileSystem fileSystem = ShrinkWrapFileSystems.newFileSystem(archive)) {
            Path rootDir = fileSystem.getPath("/");
            for (int i = 0; i < contingencies.size(); i++) {
                Contingency contingency = contingencies.get(i);
                Path seqFile = rootDir.resolve(seqFileNameFct.apply(i));
                try (BufferedWriter writer = Files.newBufferedWriter(seqFile, StandardCharsets.UTF_8)) {
                    writeFaultSeq(writer, contingency, network, dictionary);
                }
            }
        }
        return archive;
    }

}
