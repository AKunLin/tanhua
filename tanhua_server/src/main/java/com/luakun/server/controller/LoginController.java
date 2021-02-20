package com.luakun.server.controller;

import com.luakun.domain.db.User;
import com.luakun.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: luakun
 * @Date: 2021/02/03/20:24
 * @Description: 消费者业务层
 */
@RestController
@RequestMapping("/user")
public class LoginController {
    @Autowired
    private UserService userService;
    /**
     * 根据手机号码 查询 用户对象
     * ResponseEntity(主要包含 状态码 返回内容)
     */
    @RequestMapping(value = "/findUser",method = RequestMethod.GET)
    public ResponseEntity findUser(String phone){
        return userService.findByPhone(phone);
    }

    /**
     * 保存用户
     * @param params
     * @return
     */
    @RequestMapping(value = "/saveUser",method = RequestMethod.POST)
    public ResponseEntity saveUser(Map<String,Object> params){
        String phone = (String) params.get("mobile");
        String password = (String) params.get("password");
        return userService.saveUser(phone,password);
    }
/**
 * 注册登录第一步
 * 发送验证码
 */
@RequestMapping(value = "/login",method = RequestMethod.POST)
public ResponseEntity login(@RequestBody Map<String,String> param){
    String phone = param.get("phone");
    userService.sendValidateCode(phone);
    return ResponseEntity.ok(null);
}

/**
 * 注册登录第一步
 * 验证校验码(登录)
 */

@RequestMapping(value = "/loginVerification",method = RequestMethod.POST)
    public ResponseEntity loginVerification(@RequestBody Map<String,String> param){
        String phone = param.get("phone");
        String verificationCode = param.get("verificationCode");
        Map<String, Object> map = userService.loginVerification(phone,verificationCode);
        return ResponseEntity.ok(map);
}

}
