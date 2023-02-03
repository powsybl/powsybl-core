package com.powsybl.ampl.executor;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.MapModuleConfig;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.FileSystem;

public class AmplConfigTest {
    @Test
    public void test() {
        FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fs);
        MapModuleConfig moduleConfig = platformConfig.createModuleConfig("ampl");
        moduleConfig.setStringProperty("homeDir", "/home/test/ampl");
        AmplConfig cfg = AmplConfig.getConfig(platformConfig);
        Assert.assertEquals("Error parsing Ampl Home", cfg.getAmplHome(), "/home/test/ampl");
    }
}
