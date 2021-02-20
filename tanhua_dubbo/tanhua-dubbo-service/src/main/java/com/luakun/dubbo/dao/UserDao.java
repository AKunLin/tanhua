package com.luakun.dubbo.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.luakun.domain.db.User;
import org.springframework.stereotype.Repository;


/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: luakun
 * @Date: 2021/02/03/19:50
 * @Description: dao操作数据库 / mybatiis-plus
 */
@Repository
public interface UserDao extends BaseMapper<User> {
}
