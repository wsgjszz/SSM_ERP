package cn.jazz.dao;

import cn.jazz.domain.Traveller;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ITravellerDao {

    /**
     * 根据订单Id查询对应的游客信息
     * @param id
     * @return
     */
    @Select("select * from TRAVELLER where id in (select travellerId from ORDER_TRAVELLER where orderId=#{id})")
    public List<Traveller> findById(String id) throws Exception;

}
