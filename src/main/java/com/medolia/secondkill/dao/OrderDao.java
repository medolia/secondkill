package com.medolia.secondkill.dao;

import com.medolia.secondkill.domain.OrderInfo;
import com.medolia.secondkill.domain.SeckillOrder;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderDao {

    @Select("select * from seckill_order where user_id=#{userId} and goods_id=#{goodsId}")
    public SeckillOrder getSeckillOrderByUserIdGoodsId(@Param("userId") long userId, @Param("goodsId") long goodsId);

    @Insert("insert into order_info(user_id, goods_id, goods_name, goods_count, goods_price, order_channel, status, create_date)values("
            + "#{userId}, #{goodsId}, #{goodsName}, #{goodsCount}, #{goodsPrice}, #{orderChannel},#{status},#{createDate} )")
    // 返回插入后新记录对应的 order id
    @SelectKey(keyColumn = "id", keyProperty = "id", resultType = long.class, before = false, statement = "select last_insert_id()")
    public long insert(OrderInfo orderInfo);

    @Insert("insert into seckill_order (user_id, goods_id, order_id)values(#{userId}, #{goodsId}, #{orderId})")
    public int insertSeckillOrder(SeckillOrder seckillOrder);

    @Select("select * from order_info where id = #{orderId}")
    public OrderInfo getOrderById(@Param("orderId") long orderId);

    @Delete("delete from order_info")
    public void deleteOrders();

    @Delete("delete from seckill_order")
    public void deleteSeckillOrders();

    @Select("select * from seckill_order where goods_id=#{goodsId}")
    public List<SeckillOrder> listByGoodsId(@Param("goodsId") long goodsId);
}
