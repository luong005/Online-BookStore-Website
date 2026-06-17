package com.BookShop_Backend.services;

import com.BookShop_Backend.DTO.User.*;
import com.BookShop_Backend.models.UserEntity;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

public interface IUserService {

    Map<String,String> login(String phoneNumber, String password);

    String refreshToken(HttpServletRequest request) ;

    void logout(HttpServletRequest request);

    void register(UserLoginDTO userLoginDTO);

    void updateInfo(UpdateInfoDTO info);

    void updatePassword(UpdatePasswordDTO updatePasswordDTO);

    UserInfoResponseDTO getProfile();

    List<UserInfoResponseDTO> getUsers(UserRequestDTO userRequestDTO);
}
