package cn.jazz.service;

import cn.jazz.domain.Role;
import cn.jazz.domain.UserInfo;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface IUserService extends UserDetailsService {
    public List<UserInfo> findAll() throws Exception;

    public void save(UserInfo userInfo) throws Exception;

    public UserInfo findDetails(String id) throws Exception;

    public List<Role> findOrtherRole(String userId) throws Exception;

    public void addRoleToUser(String userId, String[] roleIds) throws Exception;
}
