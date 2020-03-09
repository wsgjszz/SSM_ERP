package cn.jazz.service;

import cn.jazz.domain.Permission;
import cn.jazz.domain.Role;

import java.util.List;

public interface IRoleService {

    public List<Role> findAll() throws Exception;

    public void save(Role role) throws Exception;

    public Role findDetail(String roleId) throws Exception;

    public List<Permission> findOrtherPermission(String roleId) throws Exception;

    public void addPermissionToRole(String roleId, String[] permissionIds) throws Exception;
}
