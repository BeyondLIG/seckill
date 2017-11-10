package cn.coding.service;

import cn.coding.dto.Exposer;
import cn.coding.dto.SeckillExecution;
import cn.coding.exception.RepeatKillException;
import cn.coding.exception.SeckillCloseException;
import cn.coding.model.Seckill;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
//告诉junit spring配置文件在哪
@ContextConfiguration({"classpath:spring/spring-dao.xml",
                       "classpath:spring/spring-service.xml"})
public class SeckillServiceTest {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillList() throws Exception {
        List<Seckill> seckills = seckillService.getSeckillList();
        System.out.println(seckills);
    }

    @Test
    public void getById() throws Exception {
        Seckill seckill = seckillService.getById(1000);
        System.out.println(seckill);
    }

    @Test
    public void exportSeckillUrl() throws Exception {
        Exposer exposer = seckillService.exportSeckillUrl(1000);
        System.out.println(exposer);
    }

    @Test
    public void executeSeckill() throws Exception {
        long userPhone = 15871682513L;
        String md5 = "699d36e9d75790b2c1d8f0ab5502e779";

        try {
            SeckillExecution seckillExecution = seckillService.executeSeckill(1000, userPhone, md5);
            System.out.println(seckillExecution);
        }catch (RepeatKillException e){
            System.out.println(e.getMessage());
        }catch (SeckillCloseException e){
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void testSeckill() throws Exception{
        Exposer exposer = seckillService.exportSeckillUrl(1000);
        if (exposer.isExposed()){
            String md5 = exposer.getMd5();
            long userPhone = 15871682518L;

            try{
                SeckillExecution seckillExecution = seckillService.executeSeckill(1000, userPhone, md5);
                System.out.println(seckillExecution);
            }catch (RepeatKillException e){
                System.out.println(e.getMessage());
            }catch (SeckillCloseException e){
                System.out.println(e.getMessage());
            }
        }else {
            System.out.println(exposer);
        }
    }

    @Test
    public void executeSeckillProcedure(){
        long seckillId= 1000;
        long phone = 13680115110L;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        if (exposer.isExposed()){
            String md5 = exposer.getMd5();
            SeckillExecution execution = seckillService.executeSeckillProcedure(seckillId, phone, md5);
            System.out.println(execution);
        }
    }

}