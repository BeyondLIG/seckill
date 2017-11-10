package cn.coding.dao;

import cn.coding.model.SuccessKilled;
import org.apache.ibatis.annotations.Param;

public interface SuccessKilledDao { // SuccessKilled实体类的dao层接口
    /**
     * 插入购买明细，可过滤重复
     * @param seckillId
     * @param userPhone
     * @return 插入的行数
     */
    int insertSuccessKilled(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);

    /**
     * 根据秒杀商品id查询明细SuccessKilled对象
     * @param seckillId
     * @param userPhone
     * @return
     */
    SuccessKilled queryByIdWithSeckill(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);
}
