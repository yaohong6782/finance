package com.yh.budgetly.mapper;

import com.yh.budgetly.entity.User;
import com.yh.budgetly.rest.dtos.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User mapUserDTOToUser(UserDTO userDTO);

    @Mapping(target = "password" , ignore = true)
    UserDTO mapUserToUserDTO(User user);

    @Mapping(target = "password" , ignore = true)
    List<UserDTO> mapUserListToUserDTOList(List<User> user);
}
