package com.lacak.backend.assessment.helpers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.lacak.backend.assessment.enums.ScoreTypeEnum;
import com.lacak.backend.assessment.models.dtos.suggestion.SuggestionRequestDto;
import com.lacak.backend.assessment.models.entities.suggestion.Suggestion;
import com.lacak.backend.assessment.utils.GeoDistanceUtils;
import com.lacak.backend.assessment.utils.StringMatchingUtils;

import lombok.Getter;

/**
 * Helper class to calculate the score for a suggestion based on string matching and geographical distance.
 * Computes a weighted score by combining:
 * - A textual match score using Levenshtein distance
 * - A geographical distance score using the Haversine formula
 * 
 */

@Getter
@Component
public class SuggestionScoringHelper {
    private final double matchingWeight;
    private final double geoDistanceWeight;

    public SuggestionScoringHelper(
            @Value("${scoring.matchingWeight}") double matchingWeight,
            @Value("${scoring.geoDistanceWeight}") double geoDistanceWeight) {
        this.matchingWeight = matchingWeight;
        this.geoDistanceWeight = geoDistanceWeight;
    }

    /**
     * Calculates a final score for a suggestion based on textual and geographical relevance.
     * 
     * @param requestDto the request dto
     * @param entity the suggestion entity
     * @return the final calculated score based on both string matching and geographical proximity
     */

    public double calculateScore(SuggestionRequestDto requestDto, Suggestion entity) {

        ScoreTypeEnum scoreTypeEnum = ScoreTypeEnum.NORMALIZED;

        double normalizedMatchingDistance = StringMatchingUtils.calculateLevenshteinDistance(requestDto.getQ(),
                entity.getName(), scoreTypeEnum);

        double matchingScore = this.matchingWeight * normalizedMatchingDistance;
        double geoDistanceScore = 0.0;

        if (requestDto.getLatitude() != null && requestDto.getLongitude() != null) {
            double normalizedGeoDistance = GeoDistanceUtils.calculateHaversineDistance(
                    requestDto.getLatitude(), requestDto.getLongitude(), entity.getLatitude(), entity.getLongitude(),
                    scoreTypeEnum);

            geoDistanceScore = this.geoDistanceWeight * normalizedGeoDistance;
        }

        return matchingScore + geoDistanceScore;
    }

}
