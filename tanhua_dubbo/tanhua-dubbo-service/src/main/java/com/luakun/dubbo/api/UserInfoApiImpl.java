package com.luakun.dubbo.api;

import com.luakun.domain.db.UserInfo;
import com.luakun.dubbo.dao.UserInfoDao;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;


@Service
public class UserInfoApiImpl implements UserInfoApi {

    @Autowired
    private UserInfoDao userInfoMapper;

    /**
     * 保存用户基本信息
     * @param userInfo
     */
    @Override
    public void save(UserInfo userInfo) {
        userInfoMapper.insert(userInfo);
    }

    /**
     * 通过id更新用户基本信息
     * @param userInfo
     */
    @Override
    public void update(UserInfo userInfo) {
        userInfoMapper.updateById(userInfo);
    }
}