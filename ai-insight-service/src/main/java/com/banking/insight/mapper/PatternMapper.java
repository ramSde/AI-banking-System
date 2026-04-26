package com.banking.insight.mapper;

import com.banking.insight.domain.SpendingPattern;
import com.banking.insight.dto.SpendingPatternResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PatternMapper {

    @Mapping(target = "patternType", expression = "java(pattern.getPatternType().name())")
    @Mapping(target = "frequency", expression = "java(pattern.getFrequency().name())")
    @Mapping(target = "season", expression = "java(pattern.getSeason() != null ? pattern.getSeason().name() : null)")
    @Mapping(target = "trend", expression = "java(pattern.getTrend() != null ? pattern.getTrend().name() : null)")
    SpendingPatternResponse toResponse(SpendingPattern pattern);

    List<SpendingPatternResponse> toResponseList(List<SpendingPattern> patterns);
}
