package cn.jazz.dao;

import cn.jazz.domain.Member;
import org.apache.ibatis.annotations.Select;

public interface IMemberDao {

    /**
     * 根据会员ID查询会员信息
     * @param id
     * @return
     */
    @Select("select * from MEMBER where id=#{id}")
    public Member findById(String id) throws Exception;
}
