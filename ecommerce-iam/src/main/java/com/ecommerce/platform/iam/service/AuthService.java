package com.ecommerce.platform.iam.service;

import com.ecommerce.platform.iam.dto.LoginDTO;
import com.ecommerce.platform.iam.vo.LoginVO;
import com.ecommerce.platform.iam.vo.UserInfoVO;

public interface AuthService {

    LoginVO login(LoginDTO loginDTO);

    void logout();

    UserInfoVO getUserInfo();

    LoginVO refreshToken(String refreshToken);
}
