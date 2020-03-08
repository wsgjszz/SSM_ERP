package cn.jazz.controller;

import cn.jazz.dao.ITravellerDao;
import cn.jazz.service.impl.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/test")
public class TestController {

    @Autowired
    private TestService testService;

    @RequestMapping("findByOrderid.do")
    public void findByOrderid(@RequestParam(name = "id",required = true) String ordersId) throws Exception{
        System.out.println("结果如下");
        System.out.println(testService.findById(ordersId).toString());
    }

}
