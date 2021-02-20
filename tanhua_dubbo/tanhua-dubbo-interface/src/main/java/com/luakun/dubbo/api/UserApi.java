package com.luakun.dubbo.api;


import com.luakun.domain.db.User;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: luakun
 * @Date: 2021/02/03/19:39
 * @Description:提供者接口
 */
public interface UserApi {
    /**
     * 根据手机号码 查询 用户对象
     */

    User findByPhone(String phone);


    /**
     * 保存用户
     */
    Long saveUser(User user);
}
