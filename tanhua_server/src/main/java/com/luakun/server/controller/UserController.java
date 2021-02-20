package com.luakun.server.controller;

import com.luakun.domain.db.UserInfo;
import com.luakun.domain.vo.UserInfoVo;
import com.luakun.server.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 个人信息
     * @param userInfoVo
     * @return
     */
    @PostMapping("/loginReginfo")
    public ResponseEntity loginReginfo(@RequestBody UserInfoVo userInfoVo, @RequestHeader("Authorization") String token){
        UserInfo userInfo = new UserInfo();
        // 复制属性
        BeanUtils.copyProperties(userInfoVo,userInfo);
        // 保存用户信息
        userService.saveUserInfo(userInfo,token);
        return ResponseEntity.ok(null);
    }

    /**
     * 上传用户头像
     * @param headPhoto
     * @param token
     * @return
     */
    @PostMapping("loginReginfo/head")
    public ResponseEntity uploadAvatar(MultipartFile headPhoto, @RequestHeader("Authorization") String token){
        userService.updateUserAvatar(headPhoto,token);
        return ResponseEntity.ok(null);
    }
}
