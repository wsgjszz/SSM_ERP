package cn.jazz.dao;

import cn.jazz.domain.Role;
import cn.jazz.domain.UserInfo;
import org.apache.ibatis.annotations.*;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface IUserDao {

    /**
     * 根据用户名查询出对应的用户信息，并封装为UserInfo对象
     * @param username
     * @return
     * @throws Exception
     */
    @Select("select * from users where username=#{username}")
    @Results({
            @Result(id = true, property = "id", column = "id"),
            @Result(column = "username", property = "username"),
            @Result(column = "email", property = "email"),
            @Result(column = "password", property = "password"),
            @Result(column = "phoneNum", property = "phoneNum"),
            @Result(column = "status", property = "status"),
            @Result(column = "id", property = "roles", javaType = List.class,
                    many = @Many(select = "cn.jazz.dao.IRoleDao.findByUserId"))
    })
    public UserInfo findByUsername(String username) throws Exception;

    /**
     * 查询所有用户信息
     * @return
     */
    @Select("select * from users")
    public List<UserInfo> findAll() throws Exception;

    /**
     * 插入一条用户记录
     * @param userInfo
     */
    @Insert("insert into USERS(Email,Username,Password,Phonenum,Status) values(#{email},#{username},#{password},#{phoneNum},#{status})")
    public void save(UserInfo userInfo) throws Exception;

    /**
     * 根据用户ID查询用户的详情(包括角色和权限)
     * @param id
     * @return
     * @throws Exception
     */
    @Select("select * from users where id=#{id}")
    @Results({
            @Result(id = true, property = "id", column = "id"),
            @Result(column = "username", property = "username"),
            @Result(column = "email", property = "email"),
            @Result(column = "password", property = "password"),
            @Result(column = "phoneNum", property = "phoneNum"),
            @Result(column = "status", property = "status"),
            @Result(column = "id", property = "roles", javaType = List.class,
                    many = @Many(select = "cn.jazz.dao.IRoleDao.findRolesByUserId"))
    })
    public UserInfo findById(String id) throws Exception;

    /**
     * 查询用户不具有的角色信息
     * @param userId
     * @return
     * @throws Exception
     */
    @Select("select * from role where id not in (select roleId from users_role where userId=#{userId})")
    public List<Role> findOrtherRoleById(String userId) throws Exception;

    /**
     * 插入一条用户与角色关联信息
     * @param userId
     * @param roleId
     * @throws Exception
     */
    @Select("insert into USERS_ROLE(userId,RoleId) values(#{userId},#{roleId})")
    public void addRoleToUser(@Param("userId") String userId,@Param("roleId") String roleId) throws Exception;
}
