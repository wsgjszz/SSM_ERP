package cn.jazz.dao;

import cn.jazz.domain.Permission;
import cn.jazz.domain.Role;
import org.apache.ibatis.annotations.*;

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

    /**
     * 查询所有角色信息
     * @return
     * @throws Exception
     */
    @Select("select * from role")
    public List<Role> findAll() throws Exception;

    /**
     * 插入一条角色记录
     * @param role
     * @throws Exception
     */
    @Insert("insert into role(rolename,roledesc) values(#{roleName},#{roleDesc})")
    public void save(Role role) throws Exception;

    /**
     * 根据角色ID查询对应角色信息
     * @param roleId
     * @return
     */
    @Select("select * from role where id=#{id}")
    public Role findById(String roleId);

    /**
     * 根据角色ID查询可添加的权限信息
     * @param roleId
     * @return
     * @throws Exception
     */
    @Select("select * from permission where id not in (select permissionId from role_permission where roleId=#{roleId})")
    public List<Permission> findOrtherPermissionById(String roleId) throws Exception;

    /**
     * 插入一条角色关联权限记录
     * @param roleId
     * @param permissionId
     * @throws Exception
     */
    @Select("insert into role_permission(permissionId,roleId) values(#{permissionId},#{roleId})")
    public void addPermissionToRole(@Param("roleId") String roleId,@Param("permissionId") String permissionId) throws Exception;
}
