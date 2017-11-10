package cn.coding.exception;

/**
 * 秒杀关闭异常，当秒杀关闭后，用户继续秒杀会抛出这个错误
 */
public class SeckillCloseException extends SeckillException {
    public SeckillCloseException(String messaeg){
        super(messaeg);
    }

    public SeckillCloseException(String message, Throwable cause){
        super(message, cause);
    }
}
