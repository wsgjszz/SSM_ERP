package cn.jazz.dao;

import cn.jazz.domain.Permission;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface IPermissionDao {

    /**
     * 根据用户id查询对应的权限信息
     * @param roleId
     * @return
     */
    @Select("select * from permission where id in (select permissionId from role_permission where roleId=#{roleId})")
    public List<Permission> findByRoleId(String roleId);
}
