package com.rundown.budgetTracking.mapper;

import com.rundown.budgetTracking.entity.User;
import com.rundown.budgetTracking.rest.dtos.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User mapUserDTOToUser(UserDTO userDTO);

    @Mapping(target = "password" , ignore = true)
    UserDTO mapUserToUserDTO(User user);
}
