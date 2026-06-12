package com.ecommerce.platform.iam.service;

import java.util.List;

public interface SysPermissionService {

    List<String> getPermsByUserId(Long userId);

    List<String> getRoleCodesByUserId(Long userId);

    boolean hasPerm(Long userId, String perm);

    boolean hasRole(Long userId, String roleCode);

    void refreshUserPermCache(Long userId);
}
