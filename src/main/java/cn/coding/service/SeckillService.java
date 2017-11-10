package cn.coding.service;

import cn.coding.dto.Exposer;
import cn.coding.dto.SeckillExecution;
import cn.coding.exception.RepeatKillException;
import cn.coding.exception.SeckillCloseException;
import cn.coding.exception.SeckillException;
import cn.coding.model.Seckill;

import java.util.List;

public interface SeckillService {
    /**
     * 查询全部的秒杀记录
     * @return
     */
    List<Seckill> getSeckillList();

    /**
     * 查询某个秒杀记录
     * @param seckillId
     * @return
     */
    Seckill getById(long seckillId);

    /**
     * 在秒杀开始时输出秒杀接口的地址，否则输出系统时间和秒杀时间
     * @param seckillId
     * @return
     */
    Exposer exportSeckillUrl(long seckillId);

    /**
     * 执行秒杀操作，有可能失败，有可能成功，所以要抛出我们允许的异常
     * @param seckillId
     * @param usePhone
     * @param md5
     * @return
     * @throws SeckillException
     * @throws RepeatKillException
     * @throws SeckillCloseException
     */
    SeckillExecution executeSeckill(long seckillId, long usePhone, String md5)
        throws SeckillException,RepeatKillException,SeckillCloseException;

    /**
     * 调用存储过程来执行给秒杀，不需要抛出异常，因为不需要用到Spring的事务
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     */
    SeckillExecution executeSeckillProcedure(long seckillId,long userPhone,String md5);
}
