package com.powsybl.ampl.executor;

public abstract class AbstractMandatoryOutputFile implements AmplOutputFile {
    @Override
    public boolean throwOnMissingFile() {
        return true;
    }
}
