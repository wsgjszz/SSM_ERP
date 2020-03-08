package cn.jazz.dao;

import cn.jazz.domain.Role;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface IRoleDao {

    /**
     * 根据用户Id查询出用户的角色
     * @param userId
     * @return 用户角色的集合
     */
    @Select("select * from role where id in (select roleId from USERS_ROLE where userId=#{userId})")
    public List<Role> findByUserId(String userId) throws Exception;
}
