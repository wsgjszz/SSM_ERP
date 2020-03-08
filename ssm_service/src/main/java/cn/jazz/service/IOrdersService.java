package cn.jazz.service;

import cn.jazz.domain.Orders;

import java.util.List;

public interface IOrdersService {

    public List<Orders> findAll(int page,int size) throws Exception;

    public Orders findDeatil(String id) throws Exception;
}
