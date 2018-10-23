package cn.lu.mybatis;

import cn.lu.mybatis.entity.User;
import cn.lu.mybatis.mapper.UserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author lu
 * @date 2018/6/1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-mybatis.xml"})
public class SpringMybatisTests {

    @Autowired
    UserMapper userMapper;

    @Test
    public void test() {
        User user = userMapper.queryById(100001L);
        System.out.println(user.getUserName());
    }
}
