package com.powsybl.ampl.converter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface OutputFileFormat {
    /**
     * @return regex string that will be used to separate tokens
     */
    String getTokenSeparator();

    String getFileExtension();

    Charset getFileEncoding();

    /**
     * Token separator : "( )+"
     * <p>
     * File extension : txt
     * <p>
     * Encoding : UTF-8
     * <p>
     */
    static OutputFileFormat getDefault() {
        return new OutputFileFormat() {

            @Override
            public String getTokenSeparator() {
                return "( )+";
            }

            @Override
            public String getFileExtension() {
                return "txt";
            }

            @Override
            public Charset getFileEncoding() {
                return StandardCharsets.UTF_8;
            }

        };
    }

}
