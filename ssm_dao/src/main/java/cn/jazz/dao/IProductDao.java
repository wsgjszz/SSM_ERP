package cn.jazz.dao;

import cn.jazz.domain.Product;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface IProductDao  {

    /**
     * 查询所有产品信息
     * @return List<Product>
     * @throws Exception
     */
    @Select("select * from PRODUCT")
    public List<Product> findAll() throws Exception;

    /**
     * 根据id查询一条产品信息
     * @return
     * @throws Exception
     */
    @Select("select * from PRODUCT where id=#{id}")
    public Product findById() throws Exception;

    /**
     * 新增一条产品信息
     * @param product
     */
    @Insert("insert into PRODUCT(productnum,productname,cityname,departuretime,productprice,productdesc,productstatus) values(#{productNum},#{productName},#{cityName},#{departureTime},#{productPrice},#{productDesc},#{productStatus})")
    public void save(Product product);
}
