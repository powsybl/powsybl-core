/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.powsybl.contingency.contingency.list.ContingencyList;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * @author Sylvain Leclerc <sylvain.leclerc@rte-france.com>
 */
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 10)
@Measurement(iterations = 100)
@Fork(5)
@State(Scope.Thread)
public class ParsingBenchmark {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParsingBenchmark.class);

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new ContingencyJsonModule());
    private static final ObjectReader READER = MAPPER.readerFor(ContingencyList.class);
    private static final ObjectWriter WRITER = MAPPER.writerWithDefaultPrettyPrinter();

    /**
     * Replace this parameter with your test file path
     */
    //@Param({"/home/sylvlecl/feature_tests/test-framework/srj-ij/result.json"})
    @Param({"/home/leclercsyl/tmp/branch-contingencies.json"})
    private String contingencyListPath;

    private byte[] fileContent;

    private ContingencyList contingencyList;

    @Setup
    public void read() throws IOException {
        fileContent = Files.readAllBytes(Path.of(contingencyListPath));
        try (InputStream inputStream = Files.newInputStream(Path.of(contingencyListPath))) {
            contingencyList = READER.readValue(inputStream);
        }
    }

    @Benchmark
    public void parsing(Blackhole blackhole) throws IOException {
        ContingencyList list;
        try (InputStream inputStream = Files.newInputStream(Path.of(contingencyListPath))) {
            list = READER.readValue(inputStream);
        }
        LOGGER.info("Found list of type {}.", list.getClass().getSimpleName());
    }

    @Benchmark
    public void parsingFromBytes(Blackhole blackhole) throws IOException {
        ContingencyList list = READER.readValue(fileContent);
        LOGGER.info("Found list of type {}.", list.getClass().getSimpleName());
    }

    @Benchmark
    public void justReading(Blackhole blackhole) throws IOException {
        byte[] list = Files.readAllBytes(Path.of(contingencyListPath));
        LOGGER.info("Found list of size {}.", list.length);
    }

    @Benchmark
    public void readingToString(Blackhole blackhole) throws IOException {
        String list = Files.readString(Path.of(contingencyListPath));
        LOGGER.info("Found list of size {}.", list.length());
    }

    @Benchmark
    public void writing(Blackhole blackhole) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(Path.of("/tmp/trash"))) {
            WRITER.writeValue(outputStream, contingencyList);
        }
    }

    @Benchmark
    public void bufferedWriting(Blackhole blackhole) throws IOException {
        try (Writer writer = Files.newBufferedWriter(Path.of("/tmp/trash"))) {
            WRITER.writeValue(writer, contingencyList);
        }
    }

    public static void main(String[] args) throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(ParsingBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}
