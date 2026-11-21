package com.pos.mapper;

import com.pos.dto.UserResponse;
import com.pos.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
}
