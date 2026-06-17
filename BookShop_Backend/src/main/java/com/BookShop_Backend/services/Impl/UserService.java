package com.BookShop_Backend.services.Impl;

import com.BookShop_Backend.DTO.User.*;
import com.BookShop_Backend.components.JwtTokenUtil;
import com.BookShop_Backend.components.TokenHashUtil;
import com.BookShop_Backend.converter.MapStruct;
import com.BookShop_Backend.customexceptions.BusinessException;
import com.BookShop_Backend.filters.JwtTokenFilter;
import com.BookShop_Backend.models.RefreshTokenEntity;
import com.BookShop_Backend.models.RoleEntity;
import com.BookShop_Backend.models.UserEntity;
import com.BookShop_Backend.repositories.RefreshTokenRepository;
import com.BookShop_Backend.repositories.UserRepository;
import com.BookShop_Backend.security.SecurityUtils;
import com.BookShop_Backend.services.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtTokenFilter jwtTokenFilter;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHashUtil tokenHashUtil;
    private final MapStruct mapStruct;
    @Autowired
    private AuthenticationManager authenticationManager;


    @Override
    public Map<String, String> login(String phoneNumber, String password) {
        Optional<UserEntity> userEntity = userRepository.findByPhoneNumber(phoneNumber);
        if (userEntity.isEmpty()) {
            throw new BusinessException("LOGIN_FAILED", "Wrong phone number or password", HttpStatus.UNAUTHORIZED);
        }

        UserEntity existingUser = userEntity.get();
        if (existingUser.getFacebookAccountId() == 0 && existingUser.getGoogleAccountId() == 0) {
            if (!passwordEncoder.matches(password, existingUser.getPassword())) {
                throw new BusinessException("LOGIN_FAILED", "Wrong phone number or password", HttpStatus.UNAUTHORIZED);
            }
        }

        try {
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                    new UsernamePasswordAuthenticationToken(phoneNumber, password, existingUser.getAuthorities());
            authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        } catch (BadCredentialsException e) {
            throw new BusinessException("LOGIN_FAILED", "Wrong phone number or password", HttpStatus.UNAUTHORIZED);
        }

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", jwtTokenUtil.generateAccessToken(existingUser));
        tokens.put("refreshToken", jwtTokenUtil.generateRefreshToken(existingUser));

        RefreshTokenEntity refreshToken = jwtTokenUtil.toRefreshTokenEntity(tokens.get("refreshToken"), existingUser);
        refreshToken.setToken(tokenHashUtil.hashToken(tokens.get("refreshToken")));
        refreshTokenRepository.save(refreshToken);
        return tokens;
    }

    @Override
    public String refreshToken(HttpServletRequest request) {
        String refreshToken = jwtTokenFilter.getJwtFromCookie(request);
        String tokenHash = tokenHashUtil.hashToken(refreshToken);
        Optional<RefreshTokenEntity> entity = refreshTokenRepository.findByToken(tokenHash);
        if (entity.isEmpty() || jwtTokenUtil.isTokenExpired(refreshToken)) {
            throw new BusinessException("LOGIN_REQUIRED", "Vui long dang nhap lai", HttpStatus.UNAUTHORIZED);
        }
        RefreshTokenEntity refreshTokenEntity = entity.get();
        UserEntity user = refreshTokenEntity.getUser();
        return jwtTokenUtil.generateAccessToken(user);
    }

    @Override
    public void logout(HttpServletRequest request) {
        try {
            String refreshToken = jwtTokenFilter.getJwtFromCookie(request);
            String hashToken = tokenHashUtil.hashToken(refreshToken);
            refreshTokenRepository.deleteByToken(hashToken);
        } catch (Exception e) {
            throw new BusinessException("LOGOUT_FAILED", "Dang xuat that bai", HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public void register(UserLoginDTO userLoginDTO) {
        Optional<UserEntity> user = userRepository.findByPhoneNumber(userLoginDTO.getPhoneNumber());
        if (!user.isEmpty()) {
            throw new BusinessException("ACCOUNT_EXISTS", "Tai khoan da ton tai", HttpStatus.FORBIDDEN);
        }
        UserEntity userEntity = new UserEntity();
        userEntity.setPhoneNumber(userLoginDTO.getPhoneNumber());
        userEntity.setPassword(passwordEncoder.encode(userLoginDTO.getPassword()));
        RoleEntity role = new RoleEntity();
        role.setId(2L);
        userEntity.setRole(role);
        userRepository.save(userEntity);
    }

    @Override
    public void updateInfo(UpdateInfoDTO info) {
        MyUserDetail user = SecurityUtils.getPrincipal();
        if (user == null) {
            throw new BusinessException(
                    "LOGIN_REQUIRED",
                    "Nguoi dung chua dang nhap hoac phien lam viec het han!",
                    HttpStatus.UNAUTHORIZED
            );
        }

        UserEntity userEntity = userRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Khong tim thay nguoi dung", HttpStatus.NOT_FOUND));
        userEntity.setAddress(info.getAddress());
        userEntity.setFullName(info.getFullName());
        userEntity.setDateOfBirth(info.getDateOfBirth());
        userRepository.save(userEntity);
    }

    @Override
    public void updatePassword(UpdatePasswordDTO updatePasswordDTO) {
        UserEntity user = userRepository.findByPhoneNumber(updatePasswordDTO.getPhoneNumber())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Khong tim thay nguoi dung", HttpStatus.NOT_FOUND));
        if (!passwordEncoder.matches(updatePasswordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException("WRONG_PASSWORD", "Sai mat khau", HttpStatus.UNAUTHORIZED);
        }
        user.setPassword(passwordEncoder.encode(updatePasswordDTO.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public UserInfoResponseDTO getProfile() {
        try {
            MyUserDetail user = SecurityUtils.getPrincipal();
            UserEntity userEntity = userRepository.findById(user.getId())
                    .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Khong tim thay nguoi dung", HttpStatus.NOT_FOUND));
            return mapStruct.toUserInfoResponseDTO(userEntity);
        } catch (Exception e) {
            throw new BusinessException("LOGIN_REQUIRED", "Yeu can dang nhap", HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    public List<UserInfoResponseDTO> getUsers(UserRequestDTO userRequestDTO) {
        List<UserEntity> userEntities = userRepository.searchUsers(userRequestDTO.getAddress(),userRequestDTO.getRole_id());
        return userEntities.stream()
                .map(mapStruct::toUserInfoResponseDTO)
                .toList();
    }
}
