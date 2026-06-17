//package com.BookShop_Backend.DTO.User;
//
//import com.fasterxml.jackson.annotation.JsonProperty;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.NotNull;
//import jakarta.validation.constraints.Size;
//import lombok.*;
//
//import java.util.Date;
//
//@Data //toString
//@Setter
//@Getter
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//public class UserDTO {
//    //id
//    @JsonProperty("fullname")
//    private String fullName;
//
//    @JsonProperty("phone_number")
//    @NotBlank(message = "Phone number is required")
//    private String phoneNumber;
//
//    private String address;
//
//    @JsonProperty("date_of_birth")
//    private Date dateOfBirth;
//
//    @JsonProperty("facebook_account_id")
//    private int facebookAccountId;
//
//    @JsonProperty("google_account_id")
//    private int googleAccountId;
//
//    @NotNull(message = "Role id is required")
//    @JsonProperty("role_id")
//    private Long roleId;
//}
