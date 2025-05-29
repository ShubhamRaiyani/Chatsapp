package com.shubham.chatsapp.dto;

import com.shubham.chatsapp.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private String username;
    private String email;
    // only for the user slide like in search bar and the dashboard

    public UserDTO(User user) {
        this.username = user.getUsername();
        this.email = user.getEmail();
    }

//    public UserDTO(String username, String email) {
//        this.username = username;
//        this.email =
//    }
}
