package com.powsybl.ampl.executor.output_test;

import com.powsybl.ampl.converter.AmplSubset;
import com.powsybl.ampl.executor.AmplOutputFile;
import com.powsybl.ampl.executor.EmptyAmplParameters;
import com.powsybl.commons.util.StringToIntMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class OutputTestAmplParameters extends EmptyAmplParameters {

    private static class FailingOutputFile implements AmplOutputFile {
        @Override
        public String getFileName() {
            return "";
        }

        @Override
        public void read(Path outputPath, StringToIntMapper<AmplSubset> networkAmplMapper) throws IOException {
            throw new RuntimeException("Failed to read file");
        }
    }

    @Override
    public Collection<AmplOutputFile> getOutputParameters(boolean hasConverged) {
        if (hasConverged) {
            return List.of(new FailingOutputFile());
        }
        return List.of();
    }
}
