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

    private DummyOutputFile dummyOutputFile = new DummyOutputFile();

    private static class FailingOutputFile implements AmplOutputFile {
        @Override
        public String getFileName() {
            return "";
        }

        @Override
        public void read(Path outputPath, StringToIntMapper<AmplSubset> networkAmplMapper) throws IOException {
            throw new IOException("Failed to read file");
        }
    }

    private static class DummyOutputFile implements AmplOutputFile {
        private boolean readingDone = false;

        @Override
        public String getFileName() {
            return "";
        }

        @Override
        public void read(Path outputPath, StringToIntMapper<AmplSubset> networkAmplMapper) {
            readingDone = true;
        }

        public boolean isReadingDone() {
            return readingDone;
        }
    }

    @Override
    public Collection<AmplOutputFile> getOutputParameters() {
        return List.of(new FailingOutputFile(), dummyOutputFile);
    }

    public boolean isDummyReadingDone() {
        return dummyOutputFile.isReadingDone();
    }
}
