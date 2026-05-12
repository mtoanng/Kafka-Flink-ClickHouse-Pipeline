package vn.edu.ves.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private long   expiresInMs;
    private UserDto user;
}
