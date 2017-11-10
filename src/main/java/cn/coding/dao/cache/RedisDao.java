package cn.coding.dao.cache;

import cn.coding.model.Seckill;
import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * redis数据缓存
 */
public class RedisDao {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JedisPool jedisPool;  // redis连接池

    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class); // 定义类序列化和反序列化的规则

    public RedisDao(String ip, int port){
        jedisPool = new JedisPool(ip, port);
    }

    public Seckill getSeckill(long seckillId){// 从redis缓存获取序列化对象
        try {
            Jedis jedis = jedisPool.getResource();
            jedis.auth("workhard");
            try {
                String key = "seckill:" + seckillId;
                byte[] bytes = jedis.get(key.getBytes()); // 获取对象的序列化字节
                if (bytes != null) {
                    Seckill seckill = schema.newMessage();
                    ProtostuffIOUtil.mergeFrom(bytes, seckill, schema); // seckill对象反序列化
                    return seckill;
                }
            }finally {
                jedis.close();  //释放资源
            }
        }catch (Exception e){
            logger.error(e.getMessage());
        }
        return null;
    }

    public String putSeckill(Seckill seckill){// 将对象序列化存储到redis缓存中
        try{
            Jedis jedis = jedisPool.getResource();
            jedis.auth("workhard");
            try{
                String key = "seckill:"+seckill.getSeckillId();
                //seckill 对象序列化
                byte[] bytes = ProtostuffIOUtil.toByteArray(seckill, schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                int timeout = 60 * 60;
                String result = jedis.setex(key.getBytes(), timeout, bytes);
                return result;
            }finally {
                jedis.close();
            }
        }catch (Exception e){
            logger.error(e.getMessage());
        }
        return null;
    }
}
