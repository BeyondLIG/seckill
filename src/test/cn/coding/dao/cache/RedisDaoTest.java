package cn.coding.dao.cache;

import cn.coding.dao.SeckillDao;
import cn.coding.model.Seckill;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class RedisDaoTest {
    @Autowired
    private RedisDao redisDao;

    @Autowired
    private SeckillDao seckillDao;

    @Test
    public void testRedis() throws Exception {
        long seckillId = 1000;
        Seckill seckill = redisDao.getSeckill(seckillId);
        if (seckill == null){
            seckill = seckillDao.queryById(seckillId);
            if (seckill != null){
                String result = redisDao.putSeckill(seckill);
                System.out.println(result);
                System.out.println(redisDao.getSeckill(seckillId));
            }
        }else {
            System.out.println("seckill="+seckill);
        }
    }


}