package cn.jazz.service.impl;

import cn.jazz.dao.IOrdersDao;
import cn.jazz.domain.Orders;
import cn.jazz.service.IOrdersService;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OrdersServiceImpl implements IOrdersService {

    @Autowired
    private IOrdersDao ordersDao;

    @Override
    public List<Orders> findAll(int page,int size) throws Exception {
        //使用PageHelper插件，必须在查询语句的前一行
        PageHelper.startPage(page,size);
        return ordersDao.findAll();
    }

    @Override
    public Orders findDeatil(String id) throws Exception {
        return ordersDao.findById(id);
    }
}
