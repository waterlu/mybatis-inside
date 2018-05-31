package cn.lu.mybatis.mapper;

import cn.lu.mybatis.entity.User;

/**
 * @author lu
 * @date 2018/5/31
 */
public interface UserMapper {

    User queryById(Long userId);

}
