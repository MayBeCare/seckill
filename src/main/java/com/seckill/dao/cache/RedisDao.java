package com.seckill.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Created by asus on 2017/10/20.
 */
public class RedisDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JedisPool jedisPool;

    public RedisDao(String ip, int port) {
        jedisPool = new JedisPool(ip, port);
    }

    // protostuff 序列化
    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);


    public Seckill getSeckill(long seckillId) {
        //redis操作逻辑
        try {
            Jedis jedis = jedisPool.getResource();
            try {
                String key = "seckill:" + seckillId;
                //并没有实现内部序列化操作
                // get ----> byte[] ----> 反序列化 ----> Object(Seckill)
                //采用自定义序列化
                //protostuff: pojo.
                byte[] bytes = jedis.get(key.getBytes());
                //缓存重获取到
                if (bytes != null) {
                    //空对象
                    Seckill seckill=schema.newMessage();

                    ProtostuffIOUtil.mergeFrom(bytes,seckill,schema);

                    //seckill被反序列化(已被赋值)
                    return seckill;
                }
            }finally {
                jedis.close();
            }
        }catch (Exception e) {
           logger.info(e.getMessage(),e);
        }
        return null;
    }

    /*
     * 如果没有，则放入
     */
    public String putSeckill(Seckill seckill) {
        // Object ----->  序列化 -------> bytes[]
        try {
            Jedis jedis = jedisPool.getResource();
            try {
                String key = "seckill:" + seckill.getSeckillId();

                byte[] bytes = ProtostuffIOUtil.toByteArray(seckill, schema,
                                            LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));

                //超时缓存
                int timeout = 60 * 60;//1小时
                String result = jedis.setex(key.getBytes(),timeout,bytes);

                return result;
            }finally {
                jedis.close();
            }
        }catch (Exception e) {
            logger.info(e.getMessage(),e);
        }

        return null;
    }
}
