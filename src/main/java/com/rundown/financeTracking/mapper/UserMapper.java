package com.rundown.financeTracking.mapper;

import com.rundown.financeTracking.entity.User;
import com.rundown.financeTracking.rest.dtos.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User mapUserDTOToUser(UserDTO userDTO);

    @Mapping(target = "password" , ignore = true)
    UserDTO mapUserToUserDTO(User user);
}
