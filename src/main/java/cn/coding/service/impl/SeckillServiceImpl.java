package cn.coding.service.impl;

import cn.coding.dao.SeckillDao;
import cn.coding.dao.SuccessKilledDao;
import cn.coding.dao.cache.RedisDao;
import cn.coding.dto.Exposer;
import cn.coding.dto.SeckillExecution;
import cn.coding.enums.SeckillStatEnum;
import cn.coding.exception.RepeatKillException;
import cn.coding.exception.SeckillCloseException;
import cn.coding.exception.SeckillException;
import cn.coding.model.Seckill;
import cn.coding.model.SuccessKilled;
import cn.coding.service.SeckillService;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeckillServiceImpl implements SeckillService{
    //日志对象
    private Logger logger = LoggerFactory.getLogger(SeckillServiceImpl.class);

    private final String salt = "123456";

    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    @Autowired
    private RedisDao redisDao;

    @Override
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0 ,4);
    }

    @Override
    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    @Override
    public Exposer exportSeckillUrl(long seckillId) { // 暴露秒杀地址
        //redis 缓存
        //1.访问redis
        Seckill seckill = redisDao.getSeckill(seckillId);
        if (seckill == null){
            //2. 访问数据库
            seckill = seckillDao.queryById(seckillId);
            if (seckill == null) {// 说明查询不到这个秒杀产品的记录
                return new Exposer(false, seckillId);
            }else {
                //3.放入redis
                redisDao.putSeckill(seckill);
            }
        }
        //秒杀未开启(没开始或已过)
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        Date nowTime = new Date();
        if (startTime.getTime()>nowTime.getTime() || endTime.getTime()<nowTime.getTime()){
            return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }

        //秒杀开启,返回秒杀商品的id、用给接口加密的md5
        String md5 = getMD5(seckillId);
        return new Exposer(true, md5, seckillId);

    }

    private String getMD5(long seckillId){
        String base = seckillId + "/" + salt;
        return DigestUtils.md5DigestAsHex(base.getBytes());
    }

    @Transactional // 秒杀是否成功，成功：减库存，增加明细；失败：抛出异常，事务回滚
    /**
     * 在需要进行实物声明的方法上加上事务注解
     * 怒视所有的方法都需要事务，如：只有一条修改操作、只读操作不需要事务声明
     */
    public SeckillExecution executeSeckill(long seckillId, long usePhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException {
        if (md5==null || !md5.equals(getMD5(seckillId))){
            throw new SeckillException("seckill data rewrite!"); //秒杀数据被重写了
        }

        //执行秒杀逻辑：减库存，增加明细
        Date nowTime = new Date();

        try{
            //减库存
            int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
            if(updateCount<=0){
                //没有更新库存记录，说明秒杀结束
                throw new SeckillCloseException("seckill is closed");
            }else {
                //否则更新了库存，秒杀成功，增加明细
                int insertCount = successKilledDao.insertSuccessKilled(seckillId, usePhone);
                if (insertCount<=0){
                    //重复秒杀
                    throw new RepeatKillException("seckill repeated");
                }else {
                    //秒杀成功，返回成功秒杀的信息
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, usePhone);
                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
                }
            }
        }catch (SeckillCloseException e){
            throw e;
        }catch (RepeatKillException e){
            throw e;
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            throw new SeckillException("seckill inner error :"+e.getMessage());
        }
    }

    /**
     * 调用存储过程来执行秒杀操作，不需要抛出异常
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     */
    public SeckillExecution executeSeckillProcedure(long seckillId,long userPhone,String md5){
        if (md5 == null || !md5.equals(getMD5(seckillId))){
            return new SeckillExecution(seckillId,SeckillStatEnum.DATA_REWRITE);
        }

        Date killTime = new Date();
        Map<String, Object> paramMap= new HashMap<String, Object>();
        paramMap.put("seckillId", seckillId);
        paramMap.put("phone", userPhone);
        paramMap.put("killTime", killTime);
        paramMap.put("result", null);
        //执行存储过程，result被赋值
        seckillDao.killByProcedure(paramMap);
        //获取result
        int result = MapUtils.getInteger(paramMap, "result", -2);
        if (result == 1){//执行秒杀成功
            SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
            return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
        }else {
            return new SeckillExecution(seckillId, SeckillStatEnum.stateof(result));
        }
    }
}
