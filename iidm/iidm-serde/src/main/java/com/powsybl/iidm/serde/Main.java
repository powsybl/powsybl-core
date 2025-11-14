/**
 *  Copyright (c) 2025, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *  SPDX-License-Identifier: MPL-2.0
 *
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.io.TreeDataFormat;
import com.powsybl.iidm.network.Network;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class Main {

    public static final String DATA_FOLDER = "/home/user/Data/";
    public static final String XIIDM_FILE = "network.xiidm";
    public static final Path XIIDM_FILE_PATH = Path.of(DATA_FOLDER, XIIDM_FILE);
    public static final String JIIDM_FILE = "json.jiidm";
    public static final Path JIIDM_FILE_PATH = Path.of(DATA_FOLDER, JIIDM_FILE);
    public static final String BIIDM_FILE = "bin.biidm";
    public static final Path BIIDM_FILE_PATH = Path.of(DATA_FOLDER, BIIDM_FILE);

    public static void main(String[] args) throws IOException {

        Network n0 = Network.read(XIIDM_FILE_PATH);
        Path xmlFileCopy = Path.of(DATA_FOLDER, XIIDM_FILE.replace(".xiidm", "_copy.xiidm"));
        n0.write("BIIDM", new Properties(), BIIDM_FILE_PATH);
        NetworkSerDe.write(n0, new ExportOptions().setFormat(TreeDataFormat.JSON).setIndent(true), JIIDM_FILE_PATH);
        NetworkSerDe.write(n0, new ExportOptions().setFormat(TreeDataFormat.BIN).setIndent(true), BIIDM_FILE_PATH);
        NetworkSerDe.write(n0, new ExportOptions().setFormat(TreeDataFormat.XML).setIndent(true), xmlFileCopy);
        Network n = Network.read(JIIDM_FILE_PATH);

        int nn = 10;
        for (int i = 0; i < 3; i++) {
            NetworkSerDe.read(JIIDM_FILE_PATH, new ImportOptions().setFormat(TreeDataFormat.JSON));
            NetworkSerDe.read(BIIDM_FILE_PATH, new ImportOptions().setFormat(TreeDataFormat.BIN));
            NetworkSerDe.read(xmlFileCopy, new ImportOptions().setFormat(TreeDataFormat.XML));
        }

        long t0 = System.currentTimeMillis();
        ImportOptions optionsJ = new ImportOptions().setFormat(TreeDataFormat.JSON);
        ImportOptions optionsB = new ImportOptions().setFormat(TreeDataFormat.BIN);
        ImportOptions optionsX = new ImportOptions().setFormat(TreeDataFormat.XML);
        for (int i = 0; i <nn; i++) {
            NetworkSerDe.read(JIIDM_FILE_PATH, optionsJ);
        }
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < nn; i++) {
            NetworkSerDe.read(xmlFileCopy, optionsX);
        }
        long t2 = System.currentTimeMillis();
        for (int i = 0; i < nn; i++) {
            NetworkSerDe.read(BIIDM_FILE_PATH, optionsB);
        }
        long t3 = System.currentTimeMillis();

        ExportOptions configX = new ExportOptions().setVersion("1.3");
        ExportOptions configJ = new ExportOptions().setFormat(TreeDataFormat.JSON);
        ExportOptions configB = new ExportOptions().setFormat(TreeDataFormat.BIN);
//        Network n = Network.read(BIIDM_FILE_PATH);

        long t0a = System.currentTimeMillis();
        for (int i = 0; i < nn; i++) {
            NetworkSerDe.write(n, configJ, JIIDM_FILE_PATH);
        }
        long t1a = System.currentTimeMillis();
        for (int i = 0; i < nn; i++) {
            NetworkSerDe.write(n, configX, xmlFileCopy);
        }
        long t2a = System.currentTimeMillis();
        for (int i = 0; i < nn; i++) {
            NetworkSerDe.write(n, configB, BIIDM_FILE_PATH);
        }
        long t3a = System.currentTimeMillis();

        long t0b = System.currentTimeMillis();
        for (int i = 0; i < nn; i++) {
            NetworkSerDe.write(n, configJ.setIndent(false), JIIDM_FILE_PATH);
        }
        long t1b = System.currentTimeMillis();
        for (int i = 0; i < nn; i++) {
            NetworkSerDe.write(n, configX.setIndent(false), xmlFileCopy);
        }
        long t2b = System.currentTimeMillis();
        for (int i = 0; i < nn; i++) {
            NetworkSerDe.write(n, configB.setIndent(false), BIIDM_FILE_PATH);
        }
        long t3b = System.currentTimeMillis();

        if (xmlFileCopy.compareTo(XIIDM_FILE_PATH) != 0) {
            Files.delete(xmlFileCopy);
        }

        System.out.println("JIIDM read: " + (t1 - t0) / nn);
        System.out.println("XIIDM read: " + (t2 - t1) / nn);
        System.out.println("BIIDM read: " + (t3 - t2) / nn);
        System.out.println("With indentation:");
        System.out.println("JIIDM write: " + (t1a - t0a) / nn);
        System.out.println("XIIDM write: " + (t2a - t1a) / nn);
        System.out.println("BIIDM write: " + (t3a - t2a) / nn);
        System.out.println("Without indentation:");
        System.out.println("JIIDM write: " + (t1b - t0b) / nn);
        System.out.println("XIIDM write: " + (t2b - t1b) / nn);
        System.out.println("BIIDM write: " + (t3b - t2b) / nn);
    }
}
