package cn.lu.mybatis;

import cn.lu.mybatis.entity.User;
import cn.lu.mybatis.mapper.UserMapper;
import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * @author lu
 * @date 2018/6/1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-mybatis.xml", "classpath:spring-redis.xml"})
public class SpringMybatisTests {

    private final Logger logger = LoggerFactory.getLogger(SpringMybatisTests.class);

    @Autowired
    UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

//    @Test
    public void testSetData() {
        long userId = 100002L;
        User user = userMapper.queryById(userId);
        String key = "cn:lu:mybatis:entity:User:" + userId;
        String value = JSON.toJSONString(user);
        redisTemplate.opsForValue().set(key, value);
    }


    @Test
    public void testQueryData() {
        List<User> userList = userMapper.queryByStatus(1);
        logger.info(userList.toString());
    }
}
