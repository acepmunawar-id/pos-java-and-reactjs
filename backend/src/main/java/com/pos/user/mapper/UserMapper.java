package com.pos.user.mapper;

import com.pos.user.dto.UserResponse;
import com.pos.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
}
