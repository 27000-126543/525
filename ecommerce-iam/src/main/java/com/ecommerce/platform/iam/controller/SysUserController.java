package com.ecommerce.platform.iam.controller;

import com.ecommerce.platform.common.page.PageQuery;
import com.ecommerce.platform.common.page.PageResult;
import com.ecommerce.platform.common.result.Result;
import com.ecommerce.platform.iam.entity.SysUser;
import com.ecommerce.platform.iam.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @Operation(summary = "分页查询用户")
    @GetMapping("/page")
    public Result<PageResult<SysUser>> page(PageQuery pageQuery, SysUser user) {
        return Result.success(sysUserService.page(pageQuery, user));
    }

    @Operation(summary = "根据ID获取用户")
    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        return Result.success(sysUserService.getById(id));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    public Result<Void> add(@RequestBody SysUser user, @RequestParam(required = false) List<Long> roleIds) {
        sysUserService.add(user, roleIds);
        return Result.success();
    }

    @Operation(summary = "修改用户")
    @PutMapping
    public Result<Void> update(@RequestBody SysUser user, @RequestParam(required = false) List<Long> roleIds) {
        sysUserService.update(user, roleIds);
        return Result.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.delete(id);
        return Result.success();
    }

    @Operation(summary = "修改用户状态")
    @PutMapping("/status")
    public Result<Void> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        sysUserService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "重置密码")
    @PutMapping("/resetPassword")
    public Result<Void> resetPassword(@RequestParam Long id, @RequestParam String newPassword) {
        sysUserService.resetPassword(id, newPassword);
        return Result.success();
    }
}
