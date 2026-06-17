package com.BookShop_Backend.DTO.User;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Date;

@Data //toString
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateInfoDTO {
    @JsonProperty("fullname")
    private String fullName;

    private String address;

    @JsonProperty("date_of_birth")
    private Date dateOfBirth;
}
