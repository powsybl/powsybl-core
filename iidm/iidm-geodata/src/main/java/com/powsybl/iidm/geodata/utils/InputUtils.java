package com.powsybl.iidm.geodata.utils;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Hugo Marcellin {@literal <hugo.marcelin at rte-france.com>}
 */
public final class InputUtils {
    private InputUtils() {
    }

    public static BOMInputStream toBomInputStream(InputStream inputStream) throws IOException {
        return BOMInputStream.builder().setInputStream(inputStream).setByteOrderMarks(ByteOrderMark.UTF_8).get();
    }
}
