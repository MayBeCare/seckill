----秒杀存储过程
DELITIMER $$  -- ;  转化为 $$
-- 创建存储过程
-- in 输入参数  ； out 输出参数
-- row_count 返回上一条修改类型sql的影响行数
-- row_count 0：未修改数据   >0:表示修改行数  <0:sql错误/未执行修改sql
CREATE PROCEDURE 'seckill'.'execute_seckill'
    (in v_seckill_id bigint, int v_phone bigint,
       in v_kill_time TIMESTAMP , out v_result int)
    BEGIN
      DECLARE insert_count int DEFAULT 0;
      START TRANSACTION ;
      insert ignore into success_killed
        (seckill_id,user_phone,create_time)
       values(v_seckill_id,v_phone,v_kill_time);
      select row_count() into insert_count;
      IF (insert_count = 0) THEN
          ROLLBACK ;
          set v_result = -1;
      ELSEIF (insert_count < 0) THEN
          ROLLBACK ;
          set v_result = -2;
      ELSE
        UPDATE
              seckill
          SET
             number = number - 1
          WHERE seckill_id = v_seckill_id
          AND start_time < v_kill_time
          AND end_time > v_kill_time
          AND number > 0;
        select row_count() into insert_count;
        IF (insert_count = 0) THEN
          ROLLBACK ;
          set v_result = -1;
        ELSEIF (insert_count < 0) THEN
            ROLLBACK ;
            set v_result = -2;
        ELSE
          COMMIT ;
          set v_result = 1;
        END IF;
      END IF;
    END;
$$
-- 存储过程定义结束


-- 存储过程优化：事务行级锁持有时间
-- 不要依赖存储过程
-- 简单的逻辑可以使用








