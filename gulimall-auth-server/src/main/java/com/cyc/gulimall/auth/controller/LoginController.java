package com.cyc.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.cyc.common.constant.AuthServerConstant;
import com.cyc.common.utils.R;
import com.cyc.gulimall.auth.feign.MemberFeignService;
import com.cyc.gulimall.auth.feign.ThirdPartyFeignService;
import com.cyc.gulimall.auth.vo.UserLoginVo;
import com.cyc.gulimall.auth.vo.UserRegistVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.YamlJsonParser;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import springfox.documentation.schema.Model;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Controller
public class LoginController {


    @Autowired
    private MemberFeignService memberFeignService;
    @Autowired
    private ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;
    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone){
        String code = "9988";
        //redis缓存验证码，防止同一个phone在60s内再次发送验证码

        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX+phone,code,5, TimeUnit.MINUTES);
        thirdPartyFeignService.sendCode(phone,code);
        return R.ok();
    }


    //TODO 重定向携带数据，利用session，解决分布式session问题
    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo userRegistVo, BindingResult result,
                         RedirectAttributes redirectAttributes){
        if(result.hasErrors()){
            Map<String,String> errors = result.getFieldErrors().stream().
                    collect(Collectors.toMap(FieldError::getField,FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }

        //注册成功
        //1.校验验证码
        String code = userRegistVo.getCode();
        String s = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegistVo.getPhone());
        if(StringUtils.isNotEmpty(s)){
            if(code.equals(s)){
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegistVo.getPhone());
                R r = memberFeignService.regist(userRegistVo);
                if(r.getCode() == 0){
                    //成功
                    return "redirect:http://auth.gulimall.com/login.html";
                }else {
                    //失败
                    Map<String,String> errors = new HashMap<>();
                    errors.put("msg",r.getData("msg",new TypeReference<String>(){}));
                    redirectAttributes.addFlashAttribute("errors",errors);
                    return "redirect:http://auth.gulimall.com/reg.html";
                }
            }else {
                //验证码错误
                Map<String,String> errors = new HashMap<>();
                errors.put("code","验证码错误，请重新输入");
                redirectAttributes.addFlashAttribute("errors",errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        }else{
            //redis中没有验证码，直接返回页面
            Map<String,String> errors = new HashMap<>();
            errors.put("code","验证码已失效，请尝试重新发送");
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVo userLoginVo,RedirectAttributes redirectAttributes){
        //远程登陆
        R r = memberFeignService.login(userLoginVo);
        if(r.getCode()==0){
            return "redirect://http://gulimall.com";
        }else {
            Map<String,String> errors = new HashMap<>();
            errors.put("msg",r.getData("msg",new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }

    }
}
