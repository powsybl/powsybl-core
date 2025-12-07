package com.powsybl.loadflow.validation.data;

public record Validated<T>(T data, boolean validated) {
}
