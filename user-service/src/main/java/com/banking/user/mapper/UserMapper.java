package com.banking.user.mapper;

import com.banking.user.domain.User;
import com.banking.user.dto.UserResponse;
import com.banking.user.util.MaskingUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDate;

/**
 * MapStruct mapper for User entity to DTO conversions.
 * Handles PII masking in responses.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "dateOfBirth", source = "dateOfBirth", qualifiedByName = "maskDateOfBirth")
    UserResponse toResponse(User user);

    @Named("maskDateOfBirth")
    default String maskDateOfBirth(LocalDate dateOfBirth) {
        return MaskingUtil.maskDateOfBirth(dateOfBirth);
    }
}
