package com.ecommerce.platform.iam.controller;

import com.ecommerce.platform.common.result.Result;
import com.ecommerce.platform.iam.dto.LoginDTO;
import com.ecommerce.platform.iam.service.AuthService;
import com.ecommerce.platform.iam.vo.LoginVO;
import com.ecommerce.platform.iam.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }

    @Operation(summary = "登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    @Operation(summary = "获取用户信息")
    @GetMapping("/userInfo")
    public Result<UserInfoVO> getUserInfo() {
        return Result.success(authService.getUserInfo());
    }

    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public Result<LoginVO> refreshToken(@RequestParam String refreshToken) {
        return Result.success(authService.refreshToken(refreshToken));
    }
}
