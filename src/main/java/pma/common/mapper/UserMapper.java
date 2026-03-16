package pma.common.mapper;

import org.mapstruct.Mapper;
import pma.user.dto.response.ResponseUserDto;
import pma.user.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    ResponseUserDto toResponseUserDto(User user);
}
