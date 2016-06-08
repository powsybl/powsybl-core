/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.wca.uncertainties;

import eu.itesla_project.commons.util.StringToIntMapper;
import eu.itesla_project.iidm.datasource.DataSource;
import static eu.itesla_project.iidm.export.ampl.AmplConstants.INVALID_FLOAT_VALUE;
import static eu.itesla_project.iidm.export.ampl.AmplConstants.LOCALE;
import eu.itesla_project.iidm.export.ampl.AmplSubset;
import eu.itesla_project.iidm.export.ampl.util.Column;
import eu.itesla_project.iidm.export.ampl.util.TableFormatter;
import eu.itesla_project.modules.wca.StochasticInjection;
import eu.itesla_project.modules.wca.Uncertainties;
import eu.itesla_project.wca.WCAConstants;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UncertaintiesAmplWriter implements WCAConstants {

    private final Uncertainties uncertainties;

    private final DataSource dataSource;

    private final StringToIntMapper<AmplSubset> mapper;

    public UncertaintiesAmplWriter(Uncertainties uncertainties, DataSource dataSource, StringToIntMapper<AmplSubset> mapper) {
        Objects.requireNonNull(uncertainties);
        Objects.requireNonNull(dataSource);
        Objects.requireNonNull(mapper);
        this.uncertainties = uncertainties;
        this.dataSource = dataSource;
        this.mapper = mapper;
    }

    private int getNum(StochasticInjection inj, StringToIntMapper<AmplSubset> mapper) {
        switch (inj.getType()) {
            case LOAD: return mapper.getInt(AmplSubset.LOAD, inj.getId());
            case GENERATOR: return mapper.getInt(AmplSubset.GENERATOR, inj.getId());
            default: throw new InternalError();
        }
    }

    private void writeReductionMatrix(Uncertainties uncertainties, DataSource dataSource, StringToIntMapper<AmplSubset> mapper) throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream(REDUCTION_MATRIX_FILE_SUFFIX, TXT_EXT, false), StandardCharsets.UTF_8)) {
            TableFormatter formatter = new TableFormatter(LOCALE, writer, "Reduction matrix", INVALID_FLOAT_VALUE,
                    new Column("inj. type"),
                    new Column("inj. num"),
                    new Column("var. num"),
                    new Column("coeff."));
            formatter.writeHeader();
            for (int i =  0; i < uncertainties.reductionMatrix.length; i++) {
                StochasticInjection inj = uncertainties.injections.get(i);
                for (int varNum = 0; varNum < uncertainties.reductionMatrix[i].length; varNum++) {
                    double coeff = uncertainties.reductionMatrix[i][varNum];
                    if (coeff != 0) {
                        formatter.writeCell(inj.getType().toChar())
                                 .writeCell(getNum(inj, mapper))
                                 .writeCell(varNum+1)
                                 .writeCell(coeff)
                                 .newRow();
                    }
                }
            }
        }
    }

    private void writeTrustIntervals(Uncertainties uncertainties, DataSource dataSource) throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream(TRUST_INTERVAL_FILE_SUFFIX, TXT_EXT, false), StandardCharsets.UTF_8)) {
            TableFormatter formatter = new TableFormatter(LOCALE, writer, "Trust intervals", INVALID_FLOAT_VALUE,
                    new Column("var. num"),
                    new Column("min"),
                    new Column("max"));
            formatter.writeHeader();
            for (int varNum = 0; varNum < uncertainties.min.length; varNum++) {
                formatter.writeCell(varNum+1)
                        .writeCell(uncertainties.min[varNum])
                        .writeCell(uncertainties.max[varNum])
                        .newRow();
            }
        }
    }

    private void writeMeans(Uncertainties uncertainties, DataSource dataSource, StringToIntMapper<AmplSubset> mapper) throws IOException {
        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream(MEANS_FILE_SUFFIX, TXT_EXT, false), StandardCharsets.UTF_8)) {
            TableFormatter formatter = new TableFormatter(LOCALE, writer, "Means", INVALID_FLOAT_VALUE,
                    new Column("inj. type"),
                    new Column("inj. num"),
                    new Column("mean"));
            formatter.writeHeader();
            for (int i =  0; i < uncertainties.means.length; i++) {
                StochasticInjection inj = uncertainties.injections.get(i);
                formatter.writeCell(inj.getType().toChar())
                         .writeCell(getNum(inj, mapper))
                         .writeCell(uncertainties.means[i])
                         .newRow();
            }
        }
    }

    public void write() throws IOException {
        writeReductionMatrix(uncertainties, dataSource, mapper);
        writeTrustIntervals(uncertainties, dataSource);
        writeMeans(uncertainties, dataSource, mapper);
    }

}
