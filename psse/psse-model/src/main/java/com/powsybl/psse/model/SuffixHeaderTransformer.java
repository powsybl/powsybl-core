package com.powsybl.psse.model;

import com.univocity.parsers.annotations.HeaderTransformer;

import java.lang.reflect.Field;

public class SuffixHeaderTransformer extends HeaderTransformer {
    private final String suffix;

    public SuffixHeaderTransformer(String... args) {
        suffix = args[0];
    }

    @Override
    public String transformName(Field field, String name) {
        return name + suffix;
    }
}
