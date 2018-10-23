package cn.lu.mybatis.plugin;

import cn.lu.mybatis.annotation.LedgerDataId;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@Intercepts(value = {
        @Signature(type= Executor.class,
                method="update",
                args={MappedStatement.class,Object.class}),
        @Signature(type=Executor.class,
                method="query",
                args={MappedStatement.class,Object.class, RowBounds.class, ResultHandler.class,
                        CacheKey.class, BoundSql.class}),
        @Signature(type=Executor.class,
                method="query",
                args={MappedStatement.class,Object.class,RowBounds.class,ResultHandler.class})})
/**
 * MyBatis拦截器
 * <p>拦截SQL语句的执行<p/>
 *
 * @author lutiehua
 * @date 2018-10-23
 */
public class BlockChainInterceptor implements Interceptor {

    private final Logger logger = LoggerFactory.getLogger(BlockChainInterceptor.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 读取原参数
        final Executor executor = (Executor) invocation.getTarget();
        final Object[] args = invocation.getArgs();
        final MappedStatement ms = (MappedStatement) args[0];
        final Object parameter = args[1];
        final RowBounds rowBounds = (RowBounds) args[2];

        // 读取原SQL语句
        Configuration configuration = ms.getConfiguration();
        StatementHandler handler = configuration.newStatementHandler(executor, ms,
                parameter, rowBounds, null, null);
        BoundSql boundSql = handler.getBoundSql();
        logger.info(boundSql.getSql());

        // 解析原MappedStatement的属性
        StringBuffer buffer = null;

        String keyProperties = null;
        if (null != ms.getKeyProperties()) {
            buffer = new StringBuffer();
            for (String keyProperty : ms.getKeyProperties()) {
                if (buffer.length() > 0) {
                    buffer.append(",");
                }
                buffer.append(keyProperty);
            }
            keyProperties = buffer.toString();
        }

        String keyColumns = null;
        if (null != ms.getKeyColumns()) {
            buffer = new StringBuffer();
            for (String keyColumn : ms.getKeyColumns()) {
                if (buffer.length() > 0) {
                    buffer.append(",");
                }
                buffer.append(keyColumn);
            }
            keyColumns = buffer.toString();
        }

        String resultSets = null;
        if (null != ms.getResultSets()) {
            buffer = new StringBuffer();
            for (String resultSet : ms.getResultSets()) {
                if (buffer.length() > 0) {
                    buffer.append(",");
                }
                buffer.append(resultSet);
            }
            resultSets = buffer.toString();
        }

        // 新的ID增加后缀
        String id = ms.getId();
        id += "-fabric";

        // 拼接新的SQL语句
        String sql = "select user_id from user WHERE user_status = #{userStatus}";
        RawSqlSource sqlSource  = new RawSqlSource(configuration, sql, parameter.getClass());

        // 解析原有返回值类型
        List<ResultMap> resultMapList = ms.getResultMaps();
        ResultSetType resultSetType = ms.getResultSetType();

        // 构造新的SQL语句
        MappedStatement.Builder builder = new MappedStatement.Builder(configuration, id, sqlSource, SqlCommandType.SELECT);
        builder = builder.resource(ms.getResource()).parameterMap(ms.getParameterMap()).resultMaps(resultMapList)
                .fetchSize(ms.getFetchSize()).timeout(ms.getTimeout()).statementType(ms.getStatementType())
                .resultSetType(resultSetType).cache(ms.getCache()).flushCacheRequired(ms.isFlushCacheRequired())
                .useCache(ms.isUseCache()).resultOrdered(ms.isResultOrdered()).keyGenerator(ms.getKeyGenerator())
                .databaseId(ms.getDatabaseId()).lang(ms.getLang());

        if (null != keyProperties) {
            builder = builder.keyProperty(keyProperties);
        }

        if (null != keyColumns) {
            builder = builder.keyColumn(keyColumns);
        }

        if (null != resultSets) {
            builder = builder.resultSets(resultSets);
        }

        // 执行新的SQL语句，参数保持不变
        MappedStatement statement = builder.build();
        args[0] = statement;
        Object result = invocation.proceed();

        // 处理SQL查询结果
        ResultMap resultMap = resultMapList.get(0);
        Class resultClass = resultMap.getType();

        String jsonString = JSON.toJSONString(result);
        List list = JSON.parseArray(jsonString, resultClass);

        List resultList = new ArrayList();

        for (Object object : list) {
            // 解析DATA_ID属性
            String dataId = null;
            String ledegerClassName = null;
            Class clazz = Class.forName(resultClass.getName());
            Field[] fieldList = clazz.getDeclaredFields();
            for (int i=0; i<fieldList.length; i++) {
                Field field = fieldList[i];
                // 设置可访问非公有成员变量
                field.setAccessible(true);
                Object value = field.get(object);
                // 跳过未赋值的成员变量
                if (null == value) {
                    continue;
                }
                // 检查成员变量是否声明了@LedgerDataId注解
                if (field.isAnnotationPresent(LedgerDataId.class)) {
                    LedgerDataId annotation = field.getAnnotation(LedgerDataId.class);
                    dataId = value.toString();
                    ledegerClassName = annotation.className();
                    break;
                }
            }

            if (null != dataId) {
                ValueOperations<String, String> redisClient = redisTemplate.opsForValue();

                // 拼接key
                String key = resultClass.getName();
                key = key.replace(".", ":");
                key += ":";
                key += dataId;

                // 读取json内容
                String value = redisClient.get(key);

                // 解析json内容
                Map<String, String> chainData = JSON.parseObject(value, new TypeReference<HashMap<String, String>>() {});
                String str = JSON.toJSONString(object);
                Map<String, String> localData = JSON.parseObject(str, new TypeReference<HashMap<String, String>>() {});

                Map<String, String> unionData = new HashMap<>();
                unionData.putAll(localData);
                unionData.putAll(chainData);

                String unionJsonString = JSON.toJSONString(unionData);
                Object resultObject = JSON.parseObject(unionJsonString, resultClass);
                resultList.add(resultObject);
            }
        }

        return resultList;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
