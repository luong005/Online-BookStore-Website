package com.BookShop_Backend.DTO.User;

import lombok.*;

@Data //toString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequestDTO {
    private String address;
    private Long role_id;
}
