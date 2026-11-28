package com.ashish.resultswebsite.goldenank;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record GoldenAnkRequest(
        @NotNull LocalDate resultDate,
        @NotBlank @Pattern(regexp = "\\d") String ankOne,
        @NotBlank @Pattern(regexp = "\\d") String ankTwo,
        @NotBlank @Pattern(regexp = "\\d") String ankThree,
        @NotBlank @Pattern(regexp = "\\d") String ankFour
) {
}
