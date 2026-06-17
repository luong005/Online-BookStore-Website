package com.BookShop_Backend.DTO.User;

import lombok.*;

import java.util.List;

@Data //toString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponeDTO {
     private List<UserInfoResponseDTO> userInfoResponseDTOS;
     private Long numberOfUsers;
}
