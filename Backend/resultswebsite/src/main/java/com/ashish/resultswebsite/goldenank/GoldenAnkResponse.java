package com.ashish.resultswebsite.goldenank;

import java.time.Instant;
import java.time.LocalDate;

public record GoldenAnkResponse(
        LocalDate resultDate,
        String ankOne,
        String ankTwo,
        String ankThree,
        String ankFour,
        String goldenAnk,
        Instant updatedAt
) {

    public static GoldenAnkResponse from(GoldenAnkResult result) {
        String goldenAnk = String.join(
                "-",
                result.getAnkOne(),
                result.getAnkTwo(),
                result.getAnkThree(),
                result.getAnkFour()
        );
        return new GoldenAnkResponse(
                result.getResultDate(),
                result.getAnkOne(),
                result.getAnkTwo(),
                result.getAnkThree(),
                result.getAnkFour(),
                goldenAnk,
                result.getUpdatedAt()
        );
    }
}
