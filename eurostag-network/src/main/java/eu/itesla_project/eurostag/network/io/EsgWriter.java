/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.eurostag.network.io;

import eu.itesla_project.eurostag.network.*;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EsgWriter {

    private final EsgNetwork network;

    private final EsgGeneralParameters parameters;

    public EsgWriter(EsgNetwork network, EsgGeneralParameters parameters) {
        this.network = Objects.requireNonNull(network);
        this.parameters = Objects.requireNonNull(parameters);
    }

    private void writeHeader(RecordWriter recordWriter) throws IOException {
        recordWriter.addValue("HEADER", 1, 6);
        recordWriter.addValue(parameters.getEditDate().toString("dd/MM/yy"), 12, 19);
        recordWriter.addValue(EsgNetwork.VERSION, 21, 28);
        recordWriter.newLine();
        recordWriter.newLine();
        recordWriter.addValue("B", 1, 1);
        recordWriter.newLine();
        recordWriter.newLine();
    }

    private static char toChar(EsgGeneralParameters.StartMode mode) {
        switch (mode) {
            case FLAT_START: return ' ';
            case WARM_START: return '1';
            default: throw new AssertionError();
        }
    }

    private static char toChar(boolean b) {
        return b ? ' ' : '1';
    }

    private void writeGeneralParameters(RecordWriter recordWriter) throws IOException {
        recordWriter.addValue("9", 1);
        recordWriter.addValue("1", 3); //...print-out of data in the ".lf" file.
        recordWriter.addValue("0", 5); //...no interrupt after reading and processing data
        recordWriter.addValue("0", 7); //...node optimal classification
        recordWriter.addValue("1", 9); //...print-out of results in the ".lf" file
        recordWriter.addValue("1", 11);//...aving of results in file (.sav)
        if (parameters.getMaxNumIteration() > 99) {
            throw new RuntimeException("max number of iteration has to be < 100");
        }
        recordWriter.addValue(parameters.getMaxNumIteration(), 13, 14); //...20
        recordWriter.addValue(parameters.getTolerance(), 16, 23);       //...0.005
        recordWriter.addValue("4", 57);
        recordWriter.addValue(toChar(parameters.getStartMode()), 65);       //...1 = warmStart and 0 = Flat Start
        recordWriter.addValue(parameters.getSnref(), 67, 74);           //...100. Mva
        recordWriter.addValue(toChar(parameters.isTransformerVoltageControl()), 76); //...1 = No
        recordWriter.addValue(toChar(parameters.isSvcVoltageControl()), 78);           //...
        recordWriter.newLine();
        recordWriter.newLine();
    }

    private static void writeGeneralComment(RecordWriter recordWriter, String comment) throws IOException {
        recordWriter.addValue("GC", 1, 2);
        recordWriter.addValue(comment != null ? comment : "", 4, 80);
        recordWriter.newLine();
        recordWriter.newLine();
    }

    private static void writeArea(EsgArea area, RecordWriter recordWriter) throws IOException {
        String typecard;
        switch (area.getType()) {
            case AC:
                typecard = "AA";
                break;
            case DC:
                typecard = "DA";
                break;
            default:
                throw new InternalError();
        }
        recordWriter.addValue(typecard, 1, 2);
        recordWriter.addValue(area.getName().toString(), 4, 5);
        recordWriter.addValue("", 6, 20);
        recordWriter.newLine();
    }

    private static void writeNode(EsgNode node, RecordWriter recordWriter) throws IOException {
        recordWriter.addValue("1", 1);
        recordWriter.addValue(node.getArea().toString(), 2, 3);
        recordWriter.addValue(node.getName().toString(), 4, 11);
        recordWriter.addValue(node.getVbase(), 85, 92);
        recordWriter.addValue(node.getVinit(), 99, 106);
        recordWriter.addValue(node.getVangl(), 108, 115);
        recordWriter.addValue("0.", 117, 124, RecordWriter.Justification.Right);
        recordWriter.addValue("0.", 126, 133, RecordWriter.Justification.Right);
        recordWriter.newLine();
        if (node.isSlackBus()) {
            recordWriter.addValue("5", 1);
            recordWriter.addValue(node.getName().toString(), 4, 11);
            recordWriter.addValue("0.", 40, 47, RecordWriter.Justification.Right);
            recordWriter.newLine();
        }
    }

    private static char toChar(EsgBranchConnectionStatus status) {
        switch (status) {
            case CLOSED_AT_BOTH_SIDE: return ' ';
            case OPEN_AT_BOTH_SIDES: return '-';
            case OPEN_AT_RECEIVING_SIDE: return '<';
            case OPEN_AT_SENDING_SIDE: return '>';
            default: throw new InternalError();
        }
    }

    private static void writeLine(EsgLine line, RecordWriter recordWriter) throws IOException {
        recordWriter.addValue("3", 1);
        recordWriter.addValue(line.getName().getNode1Name().toString(), 3, 10);
        recordWriter.addValue(toChar(line.getStatus()), 11);
        recordWriter.addValue(line.getName().getNode2Name().toString(), 12, 19);
        recordWriter.addValue(line.getName().getXpp(), 20);
        recordWriter.addValue(line.getRb(), 22, 29);
        recordWriter.addValue(line.getRxb(), 31, 38);
        recordWriter.addValue(line.getGs(), 40, 47);
        recordWriter.addValue(line.getBs(), 49, 56);
        recordWriter.addValue(line.getRate(), 58, 65);
        recordWriter.addValue(0.f, 67, 74);   //...Free numeric attribute 1
        recordWriter.addValue(0.f, 76, 83);   //...Free numeric attribute 2
        recordWriter.newLine();
    }

    private static char toChar(EsgCouplingDevice.ConnectionStatus code) {
        switch (code) {
            case OPEN: return '-';
            case CLOSED: return ' ';
            default: throw new InternalError();
        }
    }

    private static void writeCouplingDevice(EsgCouplingDevice couplingDevice, RecordWriter recordWriter) throws IOException {
        recordWriter.addValue("6", 1);
        recordWriter.addValue(couplingDevice.getName().getNode1Name().toString(), 3, 10);
        recordWriter.addValue(toChar(couplingDevice.getConnectionStatus()), 11);
        recordWriter.addValue(couplingDevice.getName().getNode2Name().toString(), 12, 19);
        recordWriter.addValue(couplingDevice.getName().getXpp(), 20);
        recordWriter.addValue(0.f, 67, 74);   //...Free numeric attribute 1
        recordWriter.addValue(0.f, 76, 83);   //...Free numeric attribute 2
        recordWriter.newLine();
    }

    private static void writeDissymmetricalBranch(EsgDissymmetricalBranch dissymmetricalBranch, RecordWriter recordWriter) throws IOException {
        recordWriter.addValue("P", 1);
        recordWriter.addValue(dissymmetricalBranch.getName().getNode1Name().toString(), 3, 10);
        recordWriter.addValue(toChar(dissymmetricalBranch.getStatus()), 11);
        recordWriter.addValue(dissymmetricalBranch.getName().getNode2Name().toString(), 12, 19);
        recordWriter.addValue(dissymmetricalBranch.getName().getXpp(), 20);
        recordWriter.addValue(dissymmetricalBranch.getRb(), 22, 29);
        recordWriter.addValue(dissymmetricalBranch.getRxb(), 31, 38);
        recordWriter.addValue(dissymmetricalBranch.getGs(), 40, 47);
        recordWriter.addValue(dissymmetricalBranch.getBs(), 49, 56);
        recordWriter.addValue(dissymmetricalBranch.getRate(), 58, 65);
        recordWriter.newLine();

        //...second line record
        recordWriter.addValue("P", 1);
        recordWriter.addValue(dissymmetricalBranch.getRb2(), 22, 29);
        recordWriter.addValue(dissymmetricalBranch.getRxb2(), 31, 38);
        recordWriter.addValue(dissymmetricalBranch.getGs2(), 40, 47);
        recordWriter.addValue(dissymmetricalBranch.getBs2(), 49, 56);
        recordWriter.newLine();
    }

    private static char toChar(EsgDetailedTwoWindingTransformer.RegulatingMode mode) {
        switch (mode) {
            case ACTIVE_FLUX_SIDE_1: return '1';
            case ACTIVE_FLUX_SIDE_2: return '2';
            case NOT_REGULATING: return 'N';
            case VOLTAGE: return 'V';
            default: throw new InternalError();
        }
    }

    private static void writeDetailedTwoWindingTransformer(EsgDetailedTwoWindingTransformer transformer, RecordWriter recordWriter) throws IOException {
        recordWriter.addValue("48", 1, 2);
        recordWriter.addValue(transformer.getName().getNode1Name().toString(), 3, 10);
        recordWriter.addValue(toChar(transformer.getStatus()), 11);
        recordWriter.addValue(transformer.getName().getNode2Name().toString(), 12, 19);
        recordWriter.addValue(transformer.getName().getXpp(), 20);
        recordWriter.addValue(transformer.getRate(), 22, 29);
        recordWriter.addValue(transformer.getPcu(), 30, 37);
        recordWriter.addValue(transformer.getPfer(), 39, 46);
        recordWriter.addValue(transformer.getCmagn(), 48, 55);
        recordWriter.addValue(transformer.getEsat(), 57, 64);
        recordWriter.addValue(0.f, 66, 73);   //...Free numeric attribute 1
        recordWriter.addValue(0.f, 75, 82);   //...Free numeric attribute 2
        recordWriter.newLine();

        // second line record
        recordWriter.addValue("48", 1, 2);
        recordWriter.addValue(transformer.getKtpnom(), 22,25);
        recordWriter.addValue(transformer.getKtap8(), 27,30);
        recordWriter.addValue(transformer.getZbusr() != null ? transformer.getZbusr().toString() : "", 32, 39);
        recordWriter.addValue(transformer.getVoltr(), 41,48);
        recordWriter.addValue(transformer.getPregmin(), 50, 57);
        recordWriter.addValue(transformer.getPregmax(), 59, 66);
        recordWriter.addValue(toChar(transformer.getXregtr()), 68);
        recordWriter.newLine();

        // tap records
        for(EsgDetailedTwoWindingTransformer.Tap tap : transformer.getTaps()) {
            recordWriter.addValue("48", 1, 2);
            recordWriter.addValue(tap.getIplo(), 22, 25);
            recordWriter.addValue(tap.getUno1(), 27, 34);
            recordWriter.addValue(tap.getUno2(), 36, 43);
            recordWriter.addValue(tap.getUcc(), 45, 52);
            recordWriter.addValue(tap.getDephas(), 54, 61);
            recordWriter.newLine();
        }
    }

    private static char toChar(EsgConnectionStatus status) {
        switch (status) {
            case CONNECTED: return 'Y';
            case NOT_CONNECTED: return 'N';
            default: throw new InternalError();
        }
    }

    private static void writeLoad(EsgLoad load, RecordWriter recordWriter) throws IOException {
        recordWriter.addValue("CH", 1, 2);
        recordWriter.addValue(load.getZnamlo().toString(), 4, 11);
        recordWriter.addValue(toChar(load.getIloadst()), 13);
        recordWriter.addValue(load.getZnodlo().toString(), 15, 22);
        recordWriter.addValue(load.getPldstz(), 24, 31);
        recordWriter.addValue(load.getPldsti() , 33, 40);
        recordWriter.addValue(load.getPldstp(), 42, 49);
        recordWriter.addValue(load.getQldsti(), 51, 58);
        recordWriter.addValue(load.getQldstz(), 60, 67);
        recordWriter.addValue(load.getQldstp(), 69, 76);
        recordWriter.addValue(0.f, 78, 85);       //...Free numeric attribute 1
        recordWriter.addValue(0.f, 87, 94);       //...Free numeric attribute 2
        recordWriter.newLine();
    }

    private static char toChar(EsgRegulatingMode mode) {
        switch (mode) {
            case REGULATING: return 'V';
            case NOT_REGULATING: return 'N';
            default: throw new InternalError();
        }
    }

    private static void writeGenerator(EsgGenerator generator, RecordWriter recordWriter) throws IOException {
        recordWriter.addValue("G", 1);
        recordWriter.addValue(generator.getZnamge().toString(), 4, 11);
        recordWriter.addValue(toChar(generator.getXgenest()), 13);
        recordWriter.addValue(generator.getZnodge().toString(), 15, 22);
        recordWriter.addValue(generator.getPgmin(), 24, 31);
        recordWriter.addValue(generator.getPgen() , 33, 40);
        recordWriter.addValue(generator.getPgmax(), 42, 49);
        recordWriter.addValue(generator.getQgmin(), 51, 58);
        recordWriter.addValue(generator.getQgen(), 60, 67);
        recordWriter.addValue(generator.getQgmax(), 69, 76);
        recordWriter.addValue(toChar(generator.getXregge()), 78);
        recordWriter.addValue(generator.getVregge(), 80, 87);
        recordWriter.addValue(generator.getZregnoge() != null ? generator.getZregnoge().toString() : "", 89, 96);
        recordWriter.addValue(generator.getQgensh(), 98, 105);
        recordWriter.addValue(0.f, 107, 114);       //...Free numeric attribute 1
        recordWriter.addValue(0.f, 116, 123);       //...Free numeric attribute 2
        recordWriter.newLine();
    }

    private static char toChar(EsgCapacitorOrReactorBank.RegulatingMode mode) {
        switch (mode) {
            case NOT_REGULATING: return 'N';
            default: throw new InternalError();
        }
    }

    private static void writeCapacitorOrReactorBank(EsgCapacitorOrReactorBank bank, RecordWriter recordWriter) throws IOException {
        recordWriter.addValue('C', 1);
        recordWriter.addValue(bank.getZnamba().toString(), 3, 10);
        recordWriter.addValue(bank.getZnodba().toString(), 12, 19);
        recordWriter.addValue(bank.getIeleba(), 39, 41);
        recordWriter.addValue(bank.getPlosba(), 43, 50);
        recordWriter.addValue(bank.getRcapba(), 52, 59);
        recordWriter.addValue(bank.getImaxba(), 61, 63);
        recordWriter.addValue(toChar(bank.getXregba()), 65, 65);

        recordWriter.addValue(0.f, 67, 74);       //...Unused
        recordWriter.addValue(" ", 76, 83);       //...Unused
        recordWriter.addValue(0.f, 85, 92);       //...Free numeric attribute 1
        recordWriter.addValue(0.f, 94, 101);      //...Free numeric attribute 2
        recordWriter.addValue(" ", 103, 110);      //...Free alphanumeric attribute
        recordWriter.newLine();
    }

    private static void writeStaticVarCompensator(EsgStaticVarCompensator svc, RecordWriter recordWriter) throws IOException {
        recordWriter.addValue("SV", 1, 2);
        recordWriter.addValue(svc.getZnamsvc().toString(), 4, 11);
        recordWriter.addValue(toChar(svc.getXsvcst()), 13, 13);
        recordWriter.addValue(svc.getZnodsvc().toString(), 15, 22);
        recordWriter.addValue(svc.getBmin(), 24, 31);
        recordWriter.addValue(svc.getBinit(), 33, 40);
        recordWriter.addValue(svc.getBmax(), 42, 49);
        recordWriter.addValue(toChar(svc.getXregsvc()), 78, 78);
        recordWriter.addValue(svc.getVregsvc(), 80, 87);
        recordWriter.addValue(svc.getQsvcsh(), 98, 105);

        recordWriter.addValue(0.f, 107, 114); //...Free numeric attribute 1
        recordWriter.addValue(0.f, 116, 123); //...Free numeric attribute 2
        recordWriter.addValue(" ", 125, 132); //...Free alphanumeric attribute
        recordWriter.newLine();
    }

    public void write(Writer writer) throws IOException {
        write(writer, null);
    }

    public void write(Writer writer, String comment) throws IOException {

        network.checkConsistency();

        RecordWriter recordWriter = new RecordWriter(writer);

        writeHeader(recordWriter);
        writeGeneralParameters(recordWriter);
        writeGeneralComment(recordWriter, comment);

        if (network.getAreas().size() > 0) {
            for (EsgArea area : network.getAreas()) {
                writeArea(area, recordWriter);
            }
            recordWriter.newLine();
        }

        if (network.getNodes().size() > 0) {
            for (EsgNode node : network.getNodes()) {
                writeNode(node, recordWriter);
            }
            recordWriter.newLine();
        }

        if (network.getCouplingDevices().size() > 0) {
            for (EsgCouplingDevice couplingDevice : network.getCouplingDevices()) {
                writeCouplingDevice(couplingDevice, recordWriter);
            }
            recordWriter.newLine();
        }

        if (network.getLines().size() > 0) {
            for (EsgLine line : network.getLines()) {
                writeLine(line, recordWriter);
            }
            recordWriter.newLine();
        }

        if (network.getDissymmetricalBranches().size() > 0) {
            for (EsgDissymmetricalBranch branch : network.getDissymmetricalBranches()) {
                writeDissymmetricalBranch(branch, recordWriter);
            }
            recordWriter.newLine();
        }

        if (network.getDetailedTwoWindingTransformers().size() > 0) {
            for (EsgDetailedTwoWindingTransformer transformer : network.getDetailedTwoWindingTransformers()) {
                writeDetailedTwoWindingTransformer(transformer, recordWriter);
            }
            recordWriter.newLine();
        }

        if (network.getLoads().size() > 0) {
            for (EsgLoad load : network.getLoads()) {
                writeLoad(load, recordWriter);
            }
            recordWriter.newLine();
        }

        if (network.getGenerators().size() > 0) {
            for (EsgGenerator generator : network.getGenerators()) {
                writeGenerator(generator, recordWriter);
            }
            recordWriter.newLine();
        }

        if (network.getCapacitorOrReactorBanks().size() > 0) {
            for (EsgCapacitorOrReactorBank bank : network.getCapacitorOrReactorBanks()) {
                writeCapacitorOrReactorBank(bank, recordWriter);
            }
            recordWriter.newLine();
        }

        if (network.getStaticVarCompensators().size() > 0) {
            for (EsgStaticVarCompensator svc : network.getStaticVarCompensators()) {
                writeStaticVarCompensator(svc, recordWriter);
            }
            recordWriter.newLine();
        }
    }

}
