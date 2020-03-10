package cn.jazz.service.impl;

import cn.jazz.dao.IRoleDao;
import cn.jazz.domain.Permission;
import cn.jazz.domain.Role;
import cn.jazz.service.IRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RoleServiceImpl implements IRoleService {

    @Autowired
    private IRoleDao roleDao;

    @Override
    public List<Role> findAll() throws Exception {
        return roleDao.findAll();
    }

    @Override
    public void save(Role role) throws Exception {
        roleDao.save(role);
    }

    @Override
    public Role findDetail(String roleId) throws Exception {
        return roleDao.findById(roleId);
    }

    /**
     * 查询当前角色不具有的权限信息
     * @param roleId
     * @return
     * @throws Exception
     */
    @Override
    public List<Permission> findOrtherPermission(String roleId) throws Exception {
        return roleDao.findOrtherPermissionById(roleId);
    }

    /**
     * 给角色添加一个或多个权限
     * @param roleId
     * @param permissionIds
     * @throws Exception
     */
    @Override
    public void addPermissionToRole(String roleId, String[] permissionIds) throws Exception {
        for (String permissionId : permissionIds) {
            roleDao.addPermissionToRole(roleId,permissionId);
        }
    }
}
