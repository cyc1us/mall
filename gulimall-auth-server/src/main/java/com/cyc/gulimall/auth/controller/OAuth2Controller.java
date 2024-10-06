package com.cyc.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.cyc.common.utils.HttpUtils;
import com.cyc.common.utils.R;
import com.cyc.gulimall.auth.feign.MemberFeignService;
import com.cyc.common.vo.MemberRespVo;
import com.cyc.gulimall.auth.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;

@Slf4j
@Controller
public class OAuth2Controller {
    @Autowired
    private MemberFeignService memberFeignService;

    @GetMapping("/oauth2.0/gitee/success")
    public String gitee(@RequestParam("code")String code, HttpSession session) throws Exception {

        HashMap<String, String> map = new HashMap<>();
        map.put("client_id","083d54acf6fca2c589696361ae0ba1850842dbf2f1b9a77278feb24a616ec2c7");
        map.put("client_secret","14e6077f27bccd9169ebbcc3ecd22fac9fc1761f3d7ea64aed78613698632255");
        map.put("grant_type","authorization_code");
        map.put("code",code);
        map.put("redirect_uri","http://auth.gulimall.com/oauth2.0/gitee/success");
        //1.根据code换取access_token
        //HttpResponse response = HttpUtils.doPost("https://gitee.com", "/oauth/token", "post", null, null, map);
        HttpResponse response = HttpUtils.doPost("https://gitee.com", "/oauth/token", "post", new HashMap<>(), map, new HashMap<>());
        if (response.getStatusLine().getStatusCode()==200) {
            //获取到了access_token
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);

            R r = memberFeignService.oauthlogin(socialUser);
            if(r.getCode()==0){
                MemberRespVo data = r.getData("data", new TypeReference<MemberRespVo>() {
                });
                log.info("登录成功，用户信息:"+data.toString());
                session.setAttribute("loginUser",data);
                //登陆成功返回首页
                return "redirect://http://gulimall.com";
            }else{
                return "redirect:http://auth.gulimall.com/login.html";
            }
        }else {
            //失败返回登录页
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
}
