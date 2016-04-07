/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.eurostag;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TG {

    private static final Logger LOGGER = LoggerFactory.getLogger(TG.class);

    final String fileName;

    final F f1;

    final F f2;

    final T4X t4x;

    final LH lh;

    TG(String fileName, F f1, F f2, T4X t4x, LH lh) {
        this.fileName = fileName;
        this.f1 = f1;
        this.f2 = f2;
        this.t4x = t4x;
        this.lh = lh;
    }

    void print() {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("{}", "\n"
                    +fileName + "\n"
                    + "\t" + f1 + "\n"
                    + "\t" + f2 + "\n"
                    + "\t" + t4x
                    + (lh != null ? "\n" + "\t" + lh: ""));
        }
    }

    static TG parse(Path path) throws IOException {
        String fileNameWithExt = path.getFileName().toString();
        try (BufferedReader reader = Files.newBufferedReader(path, Charset.forName("iso-8859-1"))) {
            return parse(fileNameWithExt.substring(0, fileNameWithExt.length()-3), reader);
        }
    }

    public static TG parse(String fileName, BufferedReader reader) throws IOException {
        List<F> fs = new ArrayList<>(2);
        T4X t4x = null;
        LH lh = null;
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("F") && !line.substring(3, 11).trim().isEmpty()) {
                fs.add(F.parse(line));
            } else if (line.startsWith("45") || line.startsWith("48")) {
                String line2 = reader.readLine();
                List<String> lines3andNext = new ArrayList<>();
                String line3andNext;
                while ((line3andNext = reader.readLine()) != null) {
                    if (!line3andNext.startsWith("45") && !line3andNext.startsWith("48")) {
                        break;
                    }
                    lines3andNext.add(line3andNext);
                }
                t4x = T4X.parse(line, line2, lines3andNext);
            } else if (line.startsWith("LH")) {
                lh = LH.parse(line);
            }
        }
        if (fs.size() != 2) {
            throw new RuntimeException(".tg file should contain two F records");
        }
        if (t4x == null) {
            throw new RuntimeException(".tg file should contain one 45 or 48 record");
        }
        F f1;
        F f2;
        if (fs.get(0).name.equals(t4x.name1)) {
            f1 = fs.get(0);
        } else if (fs.get(1).name.equals(t4x.name1)) {
            f1 = fs.get(1);
        } else {
            throw new RuntimeException("F record for bus " + t4x.name1 + " not found");
        }
        if (fs.get(0).name.equals(t4x.name2)) {
            f2 = fs.get(0);
        } else if (fs.get(1).name.equals(t4x.name2)) {
            f2 = fs.get(1);
        } else {
            throw new RuntimeException("F record for bus " + t4x.name2 + " not found");
        }
        return new TG(fileName, f1, f2, t4x, lh);
    }

}
