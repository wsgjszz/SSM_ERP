package cn.jazz.service.impl;

import cn.jazz.dao.ITravellerDao;
import cn.jazz.domain.Traveller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TestService {

    @Autowired
    private ITravellerDao travellerDao;

    public List<Traveller> findById(String id) throws Exception {
        System.out.println("id值为："+id);
        return travellerDao.findById(id);
    }

}
