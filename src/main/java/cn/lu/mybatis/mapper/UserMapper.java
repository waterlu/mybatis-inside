package cn.lu.mybatis.mapper;

import cn.lu.mybatis.entity.User;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author lu
 * @date 2018/5/31
 */
@Repository
public interface UserMapper {

    User queryById(Long userId);

    List<User> queryByStatus(Integer userStatus);

}