package cn.lu.mybatis;

import cn.lu.mybatis.entity.User;
import cn.lu.mybatis.mapper.UserMapper;
import com.alibaba.fastjson.JSON;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author lu
 * @date 2018/5/31
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class MybatisTests {

    private Logger logger = LoggerFactory.getLogger(MybatisTests.class);

    private SqlSessionFactory sqlSessionFactory;

    @Test
    public void testSetData() {
        SqlSession sqlSession = null;
        try {
            sqlSession = getSqlSessionFactory().openSession();
            UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != sqlSession) {
                sqlSession.close();
            }
        }
    }

    @Test
    public void testMybatisConfig() {
        SqlSession sqlSession = null;
        try {
            sqlSession = getSqlSessionFactory().openSession();
            // sqlSession中配置了数据库链接，同时也配置了mapper的XML文件
            // 每个XML文件中，mapper的namespace就是它的key，通常这个key指向Mapper接口
            // 这样getMapper()相当于根据Mapper的类名获取XML文件的内容，就得到了需要执行的SQL语句
            User user = null;

            // 第一种方法，SqlSession的Configuration中，Map的Key有全称和简称两种，"queryById"和"cn.lu.mybatis.mapper.UserMapper.queryById"都缓存了
//            user = sqlSession.selectOne("queryById", 100001L);

//            // 第二种方法，MappedStatement ms = this.configuration.getMappedStatement(statement);
//            user = sqlSession.selectOne("cn.lu.mybatis.mapper.UserMapper.queryById", 100001L);

//            // 常用方法
            UserMapper userMapper = sqlSession.getMapper(UserMapper.class);


//            user = userMapper.queryById(100001L);

            List<User> userList = userMapper.queryByStatus(1);

            assert userList.size() == 3;

//            userMapper = sqlSession.getMapper(UserMapper.class);
//            user = userMapper.queryById(100002L);

//            logger.info(user.getUserName());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != sqlSession) {
                sqlSession.close();
            }
        }
    }

    private SqlSessionFactory getSqlSessionFactory() {
        InputStream inputStream = null;
        try {
            if (null != sqlSessionFactory) {
                return sqlSessionFactory;
            }
            inputStream = Resources.getResourceAsStream("mybatis-config.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sqlSessionFactory;
    }

    @Test
    public void testSpringMybatisConfig() {

    }
}
