package vn.edu.ves.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserDto {
    private long   id;
    private String username;
    private String fullName;
    private String email;
    private String role;
    private boolean enabled;
}
