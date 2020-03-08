package cn.jazz.dao;

import cn.jazz.domain.Role;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
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

    /**
     * 根据用户Id查询出用户的角色和对应的权限信息
     * @param userId
     * @return 用户角色的集合
     */
    @Select("select * from role where id in (select roleId from USERS_ROLE where userId=#{userId})")
    @Results({
            @Result(id=true,column="id",property="id"),
            @Result(column="roleName",property="roleName"),
            @Result(column="roleDesc",property="roleDesc"),
            @Result(column="id",property="permissions",javaType=List.class,
                    many=@Many(select="cn.jazz.dao.IPermissionDao.findByRoleId"))
    })
    public List<Role> findRolesByUserId(String userId) throws Exception;
}
