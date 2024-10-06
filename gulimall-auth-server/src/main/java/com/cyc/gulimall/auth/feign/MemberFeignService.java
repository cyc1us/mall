package com.cyc.gulimall.auth.feign;

import com.cyc.common.utils.R;
import com.cyc.gulimall.auth.vo.SocialUser;
import com.cyc.gulimall.auth.vo.UserLoginVo;
import com.cyc.gulimall.auth.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
public interface MemberFeignService {
    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegistVo userRegistVo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo userLoginVo);

    @PostMapping("/member/member/oauth2/login")
    R oauthlogin(@RequestBody SocialUser socialUser) throws Exception;
}
