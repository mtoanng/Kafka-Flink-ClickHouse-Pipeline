package vn.edu.ves.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import vn.edu.ves.api.config.JwtTokenProvider;
import vn.edu.ves.api.dao.UserDao;
import vn.edu.ves.api.dto.LoginRequest;
import vn.edu.ves.api.dto.LoginResponse;
import vn.edu.ves.api.dto.UserDto;
import vn.edu.ves.api.exception.ApiException;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Authentication & token issuance")
public class AuthController {

    private final UserDao            userDao;
    private final PasswordEncoder    passwordEncoder;
    private final JwtTokenProvider   tokenProvider;

    public AuthController(UserDao userDao,
                          PasswordEncoder passwordEncoder,
                          JwtTokenProvider tokenProvider) {
        this.userDao          = userDao;
        this.passwordEncoder  = passwordEncoder;
        this.tokenProvider    = tokenProvider;
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập, trả về JWT", security = {})  // Phá Bearer requirement cho endpoint này
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        UserDao.UserRecord user = userDao.findByUsername(req.getUsername())
                .orElseThrow(() -> ApiException.unauthorized("Sai username hoặc password"));

        if (!user.enabled) {
            throw ApiException.forbidden("Tài khoản bị vô hiệu hoá");
        }
        if (!passwordEncoder.matches(req.getPassword(), user.passwordHash)) {
            throw ApiException.unauthorized("Sai username hoặc password");
        }

        userDao.touchLastLogin(user.id);
        String token = tokenProvider.generate(user.id, user.username, user.role);

        return new LoginResponse(
                token,
                tokenProvider.getExpirationMs(),
                UserDto.builder()
                        .id(user.id)
                        .username(user.username)
                        .fullName(user.fullName)
                        .email(user.email)
                        .role(user.role)
                        .enabled(user.enabled)
                        .build()
        );
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Thông tin user hiện tại từ JWT")
    public UserDto me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw ApiException.unauthorized("Thiếu hoặc sai JWT");
        }
        Long uid = (Long) auth.getDetails();
        if (uid == null) {
            throw ApiException.unauthorized("Token không có claim uid");
        }
        UserDao.UserRecord u = userDao.findById(uid)
                .orElseThrow(() -> ApiException.unauthorized("User không tồn tại nữa"));
        return UserDto.builder()
                .id(u.id)
                .username(u.username)
                .fullName(u.fullName)
                .email(u.email)
                .role(u.role)
                .enabled(u.enabled)
                .build();
    }
}
