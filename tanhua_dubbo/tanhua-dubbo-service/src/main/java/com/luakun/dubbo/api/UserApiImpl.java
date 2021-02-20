package com.luakun.dubbo.api;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.luakun.domain.db.User;
import com.luakun.dubbo.dao.UserDao;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: luakun
 * @Date: 2021/02/03/19:46
 * @Description: 提供者 实现类
 */
@Service
@Transactional
public class UserApiImpl implements UserApi {
    @Autowired
    private UserDao userDao;

    /**
     * 查询用户
     * @param phone
     * @return user
     */
    @Override
    public User findByPhone(String phone) {
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mobile",phone);
        return userDao.selectOne(queryWrapper);

    }

    /**
     * 新增用户
     * @param user
     * @return userId
     */

    @Override
    public Long saveUser(User user) {
        /**
         * 向User内设置新增时间 更新时间
         */
        user.setCreated(new Date());
        user.setUpdated(new Date());
        userDao.insert(user);
        return user.getId();

    }
}
