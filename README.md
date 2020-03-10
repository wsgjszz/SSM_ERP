企业权限管理系统

### 环境搭建

#### 数据库搭建

##### 安装Oracle数据库

##### 异常问题处理

###### 中文乱码问题

1. 查看数据库编码

   ```plsql
   select userenv('language') from dual;
   ```

   实际查到的结果为:AMERICAN_AMERICA.ZHS16GBK

2. 查询服务器编码

   ```plsql
   select * from V$NLS_PARAMETERS;
   ```

   查看第一行中PARAMETER项中为NLS_LANGUAGE 对应的VALUE项中是否和第一步得到的值一样。
   如果不是，需要设置环境变量。否则PLSQL客户端使用的编码和服务器端编码不一致,插入中文时就会出现乱码.

3. 设置环境变量
   计算机->属性->高级系统设置->环境变量->新建
   设置变量名:NLS_LANG,变量值:第1步查到的值，即AMERICAN_AMERICA.ZHS16GBK

4. 重新启动PLSQL,插入数据正常

###### 连接异常

- 有时刚开始可以正常运行，但是连接会突然异常

  ```
  ERROR:
    ORA-01034: ORACLE not available
    ORA-27101: shared memory realm does not exist
  ```

- 解决方法

  1. 用cmd进入命令行

  2. ```
     sqlplus /nolog
     ```

  3. ```
     conn /as sysdba
     ```

  4. ```
     startup
     ```

- 异常解析

  ```
  C:\ RMAN TARGET ORAC11
  
  提示输入口令，输入当前操作系统登录用户的口令
  
  如果提示（未启动），肯定是未启动，因为你此时是无法正常启动数据库的。
  
  RMAN>STARTUP MOUNT;
  
  RMAN>DELETE ARCHIVELOG ALL; // 删除所有归档日志
  
  RMAN>CROSSCHECK ARCHIVE LOG ALL; // 对归档做一致性检查
  
  退出RMAN
  
  C:\SQLPLUS /nolog
  
  SQL>conn /as sysdba;
  
  SQL>SHUTDOWN IMMEDIATE
  
  SQL>STARTUP
  
  一切恢复正常
  ```

##### SQL操作

- 创建表空间和用户

  ```plsql
  --创建表空间
  create tablespace ssm
  datafile 'c:\db\ssm.dbf'
  size 100m
  autoextend on
  next 10m;
  -- 创建ssm用户
  create user ssm 
  identified by ssm
  default tablespace ssm;
  --给ssm用户授予连接和开发者权限
  grant connect,resource to ssm;
  ```

- 创建表和插入数据

  - 产品表

    ```plsql
    --创建产品表
    CREATE TABLE product(  
           id varchar2(32) default SYS_GUID() PRIMARY KEY,  --无意义，主键uuid，默认值为随机的uuid
           productNum VARCHAR2(50) NOT NULL,  --产品编号，唯一，不为空
           productName VARCHAR2(50),  --产品名称（路线名称）
           cityName VARCHAR2(50),  --出发城市
           DepartureTime timestamp,  --出发时间
           productPrice Number,  --产品价格
           productDesc VARCHAR2(500),  --产品描述
           productStatus INT,  --状态(0 关闭 1 开启)
           CONSTRAINT product UNIQUE (id, productNum) 
    );
    
    --插入数据
    insert into PRODUCT (id, productnum, productname, cityname, departuretime, productprice, productdesc, productstatus) values ('676C5BD1D35E429A8C2E114939C5685A', 'itcast-002', '北京三日游', '北京', to_timestamp('1010-2018 10:10:00.000000', 'dd-mm-yyyy hh24:mi:ss.ff'), 1200, '不错的旅行', 1); 
    insert into PRODUCT (id, productnum, productname, cityname, departuretime, productprice, productdesc, productstatus) values ('12B7ABF2A4C544568B0A7C69F36BF8B7', 'itcast-003', '上海五日游', '上海', to_timestamp('2504-2018 14:30:00.000000', 'dd-mm-yyyy hh24:mi:ss.ff'), 1800, '魔都我来了', 0); 
    insert into PRODUCT (id, productnum, productname, cityname, departuretime, productprice, productdesc, productstatus) values ('9F71F01CB448476DAFB309AA6DF9497F', 'itcast-001', '北京三日游', '北京', to_timestamp('1010-2018 10:10:00.000000', 'dd-mm-yyyy hh24:mi:ss.ff'), 1200, '不错的旅行', 1);
     
    ```

  - 订单表

  - 会员表

  - 旅客表

  - 用户表

  - 角色表

  - 权限表

  - 日志表

#### SSM框架搭建

- 解决Oracle依赖无法导入问题

  - 在整合SSM项目，想要添加Oracle驱动包时，Maven的pom.xml总是报Missing artifact com.oracle:ojdbc14:jar:10.2.0.1.0错误

  - 因为oracle的ojdbc.jar是收费的，所以maven的中央仓库中没有这个资源，只能通过配置本地库才能加载到项目中去

  - 到 http://www.oracle.com/technetwork/database/features/jdbc/index-091264.html 下载所需要的oraclejar包

  - 在D盘新建Oracle文件夹，将下载好的oracle14.jar放到oracle文件夹中

  - 在cmd中运行一下：

    ```
    mvn install:install-file -DgroupId=com.oracle -DartifactId=ojdbc14 -Dversion=11.2.0.1.0 -Dpackaging=jar -Dfile=D:/Oracle/ojdbc14.jar
    ```

  - 在pom.xml中添加以下代码

    ```xml
     <!-- oracle数据库驱动 -->
     <dependency>
        <groupId>com.oracle</groupId>
        <artifactId>ojdbc14</artifactId>
       <version>11.2.0.1.0</version>
     </dependency>
    ```

##### 目录结构

- 创建名为ssm的父工程
- 创建子模块
  - ssm_domain
  - ssm_dao
  - ssm_service
  - ssm_web
  - ssm_utils

##### 配置文件

###### pom.xml

```xml
<properties>
        <spring.version>5.0.2.RELEASE</spring.version>
        <slf4j.version>1.6.6</slf4j.version>
        <log4j.version>1.2.12</log4j.version>
        <oracle.version>11.2.0.1.0</oracle.version>
        <mybatis.version>3.4.5</mybatis.version>
        <spring.security.version>5.0.1.RELEASE</spring.security.version>
    </properties>

    <dependencies>
        <!-- spring -->
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjweaver</artifactId>
            <version>1.6.8</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-aop</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context-support</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-web</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-orm</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-beans</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-core</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-test</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-webmvc</artifactId>
            <version>${spring.version}</version>

        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-tx</artifactId>
            <version>${spring.version}</version>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>javax.servlet-api</artifactId>
            <version>3.1.0</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>javax.servlet.jsp</groupId>
            <artifactId>jsp-api</artifactId>
            <version>2.0</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>jstl</groupId>
            <artifactId>jstl</artifactId>
            <version>1.2</version>
        </dependency>
        <!-- log start -->
        <dependency>
            <groupId>log4j</groupId>
            <artifactId>log4j</artifactId>
            <version>${log4j.version}</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>${slf4j.version}</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
            <version>${slf4j.version}</version>
        </dependency>
        <!-- log end -->

        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis</artifactId>
            <version>${mybatis.version}</version>
        </dependency>
        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis-spring</artifactId>
            <version>1.3.0</version>
        </dependency>
        <dependency>
            <groupId>c3p0</groupId>
            <artifactId>c3p0</artifactId>
            <version>0.9.1.2</version>
            <type>jar</type>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>com.github.pagehelper</groupId>
            <artifactId>pagehelper</artifactId>
            <version>5.1.2</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-web</artifactId>
            <version>${spring.security.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-config</artifactId>
            <version>${spring.security.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-core</artifactId>
            <version>${spring.security.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-taglibs</artifactId>
            <version>${spring.security.version}</version>
        </dependency>

        <dependency>
            <groupId>com.oracle</groupId>
            <artifactId>ojdbc14</artifactId>
            <version>${oracle.version}</version>
        </dependency>

        <dependency>
            <groupId>javax.annotation</groupId>
            <artifactId>jsr250-api</artifactId>
            <version>1.0</version>
        </dependency>
    </dependencies>
    <build>
        <!--maven插件-->
        <plugins>
            <!--jdk编译插件-->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>12</source>
                    <target>12</target>
                    <encoding>utf-8</encoding>
                </configuration>
            </plugin>
            <!--tomcat插件-->
            <plugin>
                <groupId>org.apache.tomcat.maven</groupId>
                <!-- tomcat7的插件， 不同tomcat版本这个也不一样 -->
                <artifactId>tomcat7-maven-plugin</artifactId>
                <version>2.1</version>
                <configuration>
                    <!-- 通过maven tomcat7:run运行项目时，访问项目的端口号 -->
                    <port>80</port>
                    <!-- 项目访问路径-->
                    <path>/</path>
                    <charset>utf-8</charset>
                    <uriEncoding>utf-8</uriEncoding>
                </configuration>
            </plugin>
        </plugins>
    </build>
```

###### applicationContext.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
	http://www.springframework.org/schema/beans/spring-beans.xsd
	http://www.springframework.org/schema/context
	http://www.springframework.org/schema/context/spring-context.xsd
	http://www.springframework.org/schema/aop
	http://www.springframework.org/schema/aop/spring-aop.xsd
	http://www.springframework.org/schema/tx
	http://www.springframework.org/schema/tx/spring-tx.xsd">

    <!-- 开启注解扫描，管理service和dao -->
    <context:component-scan base-package="cn.jazz.dao"/>
    <context:component-scan base-package="cn.jazz.service"/>

    <context:property-placeholder location="classpath:db.properties"/>
    <!-- 配置连接池 -->
    <bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource">
        <property name="driverClass" value="${jdbc.driver}" />
        <property name="jdbcUrl" value="${jdbc.url}" />
        <property name="user" value="${jdbc.username}" />
        <property name="password" value="${jdbc.password}" />
    </bean>
    <!-- 把SqlSessionFactory交给IOC管理 -->
    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="dataSource" />
        <!-- 传入PageHelper的插件 -->
    </bean>

    <!-- 扫描dao接口 -->
    <bean id="mapperScanner" class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <property name="basePackage" value="cn.jazz.dao"/>
    </bean>

    <!-- 配置Spring的声明式事务管理 -->
    <!-- 配置事务管理器 -->
    <bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <property name="dataSource" ref="dataSource"/>
    </bean>

    <tx:annotation-driven transaction-manager="transactionManager"/>

</beans>
```

###### springmvc.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans.xsd
           http://www.springframework.org/schema/mvc
           http://www.springframework.org/schema/mvc/spring-mvc.xsd
           http://www.springframework.org/schema/context
           http://www.springframework.org/schema/context/spring-context.xsd
           http://www.springframework.org/schema/aop
		http://www.springframework.org/schema/aop/spring-aop.xsd
           ">

    <!-- 扫描controller的注解，别的不扫描 -->
    <context:component-scan base-package="cn.jazz.controller"/>

    <!-- 配置视图解析器 -->
    <bean id="viewResolver" class="org.springframework.web.servlet.view.InternalResourceViewResolver">
        <!-- JSP文件所在的目录 -->
        <property name="prefix" value="/pages/" />
        <!-- 文件的后缀名 -->
        <property name="suffix" value=".jsp" />
    </bean>

    <!-- 设置静态资源不过滤 -->
    <mvc:resources location="/css/" mapping="/css/**" />
    <mvc:resources location="/img/" mapping="/img/**" />
    <mvc:resources location="/js/" mapping="/js/**" />
    <mvc:resources location="/plugins/" mapping="/plugins/**" />

    <!-- 开启对SpringMVC注解的支持 -->
    <mvc:annotation-driven />

    <!--
        支持AOP的注解支持，AOP底层使用代理技术
        JDK动态代理，要求必须有接口
        cglib代理，生成子类对象，proxy-target-class="true" 默认使用cglib的方式
    -->
    <aop:aspectj-autoproxy proxy-target-class="true"/>

</beans>
```

###### web.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
         version="3.1">

    <!-- 配置加载类路径的配置文件 -->
    <context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>classpath*:applicationContext.xml</param-value>
    </context-param>

    <!-- 配置监听器 -->
    <listener>
        <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
    </listener>


    <!-- 前端控制器（加载classpath:springmvc.xml 服务器启动创建servlet） -->
    <servlet>
        <servlet-name>dispatcherServlet</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <!-- 配置初始化参数，创建完DispatcherServlet对象，加载springmvc.xml配置文件 -->
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>classpath:spring-mvc.xml</param-value>
        </init-param>
        <!-- 服务器启动的时候，让DispatcherServlet对象创建 -->
        <load-on-startup>1</load-on-startup>
    </servlet>
    <servlet-mapping>
        <servlet-name>dispatcherServlet</servlet-name>
        <url-pattern>*.do</url-pattern>
    </servlet-mapping>

    <!-- 解决中文乱码过滤器 -->
    <filter>
        <filter-name>characterEncodingFilter</filter-name>
        <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
        <init-param>
            <param-name>encoding</param-name>
            <param-value>UTF-8</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>characterEncodingFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>

    <!-- 设置默认首页文件 -->
    <welcome-file-list>
        <welcome-file>index.html</welcome-file>
        <welcome-file>index.htm</welcome-file>
        <welcome-file>index.jsp</welcome-file>
        <welcome-file>default.html</welcome-file>
        <welcome-file>default.htm</welcome-file>
        <welcome-file>default.jsp</welcome-file>
    </welcome-file-list>
</web-app>
```

###### properties文件

- db.properties

  ```properties
  jdbc.driver=oracle.jdbc.driver.OracleDriver
  jdbc.url=jdbc:oracle:thin:@192.168.80.10:1521:orcl
  jdbc.username=ssm
  jdbc.password=ssm
  ```

- log4j.properties

  ```properties
  # Set root category priority to INFO and its only appender to CONSOLE.
  #log4j.rootCategory=INFO, CONSOLE            debug   info   warn error fatal
  log4j.rootCategory=debug, CONSOLE, LOGFILE
  
  # Set the enterprise logger category to FATAL and its only appender to CONSOLE.
  log4j.logger.org.apache.axis.enterprise=FATAL, CONSOLE
  
  # CONSOLE is set to be a ConsoleAppender using a PatternLayout.
  log4j.appender.CONSOLE=org.apache.log4j.ConsoleAppender
  log4j.appender.CONSOLE.layout=org.apache.log4j.PatternLayout
  log4j.appender.CONSOLE.layout.ConversionPattern=%d{ISO8601} %-6r [%15.15t] %-5p %30.30c %x - %m\n
  
  # LOGFILE is set to be a File appender using a PatternLayout.
  # log4j.appender.LOGFILE=org.apache.log4j.FileAppender
  # log4j.appender.LOGFILE.File=d:\axis.log
  # log4j.appender.LOGFILE.Append=true
  # log4j.appender.LOGFILE.layout=org.apache.log4j.PatternLayout
  # log4j.appender.LOGFILE.layout.ConversionPattern=%d{ISO8601} %-6r [%15.15t] %-5p %30.30c %x - %m\n
  ```

###### 各模块pom文件

- ssm_domain

  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <project xmlns="http://maven.apache.org/POM/4.0.0"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
      <parent>
          <artifactId>SSM</artifactId>
          <groupId>cn.jazz</groupId>
          <version>1.0-SNAPSHOT</version>
      </parent>
      <modelVersion>4.0.0</modelVersion>
  
      <artifactId>ssm_domain</artifactId>
      <dependencies>
          <dependency>
              <groupId>cn.jazz</groupId>
              <artifactId>ssm_utils</artifactId>
              <version>1.0-SNAPSHOT</version>
              <scope>compile</scope>
          </dependency>
      </dependencies>
  
  
  </project>
  ```

- ssm_dao

  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <project xmlns="http://maven.apache.org/POM/4.0.0"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
      <parent>
          <artifactId>SSM</artifactId>
          <groupId>cn.jazz</groupId>
          <version>1.0-SNAPSHOT</version>
      </parent>
      <modelVersion>4.0.0</modelVersion>
  
      <artifactId>ssm_dao</artifactId>
      <dependencies>
          <dependency>
              <groupId>cn.jazz</groupId>
              <artifactId>ssm_domain</artifactId>
              <version>1.0-SNAPSHOT</version>
              <scope>compile</scope>
          </dependency>
          <dependency>
              <groupId>cn.jazz</groupId>
              <artifactId>ssm_domain</artifactId>
              <version>1.0-SNAPSHOT</version>
              <scope>compile</scope>
          </dependency>
      </dependencies>
  
  
  </project>
  ```

- ssm_service

  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <project xmlns="http://maven.apache.org/POM/4.0.0"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
      <parent>
          <artifactId>SSM</artifactId>
          <groupId>cn.jazz</groupId>
          <version>1.0-SNAPSHOT</version>
      </parent>
      <modelVersion>4.0.0</modelVersion>
  
      <artifactId>ssm_service</artifactId>
      <dependencies>
          <dependency>
              <groupId>cn.jazz</groupId>
              <artifactId>ssm_domain</artifactId>
              <version>1.0-SNAPSHOT</version>
              <scope>compile</scope>
          </dependency>
          <dependency>
              <groupId>cn.jazz</groupId>
              <artifactId>ssm_dao</artifactId>
              <version>1.0-SNAPSHOT</version>
              <scope>compile</scope>
          </dependency>
      </dependencies>
  
  
  </project>
  ```

- ssm_utils

  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <project xmlns="http://maven.apache.org/POM/4.0.0"
           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
      <parent>
          <artifactId>SSM</artifactId>
          <groupId>cn.jazz</groupId>
          <version>1.0-SNAPSHOT</version>
      </parent>
      <modelVersion>4.0.0</modelVersion>
  
      <artifactId>ssm_utils</artifactId>
  
  
  </project>
  ```

- ssm_web

  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  
  <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
      <parent>
          <artifactId>SSM</artifactId>
          <groupId>cn.jazz</groupId>
          <version>1.0-SNAPSHOT</version>
      </parent>
      <modelVersion>4.0.0</modelVersion>
  
      <artifactId>ssm_web</artifactId>
      <packaging>war</packaging>
  
      <name>ssm_web Maven Webapp</name>
      <!-- FIXME change it to the project's website -->
      <url>http://www.example.com</url>
  
      <properties>
          <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
          <maven.compiler.source>1.7</maven.compiler.source>
          <maven.compiler.target>1.7</maven.compiler.target>
      </properties>
  
      <dependencies>
          <dependency>
              <groupId>cn.jazz</groupId>
              <artifactId>ssm_dao</artifactId>
              <version>1.0-SNAPSHOT</version>
              <scope>compile</scope>
          </dependency>
          <dependency>
              <groupId>cn.jazz</groupId>
              <artifactId>ssm_service</artifactId>
              <version>1.0-SNAPSHOT</version>
              <scope>compile</scope>
          </dependency>
          <dependency>
              <groupId>junit</groupId>
              <artifactId>junit</artifactId>
              <version>4.11</version>
              <scope>test</scope>
          </dependency>
      </dependencies>
  
      <build>
          <finalName>ssm_web</finalName>
          <pluginManagement><!-- lock down plugins versions to avoid using Maven defaults (may be moved to parent pom) -->
              <plugins>
                  <plugin>
                      <artifactId>maven-clean-plugin</artifactId>
                      <version>3.1.0</version>
                  </plugin>
                  <!-- see http://maven.apache.org/ref/current/maven-core/default-bindings.html#Plugin_bindings_for_war_packaging -->
                  <plugin>
                      <artifactId>maven-resources-plugin</artifactId>
                      <version>3.0.2</version>
                  </plugin>
                  <plugin>
                      <artifactId>maven-compiler-plugin</artifactId>
                      <version>3.8.0</version>
                  </plugin>
                  <plugin>
                      <artifactId>maven-surefire-plugin</artifactId>
                      <version>2.22.1</version>
                  </plugin>
                  <plugin>
                      <artifactId>maven-war-plugin</artifactId>
                      <version>3.2.2</version>
                  </plugin>
                  <plugin>
                      <artifactId>maven-install-plugin</artifactId>
                      <version>2.5.2</version>
                  </plugin>
                  <plugin>
                      <artifactId>maven-deploy-plugin</artifactId>
                      <version>2.8.2</version>
                  </plugin>
              </plugins>
          </pluginManagement>
      </build>
  </project>
  ```

### 功能开发

#### 前台开发

##### 主干模块

###### header.jsp

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!-- 页面头部 -->
<header class="main-header">
	<!-- Logo -->
	<a href="all-admin-index.html" class="logo"> <!-- mini logo for sidebar mini 50x50 pixels -->
		<span class="logo-mini"><b>数据</b></span> <!-- logo for regular state and mobile devices -->
		<span class="logo-lg"><b>数据</b>后台管理</span>
	</a>
	<!-- Header Navbar: style can be found in header.less -->
	<nav class="navbar navbar-static-top">
		<!-- Sidebar toggle button-->
		<a href="#" class="sidebar-toggle" data-toggle="offcanvas"
			role="button"> <span class="sr-only">Toggle navigation</span>
		</a>

		<div class="navbar-custom-menu">
			<ul class="nav navbar-nav">

				<li class="dropdown user user-menu"><a href="#"
					class="dropdown-toggle" data-toggle="dropdown"> <img
						src="${pageContext.request.contextPath}/img/user2-160x160.jpg"
						class="user-image" alt="User Image"> <span class="hidden-xs">
							xxx
					</span>

				</a>
					<ul class="dropdown-menu">
						<!-- User image -->
						<li class="user-header"><img
							src="${pageContext.request.contextPath}/img/user2-160x160.jpg"
							class="img-circle" alt="User Image"></li>

						<!-- Menu Footer-->
						<li class="user-footer">
							<div class="pull-left">
								<a href="#" class="btn btn-default btn-flat">修改密码</a>
							</div>
							<div class="pull-right">
								<a href="${pageContext.request.contextPath}/logout.do"
									class="btn btn-default btn-flat">注销</a>
							</div>
						</li>
					</ul></li>

			</ul>
		</div>
	</nav>
</header>
<!-- 页面头部 /-->
```

###### aside.jsp

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<aside class="main-sidebar">
	<!-- sidebar: style can be found in sidebar.less -->
	<section class="sidebar">
		<!-- Sidebar user panel -->
		<div class="user-panel">
			<div class="pull-left image">
				<img src="${pageContext.request.contextPath}/img/user2-160x160.jpg"
					class="img-circle" alt="User Image">
			</div>
			<div class="pull-left info">
				<p>xxx</p>
				<a href="#"><i class="fa fa-circle text-success"></i> 在线</a>
			</div>
		</div>

		<!-- sidebar menu: : style can be found in sidebar.less -->
		<ul class="sidebar-menu">
			<li class="header">菜单</li>
			<li id="admin-index"><a
				href="${pageContext.request.contextPath}/pages/main.jsp"><i
					class="fa fa-dashboard"></i> <span>首页</span></a></li>

			<li class="treeview"><a href="#"> <i class="fa fa-cogs"></i>
					<span>系统管理</span> <span class="pull-right-container"> <i
						class="fa fa-angle-left pull-right"></i>
				</span>


			</a>
				<ul class="treeview-menu">

					<li id="system-setting"><a
						href="${pageContext.request.contextPath}/user/findAll.do"> <i
							class="fa fa-circle-o"></i> 用户管理
					</a></li>
					<li id="system-setting"><a
						href="${pageContext.request.contextPath}/role/findAll.do"> <i
							class="fa fa-circle-o"></i> 角色管理
					</a></li>
					<li id="system-setting"><a
						href="${pageContext.request.contextPath}/permission/findAll.do">
							<i class="fa fa-circle-o"></i> 资源权限管理
					</a></li>
					<li id="system-setting"><a
						href="${pageContext.request.contextPath}/sysLog/findAll.do"> <i
							class="fa fa-circle-o"></i> 访问日志
					</a></li>
				</ul></li>
			<li class="treeview"><a href="#"> <i class="fa fa-cube"></i>
					<span>基础数据</span> <span class="pull-right-container"> <i
						class="fa fa-angle-left pull-right"></i>
				</span>
			</a>
				<ul class="treeview-menu">

					<li id="system-setting"><a
						href="${pageContext.request.contextPath}/product/findAll.do">
							<i class="fa fa-circle-o"></i> 产品管理
					</a></li>
					<li id="system-setting"><a
						href="${pageContext.request.contextPath}/orders/findAll.do?page=1&size=4"> <i
							class="fa fa-circle-o"></i> 订单管理
					</a></li>

				</ul></li>

		</ul>
	</section>
	<!-- /.sidebar -->
</aside>
```

###### main.jsp

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<!-- 页面meta -->
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">

<title>ITCAST - AdminLTE2定制版</title>
<meta name="description" content="AdminLTE2定制版">
<meta name="keywords" content="AdminLTE2定制版">

<!-- Tell the browser to be responsive to screen width -->
<meta
	content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no"
	name="viewport">
<!-- Bootstrap 3.3.6 -->
<!-- Font Awesome -->
<!-- Ionicons -->
<!-- iCheck -->
<!-- Morris chart -->
<!-- jvectormap -->
<!-- Date Picker -->
<!-- Daterange picker -->
<!-- Bootstrap time Picker -->
<!--<link rel="stylesheet" href="${pageContext.request.contextPath}/${pageContext.request.contextPath}/${pageContext.request.contextPath}/plugins/timepicker/bootstrap-timepicker.min.css">-->
<!-- bootstrap wysihtml5 - text editor -->
<!--数据表格-->
<!-- 表格树 -->
<!-- select2 -->
<!-- Bootstrap Color Picker -->
<!-- bootstrap wysihtml5 - text editor -->
<!--bootstrap-markdown-->
<!-- Theme style -->
<!-- AdminLTE Skins. Choose a skin from the css/skins
       folder instead of downloading all of them to reduce the load. -->
<!-- Ion Slider -->
<!-- ion slider Nice -->
<!-- bootstrap slider -->
<!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
<!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
<!--[if lt IE 9]>
  <script src="https://oss.maxcdn.com/html5shiv/3.7.3/html5shiv.min.js"></script>
  <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
  <![endif]-->

<!-- jQuery 2.2.3 -->
<!-- jQuery UI 1.11.4 -->
<!-- Resolve conflict in jQuery UI tooltip with Bootstrap tooltip -->
<!-- Bootstrap 3.3.6 -->
<!-- Morris.js charts -->
<!-- Sparkline -->
<!-- jvectormap -->
<!-- jQuery Knob Chart -->
<!-- daterangepicker -->
<!-- datepicker -->
<!-- Bootstrap WYSIHTML5 -->
<!-- Slimscroll -->
<!-- FastClick -->
<!-- iCheck -->
<!-- AdminLTE App -->
<!-- 表格树 -->
<!-- select2 -->
<!-- bootstrap color picker -->
<!-- bootstrap time picker -->
<!--<script src="${pageContext.request.contextPath}/${pageContext.request.contextPath}/${pageContext.request.contextPath}/plugins/timepicker/bootstrap-timepicker.min.js"></script>-->
<!-- Bootstrap WYSIHTML5 -->
<!--bootstrap-markdown-->
<!-- CK Editor -->
<!-- InputMask -->
<!-- DataTables -->
<!-- ChartJS 1.0.1 -->
<!-- FLOT CHARTS -->
<!-- FLOT RESIZE PLUGIN - allows the chart to redraw when the window is resized -->
<!-- FLOT PIE PLUGIN - also used to draw donut charts -->
<!-- FLOT CATEGORIES PLUGIN - Used to draw bar charts -->
<!-- jQuery Knob -->
<!-- Sparkline -->
<!-- Morris.js charts -->
<!-- Ion Slider -->
<!-- Bootstrap slider -->
<!-- 页面meta /-->

<link rel="stylesheet"
	href="${pageContext.request.contextPath}/plugins/bootstrap/css/bootstrap.min.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/plugins/font-awesome/css/font-awesome.min.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/plugins/ionicons/css/ionicons.min.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/plugins/iCheck/square/blue.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/plugins/morris/morris.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/plugins/jvectormap/jquery-jvectormap-1.2.2.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/plugins/datepicker/datepicker3.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/plugins/daterangepicker/daterangepicker.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/plugins/datatables/dataTables.bootstrap.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.theme.default.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/plugins/select2/select2.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/plugins/colorpicker/bootstrap-colorpicker.min.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/plugins/bootstrap-markdown/css/bootstrap-markdown.min.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/plugins/adminLTE/css/AdminLTE.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/plugins/adminLTE/css/skins/_all-skins.min.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.skinNice.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/plugins/bootstrap-slider/slider.css">
</head>

<body class="hold-transition skin-blue sidebar-mini">

	<div class="wrapper">

		<!-- 页面头部 -->
		<jsp:include page="header.jsp"></jsp:include>
			<!-- 页面头部 /-->

		<!-- 导航侧栏 -->
		<jsp:include page="aside.jsp"></jsp:include>
		<!-- 导航侧栏 /-->

		<!-- 内容区域 -->
		<div class="content-wrapper">

			<img src="${pageContext.request.contextPath}/img/center.jpg"
				width="100%" height="100%" />

		</div>
		<!-- 内容区域 /-->

		<!-- 底部导航 -->
		<footer class="main-footer">
		<div class="pull-right hidden-xs">
			<b>Version</b> 1.0.8
		</div>
		<strong>Copyright &copy; 2014-2017 <a
			href="http://www.itcast.cn">研究院研发部</a>.
		</strong> All rights reserved. </footer>
		<!-- 底部导航 /-->

	</div>

	<script
		src="${pageContext.request.contextPath}/plugins/jQuery/jquery-2.2.3.min.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/jQueryUI/jquery-ui.min.js"></script>
	<script>
		$.widget.bridge('uibutton', $.ui.button);
	</script>
	<script
		src="${pageContext.request.contextPath}/plugins/bootstrap/js/bootstrap.min.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/raphael/raphael-min.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/morris/morris.min.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/sparkline/jquery.sparkline.min.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/jvectormap/jquery-jvectormap-1.2.2.min.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/jvectormap/jquery-jvectormap-world-mill-en.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/knob/jquery.knob.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/daterangepicker/moment.min.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/daterangepicker/daterangepicker.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/daterangepicker/daterangepicker.zh-CN.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/datepicker/bootstrap-datepicker.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/datepicker/locales/bootstrap-datepicker.zh-CN.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.all.min.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/slimScroll/jquery.slimscroll.min.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/fastclick/fastclick.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/iCheck/icheck.min.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/adminLTE/js/app.min.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/select2/select2.full.min.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/colorpicker/bootstrap-colorpicker.min.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/bootstrap-wysihtml5/bootstrap-wysihtml5.zh-CN.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/js/bootstrap-markdown.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/locale/bootstrap-markdown.zh.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/js/markdown.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/js/to-markdown.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/ckeditor/ckeditor.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/input-mask/jquery.inputmask.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/input-mask/jquery.inputmask.date.extensions.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/input-mask/jquery.inputmask.extensions.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/datatables/jquery.dataTables.min.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/datatables/dataTables.bootstrap.min.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/chartjs/Chart.min.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.min.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.resize.min.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.pie.min.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.categories.min.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.min.js"></script>
	<script
		src="${pageContext.request.contextPath}/plugins/bootstrap-slider/bootstrap-slider.js"></script>
	<script>
		$(document).ready(function() {
			// 选择框
			$(".select2").select2();

			// WYSIHTML5编辑器
			$(".textarea").wysihtml5({
				locale : 'zh-CN'
			});
		});

		// 设置激活菜单
		function setSidebarActive(tagUri) {
			var liObj = $("#" + tagUri);
			if (liObj.length > 0) {
				liObj.parent().parent().addClass("active");
				liObj.addClass("active");
			}
		}

		$(document).ready(function() {
			// 激活导航位置
			setSidebarActive("admin-index");
		});
	</script>
</body>

</html>
```

##### 产品模块

###### product-list.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>

<head>
    <!-- 页面meta -->
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">

    <title>商品信息列表</title>
    <meta name="description" content="AdminLTE2定制版">
    <meta name="keywords" content="AdminLTE2定制版">




    <!-- Tell the browser to be responsive to screen width -->
    <meta content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no" name="viewport">
    <!-- Bootstrap 3.3.6 -->
    <!-- Font Awesome -->
    <!-- Ionicons -->
    <!-- iCheck -->
    <!-- Morris chart -->
    <!-- jvectormap -->
    <!-- Date Picker -->
    <!-- Daterange picker -->
    <!-- Bootstrap time Picker -->
    <!--<link rel="stylesheet" href="../../../plugins/timepicker/bootstrap-timepicker.min.css">-->
    <!-- bootstrap wysihtml5 - text editor -->
    <!--数据表格-->
    <!-- 表格树 -->
    <!-- select2 -->
    <!-- Bootstrap Color Picker -->
    <!-- bootstrap wysihtml5 - text editor -->
    <!--bootstrap-markdown-->
    <!-- Theme style -->
    <!-- AdminLTE Skins. Choose a skin from the css/skins
       folder instead of downloading all of them to reduce the load. -->
    <!-- Ion Slider -->
    <!-- ion slider Nice -->
    <!-- bootstrap slider -->
    <!-- bootstrap-datetimepicker -->

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/html5shiv/3.7.3/html5shiv.min.js"></script>
    <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->








    <!-- jQuery 2.2.3 -->
    <!-- jQuery UI 1.11.4 -->
    <!-- Resolve conflict in jQuery UI tooltip with Bootstrap tooltip -->
    <!-- Bootstrap 3.3.6 -->
    <!-- Morris.js charts -->
    <!-- Sparkline -->
    <!-- jvectormap -->
    <!-- jQuery Knob Chart -->
    <!-- daterangepicker -->
    <!-- datepicker -->
    <!-- Bootstrap WYSIHTML5 -->
    <!-- Slimscroll -->
    <!-- FastClick -->
    <!-- iCheck -->
    <!-- AdminLTE App -->
    <!-- 表格树 -->
    <!-- select2 -->
    <!-- bootstrap color picker -->
    <!-- bootstrap time picker -->
    <!--<script src="../../../plugins/timepicker/bootstrap-timepicker.min.js"></script>-->
    <!-- Bootstrap WYSIHTML5 -->
    <!--bootstrap-markdown-->
    <!-- CK Editor -->
    <!-- InputMask -->
    <!-- DataTables -->
    <!-- ChartJS 1.0.1 -->
    <!-- FLOT CHARTS -->
    <!-- FLOT RESIZE PLUGIN - allows the chart to redraw when the window is resized -->
    <!-- FLOT PIE PLUGIN - also used to draw donut charts -->
    <!-- FLOT CATEGORIES PLUGIN - Used to draw bar charts -->
    <!-- jQuery Knob -->
    <!-- Sparkline -->
    <!-- Morris.js charts -->
    <!-- Ion Slider -->
    <!-- Bootstrap slider -->
    <!-- bootstrap-datetimepicker -->
    <!-- 页面meta /-->

    <link rel="stylesheet" href="../plugins/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="../plugins/font-awesome/css/font-awesome.min.css">
    <link rel="stylesheet" href="../plugins/ionicons/css/ionicons.min.css">
    <link rel="stylesheet" href="../plugins/iCheck/square/blue.css">
    <link rel="stylesheet" href="../plugins/morris/morris.css">
    <link rel="stylesheet" href="../plugins/jvectormap/jquery-jvectormap-1.2.2.css">
    <link rel="stylesheet" href="../plugins/datepicker/datepicker3.css">
    <link rel="stylesheet" href="../plugins/daterangepicker/daterangepicker.css">
    <link rel="stylesheet" href="../plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css">
    <link rel="stylesheet" href="../plugins/datatables/dataTables.bootstrap.css">
    <link rel="stylesheet" href="../plugins/treeTable/jquery.treetable.css">
    <link rel="stylesheet" href="../plugins/treeTable/jquery.treetable.theme.default.css">
    <link rel="stylesheet" href="../plugins/select2/select2.css">
    <link rel="stylesheet" href="../plugins/colorpicker/bootstrap-colorpicker.min.css">
    <link rel="stylesheet" href="../plugins/bootstrap-markdown/css/bootstrap-markdown.min.css">
    <link rel="stylesheet" href="../plugins/adminLTE/css/AdminLTE.css">
    <link rel="stylesheet" href="../plugins/adminLTE/css/skins/_all-skins.min.css">
    <link rel="stylesheet" href="../css/style.css">
    <link rel="stylesheet" href="../plugins/ionslider/ion.rangeSlider.css">
    <link rel="stylesheet" href="../plugins/ionslider/ion.rangeSlider.skinNice.css">
    <link rel="stylesheet" href="../plugins/bootstrap-slider/slider.css">
    <link rel="stylesheet" href="../plugins/bootstrap-datetimepicker/bootstrap-datetimepicker.css">
</head>

<body class="hold-transition skin-purple sidebar-mini">

<div class="wrapper">

    <!-- 页面头部 -->
    <jsp:include page="header.jsp"></jsp:include>
    <!-- 页面头部 /-->

    <!-- 导航侧栏 -->
    <jsp:include page="aside.jsp"></jsp:include>
    <!-- 导航侧栏 /-->

    <!-- 内容区域 -->
    <!-- @@master = admin-layout.html-->
    <!-- @@block = content -->

    <div class="content-wrapper">

        <!-- 内容头部 -->
        <section class="content-header">
            <h1>
                数据管理
                <small>数据列表</small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="#"><i class="fa fa-dashboard"></i> 首页</a></li>
                <li><a href="#">数据管理</a></li>
                <li class="active">数据列表</li>
            </ol>
        </section>
        <!-- 内容头部 /-->

        <!-- 正文区域 -->
        <section class="content">

            <!-- .box-body -->
            <div class="box box-primary">
                <div class="box-header with-border">
                    <h3 class="box-title">列表</h3>
                </div>

                <div class="box-body">

                    <!-- 数据表格 -->
                    <div class="table-box">

                        <!--工具栏-->
                        <div class="pull-left">
                            <div class="form-group form-inline">
                                <div class="btn-group">
                                    <button type="button" class="btn btn-default" title="新建" onclick="location.href='${pageContext.request.contextPath}/pages/product-add.jsp'"><i class="fa fa-file-o"></i> 新建</button>
                                    <button type="button" class="btn btn-default" title="删除"><i class="fa fa-trash-o"></i> 删除</button>
                                    <button type="button" class="btn btn-default" title="开启"><i class="fa fa-check"></i> 开启</button>
                                    <button type="button" class="btn btn-default" title="屏蔽"><i class="fa fa-ban"></i> 屏蔽</button>
                                    <button type="button" class="btn btn-default" title="刷新"><i class="fa fa-refresh"></i> 刷新</button>
                                </div>
                            </div>
                        </div>
                        <div class="box-tools pull-right">
                            <div class="has-feedback">
                                <input type="text" class="form-control input-sm" placeholder="搜索">
                                <span class="glyphicon glyphicon-search form-control-feedback"></span>
                            </div>
                        </div>
                        <!--工具栏/-->

                        <!--数据列表-->
                        <table id="dataList" class="table table-bordered table-striped table-hover dataTable">
                            <thead>
                            <tr>
                                <th class="" style="padding-right:0px;">
                                    <input id="selall" type="checkbox" class="icheckbox_square-blue">
                                </th>
                                <th class="sorting_asc">ID</th>
                                <th class="sorting_desc">编号</th>
                                <th class="sorting_asc sorting_asc_disabled">产品名称</th>
                                <th class="sorting_desc sorting_desc_disabled">出发城市</th>
                                <th class="sorting">出发时间</th>
                                <th class="text-center sorting">产品价格</th>
                                <th class="text-center sorting">产品描述</th>
                                <th class="text-center sorting">状态</th>
                                <th class="text-center">操作</th>
                            </tr>
                            </thead>
                            <tbody>

                            <c:forEach items="${productList}" var="product">
                            <tr>
                                <td><input name="ids" type="checkbox"></td>
                                <td>${product.id}</td>
                                <td>${product.productNum}</td>
                                <td>${product.productName}</td>
                                <td>${product.cityName}</td>
                                <td>${product.departureTimeStr}</td>
                                <td class="text-center">${product.productPrice}</td>
                                <td class="text-center">${product.productDesc}</td>
                                <td class="text-center">${product.productStatusStr}</td>
                                <td class="text-center">
                                    <button type="button" class="btn bg-olive btn-xs">订单</button>
                                    <button type="button" class="btn bg-olive btn-xs">详情</button>
                                    <button type="button" class="btn bg-olive btn-xs">编辑</button>
                                </td>
                            </tr>
                            </c:forEach>

                            </tbody>
                            <!--
                        <tfoot>
                        <tr>
                        <th>Rendering engine</th>
                        <th>Browser</th>
                        <th>Platform(s)</th>
                        <th>Engine version</th>
                        <th>CSS grade</th>
                        </tr>
                        </tfoot>-->
                        </table>
                        <!--数据列表/-->

                        <!--工具栏-->
                        <div class="pull-left">
                            <div class="form-group form-inline">
                                <div class="btn-group">
                                    <button type="button" class="btn btn-default" title="新建"><i class="fa fa-file-o"></i> 新建</button>
                                    <button type="button" class="btn btn-default" title="删除"><i class="fa fa-trash-o"></i> 删除</button>
                                    <button type="button" class="btn btn-default" title="开启"><i class="fa fa-check"></i> 开启</button>
                                    <button type="button" class="btn btn-default" title="屏蔽"><i class="fa fa-ban"></i> 屏蔽</button>
                                    <button type="button" class="btn btn-default" title="刷新"><i class="fa fa-refresh"></i> 刷新</button>
                                </div>
                            </div>
                        </div>
                        <div class="box-tools pull-right">
                            <div class="has-feedback">
                                <input type="text" class="form-control input-sm" placeholder="搜索">
                                <span class="glyphicon glyphicon-search form-control-feedback"></span>
                            </div>
                        </div>
                        <!--工具栏/-->

                    </div>
                    <!-- 数据表格 /-->


                </div>
                <!-- /.box-body -->

                <!-- .box-footer-->
                <div class="box-footer">
                    <div class="pull-left">
                        <div class="form-group form-inline">
                            总共2 页，共14 条数据。 每页
                            <select class="form-control">
                                <option>1</option>
                                <option>2</option>
                                <option>3</option>
                                <option>4</option>
                                <option>5</option>
                            </select> 条
                        </div>
                    </div>

                    <div class="box-tools pull-right">
                        <ul class="pagination">
                            <li>
                                <a href="#" aria-label="Previous">首页</a>
                            </li>
                            <li><a href="#">上一页</a></li>
                            <li><a href="#">1</a></li>
                            <li><a href="#">2</a></li>
                            <li><a href="#">3</a></li>
                            <li><a href="#">4</a></li>
                            <li><a href="#">5</a></li>
                            <li><a href="#">下一页</a></li>
                            <li>
                                <a href="#" aria-label="Next">尾页</a>
                            </li>
                        </ul>
                    </div>

                </div>
                <!-- /.box-footer-->



            </div>

        </section>
        <!-- 正文区域 /-->

    </div>
    <!-- @@close -->
    <!-- 内容区域 /-->

    <!-- 底部导航 -->
    <footer class="main-footer">
        <div class="pull-right hidden-xs">
            <b>Version</b> 1.0.8
        </div>
        <strong>Copyright &copy; 2014-2017 <a href="http://www.itcast.cn">研究院研发部</a>.</strong> All rights reserved.
    </footer>
    <!-- 底部导航 /-->

</div>


<script src="../plugins/jQuery/jquery-2.2.3.min.js"></script>
<script src="../plugins/jQueryUI/jquery-ui.min.js"></script>
<script>
    $.widget.bridge('uibutton', $.ui.button);
</script>
<script src="../plugins/bootstrap/js/bootstrap.min.js"></script>
<script src="../plugins/raphael/raphael-min.js"></script>
<script src="../plugins/morris/morris.min.js"></script>
<script src="../plugins/sparkline/jquery.sparkline.min.js"></script>
<script src="../plugins/jvectormap/jquery-jvectormap-1.2.2.min.js"></script>
<script src="../plugins/jvectormap/jquery-jvectormap-world-mill-en.js"></script>
<script src="../plugins/knob/jquery.knob.js"></script>
<script src="../plugins/daterangepicker/moment.min.js"></script>
<script src="../plugins/daterangepicker/daterangepicker.js"></script>
<script src="../plugins/daterangepicker/daterangepicker.zh-CN.js"></script>
<script src="../plugins/datepicker/bootstrap-datepicker.js"></script>
<script src="../plugins/datepicker/locales/bootstrap-datepicker.zh-CN.js"></script>
<script src="../plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.all.min.js"></script>
<script src="../plugins/slimScroll/jquery.slimscroll.min.js"></script>
<script src="../plugins/fastclick/fastclick.js"></script>
<script src="../plugins/iCheck/icheck.min.js"></script>
<script src="../plugins/adminLTE/js/app.min.js"></script>
<script src="../plugins/treeTable/jquery.treetable.js"></script>
<script src="../plugins/select2/select2.full.min.js"></script>
<script src="../plugins/colorpicker/bootstrap-colorpicker.min.js"></script>
<script src="../plugins/bootstrap-wysihtml5/bootstrap-wysihtml5.zh-CN.js"></script>
<script src="../plugins/bootstrap-markdown/js/bootstrap-markdown.js"></script>
<script src="../plugins/bootstrap-markdown/locale/bootstrap-markdown.zh.js"></script>
<script src="../plugins/bootstrap-markdown/js/markdown.js"></script>
<script src="../plugins/bootstrap-markdown/js/to-markdown.js"></script>
<script src="../plugins/ckeditor/ckeditor.js"></script>
<script src="../plugins/input-mask/jquery.inputmask.js"></script>
<script src="../plugins/input-mask/jquery.inputmask.date.extensions.js"></script>
<script src="../plugins/input-mask/jquery.inputmask.extensions.js"></script>
<script src="../plugins/datatables/jquery.dataTables.min.js"></script>
<script src="../plugins/datatables/dataTables.bootstrap.min.js"></script>
<script src="../plugins/chartjs/Chart.min.js"></script>
<script src="../plugins/flot/jquery.flot.min.js"></script>
<script src="../plugins/flot/jquery.flot.resize.min.js"></script>
<script src="../plugins/flot/jquery.flot.pie.min.js"></script>
<script src="../plugins/flot/jquery.flot.categories.min.js"></script>
<script src="../plugins/ionslider/ion.rangeSlider.min.js"></script>
<script src="../plugins/bootstrap-slider/bootstrap-slider.js"></script>
<script src="../plugins/bootstrap-datetimepicker/bootstrap-datetimepicker.js"></script>
<script src="../plugins/bootstrap-datetimepicker/locales/bootstrap-datetimepicker.zh-CN.js"></script>
<script>
    $(document).ready(function() {
        // 选择框
        $(".select2").select2();

        // WYSIHTML5编辑器
        $(".textarea").wysihtml5({
            locale: 'zh-CN'
        });
    });


    // 设置激活菜单
    function setSidebarActive(tagUri) {
        var liObj = $("#" + tagUri);
        if (liObj.length > 0) {
            liObj.parent().parent().addClass("active");
            liObj.addClass("active");
        }
    }


    $(document).ready(function() {

        // 激活导航位置
        setSidebarActive("admin-datalist");

        // 列表按钮
        $("#dataList td input[type='checkbox']").iCheck({
            checkboxClass: 'icheckbox_square-blue',
            increaseArea: '20%'
        });
        // 全选操作
        $("#selall").click(function() {
            var clicks = $(this).is(':checked');
            if (!clicks) {
                $("#dataList td input[type='checkbox']").iCheck("uncheck");
            } else {
                $("#dataList td input[type='checkbox']").iCheck("check");
            }
            $(this).data("clicks", !clicks);
        });
    });
</script>
</body>

</html>
```

###### product-add.jsp

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <!-- 页面meta -->
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>数据 - AdminLTE2定制版</title>
    <meta name="description" content="AdminLTE2定制版">
    <meta name="keywords" content="AdminLTE2定制版">

    <!-- Tell the browser to be responsive to screen width -->
    <meta
            content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no"
            name="viewport">


    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/font-awesome/css/font-awesome.min.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/ionicons/css/ionicons.min.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/iCheck/square/blue.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/morris/morris.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/jvectormap/jquery-jvectormap-1.2.2.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/datepicker/datepicker3.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/daterangepicker/daterangepicker.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/datatables/dataTables.bootstrap.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.theme.default.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/select2/select2.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/colorpicker/bootstrap-colorpicker.min.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/bootstrap-markdown/css/bootstrap-markdown.min.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/adminLTE/css/AdminLTE.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/adminLTE/css/skins/_all-skins.min.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.skinNice.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/bootstrap-slider/slider.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/bootstrap-datetimepicker/bootstrap-datetimepicker.css">
</head>

<body class="hold-transition skin-purple sidebar-mini">

<div class="wrapper">

    <!-- 页面头部 -->
    <jsp:include page="header.jsp"></jsp:include>
    <!-- 页面头部 /-->
    <!-- 导航侧栏 -->
    <jsp:include page="aside.jsp"></jsp:include>
    <!-- 导航侧栏 /-->

    <!-- 内容区域 -->
    <div class="content-wrapper">

        <!-- 内容头部 -->
        <section class="content-header">
            <h1>
                产品管理 <small>产品表单</small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="${pageContext.request.contextPath}/index.jsp"><i
                        class="fa fa-dashboard"></i> 首页</a></li>
                <li><a
                        href="${pageContext.request.contextPath}/product/findAll.do">产品管理</a></li>
                <li class="active">产品表单</li>
            </ol>
        </section>
        <!-- 内容头部 /-->

        <form action="${pageContext.request.contextPath}/product/add.do"
              method="post">
            <!-- 正文区域 -->
            <section class="content"> <!--产品信息-->

                <div class="panel panel-default">
                    <div class="panel-heading">产品信息</div>
                    <div class="row data-type">

                        <div class="col-md-2 title">产品编号</div>
                        <div class="col-md-4 data">
                            <input type="text" class="form-control" name="productNum"
                                   placeholder="产品编号" value="">
                        </div>
                        <div class="col-md-2 title">产品名称</div>
                        <div class="col-md-4 data">
                            <input type="text" class="form-control" name="productName"
                                   placeholder="产品名称" value="">
                        </div>
                        <div class="col-md-2 title">出发时间</div>
                        <div class="col-md-4 data">
                            <div class="input-group date">
                                <div class="input-group-addon">
                                    <i class="fa fa-calendar"></i>
                                </div>
                                <input type="text" class="form-control pull-right"
                                       id="datepicker-a3" name="departureTime">
                            </div>
                        </div>


                        <div class="col-md-2 title">出发城市</div>
                        <div class="col-md-4 data">
                            <input type="text" class="form-control" name="cityName"
                                   placeholder="出发城市" value="">
                        </div>

                        <div class="col-md-2 title">产品价格</div>
                        <div class="col-md-4 data">
                            <input type="text" class="form-control" placeholder="产品价格"
                                   name="productPrice" value="">
                        </div>

                        <div class="col-md-2 title">产品状态</div>
                        <div class="col-md-4 data">
                            <select class="form-control select2" style="width: 100%"
                                    name="productStatus">
                                <option value="0" selected="selected">关闭</option>
                                <option value="1">开启</option>
                            </select>
                        </div>

                        <div class="col-md-2 title rowHeight2x">其他信息</div>
                        <div class="col-md-10 data rowHeight2x">
							<textarea class="form-control" rows="3" placeholder="其他信息"
                                      name="productDesc"></textarea>
                        </div>

                    </div>
                </div>
                <!--订单信息/--> <!--工具栏-->
                <div class="box-tools text-center">
                    <button type="submit" class="btn bg-maroon">保存</button>
                    <button type="button" class="btn bg-default"
                            onclick="history.back(-1);">返回
                    </button>
                </div>
                <!--工具栏/--> </section>
            <!-- 正文区域 /-->
        </form>
    </div>
    <!-- 内容区域 /-->

    <!-- 底部导航 -->
    <footer class="main-footer">
        <div class="pull-right hidden-xs">
            <b>Version</b> 1.0.8
        </div>
        <strong>Copyright &copy; 2014-2017 <a
                href="http://www.itcast.cn">研究院研发部</a>.
        </strong> All rights reserved.
    </footer>
    <!-- 底部导航 /-->

</div>


<script
        src="${pageContext.request.contextPath}/plugins/jQuery/jquery-2.2.3.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/jQueryUI/jquery-ui.min.js"></script>
<script>
    $.widget.bridge('uibutton', $.ui.button);
</script>
<script
        src="${pageContext.request.contextPath}/plugins/bootstrap/js/bootstrap.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/raphael/raphael-min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/morris/morris.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/sparkline/jquery.sparkline.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/jvectormap/jquery-jvectormap-1.2.2.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/jvectormap/jquery-jvectormap-world-mill-en.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/knob/jquery.knob.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/daterangepicker/moment.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/daterangepicker/daterangepicker.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/daterangepicker/daterangepicker.zh-CN.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/datepicker/bootstrap-datepicker.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/datepicker/locales/bootstrap-datepicker.zh-CN.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.all.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/slimScroll/jquery.slimscroll.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/fastclick/fastclick.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/iCheck/icheck.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/adminLTE/js/app.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/select2/select2.full.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/colorpicker/bootstrap-colorpicker.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/bootstrap-wysihtml5/bootstrap-wysihtml5.zh-CN.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/js/bootstrap-markdown.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/locale/bootstrap-markdown.zh.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/js/markdown.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/js/to-markdown.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/ckeditor/ckeditor.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/input-mask/jquery.inputmask.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/input-mask/jquery.inputmask.date.extensions.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/input-mask/jquery.inputmask.extensions.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/datatables/jquery.dataTables.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/datatables/dataTables.bootstrap.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/chartjs/Chart.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.resize.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.pie.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.categories.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/bootstrap-slider/bootstrap-slider.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/bootstrap-datetimepicker/bootstrap-datetimepicker.min.js"></script>

<script>
    $(document).ready(function () {
        // 选择框
        $(".select2").select2();

        // WYSIHTML5编辑器
        $(".textarea").wysihtml5({
            locale: 'zh-CN'
        });
    });

    // 设置激活菜单
    function setSidebarActive(tagUri) {
        var liObj = $("#" + tagUri);
        if (liObj.length > 0) {
            liObj.parent().parent().addClass("active");
            liObj.addClass("active");
        }
    }

    $(document).ready(function () {
        $('#datepicker-a3').datetimepicker({
            format: "yyyy-mm-dd hh:ii",
            autoclose: true,
            todayBtn: true,
            language: "zh-CN"
        });
    });

    $(document).ready(function () {
        // 激活导航位置
        setSidebarActive("order-manage");
        $("#datepicker-a3").datetimepicker({
            format: "yyyy-mm-dd hh:ii",

        });

    });
</script>


</body>

</html>
```

##### 订单模块

###### orders-list.jsp

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>

<head>
    <!-- 页面meta -->
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">


    <title>数据 - AdminLTE2定制版</title>
    <meta name="description" content="AdminLTE2定制版">
    <meta name="keywords" content="AdminLTE2定制版">


    <!-- Tell the browser to be responsive to screen width -->
    <meta
            content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no"
            name="viewport">
    <!-- Bootstrap 3.3.6 -->
    <!-- Font Awesome -->
    <!-- Ionicons -->
    <!-- iCheck -->
    <!-- Morris chart -->
    <!-- jvectormap -->
    <!-- Date Picker -->
    <!-- Daterange picker -->
    <!-- Bootstrap time Picker -->
    <!--<link rel="stylesheet" href="${pageContext.request.contextPath}/${pageContext.request.contextPath}/${pageContext.request.contextPath}/plugins/timepicker/bootstrap-timepicker.min.css">-->
    <!-- bootstrap wysihtml5 - text editor -->
    <!--数据表格-->
    <!-- 表格树 -->
    <!-- select2 -->
    <!-- Bootstrap Color Picker -->
    <!-- bootstrap wysihtml5 - text editor -->
    <!--bootstrap-markdown-->
    <!-- Theme style -->
    <!-- AdminLTE Skins. Choose a skin from the css/skins
           folder instead of downloading all of them to reduce the load. -->
    <!-- Ion Slider -->
    <!-- ion slider Nice -->
    <!-- bootstrap slider -->
    <!-- bootstrap-datetimepicker -->

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/html5shiv/3.7.3/html5shiv.min.js"></script>
    <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->


    <!-- jQuery 2.2.3 -->
    <!-- jQuery UI 1.11.4 -->
    <!-- Resolve conflict in jQuery UI tooltip with Bootstrap tooltip -->
    <!-- Bootstrap 3.3.6 -->
    <!-- Morris.js charts -->
    <!-- Sparkline -->
    <!-- jvectormap -->
    <!-- jQuery Knob Chart -->
    <!-- daterangepicker -->
    <!-- datepicker -->
    <!-- Bootstrap WYSIHTML5 -->
    <!-- Slimscroll -->
    <!-- FastClick -->
    <!-- iCheck -->
    <!-- AdminLTE App -->
    <!-- 表格树 -->
    <!-- select2 -->
    <!-- bootstrap color picker -->
    <!-- bootstrap time picker -->
    <!--<script src="${pageContext.request.contextPath}/${pageContext.request.contextPath}/${pageContext.request.contextPath}/plugins/timepicker/bootstrap-timepicker.min.js"></script>-->
    <!-- Bootstrap WYSIHTML5 -->
    <!--bootstrap-markdown-->
    <!-- CK Editor -->
    <!-- InputMask -->
    <!-- DataTables -->
    <!-- ChartJS 1.0.1 -->
    <!-- FLOT CHARTS -->
    <!-- FLOT RESIZE PLUGIN - allows the chart to redraw when the window is resized -->
    <!-- FLOT PIE PLUGIN - also used to draw donut charts -->
    <!-- FLOT CATEGORIES PLUGIN - Used to draw bar charts -->
    <!-- jQuery Knob -->
    <!-- Sparkline -->
    <!-- Morris.js charts -->
    <!-- Ion Slider -->
    <!-- Bootstrap slider -->
    <!-- bootstrap-datetimepicker -->
    <!-- 页面meta /-->

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/font-awesome/css/font-awesome.min.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/ionicons/css/ionicons.min.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/iCheck/square/blue.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/morris/morris.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/jvectormap/jquery-jvectormap-1.2.2.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/datepicker/datepicker3.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/daterangepicker/daterangepicker.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/datatables/dataTables.bootstrap.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.theme.default.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/select2/select2.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/colorpicker/bootstrap-colorpicker.min.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/bootstrap-markdown/css/bootstrap-markdown.min.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/adminLTE/css/AdminLTE.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/adminLTE/css/skins/_all-skins.min.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.skinNice.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/bootstrap-slider/slider.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/bootstrap-datetimepicker/bootstrap-datetimepicker.css">
</head>

<body class="hold-transition skin-purple sidebar-mini">

<div class="wrapper">

    <!-- 页面头部 -->
    <jsp:include page="header.jsp"></jsp:include>
    <!-- 页面头部 /-->
    <!-- 导航侧栏 -->
    <jsp:include page="aside.jsp"></jsp:include>
    <!-- 导航侧栏 /-->

    <!-- 内容区域 -->
    <!-- @@master = admin-layout.html-->
    <!-- @@block = content -->

    <div class="content-wrapper">

        <!-- 内容头部 -->
        <section class="content-header">
            <h1>
                数据管理 <small>数据列表</small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="#"><i class="fa fa-dashboard"></i> 首页</a></li>
                <li><a href="#">数据管理</a></li>
                <li class="active">数据列表</li>
            </ol>
        </section>
        <!-- 内容头部 /-->

        <!-- 正文区域 -->
        <section class="content">

            <!-- .box-body -->
            <div class="box box-primary">
                <div class="box-header with-border">
                    <h3 class="box-title">列表</h3>
                </div>

                <div class="box-body">

                    <!-- 数据表格 -->
                    <div class="table-box">

                        <!--工具栏-->
                        <div class="pull-left">
                            <div class="form-group form-inline">
                                <div class="btn-group">
                                    <button type="button" class="btn btn-default" title="新建"
                                            onclick="location.href='${pageContext.request.contextPath}/pages/product-add.jsp'">
                                        <i class="fa fa-file-o"></i> 新建
                                    </button>
                                    <button type="button" class="btn btn-default" title="删除">
                                        <i class="fa fa-trash-o"></i> 删除
                                    </button>
                                    <button type="button" class="btn btn-default" title="开启">
                                        <i class="fa fa-check"></i> 开启
                                    </button>
                                    <button type="button" class="btn btn-default" title="屏蔽">
                                        <i class="fa fa-ban"></i> 屏蔽
                                    </button>
                                    <button type="button" class="btn btn-default" title="刷新">
                                        <i class="fa fa-refresh"></i> 刷新
                                    </button>
                                </div>
                            </div>
                        </div>
                        <div class="box-tools pull-right">
                            <div class="has-feedback">
                                <input type="text" class="form-control input-sm"
                                       placeholder="搜索"> <span
                                    class="glyphicon glyphicon-search form-control-feedback"></span>
                            </div>
                        </div>
                        <!--工具栏/-->

                        <!--数据列表-->
                        <table id="dataList"
                               class="table table-bordered table-striped table-hover dataTable">
                            <thead>
                            <tr>
                                <th class="" style="padding-right: 0px;"><input
                                        id="selall" type="checkbox" class="icheckbox_square-blue">
                                </th>
                                <th class="sorting_asc">ID</th>
                                <th class="sorting_desc">订单编号</th>
                                <th class="sorting_asc sorting_asc_disabled">产品名称</th>
                                <th class="sorting_desc sorting_desc_disabled">金额</th>
                                <th class="sorting">下单时间</th>
                                <th class="text-center sorting">订单状态</th>
                                <th class="text-center">操作</th>
                            </tr>
                            </thead>
                            <tbody>


                            <c:forEach items="${ordersList}" var="orders">

                                <tr>
                                    <td><input name="ids" type="checkbox"></td>
                                    <td>${orders.id }</td>
                                    <td>${orders.orderNum }</td>
                                    <td>${orders.product.productName }</td>
                                    <td>${orders.product.productPrice }</td>
                                    <td>${orders.orderTimeStr }</td>
                                    <td class="text-center">${orders.orderStatusStr }</td>
                                    <td class="text-center">
                                        <button type="button" class="btn bg-olive btn-xs">订单</button>
                                        <button type="button" class="btn bg-olive btn-xs"
                                                onclick="location.href='${pageContext.request.contextPath}/orders/findById.do?id=${orders.id}'">
                                            详情
                                        </button>
                                        <button type="button" class="btn bg-olive btn-xs">编辑</button>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                            <!--
                        <tfoot>
                        <tr>
                        <th>Rendering engine</th>
                        <th>Browser</th>
                        <th>Platform(s)</th>
                        <th>Engine version</th>
                        <th>CSS grade</th>
                        </tr>
                        </tfoot>-->
                        </table>
                        <!--数据列表/-->

                        <!--工具栏-->
                        <div class="pull-left">
                            <div class="form-group form-inline">
                                <div class="btn-group">
                                    <button type="button" class="btn btn-default" title="新建">
                                        <i class="fa fa-file-o"></i> 新建
                                    </button>
                                    <button type="button" class="btn btn-default" title="删除">
                                        <i class="fa fa-trash-o"></i> 删除
                                    </button>
                                    <button type="button" class="btn btn-default" title="开启">
                                        <i class="fa fa-check"></i> 开启
                                    </button>
                                    <button type="button" class="btn btn-default" title="屏蔽">
                                        <i class="fa fa-ban"></i> 屏蔽
                                    </button>
                                    <button type="button" class="btn btn-default" title="刷新">
                                        <i class="fa fa-refresh"></i> 刷新
                                    </button>
                                </div>
                            </div>
                        </div>
                        <div class="box-tools pull-right">
                            <div class="has-feedback">
                                <input type="text" class="form-control input-sm"
                                       placeholder="搜索"> <span
                                    class="glyphicon glyphicon-search form-control-feedback"></span>
                            </div>
                        </div>
                        <!--工具栏/-->

                    </div>
                    <!-- 数据表格 /-->


                </div>
                <!-- /.box-body -->

                <!-- .box-footer-->
                <div class="box-footer">
                    <div class="pull-left">
                        <div class="form-group form-inline">
                            总共2 页，共14 条数据。 每页
                            <select class="form-control">
                                <option>1</option>
                                <option>2</option>
                                <option>3</option>
                                <option>4</option>
                                <option>5</option>
                            </select> 条
                        </div>
                    </div>

                    <div class="box-tools pull-right">
                        <ul class="pagination">
                            <li>
                                <a href="#" aria-label="Previous">首页</a>
                            </li>
                            <li><a href="#">上一页</a></li>
                            <li><a href="#">1</a></li>
                            <li><a href="#">2</a></li>
                            <li><a href="#">3</a></li>
                            <li><a href="#">4</a></li>
                            <li><a href="#">5</a></li>
                            <li><a href="#">下一页</a></li>
                            <li>
                                <a href="#" aria-label="Next">尾页</a>
                            </li>
                        </ul>
                    </div>

                </div>
                <!-- /.box-footer-->


            </div>

        </section>
        <!-- 正文区域 /-->

    </div>
    <!-- @@close -->
    <!-- 内容区域 /-->

    <!-- 底部导航 -->
    <footer class="main-footer">
        <div class="pull-right hidden-xs">
            <b>Version</b> 1.0.8
        </div>
        <strong>Copyright &copy; 2014-2017 <a
                href="http://www.itcast.cn">研究院研发部</a>.
        </strong> All rights reserved.
    </footer>
    <!-- 底部导航 /-->

</div>


<script
        src="${pageContext.request.contextPath}/plugins/jQuery/jquery-2.2.3.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/jQueryUI/jquery-ui.min.js"></script>
<script>
    $.widget.bridge('uibutton', $.ui.button);
</script>
<script
        src="${pageContext.request.contextPath}/plugins/bootstrap/js/bootstrap.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/raphael/raphael-min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/morris/morris.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/sparkline/jquery.sparkline.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/jvectormap/jquery-jvectormap-1.2.2.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/jvectormap/jquery-jvectormap-world-mill-en.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/knob/jquery.knob.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/daterangepicker/moment.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/daterangepicker/daterangepicker.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/daterangepicker/daterangepicker.zh-CN.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/datepicker/bootstrap-datepicker.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/datepicker/locales/bootstrap-datepicker.zh-CN.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.all.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/slimScroll/jquery.slimscroll.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/fastclick/fastclick.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/iCheck/icheck.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/adminLTE/js/app.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/select2/select2.full.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/colorpicker/bootstrap-colorpicker.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/bootstrap-wysihtml5/bootstrap-wysihtml5.zh-CN.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/js/bootstrap-markdown.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/locale/bootstrap-markdown.zh.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/js/markdown.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/js/to-markdown.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/ckeditor/ckeditor.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/input-mask/jquery.inputmask.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/input-mask/jquery.inputmask.date.extensions.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/input-mask/jquery.inputmask.extensions.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/datatables/jquery.dataTables.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/datatables/dataTables.bootstrap.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/chartjs/Chart.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.resize.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.pie.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.categories.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/bootstrap-slider/bootstrap-slider.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/bootstrap-datetimepicker/bootstrap-datetimepicker.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/bootstrap-datetimepicker/locales/bootstrap-datetimepicker.zh-CN.js"></script>
<script>
    function changePageSize() {
        //获取下拉框的值
        var pageSize = $("#changePageSize").val();

        //向服务器发送请求，改变没页显示条数
        location.href = "${pageContext.request.contextPath}/orders/findAll.do?page=1&pageSize="
            + pageSize;
    }

    $(document).ready(function () {
        // 选择框
        $(".select2").select2();

        // WYSIHTML5编辑器
        $(".textarea").wysihtml5({
            locale: 'zh-CN'
        });
    });

    // 设置激活菜单
    function setSidebarActive(tagUri) {
        var liObj = $("#" + tagUri);
        if (liObj.length > 0) {
            liObj.parent().parent().addClass("active");
            liObj.addClass("active");
        }
    }

    $(document).ready(function () {

        // 激活导航位置
        setSidebarActive("admin-datalist");

        // 列表按钮
        $("#dataList td input[type='checkbox']").iCheck({
            checkboxClass: 'icheckbox_square-blue',
            increaseArea: '20%'
        });
        // 全选操作
        $("#selall").click(function () {
            var clicks = $(this).is(':checked');
            if (!clicks) {
                $("#dataList td input[type='checkbox']").iCheck("uncheck");
            } else {
                $("#dataList td input[type='checkbox']").iCheck("check");
            }
            $(this).data("clicks", !clicks);
        });
    });
</script>
</body>

</html>
```

###### orders-page-list.jsp

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>

<head>
    <!-- 页面meta -->
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">


    <title>数据 - AdminLTE2定制版</title>
    <meta name="description" content="AdminLTE2定制版">
    <meta name="keywords" content="AdminLTE2定制版">


    <!-- Tell the browser to be responsive to screen width -->
    <meta
            content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no"
            name="viewport">
    <!-- Bootstrap 3.3.6 -->
    <!-- Font Awesome -->
    <!-- Ionicons -->
    <!-- iCheck -->
    <!-- Morris chart -->
    <!-- jvectormap -->
    <!-- Date Picker -->
    <!-- Daterange picker -->
    <!-- Bootstrap time Picker -->
    <!--<link rel="stylesheet" href="${pageContext.request.contextPath}/${pageContext.request.contextPath}/${pageContext.request.contextPath}/plugins/timepicker/bootstrap-timepicker.min.css">-->
    <!-- bootstrap wysihtml5 - text editor -->
    <!--数据表格-->
    <!-- 表格树 -->
    <!-- select2 -->
    <!-- Bootstrap Color Picker -->
    <!-- bootstrap wysihtml5 - text editor -->
    <!--bootstrap-markdown-->
    <!-- Theme style -->
    <!-- AdminLTE Skins. Choose a skin from the css/skins
           folder instead of downloading all of them to reduce the load. -->
    <!-- Ion Slider -->
    <!-- ion slider Nice -->
    <!-- bootstrap slider -->
    <!-- bootstrap-datetimepicker -->

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/html5shiv/3.7.3/html5shiv.min.js"></script>
    <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->


    <!-- jQuery 2.2.3 -->
    <!-- jQuery UI 1.11.4 -->
    <!-- Resolve conflict in jQuery UI tooltip with Bootstrap tooltip -->
    <!-- Bootstrap 3.3.6 -->
    <!-- Morris.js charts -->
    <!-- Sparkline -->
    <!-- jvectormap -->
    <!-- jQuery Knob Chart -->
    <!-- daterangepicker -->
    <!-- datepicker -->
    <!-- Bootstrap WYSIHTML5 -->
    <!-- Slimscroll -->
    <!-- FastClick -->
    <!-- iCheck -->
    <!-- AdminLTE App -->
    <!-- 表格树 -->
    <!-- select2 -->
    <!-- bootstrap color picker -->
    <!-- bootstrap time picker -->
    <!--<script src="${pageContext.request.contextPath}/${pageContext.request.contextPath}/${pageContext.request.contextPath}/plugins/timepicker/bootstrap-timepicker.min.js"></script>-->
    <!-- Bootstrap WYSIHTML5 -->
    <!--bootstrap-markdown-->
    <!-- CK Editor -->
    <!-- InputMask -->
    <!-- DataTables -->
    <!-- ChartJS 1.0.1 -->
    <!-- FLOT CHARTS -->
    <!-- FLOT RESIZE PLUGIN - allows the chart to redraw when the window is resized -->
    <!-- FLOT PIE PLUGIN - also used to draw donut charts -->
    <!-- FLOT CATEGORIES PLUGIN - Used to draw bar charts -->
    <!-- jQuery Knob -->
    <!-- Sparkline -->
    <!-- Morris.js charts -->
    <!-- Ion Slider -->
    <!-- Bootstrap slider -->
    <!-- bootstrap-datetimepicker -->
    <!-- 页面meta /-->

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/font-awesome/css/font-awesome.min.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/ionicons/css/ionicons.min.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/iCheck/square/blue.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/morris/morris.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/jvectormap/jquery-jvectormap-1.2.2.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/datepicker/datepicker3.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/daterangepicker/daterangepicker.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/datatables/dataTables.bootstrap.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.theme.default.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/select2/select2.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/colorpicker/bootstrap-colorpicker.min.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/bootstrap-markdown/css/bootstrap-markdown.min.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/adminLTE/css/AdminLTE.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/adminLTE/css/skins/_all-skins.min.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.skinNice.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/bootstrap-slider/slider.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/plugins/bootstrap-datetimepicker/bootstrap-datetimepicker.css">
</head>

<body class="hold-transition skin-purple sidebar-mini">

<div class="wrapper">

    <!-- 页面头部 -->
    <jsp:include page="header.jsp"></jsp:include>
    <!-- 页面头部 /-->
    <!-- 导航侧栏 -->
    <jsp:include page="aside.jsp"></jsp:include>
    <!-- 导航侧栏 /-->

    <!-- 内容区域 -->
    <!-- @@master = admin-layout.html-->
    <!-- @@block = content -->

    <div class="content-wrapper">

        <!-- 内容头部 -->
        <section class="content-header">
            <h1>
                数据管理 <small>数据列表</small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="#"><i class="fa fa-dashboard"></i> 首页</a></li>
                <li><a href="#">数据管理</a></li>
                <li class="active">数据列表</li>
            </ol>
        </section>
        <!-- 内容头部 /-->

        <!-- 正文区域 -->
        <section class="content">

            <!-- .box-body -->
            <div class="box box-primary">
                <div class="box-header with-border">
                    <h3 class="box-title">列表</h3>
                </div>

                <div class="box-body">

                    <!-- 数据表格 -->
                    <div class="table-box">

                        <!--工具栏-->
                        <div class="pull-left">
                            <div class="form-group form-inline">
                                <div class="btn-group">
                                    <button type="button" class="btn btn-default" title="新建"
                                            onclick="location.href='${pageContext.request.contextPath}/pages/product-add.jsp'">
                                        <i class="fa fa-file-o"></i> 新建
                                    </button>
                                    <button type="button" class="btn btn-default" title="删除">
                                        <i class="fa fa-trash-o"></i> 删除
                                    </button>
                                    <button type="button" class="btn btn-default" title="开启">
                                        <i class="fa fa-check"></i> 开启
                                    </button>
                                    <button type="button" class="btn btn-default" title="屏蔽">
                                        <i class="fa fa-ban"></i> 屏蔽
                                    </button>
                                    <button type="button" class="btn btn-default" title="刷新">
                                        <i class="fa fa-refresh"></i> 刷新
                                    </button>
                                </div>
                            </div>
                        </div>
                        <div class="box-tools pull-right">
                            <div class="has-feedback">
                                <input type="text" class="form-control input-sm"
                                       placeholder="搜索"> <span
                                    class="glyphicon glyphicon-search form-control-feedback"></span>
                            </div>
                        </div>
                        <!--工具栏/-->

                        <!--数据列表-->
                        <table id="dataList"
                               class="table table-bordered table-striped table-hover dataTable">
                            <thead>
                            <tr>
                                <th class="" style="padding-right: 0px;"><input
                                        id="selall" type="checkbox" class="icheckbox_square-blue">
                                </th>
                                <th class="sorting_asc">ID</th>
                                <th class="sorting_desc">订单编号</th>
                                <th class="sorting_asc sorting_asc_disabled">产品名称</th>
                                <th class="sorting_desc sorting_desc_disabled">金额</th>
                                <th class="sorting">下单时间</th>
                                <th class="text-center sorting">订单状态</th>
                                <th class="text-center">操作</th>
                            </tr>
                            </thead>
                            <tbody>


                            <c:forEach items="${pageInfo.list}" var="orders">

                                <tr>
                                    <td><input name="ids" type="checkbox"></td>
                                    <td>${orders.id }</td>
                                    <td>${orders.orderNum }</td>
                                    <td>${orders.product.productName }</td>
                                    <td>${orders.product.productPrice }</td>
                                    <td>${orders.orderTimeStr }</td>
                                    <td class="text-center">${orders.orderStatusStr }</td>
                                    <td class="text-center">
                                        <button type="button" class="btn bg-olive btn-xs">订单</button>
                                        <button type="button" class="btn bg-olive btn-xs"
                                                onclick="location.href='${pageContext.request.contextPath}/orders/findDetail.do?id=${orders.id}'">
                                            详情
                                        </button>
                                        <button type="button" class="btn bg-olive btn-xs">编辑</button>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                            <!--
                        <tfoot>
                        <tr>
                        <th>Rendering engine</th>
                        <th>Browser</th>
                        <th>Platform(s)</th>
                        <th>Engine version</th>
                        <th>CSS grade</th>
                        </tr>
                        </tfoot>-->
                        </table>
                        <!--数据列表/-->

                        <!--工具栏-->
                        <div class="pull-left">
                            <div class="form-group form-inline">
                                <div class="btn-group">
                                    <button type="button" class="btn btn-default" title="新建">
                                        <i class="fa fa-file-o"></i> 新建
                                    </button>
                                    <button type="button" class="btn btn-default" title="删除">
                                        <i class="fa fa-trash-o"></i> 删除
                                    </button>
                                    <button type="button" class="btn btn-default" title="开启">
                                        <i class="fa fa-check"></i> 开启
                                    </button>
                                    <button type="button" class="btn btn-default" title="屏蔽">
                                        <i class="fa fa-ban"></i> 屏蔽
                                    </button>
                                    <button type="button" class="btn btn-default" title="刷新">
                                        <i class="fa fa-refresh"></i> 刷新
                                    </button>
                                </div>
                            </div>
                        </div>
                        <div class="box-tools pull-right">
                            <div class="has-feedback">
                                <input type="text" class="form-control input-sm"
                                       placeholder="搜索"> <span
                                    class="glyphicon glyphicon-search form-control-feedback"></span>
                            </div>
                        </div>
                        <!--工具栏/-->

                    </div>
                    <!-- 数据表格 /-->


                </div>
                <!-- /.box-body -->

                <!-- .box-footer-->
                <div class="box-footer">
                    <div class="pull-left">
                        <div class="form-group form-inline">
                            总共${pageInfo.pages} 页，共${pageInfo.total} 条数据。 每页
                            <select class="form-control" id="changePageSize" onchange="changePageSize()">
                                <c:if test="${pageInfo.pageSize==1}">
                                    <option selected>1</option>
                                    <option>2</option>
                                    <option>3</option>
                                    <option>4</option>
                                    <option>5</option>
                                </c:if>
                                <c:if test="${pageInfo.pageSize==2}">
                                    <option>1</option>
                                    <option selected>2</option>
                                    <option>3</option>
                                    <option>4</option>
                                    <option>5</option>
                                </c:if>
                                <c:if test="${pageInfo.pageSize==3}">
                                    <option>1</option>
                                    <option>2</option>
                                    <option selected>3</option>
                                    <option>4</option>
                                    <option>5</option>
                                </c:if>
                                <c:if test="${pageInfo.pageSize==4}">
                                    <option>1</option>
                                    <option>2</option>
                                    <option>3</option>
                                    <option selected>4</option>
                                    <option>5</option>
                                </c:if>
                                <c:if test="${pageInfo.pageSize==5}">
                                    <option>1</option>
                                    <option>2</option>
                                    <option>3</option>
                                    <option>4</option>
                                    <option selected>5</option>
                                </c:if>
                            </select> 条
                        </div>
                    </div>

                    <div class="box-tools pull-right">
                        <ul class="pagination">
                            <li>
                                <a href="${pageContext.request.contextPath}/orders/findAll.do?page=1&size=${pageInfo.pageSize}" aria-label="Previous">首页</a>
                            </li>
                            <li><a href="${pageContext.request.contextPath}/orders/findAll.do?page=${pageInfo.pageNum-1}&size=${pageInfo.pageSize}">上一页</a></li>
                            <c:forEach begin="1" end="${pageInfo.pages}" var="pageNum">
                                <li><a href="${pageContext.request.contextPath}/orders/findAll.do?page=${pageNum}&size=${pageInfo.pageSize}">${pageNum}</a></li>
                            </c:forEach>
                            <li><a href="${pageContext.request.contextPath}/orders/findAll.do?page=${pageInfo.pageNum+1}&size=${pageInfo.pageSize}">下一页</a></li>
                            <li>
                                <a href="${pageContext.request.contextPath}/orders/findAll.do?page=${pageInfo.pages}&size=${pageInfo.pageSize}" aria-label="Next">尾页</a>
                            </li>
                        </ul>
                    </div>

                </div>
                <!-- /.box-footer-->


            </div>

        </section>
        <!-- 正文区域 /-->

    </div>
    <!-- @@close -->
    <!-- 内容区域 /-->

    <!-- 底部导航 -->
    <footer class="main-footer">
        <div class="pull-right hidden-xs">
            <b>Version</b> 1.0.8
        </div>
        <strong>Copyright &copy; 2014-2017 <a
                href="http://www.itcast.cn">研究院研发部</a>.
        </strong> All rights reserved.
    </footer>
    <!-- 底部导航 /-->

</div>


<script
        src="${pageContext.request.contextPath}/plugins/jQuery/jquery-2.2.3.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/jQueryUI/jquery-ui.min.js"></script>
<script>
    $.widget.bridge('uibutton', $.ui.button);
</script>
<script
        src="${pageContext.request.contextPath}/plugins/bootstrap/js/bootstrap.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/raphael/raphael-min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/morris/morris.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/sparkline/jquery.sparkline.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/jvectormap/jquery-jvectormap-1.2.2.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/jvectormap/jquery-jvectormap-world-mill-en.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/knob/jquery.knob.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/daterangepicker/moment.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/daterangepicker/daterangepicker.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/daterangepicker/daterangepicker.zh-CN.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/datepicker/bootstrap-datepicker.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/datepicker/locales/bootstrap-datepicker.zh-CN.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.all.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/slimScroll/jquery.slimscroll.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/fastclick/fastclick.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/iCheck/icheck.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/adminLTE/js/app.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/select2/select2.full.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/colorpicker/bootstrap-colorpicker.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/bootstrap-wysihtml5/bootstrap-wysihtml5.zh-CN.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/js/bootstrap-markdown.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/locale/bootstrap-markdown.zh.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/js/markdown.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/js/to-markdown.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/ckeditor/ckeditor.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/input-mask/jquery.inputmask.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/input-mask/jquery.inputmask.date.extensions.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/input-mask/jquery.inputmask.extensions.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/datatables/jquery.dataTables.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/datatables/dataTables.bootstrap.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/chartjs/Chart.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.resize.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.pie.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.categories.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.min.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/bootstrap-slider/bootstrap-slider.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/bootstrap-datetimepicker/bootstrap-datetimepicker.js"></script>
<script
        src="${pageContext.request.contextPath}/plugins/bootstrap-datetimepicker/locales/bootstrap-datetimepicker.zh-CN.js"></script>
<script>
    function changePageSize() {
        //获取下拉框的值
        var pageSize = $("#changePageSize").val();

        //向服务器发送请求，改变没页显示条数
        location.href = "${pageContext.request.contextPath}/orders/findAll.do?page=1&size="
            + pageSize;
    }

    $(document).ready(function () {
        // 选择框
        $(".select2").select2();

        // WYSIHTML5编辑器
        $(".textarea").wysihtml5({
            locale: 'zh-CN'
        });
    });

    // 设置激活菜单
    function setSidebarActive(tagUri) {
        var liObj = $("#" + tagUri);
        if (liObj.length > 0) {
            liObj.parent().parent().addClass("active");
            liObj.addClass("active");
        }
    }

    $(document).ready(function () {

        // 激活导航位置
        setSidebarActive("admin-datalist");

        // 列表按钮
        $("#dataList td input[type='checkbox']").iCheck({
            checkboxClass: 'icheckbox_square-blue',
            increaseArea: '20%'
        });
        // 全选操作
        $("#selall").click(function () {
            var clicks = $(this).is(':checked');
            if (!clicks) {
                $("#dataList td input[type='checkbox']").iCheck("uncheck");
            } else {
                $("#dataList td input[type='checkbox']").iCheck("check");
            }
            $(this).data("clicks", !clicks);
        });
    });
</script>
</body>

</html>
```

###### orders-show.jsp

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8"
   pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<!-- 页面meta -->
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">

<title>数据 - AdminLTE2定制版</title>
<meta name="description" content="AdminLTE2定制版">
<meta name="keywords" content="AdminLTE2定制版">

<!-- Tell the browser to be responsive to screen width -->
<meta
   content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no"
   name="viewport">

<link rel=“stylesheet”
   href="${pageContext.request.contextPath}/plugins/bootstrap-datetimepicker/bootstrap-datetimepicker.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap/css/bootstrap.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/font-awesome/css/font-awesome.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/ionicons/css/ionicons.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/iCheck/square/blue.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/morris/morris.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/jvectormap/jquery-jvectormap-1.2.2.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/datepicker/datepicker3.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/daterangepicker/daterangepicker.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/datatables/dataTables.bootstrap.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.theme.default.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/select2/select2.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/colorpicker/bootstrap-colorpicker.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap-markdown/css/bootstrap-markdown.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/adminLTE/css/AdminLTE.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/adminLTE/css/skins/_all-skins.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/css/style.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.skinNice.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap-slider/slider.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap-datetimepicker/bootstrap-datetimepicker.css">
</head>

<body class="hold-transition skin-blue sidebar-mini">

   <div class="wrapper">

      <!-- 页面头部 -->
      <jsp:include page="header.jsp"></jsp:include>
      <!-- 页面头部 /-->

      <!-- 导航侧栏 -->
      <jsp:include page="aside.jsp"></jsp:include>
      <!-- 导航侧栏 /-->

      <!-- 内容区域 -->
      <div class="content-wrapper">

         <!-- 内容头部 -->
         <section class="content-header">
         <h1>
            订单管理 <small>全部订单</small>
         </h1>
         <ol class="breadcrumb">
            <li><a href="all-admin-index.html"><i
                  class="fa fa-dashboard"></i> 首页</a></li>
            <li><a href="all-order-manage-list.html">订单管理</a></li>
            <li class="active">订单详情</li>
         </ol>
         </section>
         <!-- 内容头部 /-->

         <!-- 正文区域 -->
         <section class="content"> <!--订单信息-->
         <div class="panel panel-default">
            <div class="panel-heading">订单信息</div>
            <div class="row data-type">

               <div class="col-md-2 title">订单编号</div>
               <div class="col-md-4 data">
                  <input type="text" class="form-control" placeholder="订单编号"
                     value="${orders.orderNum }" readonly="readonly">
               </div>

               <div class="col-md-2 title">下单时间</div>
               <div class="col-md-4 data">
                  <div class="input-group date">
                     <div class="input-group-addon">
                        <i class="fa fa-calendar"></i>
                     </div>
                     <input type="text" class="form-control pull-right"
                        id="datepicker-a3" readonly="readonly"
                        value="${orders.orderTimeStr}">
                  </div>
               </div>
               <div class="col-md-2 title">路线名称</div>
               <div class="col-md-4 data">
                  <input type="text" class="form-control" placeholder="路线名称"
                     value="${orders.product.productName }" readonly="readonly">
               </div>

               <div class="col-md-2 title">出发城市</div>
               <div class="col-md-4 data">
                  <input type="text" class="form-control" placeholder="出发城市"
                     value="${orders.product.cityName }" readonly="readonly">
               </div>

               <div class="col-md-2 title">出发时间</div>
               <div class="col-md-4 data">
                  <div class="input-group date">
                     <div class="input-group-addon">
                        <i class="fa fa-calendar"></i>
                     </div>
                     <input type="text" class="form-control pull-right"
                        id="datepicker-a6" value="${orders.product.departureTimeStr}"
                        readonly="readonly">
                  </div>
               </div>
               <div class="col-md-2 title">出游人数</div>
               <div class="col-md-4 data">
                  <input type="text" class="form-control" placeholder="出游人数"
                     value="${orders.peopleCount}" readonly="readonly">
               </div>

               <div class="col-md-2 title rowHeight2x">其他信息</div>
               <div class="col-md-10 data rowHeight2x">
                  <textarea class="form-control" rows="3" placeholder="其他信息">
                     ${orders.orderDesc }
                  </textarea>
               </div>

            </div>
         </div>
         <!--订单信息/--> <!--游客信息-->
         <div class="panel panel-default">
            <div class="panel-heading">游客信息</div>
            <!--数据列表-->
            <table id="dataList"
               class="table table-bordered table-striped table-hover dataTable">
               <thead>
                  <tr>
                     <th class="">人群</th>
                     <th class="">姓名</th>
                     <th class="">性别</th>
                     <th class="">手机号码</th>
                     <th class="">证件类型</th>
                     <th class="">证件号码</th>
                  </tr>
               </thead>
               <tbody>
                  <c:forEach var="traveller" items="${orders.travellers}">

                     <tr>
                        <td>${traveller.travellerTypeStr}</td>
                        <td><input type="text" size="10" value="${traveller.name}"
                           readonly="readonly"></td>
                        <td><input type="text" size="10" value="${traveller.sex}"
                           readonly="readonly"></td>
                        <td><input type="text" size="20"
                           value="${traveller.phoneNum}" readonly="readonly"></td>
                        <td><input type="text" size="15"
                           value="${traveller.credentialsTypeStr}" readonly="readonly"></td>
                        <td><input type="text" size="28"
                           value="${traveller.credentialsNum }" readonly="readonly"></td>
                     </tr>
                  </c:forEach>


               </tbody>
            </table>
            <!--数据列表/-->
         </div>
         <!--游客信息/--> <!--联系人信息-->
         <div class="panel panel-default">
            <div class="panel-heading">联系人信息</div>
            <div class="row data-type">

               <div class="col-md-2 title">会员</div>
               <div class="col-md-4 data text">${orders.member.nickname }</div>

               <div class="col-md-2 title">联系人</div>
               <div class="col-md-4 data text">${orders.member.name}</div>

               <div class="col-md-2 title">手机号</div>
               <div class="col-md-4 data text">${orders.member.phoneNum}</div>

               <div class="col-md-2 title">邮箱</div>
               <div class="col-md-4 data text">${orders.member.email}</div>

            </div>
         </div>
         <!--联系人信息/--> <!--费用信息--> <c:if test="${orders.orderStatus==1}">
            <div class="panel panel-default">
               <div class="panel-heading">费用信息</div>
               <div class="row data-type">

                  <div class="col-md-2 title">支付方式</div>
                  <div class="col-md-4 data text">在线支付-${orders.payTypeStr}</div>

                  <div class="col-md-2 title">金额</div>
                  <div class="col-md-4 data text">￥${orders.product.productPrice}</div>

               </div>
            </div>
         </c:if> <!--费用信息/--> <!--工具栏-->
         <div class="box-tools text-center">

            <button type="button" class="btn bg-default"
               onclick="history.back(-1);">返回</button>
         </div>
         <!--工具栏/--> </section>
         <!-- 正文区域 /-->


      </div>
      <!-- 内容区域 /-->

      <!-- 底部导航 -->
      <footer class="main-footer">
      <div class="pull-right hidden-xs">
         <b>Version</b> 1.0.8
      </div>
      <strong>Copyright &copy; 2014-2017 <a
         href="http://www.itcast.cn">研究院研发部</a>.
      </strong> All rights reserved. </footer>
      <!-- 底部导航 /-->

   </div>

   <script
      src="${pageContext.request.contextPath}/plugins/jQuery/jquery-2.2.3.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/jQueryUI/jquery-ui.min.js"></script>
   <script>
      $.widget.bridge('uibutton', $.ui.button);
   </script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap/js/bootstrap.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/raphael/raphael-min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/morris/morris.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/sparkline/jquery.sparkline.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/jvectormap/jquery-jvectormap-1.2.2.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/jvectormap/jquery-jvectormap-world-mill-en.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/knob/jquery.knob.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/daterangepicker/moment.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/daterangepicker/daterangepicker.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/daterangepicker/daterangepicker.zh-CN.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/datepicker/bootstrap-datepicker.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/datepicker/locales/bootstrap-datepicker.zh-CN.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.all.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/slimScroll/jquery.slimscroll.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/fastclick/fastclick.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/iCheck/icheck.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/adminLTE/js/app.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/select2/select2.full.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/colorpicker/bootstrap-colorpicker.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-wysihtml5/bootstrap-wysihtml5.zh-CN.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/js/bootstrap-markdown.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/locale/bootstrap-markdown.zh.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/js/markdown.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/js/to-markdown.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/ckeditor/ckeditor.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/input-mask/jquery.inputmask.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/input-mask/jquery.inputmask.date.extensions.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/input-mask/jquery.inputmask.extensions.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/datatables/jquery.dataTables.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/datatables/dataTables.bootstrap.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/chartjs/Chart.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.resize.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.pie.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.categories.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-slider/bootstrap-slider.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-datetimepicker/bootstrap-datetimepicker.min.js"></script>

   <script>
      $(document).ready(function() {
         // 选择框
         $(".select2").select2();

         // WYSIHTML5编辑器
         $(".textarea").wysihtml5({
            locale : 'zh-CN'
         });
      });

      // 设置激活菜单
      function setSidebarActive(tagUri) {
         var liObj = $("#" + tagUri);
         if (liObj.length > 0) {
            liObj.parent().parent().addClass("active");
            liObj.addClass("active");
         }
      }

      $(document).ready(function() {

         // 激活导航位置
         setSidebarActive("order-manage");

         // 列表按钮 
         $("#dataList td input[type='checkbox']").iCheck({
            checkboxClass : 'icheckbox_square-blue',
            increaseArea : '20%'
         });
         // 全选操作 
         $("#selall").click(function() {
            var clicks = $(this).is(':checked');
            if (!clicks) {
               $("#dataList td input[type='checkbox']").iCheck("uncheck");
            } else {
               $("#dataList td input[type='checkbox']").iCheck("check");
            }
            $(this).data("clicks", !clicks);
         });
      });
   </script>
</body>


</html>
```

##### 用户模块

###### login.jsp

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8"
   pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">

<title>数据 - AdminLTE2定制版 | Log in</title>

<meta
   content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no"
   name="viewport">

<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap/css/bootstrap.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/font-awesome/css/font-awesome.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/ionicons/css/ionicons.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/adminLTE/css/AdminLTE.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/iCheck/square/blue.css">
</head>

<body class="hold-transition login-page">
   <div class="login-box">
      <div class="login-logo">
         <a href="all-admin-index.html"><b>ITCAST</b>后台管理系统</a>
      </div>
      <!-- /.login-logo -->
      <div class="login-box-body">
         <p class="login-box-msg">登录系统</p>

         <form action="${pageContext.request.contextPath}/login.do" method="post">
            <div class="form-group has-feedback">
               <input type="text" name="username" class="form-control"
                  placeholder="用户名"> <span
                  class="glyphicon glyphicon-envelope form-control-feedback"></span>
            </div>
            <div class="form-group has-feedback">
               <input type="password" name="password" class="form-control"
                  placeholder="密码"> <span
                  class="glyphicon glyphicon-lock form-control-feedback"></span>
            </div>
            <div class="row">
               <div class="col-xs-8">
                  <div class="checkbox icheck">
                     <label><input type="checkbox"> 记住 下次自动登录</label>
                  </div>
               </div>
               <!-- /.col -->
               <div class="col-xs-4">
                  <button type="submit" class="btn btn-primary btn-block btn-flat">登录</button>
               </div>
               <!-- /.col -->
            </div>
         </form>

         <a href="#">忘记密码</a><br>


      </div>
      <!-- /.login-box-body -->
   </div>
   <!-- /.login-box -->

   <!-- jQuery 2.2.3 -->
   <!-- Bootstrap 3.3.6 -->
   <!-- iCheck -->
   <script
      src="${pageContext.request.contextPath}/plugins/jQuery/jquery-2.2.3.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap/js/bootstrap.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/iCheck/icheck.min.js"></script>
   <script>
      $(function() {
         $('input').iCheck({
            checkboxClass : 'icheckbox_square-blue',
            radioClass : 'iradio_square-blue',
            increaseArea : '20%' // optional
         });
      });
   </script>
</body>

</html>
```

###### failer.jsp

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8"
   pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>

<head>
<!-- 页面meta -->
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">

<title>数据 - AdminLTE2定制版</title>
<meta name="description" content="AdminLTE2定制版">
<meta name="keywords" content="AdminLTE2定制版">

<!-- Tell the browser to be responsive to screen width -->
<meta
   content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no"
   name="viewport">
```

###### user-list.jsp

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8"
   pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<!-- 页面meta -->
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">

<title>数据 - AdminLTE2定制版</title>
<meta name="description" content="AdminLTE2定制版">
<meta name="keywords" content="AdminLTE2定制版">

<!-- Tell the browser to be responsive to screen width -->
<meta
   content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no"
   name="viewport">

<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap/css/bootstrap.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/font-awesome/css/font-awesome.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/ionicons/css/ionicons.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/iCheck/square/blue.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/morris/morris.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/jvectormap/jquery-jvectormap-1.2.2.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/datepicker/datepicker3.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/daterangepicker/daterangepicker.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/datatables/dataTables.bootstrap.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.theme.default.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/select2/select2.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/colorpicker/bootstrap-colorpicker.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap-markdown/css/bootstrap-markdown.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/adminLTE/css/AdminLTE.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/adminLTE/css/skins/_all-skins.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/css/style.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.skinNice.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap-slider/slider.css">
</head>

<body class="hold-transition skin-blue sidebar-mini">

   <div class="wrapper">

      <!-- 页面头部 -->
      <jsp:include page="header.jsp"></jsp:include>
      <!-- 页面头部 /-->

      <!-- 导航侧栏 -->
      <jsp:include page="aside.jsp"></jsp:include>
      <!-- 导航侧栏 /-->

      <!-- 内容区域 -->
      <div class="content-wrapper">

         <!-- 内容头部 -->
         <section class="content-header">
         <h1>
            用户管理 <small>全部用户</small>
         </h1>
         <ol class="breadcrumb">
            <li><a href="${pageContext.request.contextPath}/index.jsp"><i
                  class="fa fa-dashboard"></i> 首页</a></li>
            <li><a
               href="${pageContext.request.contextPath}/user/findAll.do">用户管理</a></li>

            <li class="active">全部用户</li>
         </ol>
         </section>
         <!-- 内容头部 /-->

            <!-- 正文区域 -->
            <section class="content"> <!-- .box-body -->
            <div class="box box-primary">
               <div class="box-header with-border">
                  <h3 class="box-title">列表</h3>
               </div>

               <div class="box-body">

                  <!-- 数据表格 -->
                  <div class="table-box">

                     <!--工具栏-->
                     <div class="pull-left">
                        <div class="form-group form-inline">
                           <div class="btn-group">
                              <button type="button" class="btn btn-default" title="新建" onclick="location.href='${pageContext.request.contextPath}/pages/user-add.jsp'">
                                 <i class="fa fa-file-o"></i> 新建
                              </button>
                              
                              <button type="button" class="btn btn-default" title="刷新">
                                 <i class="fa fa-refresh"></i> 刷新
                              </button>
                           </div>
                        </div>
                     </div>
                     <div class="box-tools pull-right">
                        <div class="has-feedback">
                           <input type="text" class="form-control input-sm"
                              placeholder="搜索"> <span
                              class="glyphicon glyphicon-search form-control-feedback"></span>
                        </div>
                     </div>
                     <!--工具栏/-->

                     <!--数据列表-->
                     <table id="dataList"
                        class="table table-bordered table-striped table-hover dataTable">
                        <thead>
                           <tr>
                              <th class="" style="padding-right: 0px"><input
                                 id="selall" type="checkbox" class="icheckbox_square-blue">
                              </th>
                              <th class="sorting_asc">ID</th>
                              <th class="sorting_desc">用户名</th>
                              <th class="sorting_asc sorting_asc_disabled">邮箱</th>
                              <th class="sorting_desc sorting_desc_disabled">联系电话</th>
                              <th class="sorting">状态</th>
                              <th class="text-center">操作</th>
                           </tr>
                        </thead>
                        <tbody>

                           <c:forEach items="${userList}" var="user">
                              <tr>
                                 <td><input name="ids" type="checkbox"></td>
                                 <td>${user.id }</td>
                                 <td>${user.username }</td>
                                 <td>${user.email }</td>
                                 <td>${user.phoneNum }</td>
                                 <td>${user.statusStr }</td>                                  
                                 <td class="text-center">
                                    <a href="${pageContext.request.contextPath}/user/findById.do?id=${user.id}" class="btn bg-olive btn-xs">详情</a>
                                    <a href="${pageContext.request.contextPath}/user/findUserByIdAndAllRole.do?id=${user.id}" class="btn bg-olive btn-xs">添加角色</a>
                                 </td>
                              </tr>
                           </c:forEach>
                        </tbody>
                        <!--
                            <tfoot>
                            <tr>
                            <th>Rendering engine</th>
                            <th>Browser</th>
                            <th>Platform(s)</th>
                            <th>Engine version</th>
                            <th>CSS grade</th>
                            </tr>
                            </tfoot>-->
                     </table>
                     <!--数据列表/-->

                  </div>
                  <!-- 数据表格 /-->

               </div>
               <!-- /.box-body -->

               <!-- .box-footer-->
               <div class="box-footer">
                  <div class="pull-left">
                     <div class="form-group form-inline">
                        总共2 页，共14 条数据。 每页 <select class="form-control">
                           <option>1</option>
                           <option>2</option>
                           <option>3</option>
                           <option>4</option>
                           <option>5</option>
                        </select> 条
                     </div>
                  </div>

                  <div class="box-tools pull-right">
                     <ul class="pagination">
                        <li><a href="#" aria-label="Previous">首页</a></li>
                        <li><a href="#">上一页</a></li>
                        <li><a href="#">1</a></li>
                        <li><a href="#">2</a></li>
                        <li><a href="#">3</a></li>
                        <li><a href="#">4</a></li>
                        <li><a href="#">5</a></li>
                        <li><a href="#">下一页</a></li>
                        <li><a href="#" aria-label="Next">尾页</a></li>
                     </ul>
                  </div>

               </div>
               <!-- /.box-footer-->

            </div>

            </section>
            <!-- 正文区域 /-->

         </div>
         <!-- @@close -->
         <!-- 内容区域 /-->

         <!-- 底部导航 -->
         <footer class="main-footer">
         <div class="pull-right hidden-xs">
            <b>Version</b> 1.0.8
         </div>
         <strong>Copyright &copy; 2014-2017 <a
            href="http://www.itcast.cn">研究院研发部</a>.
         </strong> All rights reserved. </footer>
         <!-- 底部导航 /-->

      </div>

      <script src="../plugins/jQuery/jquery-2.2.3.min.js"></script>
      <script src="../plugins/jQueryUI/jquery-ui.min.js"></script>
      <script>
         $.widget.bridge('uibutton', $.ui.button);
      </script>
      <script src="../plugins/bootstrap/js/bootstrap.min.js"></script>
      <script src="../plugins/raphael/raphael-min.js"></script>
      <script src="../plugins/morris/morris.min.js"></script>
      <script src="../plugins/sparkline/jquery.sparkline.min.js"></script>
      <script src="../plugins/jvectormap/jquery-jvectormap-1.2.2.min.js"></script>
      <script src="../plugins/jvectormap/jquery-jvectormap-world-mill-en.js"></script>
      <script src="../plugins/knob/jquery.knob.js"></script>
      <script src="../plugins/daterangepicker/moment.min.js"></script>
      <script src="../plugins/daterangepicker/daterangepicker.js"></script>
      <script src="../plugins/daterangepicker/daterangepicker.zh-CN.js"></script>
      <script src="../plugins/datepicker/bootstrap-datepicker.js"></script>
      <script
         src="../plugins/datepicker/locales/bootstrap-datepicker.zh-CN.js"></script>
      <script
         src="../plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.all.min.js"></script>
      <script src="../plugins/slimScroll/jquery.slimscroll.min.js"></script>
      <script src="../plugins/fastclick/fastclick.js"></script>
      <script src="../plugins/iCheck/icheck.min.js"></script>
      <script src="../plugins/adminLTE/js/app.min.js"></script>
      <script src="../plugins/treeTable/jquery.treetable.js"></script>
      <script src="../plugins/select2/select2.full.min.js"></script>
      <script src="../plugins/colorpicker/bootstrap-colorpicker.min.js"></script>
      <script
         src="../plugins/bootstrap-wysihtml5/bootstrap-wysihtml5.zh-CN.js"></script>
      <script src="../plugins/bootstrap-markdown/js/bootstrap-markdown.js"></script>
      <script
         src="../plugins/bootstrap-markdown/locale/bootstrap-markdown.zh.js"></script>
      <script src="../plugins/bootstrap-markdown/js/markdown.js"></script>
      <script src="../plugins/bootstrap-markdown/js/to-markdown.js"></script>
      <script src="../plugins/ckeditor/ckeditor.js"></script>
      <script src="../plugins/input-mask/jquery.inputmask.js"></script>
      <script
         src="../plugins/input-mask/jquery.inputmask.date.extensions.js"></script>
      <script src="../plugins/input-mask/jquery.inputmask.extensions.js"></script>
      <script src="../plugins/datatables/jquery.dataTables.min.js"></script>
      <script src="../plugins/datatables/dataTables.bootstrap.min.js"></script>
      <script src="../plugins/chartjs/Chart.min.js"></script>
      <script src="../plugins/flot/jquery.flot.min.js"></script>
      <script src="../plugins/flot/jquery.flot.resize.min.js"></script>
      <script src="../plugins/flot/jquery.flot.pie.min.js"></script>
      <script src="../plugins/flot/jquery.flot.categories.min.js"></script>
      <script src="../plugins/ionslider/ion.rangeSlider.min.js"></script>
      <script src="../plugins/bootstrap-slider/bootstrap-slider.js"></script>
      <script>
         $(document).ready(function() {
            // 选择框
            $(".select2").select2();

            // WYSIHTML5编辑器
            $(".textarea").wysihtml5({
               locale : 'zh-CN'
            });
         });

         // 设置激活菜单
         function setSidebarActive(tagUri) {
            var liObj = $("#" + tagUri);
            if (liObj.length > 0) {
               liObj.parent().parent().addClass("active");
               liObj.addClass("active");
            }
         }

         $(document)
               .ready(
                     function() {

                        // 激活导航位置
                        setSidebarActive("admin-datalist");

                        // 列表按钮 
                        $("#dataList td input[type='checkbox']")
                              .iCheck(
                                    {
                                       checkboxClass : 'icheckbox_square-blue',
                                       increaseArea : '20%'
                                    });
                        // 全选操作 
                        $("#selall")
                              .click(
                                    function() {
                                       var clicks = $(this).is(
                                             ':checked');
                                       if (!clicks) {
                                          $(
                                                "#dataList td input[type='checkbox']")
                                                .iCheck(
                                                      "uncheck");
                                       } else {
                                          $(
                                                "#dataList td input[type='checkbox']")
                                                .iCheck("check");
                                       }
                                       $(this).data("clicks",
                                             !clicks);
                                    });
                     });
      </script>
</body>

</html>
```

###### user-show.jsp

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8"
   pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<!-- 页面meta -->
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">

<title>数据 - AdminLTE2定制版</title>
<meta name="description" content="AdminLTE2定制版">
<meta name="keywords" content="AdminLTE2定制版">

<!-- Tell the browser to be responsive to screen width -->
<meta
   content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no"
   name="viewport">

<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap/css/bootstrap.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/font-awesome/css/font-awesome.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/ionicons/css/ionicons.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/iCheck/square/blue.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/morris/morris.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/jvectormap/jquery-jvectormap-1.2.2.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/datepicker/datepicker3.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/daterangepicker/daterangepicker.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/datatables/dataTables.bootstrap.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.theme.default.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/select2/select2.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/colorpicker/bootstrap-colorpicker.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap-markdown/css/bootstrap-markdown.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/adminLTE/css/AdminLTE.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/adminLTE/css/skins/_all-skins.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/css/style.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.skinNice.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap-slider/slider.css">
</head>

<body class="hold-transition skin-blue sidebar-mini">

   <div class="wrapper">

      <!-- 页面头部 -->
      <jsp:include page="header.jsp"></jsp:include>
      <!-- 页面头部 /-->

      <!-- 导航侧栏 -->
      <jsp:include page="aside.jsp"></jsp:include>
      <!-- 导航侧栏 /-->

      <!-- 内容区域 -->
      <div class="content-wrapper">

         <!-- 内容头部 -->
         <section class="content-header">
         <h1>
            用户管理 <small>全部用户</small>
         </h1>
         <ol class="breadcrumb">
            <li><a href="${pageContext.request.contextPath}/index.jsp"><i
                  class="fa fa-dashboard"></i> 首页</a></li>
            <li><a
               href="${pageContext.request.contextPath}/user/findAll.do">用户管理</a></li>

            <li class="active">全部用户</li>
         </ol>
         </section>
         <!-- 内容头部 /-->

         <!-- 正文区域 -->
         <section class="content"> <!-- .box-body -->
         <div class="box box-primary">
            <div class="box-header with-border">
               <h3 class="box-title">列表</h3>
            </div>

            <div class="box-body">

               <!-- 数据表格 -->
               <div class="table-box">

                  <!--工具栏-->
                  <div class="pull-left">
                     <div class="form-group form-inline">
                        <div class="btn-group">
                           <button type="button" class="btn btn-default" title="新建">
                              <i class="fa fa-file-o"></i> 新建
                           </button>

                           <button type="button" class="btn btn-default" title="刷新">
                              <i class="fa fa-refresh"></i> 刷新
                           </button>
                        </div>
                     </div>
                  </div>
                  <div class="box-tools pull-right">
                     <div class="has-feedback">
                        <input type="text" class="form-control input-sm"
                           placeholder="搜索"> <span
                           class="glyphicon glyphicon-search form-control-feedback"></span>
                     </div>
                  </div>
                  <!--工具栏/-->

                  <!--数据列表-->
                  <div class="tab-pane" id="tab-treetable">
                     <table id="collapse-table"
                        class="table table-bordered table-hover dataTable">
                        <thead>
                           <tr>
                              <th>名称</th>
                              <th>描述</th>
                           </tr>
                        </thead>

                        <tr data-tt-id="0">
                           <td colspan="2">${user.username}</td>
                        </tr>

                        <tbody>
                           <c:forEach items="${user.roles}" var="role" varStatus="vs">
                              <tr data-tt-id="${vs.index+1}" data-tt-parent-id="0">
                                 <td>${role.roleName }</td>
                                 <td>${role.roleDesc }</td>
                              </tr>
                              <c:forEach items="${role.permissions}" var="permission">
                                 <tr data-tt-id="1-1" data-tt-parent-id="${vs.index+1}">
                                    <td>${permission.permissionName}</td>
                                    <td>${permission.url}</td>
                                 </tr>

                              </c:forEach>
                           </c:forEach>
                        </tbody>
                     </table>
                  </div>
                  <!--数据列表/-->

               </div>
               <!-- 数据表格 /-->

            </div>
            <!-- /.box-body -->

            <!-- .box-footer-->
            <div class="box-footer">
               <div class="pull-left">
                  <div class="form-group form-inline">
                     总共2 页，共14 条数据。 每页 <select class="form-control">
                        <option>1</option>
                        <option>2</option>
                        <option>3</option>
                        <option>4</option>
                        <option>5</option>
                     </select> 条
                  </div>
               </div>

               <div class="box-tools pull-right">
                  <ul class="pagination">
                     <li><a href="#" aria-label="Previous">首页</a></li>
                     <li><a href="#">上一页</a></li>
                     <li><a href="#">1</a></li>
                     <li><a href="#">2</a></li>
                     <li><a href="#">3</a></li>
                     <li><a href="#">4</a></li>
                     <li><a href="#">5</a></li>
                     <li><a href="#">下一页</a></li>
                     <li><a href="#" aria-label="Next">尾页</a></li>
                  </ul>
               </div>

            </div>
            <!-- /.box-footer-->

         </div>

         </section>
         <!-- 正文区域 /-->

      </div>
      <!-- @@close -->
      <!-- 内容区域 /-->

      <!-- 底部导航 -->
      <footer class="main-footer">
      <div class="pull-right hidden-xs">
         <b>Version</b> 1.0.8
      </div>
      <strong>Copyright &copy; 2014-2017 <a
         href="http://www.itcast.cn">研究院研发部</a>.
      </strong> All rights reserved. </footer>
      <!-- 底部导航 /-->

   </div>

   <script src="../plugins/jQuery/jquery-2.2.3.min.js"></script>
   <script src="../plugins/jQueryUI/jquery-ui.min.js"></script>
   <script>
      $.widget.bridge('uibutton', $.ui.button);
   </script>
   <script src="../plugins/bootstrap/js/bootstrap.min.js"></script>
   <script src="../plugins/raphael/raphael-min.js"></script>
   <script src="../plugins/morris/morris.min.js"></script>
   <script src="../plugins/sparkline/jquery.sparkline.min.js"></script>
   <script src="../plugins/jvectormap/jquery-jvectormap-1.2.2.min.js"></script>
   <script src="../plugins/jvectormap/jquery-jvectormap-world-mill-en.js"></script>
   <script src="../plugins/knob/jquery.knob.js"></script>
   <script src="../plugins/daterangepicker/moment.min.js"></script>
   <script src="../plugins/daterangepicker/daterangepicker.js"></script>
   <script src="../plugins/daterangepicker/daterangepicker.zh-CN.js"></script>
   <script src="../plugins/datepicker/bootstrap-datepicker.js"></script>
   <script
      src="../plugins/datepicker/locales/bootstrap-datepicker.zh-CN.js"></script>
   <script
      src="../plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.all.min.js"></script>
   <script src="../plugins/slimScroll/jquery.slimscroll.min.js"></script>
   <script src="../plugins/fastclick/fastclick.js"></script>
   <script src="../plugins/iCheck/icheck.min.js"></script>
   <script src="../plugins/adminLTE/js/app.min.js"></script>
   <script src="../plugins/treeTable/jquery.treetable.js"></script>
   <script src="../plugins/select2/select2.full.min.js"></script>
   <script src="../plugins/colorpicker/bootstrap-colorpicker.min.js"></script>
   <script
      src="../plugins/bootstrap-wysihtml5/bootstrap-wysihtml5.zh-CN.js"></script>
   <script src="../plugins/bootstrap-markdown/js/bootstrap-markdown.js"></script>
   <script
      src="../plugins/bootstrap-markdown/locale/bootstrap-markdown.zh.js"></script>
   <script src="../plugins/bootstrap-markdown/js/markdown.js"></script>
   <script src="../plugins/bootstrap-markdown/js/to-markdown.js"></script>
   <script src="../plugins/ckeditor/ckeditor.js"></script>
   <script src="../plugins/input-mask/jquery.inputmask.js"></script>
   <script src="../plugins/input-mask/jquery.inputmask.date.extensions.js"></script>
   <script src="../plugins/input-mask/jquery.inputmask.extensions.js"></script>
   <script src="../plugins/datatables/jquery.dataTables.min.js"></script>
   <script src="../plugins/datatables/dataTables.bootstrap.min.js"></script>
   <script src="../plugins/chartjs/Chart.min.js"></script>
   <script src="../plugins/flot/jquery.flot.min.js"></script>
   <script src="../plugins/flot/jquery.flot.resize.min.js"></script>
   <script src="../plugins/flot/jquery.flot.pie.min.js"></script>
   <script src="../plugins/flot/jquery.flot.categories.min.js"></script>
   <script src="../plugins/ionslider/ion.rangeSlider.min.js"></script>
   <script src="../plugins/bootstrap-slider/bootstrap-slider.js"></script>
   <script>
      $(document).ready(function() {
         // 选择框
         $(".select2").select2();

         // WYSIHTML5编辑器
         $(".textarea").wysihtml5({
            locale : 'zh-CN'
         });
         $("#collapse-table").treetable({
            expandable : true
         });
      });

      // 设置激活菜单
      function setSidebarActive(tagUri) {
         var liObj = $("#" + tagUri);
         if (liObj.length > 0) {
            liObj.parent().parent().addClass("active");
            liObj.addClass("active");
         }
      }

      $(document).ready(function() {

         // 激活导航位置
         setSidebarActive("admin-datalist");

         // 列表按钮 
         $("#dataList td input[type='checkbox']").iCheck({
            checkboxClass : 'icheckbox_square-blue',
            increaseArea : '20%'
         });
         // 全选操作 
         $("#selall").click(function() {
            var clicks = $(this).is(':checked');
            if (!clicks) {
               $("#dataList td input[type='checkbox']").iCheck("uncheck");
            } else {
               $("#dataList td input[type='checkbox']").iCheck("check");
            }
            $(this).data("clicks", !clicks);
         });
      });
   </script>
</body>

</html>
```

##### 权限模块

###### role-list.jsp

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8"
   pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<!-- 页面meta -->
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">

<title>数据 - AdminLTE2定制版</title>
<meta name="description" content="AdminLTE2定制版">
<meta name="keywords" content="AdminLTE2定制版">

<!-- Tell the browser to be responsive to screen width -->
<meta
   content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no"
   name="viewport">

<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap/css/bootstrap.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/font-awesome/css/font-awesome.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/ionicons/css/ionicons.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/iCheck/square/blue.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/morris/morris.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/jvectormap/jquery-jvectormap-1.2.2.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/datepicker/datepicker3.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/daterangepicker/daterangepicker.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/datatables/dataTables.bootstrap.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.theme.default.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/select2/select2.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/colorpicker/bootstrap-colorpicker.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap-markdown/css/bootstrap-markdown.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/adminLTE/css/AdminLTE.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/adminLTE/css/skins/_all-skins.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/css/style.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.skinNice.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap-slider/slider.css">
</head>

<body class="hold-transition skin-blue sidebar-mini">

   <div class="wrapper">

      <!-- 页面头部 -->
      <jsp:include page="header.jsp"></jsp:include>
      <!-- 页面头部 /-->

      <!-- 导航侧栏 -->
      <jsp:include page="aside.jsp"></jsp:include>
      <!-- 导航侧栏 /-->

      <!-- 内容区域 -->
      <div class="content-wrapper">

         <!-- 内容头部 -->
         <section class="content-header">
         <h1>
            角色管理 <small>全部角色</small>
         </h1>
         <ol class="breadcrumb">
            <li><a href="${pageContext.request.contextPath}/index.jsp"><i
                  class="fa fa-dashboard"></i> 首页</a></li>
            <li><a
               href="${pageContext.request.contextPath}/role/findAll.do">角色管理</a></li>

            <li class="active">全部角色</li>
         </ol>
         </section>
         <!-- 内容头部 /-->

            <!-- 正文区域 -->
            <section class="content"> <!-- .box-body -->
            <div class="box box-primary">
               <div class="box-header with-border">
                  <h3 class="box-title">列表</h3>
               </div>

               <div class="box-body">

                  <!-- 数据表格 -->
                  <div class="table-box">

                     <!--工具栏-->
                     <div class="pull-left">
                        <div class="form-group form-inline">
                           <div class="btn-group">
                              <button type="button" class="btn btn-default" title="新建" onclick="location.href='${pageContext.request.contextPath}/pages/role-add.jsp'">
                                 <i class="fa fa-file-o"></i> 新建
                              </button>
                              
                              <button type="button" class="btn btn-default" title="刷新">
                                 <i class="fa fa-refresh"></i> 刷新
                              </button>
                           </div>
                        </div>
                     </div>
                     <div class="box-tools pull-right">
                        <div class="has-feedback">
                           <input type="text" class="form-control input-sm"
                              placeholder="搜索"> <span
                              class="glyphicon glyphicon-search form-control-feedback"></span>
                        </div>
                     </div>
                     <!--工具栏/-->

                     <!--数据列表-->
                     <table id="dataList"
                        class="table table-bordered table-striped table-hover dataTable">
                        <thead>
                           <tr>
                              <th class="" style="padding-right: 0px"><input
                                 id="selall" type="checkbox" class="icheckbox_square-blue">
                              </th>
                              <th class="sorting_asc">ID</th>
                              <th class="sorting_desc">角色名称</th>
                              <th class="sorting_asc sorting_asc_disabled">描述</th>                              
                              <th class="text-center">操作</th>
                           </tr>
                        </thead>
                        <tbody>

                           <c:forEach items="${roleList}" var="role">
                              <tr>
                                 <td><input name="ids" type="checkbox"></td>
                                 <td>${role.id }</td>
                                 <td>${role.roleName }</td>
                                 <td>${role.roleDesc }</td>                                                          
                                 <td class="text-center">
                                    <a href="${pageContext.request.contextPath}/role/findById.do?id=${role.id}" class="btn bg-olive btn-xs">详情</a>
                                    <a href="${pageContext.request.contextPath}/role/findRoleByIdAndAllPermission.do?id=${role.id}" class="btn bg-olive btn-xs">添加权限</a>
                                 </td>
                              </tr>
                           </c:forEach>
                        </tbody>
                        <!--
                            <tfoot>
                            <tr>
                            <th>Rendering engine</th>
                            <th>Browser</th>
                            <th>Platform(s)</th>
                            <th>Engine version</th>
                            <th>CSS grade</th>
                            </tr>
                            </tfoot>-->
                     </table>
                     <!--数据列表/-->

                  </div>
                  <!-- 数据表格 /-->

               </div>
               <!-- /.box-body -->

               <!-- .box-footer-->
               <div class="box-footer">
                  <div class="pull-left">
                     <div class="form-group form-inline">
                        总共2 页，共14 条数据。 每页 <select class="form-control">
                           <option>1</option>
                           <option>2</option>
                           <option>3</option>
                           <option>4</option>
                           <option>5</option>
                        </select> 条
                     </div>
                  </div>

                  <div class="box-tools pull-right">
                     <ul class="pagination">
                        <li><a href="#" aria-label="Previous">首页</a></li>
                        <li><a href="#">上一页</a></li>
                        <li><a href="#">1</a></li>
                        <li><a href="#">2</a></li>
                        <li><a href="#">3</a></li>
                        <li><a href="#">4</a></li>
                        <li><a href="#">5</a></li>
                        <li><a href="#">下一页</a></li>
                        <li><a href="#" aria-label="Next">尾页</a></li>
                     </ul>
                  </div>

               </div>
               <!-- /.box-footer-->

            </div>

            </section>
            <!-- 正文区域 /-->

         </div>
         <!-- @@close -->
         <!-- 内容区域 /-->

         <!-- 底部导航 -->
         <footer class="main-footer">
         <div class="pull-right hidden-xs">
            <b>Version</b> 1.0.8
         </div>
         <strong>Copyright &copy; 2014-2017 <a
            href="http://www.itcast.cn">研究院研发部</a>.
         </strong> All rights reserved. </footer>
         <!-- 底部导航 /-->

      </div>

      <script src="../plugins/jQuery/jquery-2.2.3.min.js"></script>
      <script src="../plugins/jQueryUI/jquery-ui.min.js"></script>
      <script>
         $.widget.bridge('uibutton', $.ui.button);
      </script>
      <script src="../plugins/bootstrap/js/bootstrap.min.js"></script>
      <script src="../plugins/raphael/raphael-min.js"></script>
      <script src="../plugins/morris/morris.min.js"></script>
      <script src="../plugins/sparkline/jquery.sparkline.min.js"></script>
      <script src="../plugins/jvectormap/jquery-jvectormap-1.2.2.min.js"></script>
      <script src="../plugins/jvectormap/jquery-jvectormap-world-mill-en.js"></script>
      <script src="../plugins/knob/jquery.knob.js"></script>
      <script src="../plugins/daterangepicker/moment.min.js"></script>
      <script src="../plugins/daterangepicker/daterangepicker.js"></script>
      <script src="../plugins/daterangepicker/daterangepicker.zh-CN.js"></script>
      <script src="../plugins/datepicker/bootstrap-datepicker.js"></script>
      <script
         src="../plugins/datepicker/locales/bootstrap-datepicker.zh-CN.js"></script>
      <script
         src="../plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.all.min.js"></script>
      <script src="../plugins/slimScroll/jquery.slimscroll.min.js"></script>
      <script src="../plugins/fastclick/fastclick.js"></script>
      <script src="../plugins/iCheck/icheck.min.js"></script>
      <script src="../plugins/adminLTE/js/app.min.js"></script>
      <script src="../plugins/treeTable/jquery.treetable.js"></script>
      <script src="../plugins/select2/select2.full.min.js"></script>
      <script src="../plugins/colorpicker/bootstrap-colorpicker.min.js"></script>
      <script
         src="../plugins/bootstrap-wysihtml5/bootstrap-wysihtml5.zh-CN.js"></script>
      <script src="../plugins/bootstrap-markdown/js/bootstrap-markdown.js"></script>
      <script
         src="../plugins/bootstrap-markdown/locale/bootstrap-markdown.zh.js"></script>
      <script src="../plugins/bootstrap-markdown/js/markdown.js"></script>
      <script src="../plugins/bootstrap-markdown/js/to-markdown.js"></script>
      <script src="../plugins/ckeditor/ckeditor.js"></script>
      <script src="../plugins/input-mask/jquery.inputmask.js"></script>
      <script
         src="../plugins/input-mask/jquery.inputmask.date.extensions.js"></script>
      <script src="../plugins/input-mask/jquery.inputmask.extensions.js"></script>
      <script src="../plugins/datatables/jquery.dataTables.min.js"></script>
      <script src="../plugins/datatables/dataTables.bootstrap.min.js"></script>
      <script src="../plugins/chartjs/Chart.min.js"></script>
      <script src="../plugins/flot/jquery.flot.min.js"></script>
      <script src="../plugins/flot/jquery.flot.resize.min.js"></script>
      <script src="../plugins/flot/jquery.flot.pie.min.js"></script>
      <script src="../plugins/flot/jquery.flot.categories.min.js"></script>
      <script src="../plugins/ionslider/ion.rangeSlider.min.js"></script>
      <script src="../plugins/bootstrap-slider/bootstrap-slider.js"></script>
      <script>
         $(document).ready(function() {
            // 选择框
            $(".select2").select2();

            // WYSIHTML5编辑器
            $(".textarea").wysihtml5({
               locale : 'zh-CN'
            });
         });

         // 设置激活菜单
         function setSidebarActive(tagUri) {
            var liObj = $("#" + tagUri);
            if (liObj.length > 0) {
               liObj.parent().parent().addClass("active");
               liObj.addClass("active");
            }
         }

         $(document)
               .ready(
                     function() {

                        // 激活导航位置
                        setSidebarActive("admin-datalist");

                        // 列表按钮 
                        $("#dataList td input[type='checkbox']")
                              .iCheck(
                                    {
                                       checkboxClass : 'icheckbox_square-blue',
                                       increaseArea : '20%'
                                    });
                        // 全选操作 
                        $("#selall")
                              .click(
                                    function() {
                                       var clicks = $(this).is(
                                             ':checked');
                                       if (!clicks) {
                                          $(
                                                "#dataList td input[type='checkbox']")
                                                .iCheck(
                                                      "uncheck");
                                       } else {
                                          $(
                                                "#dataList td input[type='checkbox']")
                                                .iCheck("check");
                                       }
                                       $(this).data("clicks",
                                             !clicks);
                                    });
                     });
      </script>
</body>

</html>
```

###### role-add.jsp

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8"
   pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<!-- 页面meta -->
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<title>数据 - AdminLTE2定制版</title>
<meta name="description" content="AdminLTE2定制版">
<meta name="keywords" content="AdminLTE2定制版">

<!-- Tell the browser to be responsive to screen width -->
<meta
   content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no"
   name="viewport">


<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap/css/bootstrap.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/font-awesome/css/font-awesome.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/ionicons/css/ionicons.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/iCheck/square/blue.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/morris/morris.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/jvectormap/jquery-jvectormap-1.2.2.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/datepicker/datepicker3.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/daterangepicker/daterangepicker.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/datatables/dataTables.bootstrap.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.theme.default.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/select2/select2.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/colorpicker/bootstrap-colorpicker.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap-markdown/css/bootstrap-markdown.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/adminLTE/css/AdminLTE.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/adminLTE/css/skins/_all-skins.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/css/style.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.skinNice.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap-slider/slider.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap-datetimepicker/bootstrap-datetimepicker.css">
</head>

   <body class="hold-transition skin-purple sidebar-mini">

   <div class="wrapper">

      <!-- 页面头部 -->
      <jsp:include page="header.jsp"></jsp:include>
      <!-- 页面头部 /-->
      <!-- 导航侧栏 -->
      <jsp:include page="aside.jsp"></jsp:include>
      <!-- 导航侧栏 /-->

      <!-- 内容区域 -->
      <div class="content-wrapper">

         <!-- 内容头部 -->
         <section class="content-header">
         <h1>
            角色管理 <small>角色表单</small>
         </h1>
         <ol class="breadcrumb">
            <li><a href="${pageContext.request.contextPath}/index.jsp"><i
                  class="fa fa-dashboard"></i> 首页</a></li>
            <li><a href="${pageContext.request.contextPath}/role/findAll.do">角色管理</a></li>
            <li class="active">角色表单</li>
         </ol>
         </section>
         <!-- 内容头部 /-->

         <form action="${pageContext.request.contextPath}/role/save.do"
            method="post">
            <!-- 正文区域 -->
            <section class="content"> <!--产品信息-->

            <div class="panel panel-default">
               <div class="panel-heading">角色信息</div>
               <div class="row data-type">

                  <div class="col-md-2 title">角色名称</div>
                  <div class="col-md-4 data">
                     <input type="text" class="form-control" name="roleName"
                        placeholder="角色名称" value="">
                  </div>
                  <div class="col-md-2 title">角色描述</div>
                  <div class="col-md-4 data">
                     <input type="text" class="form-control" name="roleDesc"
                        placeholder="角色描述" value="">
                  </div>
                              

               </div>
            </div>
            <!--订单信息/--> <!--工具栏-->
            <div class="box-tools text-center">
               <button type="submit" class="btn bg-maroon">保存</button>
               <button type="button" class="btn bg-default"
                  onclick="history.back(-1);">返回</button>
            </div>
            <!--工具栏/--> </section>
            <!-- 正文区域 /-->
         </form>
      </div>
      <!-- 内容区域 /-->

      <!-- 底部导航 -->
      <footer class="main-footer">
      <div class="pull-right hidden-xs">
         <b>Version</b> 1.0.8
      </div>
      <strong>Copyright &copy; 2014-2017 <a
         href="http://www.itcast.cn">研究院研发部</a>.
      </strong> All rights reserved. </footer>
      <!-- 底部导航 /-->

   </div>


   <script
      src="${pageContext.request.contextPath}/plugins/jQuery/jquery-2.2.3.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/jQueryUI/jquery-ui.min.js"></script>
   <script>
      $.widget.bridge('uibutton', $.ui.button);
   </script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap/js/bootstrap.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/raphael/raphael-min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/morris/morris.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/sparkline/jquery.sparkline.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/jvectormap/jquery-jvectormap-1.2.2.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/jvectormap/jquery-jvectormap-world-mill-en.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/knob/jquery.knob.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/daterangepicker/moment.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/daterangepicker/daterangepicker.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/daterangepicker/daterangepicker.zh-CN.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/datepicker/bootstrap-datepicker.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/datepicker/locales/bootstrap-datepicker.zh-CN.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.all.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/slimScroll/jquery.slimscroll.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/fastclick/fastclick.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/iCheck/icheck.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/adminLTE/js/app.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/select2/select2.full.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/colorpicker/bootstrap-colorpicker.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-wysihtml5/bootstrap-wysihtml5.zh-CN.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/js/bootstrap-markdown.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/locale/bootstrap-markdown.zh.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/js/markdown.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/js/to-markdown.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/ckeditor/ckeditor.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/input-mask/jquery.inputmask.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/input-mask/jquery.inputmask.date.extensions.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/input-mask/jquery.inputmask.extensions.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/datatables/jquery.dataTables.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/datatables/dataTables.bootstrap.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/chartjs/Chart.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.resize.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.pie.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.categories.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-slider/bootstrap-slider.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-datetimepicker/bootstrap-datetimepicker.min.js"></script>

   <script>
      $(document).ready(function() {
         // 选择框
         $(".select2").select2();

         // WYSIHTML5编辑器
         $(".textarea").wysihtml5({
            locale : 'zh-CN'
         });
      });

      // 设置激活菜单
      function setSidebarActive(tagUri) {
         var liObj = $("#" + tagUri);
         if (liObj.length > 0) {
            liObj.parent().parent().addClass("active");
            liObj.addClass("active");
         }
      }

   </script>
   

</body>

</html>
```

###### permission-list.jsp

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8"
   pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<!-- 页面meta -->
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">

<title>数据 - AdminLTE2定制版</title>
<meta name="description" content="AdminLTE2定制版">
<meta name="keywords" content="AdminLTE2定制版">

<!-- Tell the browser to be responsive to screen width -->
<meta
   content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no"
   name="viewport">

<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap/css/bootstrap.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/font-awesome/css/font-awesome.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/ionicons/css/ionicons.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/iCheck/square/blue.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/morris/morris.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/jvectormap/jquery-jvectormap-1.2.2.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/datepicker/datepicker3.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/daterangepicker/daterangepicker.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/datatables/dataTables.bootstrap.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.theme.default.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/select2/select2.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/colorpicker/bootstrap-colorpicker.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap-markdown/css/bootstrap-markdown.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/adminLTE/css/AdminLTE.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/adminLTE/css/skins/_all-skins.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/css/style.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.skinNice.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap-slider/slider.css">
</head>

<body class="hold-transition skin-blue sidebar-mini">

   <div class="wrapper">

      <!-- 页面头部 -->
      <jsp:include page="header.jsp"></jsp:include>
      <!-- 页面头部 /-->

      <!-- 导航侧栏 -->
      <jsp:include page="aside.jsp"></jsp:include>
      <!-- 导航侧栏 /-->

      <!-- 内容区域 -->
      <div class="content-wrapper">

         <!-- 内容头部 -->
         <section class="content-header">
         <h1>
            资源权限管理 <small>全部权限</small>
         </h1>
         <ol class="breadcrumb">
            <li><a href="${pageContext.request.contextPath}/index.jsp"><i
                  class="fa fa-dashboard"></i> 首页</a></li>
            <li><a
               href="${pageContext.request.contextPath}/permission/findAll.do">资源权限管理</a></li>

            <li class="active">全部权限</li>
         </ol>
         </section>
         <!-- 内容头部 /-->

            <!-- 正文区域 -->
            <section class="content"> <!-- .box-body -->
            <div class="box box-primary">
               <div class="box-header with-border">
                  <h3 class="box-title">列表</h3>
               </div>

               <div class="box-body">

                  <!-- 数据表格 -->
                  <div class="table-box">

                     <!--工具栏-->
                     <div class="pull-left">
                        <div class="form-group form-inline">
                           <div class="btn-group">
                              <button type="button" class="btn btn-default" title="新建" onclick="location.href='${pageContext.request.contextPath}/pages/permission-add.jsp'">
                                 <i class="fa fa-file-o"></i> 新建
                              </button>
                              
                              <button type="button" class="btn btn-default" title="刷新">
                                 <i class="fa fa-refresh"></i> 刷新
                              </button>
                           </div>
                        </div>
                     </div>
                     <div class="box-tools pull-right">
                        <div class="has-feedback">
                           <input type="text" class="form-control input-sm"
                              placeholder="搜索"> <span
                              class="glyphicon glyphicon-search form-control-feedback"></span>
                        </div>
                     </div>
                     <!--工具栏/-->

                     <!--数据列表-->
                     <table id="dataList"
                        class="table table-bordered table-striped table-hover dataTable">
                        <thead>
                           <tr>
                              <th class="" style="padding-right: 0px"><input
                                 id="selall" type="checkbox" class="icheckbox_square-blue">
                              </th>
                              <th class="sorting_asc">ID</th>
                              <th class="sorting_desc">权限名称</th>
                              <th class="sorting_asc sorting_asc_disabled">URL</th>
                              <th class="text-center">操作</th>
                           </tr>
                        </thead>
                        <tbody>

                           <c:forEach items="${permissionList}" var="p">
                              <tr>
                                 <td><input name="ids" type="checkbox"></td>
                                 <td>${p.id }</td>
                                 <td>${p.permissionName }</td>
                                 <td>${p.url }</td>
                                 <td class="text-center">
                                    <a href="${pageContext.request.contextPath}/role/findById.do?id=${p.id}" class="btn bg-olive btn-xs">详情</a>
                                    <a href="${pageContext.request.contextPath}/user/findUserByIdAndAllRole.do?id=${p.id}" class="btn bg-olive btn-xs">添加角色</a>
                                 </td>
                              </tr>
                           </c:forEach>
                        </tbody>
                        <!--
                            <tfoot>
                            <tr>
                            <th>Rendering engine</th>
                            <th>Browser</th>
                            <th>Platform(s)</th>
                            <th>Engine version</th>
                            <th>CSS grade</th>
                            </tr>
                            </tfoot>-->
                     </table>
                     <!--数据列表/-->

                  </div>
                  <!-- 数据表格 /-->

               </div>
               <!-- /.box-body -->

               <!-- .box-footer-->
               <div class="box-footer">
                  <div class="pull-left">
                     <div class="form-group form-inline">
                        总共2 页，共14 条数据。 每页 <select class="form-control">
                           <option>1</option>
                           <option>2</option>
                           <option>3</option>
                           <option>4</option>
                           <option>5</option>
                        </select> 条
                     </div>
                  </div>

                  <div class="box-tools pull-right">
                     <ul class="pagination">
                        <li><a href="#" aria-label="Previous">首页</a></li>
                        <li><a href="#">上一页</a></li>
                        <li><a href="#">1</a></li>
                        <li><a href="#">2</a></li>
                        <li><a href="#">3</a></li>
                        <li><a href="#">4</a></li>
                        <li><a href="#">5</a></li>
                        <li><a href="#">下一页</a></li>
                        <li><a href="#" aria-label="Next">尾页</a></li>
                     </ul>
                  </div>

               </div>
               <!-- /.box-footer-->

            </div>

            </section>
            <!-- 正文区域 /-->

         </div>
         <!-- @@close -->
         <!-- 内容区域 /-->

         <!-- 底部导航 -->
         <footer class="main-footer">
         <div class="pull-right hidden-xs">
            <b>Version</b> 1.0.8
         </div>
         <strong>Copyright &copy; 2014-2017 <a
            href="http://www.itcast.cn">研究院研发部</a>.
         </strong> All rights reserved. </footer>
         <!-- 底部导航 /-->

      </div>

      <script src="../plugins/jQuery/jquery-2.2.3.min.js"></script>
      <script src="../plugins/jQueryUI/jquery-ui.min.js"></script>
      <script>
         $.widget.bridge('uibutton', $.ui.button);
      </script>
      <script src="../plugins/bootstrap/js/bootstrap.min.js"></script>
      <script src="../plugins/raphael/raphael-min.js"></script>
      <script src="../plugins/morris/morris.min.js"></script>
      <script src="../plugins/sparkline/jquery.sparkline.min.js"></script>
      <script src="../plugins/jvectormap/jquery-jvectormap-1.2.2.min.js"></script>
      <script src="../plugins/jvectormap/jquery-jvectormap-world-mill-en.js"></script>
      <script src="../plugins/knob/jquery.knob.js"></script>
      <script src="../plugins/daterangepicker/moment.min.js"></script>
      <script src="../plugins/daterangepicker/daterangepicker.js"></script>
      <script src="../plugins/daterangepicker/daterangepicker.zh-CN.js"></script>
      <script src="../plugins/datepicker/bootstrap-datepicker.js"></script>
      <script
         src="../plugins/datepicker/locales/bootstrap-datepicker.zh-CN.js"></script>
      <script
         src="../plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.all.min.js"></script>
      <script src="../plugins/slimScroll/jquery.slimscroll.min.js"></script>
      <script src="../plugins/fastclick/fastclick.js"></script>
      <script src="../plugins/iCheck/icheck.min.js"></script>
      <script src="../plugins/adminLTE/js/app.min.js"></script>
      <script src="../plugins/treeTable/jquery.treetable.js"></script>
      <script src="../plugins/select2/select2.full.min.js"></script>
      <script src="../plugins/colorpicker/bootstrap-colorpicker.min.js"></script>
      <script
         src="../plugins/bootstrap-wysihtml5/bootstrap-wysihtml5.zh-CN.js"></script>
      <script src="../plugins/bootstrap-markdown/js/bootstrap-markdown.js"></script>
      <script
         src="../plugins/bootstrap-markdown/locale/bootstrap-markdown.zh.js"></script>
      <script src="../plugins/bootstrap-markdown/js/markdown.js"></script>
      <script src="../plugins/bootstrap-markdown/js/to-markdown.js"></script>
      <script src="../plugins/ckeditor/ckeditor.js"></script>
      <script src="../plugins/input-mask/jquery.inputmask.js"></script>
      <script
         src="../plugins/input-mask/jquery.inputmask.date.extensions.js"></script>
      <script src="../plugins/input-mask/jquery.inputmask.extensions.js"></script>
      <script src="../plugins/datatables/jquery.dataTables.min.js"></script>
      <script src="../plugins/datatables/dataTables.bootstrap.min.js"></script>
      <script src="../plugins/chartjs/Chart.min.js"></script>
      <script src="../plugins/flot/jquery.flot.min.js"></script>
      <script src="../plugins/flot/jquery.flot.resize.min.js"></script>
      <script src="../plugins/flot/jquery.flot.pie.min.js"></script>
      <script src="../plugins/flot/jquery.flot.categories.min.js"></script>
      <script src="../plugins/ionslider/ion.rangeSlider.min.js"></script>
      <script src="../plugins/bootstrap-slider/bootstrap-slider.js"></script>
      <script>
         $(document).ready(function() {
            // 选择框
            $(".select2").select2();

            // WYSIHTML5编辑器
            $(".textarea").wysihtml5({
               locale : 'zh-CN'
            });
         });

         // 设置激活菜单
         function setSidebarActive(tagUri) {
            var liObj = $("#" + tagUri);
            if (liObj.length > 0) {
               liObj.parent().parent().addClass("active");
               liObj.addClass("active");
            }
         }

         $(document)
               .ready(
                     function() {

                        // 激活导航位置
                        setSidebarActive("admin-datalist");

                        // 列表按钮 
                        $("#dataList td input[type='checkbox']")
                              .iCheck(
                                    {
                                       checkboxClass : 'icheckbox_square-blue',
                                       increaseArea : '20%'
                                    });
                        // 全选操作 
                        $("#selall")
                              .click(
                                    function() {
                                       var clicks = $(this).is(
                                             ':checked');
                                       if (!clicks) {
                                          $(
                                                "#dataList td input[type='checkbox']")
                                                .iCheck(
                                                      "uncheck");
                                       } else {
                                          $(
                                                "#dataList td input[type='checkbox']")
                                                .iCheck("check");
                                       }
                                       $(this).data("clicks",
                                             !clicks);
                                    });
                     });
      </script>
</body>

</html>
```

###### permission-add.jsp

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8"
   pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<!-- 页面meta -->
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<title>数据 - AdminLTE2定制版</title>
<meta name="description" content="AdminLTE2定制版">
<meta name="keywords" content="AdminLTE2定制版">

<!-- Tell the browser to be responsive to screen width -->
<meta
   content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no"
   name="viewport">


<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap/css/bootstrap.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/font-awesome/css/font-awesome.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/ionicons/css/ionicons.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/iCheck/square/blue.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/morris/morris.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/jvectormap/jquery-jvectormap-1.2.2.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/datepicker/datepicker3.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/daterangepicker/daterangepicker.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/datatables/dataTables.bootstrap.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.theme.default.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/select2/select2.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/colorpicker/bootstrap-colorpicker.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap-markdown/css/bootstrap-markdown.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/adminLTE/css/AdminLTE.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/adminLTE/css/skins/_all-skins.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/css/style.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.skinNice.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap-slider/slider.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap-datetimepicker/bootstrap-datetimepicker.css">
</head>

   <body class="hold-transition skin-purple sidebar-mini">

   <div class="wrapper">

      <!-- 页面头部 -->
      <jsp:include page="header.jsp"></jsp:include>
      <!-- 页面头部 /-->
      <!-- 导航侧栏 -->
      <jsp:include page="aside.jsp"></jsp:include>
      <!-- 导航侧栏 /-->

      <!-- 内容区域 -->
      <div class="content-wrapper">

         <!-- 内容头部 -->
         <section class="content-header">
         <h1>
            资源权限管理 <small>资源权限表单</small>
         </h1>
         <ol class="breadcrumb">
            <li><a href="${pageContext.request.contextPath}/index.jsp"><i
                  class="fa fa-dashboard"></i> 首页</a></li>
            <li><a href="${pageContext.request.contextPath}/permission/findAll.do">资源权限管理</a></li>
            <li class="active">资源权限表单</li>
         </ol>
         </section>
         <!-- 内容头部 /-->

         <form action="${pageContext.request.contextPath}/permission/save.do"
            method="post">
            <!-- 正文区域 -->
            <section class="content"> <!--产品信息-->

            <div class="panel panel-default">
               <div class="panel-heading">资源权限信息</div>
               <div class="row data-type">

                  <div class="col-md-2 title">权限名称</div>
                  <div class="col-md-4 data">
                     <input type="text" class="form-control" name="permissionName"
                        placeholder="权限名称" value="">
                  </div>
                  <div class="col-md-2 title">RUL</div>
                  <div class="col-md-4 data">
                     <input type="text" class="form-control" name="url"
                        placeholder="URL" value="">
                  </div>
                              

               </div>
            </div>
            <!--订单信息/--> <!--工具栏-->
            <div class="box-tools text-center">
               <button type="submit" class="btn bg-maroon">保存</button>
               <button type="button" class="btn bg-default"
                  onclick="history.back(-1);">返回</button>
            </div>
            <!--工具栏/--> </section>
            <!-- 正文区域 /-->
         </form>
      </div>
      <!-- 内容区域 /-->

      <!-- 底部导航 -->
      <footer class="main-footer">
      <div class="pull-right hidden-xs">
         <b>Version</b> 1.0.8
      </div>
      <strong>Copyright &copy; 2014-2017 <a
         href="http://www.itcast.cn">研究院研发部</a>.
      </strong> All rights reserved. </footer>
      <!-- 底部导航 /-->

   </div>


   <script
      src="${pageContext.request.contextPath}/plugins/jQuery/jquery-2.2.3.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/jQueryUI/jquery-ui.min.js"></script>
   <script>
      $.widget.bridge('uibutton', $.ui.button);
   </script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap/js/bootstrap.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/raphael/raphael-min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/morris/morris.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/sparkline/jquery.sparkline.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/jvectormap/jquery-jvectormap-1.2.2.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/jvectormap/jquery-jvectormap-world-mill-en.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/knob/jquery.knob.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/daterangepicker/moment.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/daterangepicker/daterangepicker.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/daterangepicker/daterangepicker.zh-CN.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/datepicker/bootstrap-datepicker.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/datepicker/locales/bootstrap-datepicker.zh-CN.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.all.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/slimScroll/jquery.slimscroll.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/fastclick/fastclick.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/iCheck/icheck.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/adminLTE/js/app.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/select2/select2.full.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/colorpicker/bootstrap-colorpicker.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-wysihtml5/bootstrap-wysihtml5.zh-CN.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/js/bootstrap-markdown.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/locale/bootstrap-markdown.zh.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/js/markdown.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/js/to-markdown.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/ckeditor/ckeditor.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/input-mask/jquery.inputmask.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/input-mask/jquery.inputmask.date.extensions.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/input-mask/jquery.inputmask.extensions.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/datatables/jquery.dataTables.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/datatables/dataTables.bootstrap.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/chartjs/Chart.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.resize.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.pie.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.categories.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-slider/bootstrap-slider.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-datetimepicker/bootstrap-datetimepicker.min.js"></script>

   <script>
      $(document).ready(function() {
         // 选择框
         $(".select2").select2();

         // WYSIHTML5编辑器
         $(".textarea").wysihtml5({
            locale : 'zh-CN'
         });
      });

      // 设置激活菜单
      function setSidebarActive(tagUri) {
         var liObj = $("#" + tagUri);
         if (liObj.length > 0) {
            liObj.parent().parent().addClass("active");
            liObj.addClass("active");
         }
      }

   </script>
   

</body>

</html>
```

##### 日志模块

###### syslog-list.jsp

```jsp
<%@ page language="java" contentType="text/html; charset=UTF-8"
   pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<!-- 页面meta -->
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<title>数据 - AdminLTE2定制版</title>
<meta name="description" content="AdminLTE2定制版">
<meta name="keywords" content="AdminLTE2定制版">

<!-- Tell the browser to be responsive to screen width -->
<meta
   content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no"
   name="viewport">


<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap/css/bootstrap.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/font-awesome/css/font-awesome.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/ionicons/css/ionicons.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/iCheck/square/blue.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/morris/morris.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/jvectormap/jquery-jvectormap-1.2.2.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/datepicker/datepicker3.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/daterangepicker/daterangepicker.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/datatables/dataTables.bootstrap.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.theme.default.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/select2/select2.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/colorpicker/bootstrap-colorpicker.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap-markdown/css/bootstrap-markdown.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/adminLTE/css/AdminLTE.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/adminLTE/css/skins/_all-skins.min.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/css/style.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.skinNice.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap-slider/slider.css">
<link rel="stylesheet"
   href="${pageContext.request.contextPath}/plugins/bootstrap-datetimepicker/bootstrap-datetimepicker.css">
</head>

   <body class="hold-transition skin-purple sidebar-mini">

   <div class="wrapper">

      <!-- 页面头部 -->
      <jsp:include page="header.jsp"></jsp:include>
      <!-- 页面头部 /-->
      <!-- 导航侧栏 -->
      <jsp:include page="aside.jsp"></jsp:include>
      <!-- 导航侧栏 /-->

      <!-- 内容区域 -->
      <div class="content-wrapper">

         <!-- 内容头部 -->
         <section class="content-header">
         <h1>
            资源权限管理 <small>资源权限表单</small>
         </h1>
         <ol class="breadcrumb">
            <li><a href="${pageContext.request.contextPath}/index.jsp"><i
                  class="fa fa-dashboard"></i> 首页</a></li>
            <li><a href="${pageContext.request.contextPath}/permission/findAll.do">资源权限管理</a></li>
            <li class="active">资源权限表单</li>
         </ol>
         </section>
         <!-- 内容头部 /-->

         <form action="${pageContext.request.contextPath}/permission/save.do"
            method="post">
            <!-- 正文区域 -->
            <section class="content"> <!--产品信息-->

            <div class="panel panel-default">
               <div class="panel-heading">资源权限信息</div>
               <div class="row data-type">

                  <div class="col-md-2 title">权限名称</div>
                  <div class="col-md-4 data">
                     <input type="text" class="form-control" name="permissionName"
                        placeholder="权限名称" value="">
                  </div>
                  <div class="col-md-2 title">RUL</div>
                  <div class="col-md-4 data">
                     <input type="text" class="form-control" name="url"
                        placeholder="URL" value="">
                  </div>
                              

               </div>
            </div>
            <!--订单信息/--> <!--工具栏-->
            <div class="box-tools text-center">
               <button type="submit" class="btn bg-maroon">保存</button>
               <button type="button" class="btn bg-default"
                  onclick="history.back(-1);">返回</button>
            </div>
            <!--工具栏/--> </section>
            <!-- 正文区域 /-->
         </form>
      </div>
      <!-- 内容区域 /-->

      <!-- 底部导航 -->
      <footer class="main-footer">
      <div class="pull-right hidden-xs">
         <b>Version</b> 1.0.8
      </div>
      <strong>Copyright &copy; 2014-2017 <a
         href="http://www.itcast.cn">研究院研发部</a>.
      </strong> All rights reserved. </footer>
      <!-- 底部导航 /-->

   </div>


   <script
      src="${pageContext.request.contextPath}/plugins/jQuery/jquery-2.2.3.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/jQueryUI/jquery-ui.min.js"></script>
   <script>
      $.widget.bridge('uibutton', $.ui.button);
   </script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap/js/bootstrap.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/raphael/raphael-min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/morris/morris.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/sparkline/jquery.sparkline.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/jvectormap/jquery-jvectormap-1.2.2.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/jvectormap/jquery-jvectormap-world-mill-en.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/knob/jquery.knob.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/daterangepicker/moment.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/daterangepicker/daterangepicker.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/daterangepicker/daterangepicker.zh-CN.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/datepicker/bootstrap-datepicker.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/datepicker/locales/bootstrap-datepicker.zh-CN.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.all.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/slimScroll/jquery.slimscroll.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/fastclick/fastclick.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/iCheck/icheck.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/adminLTE/js/app.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/treeTable/jquery.treetable.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/select2/select2.full.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/colorpicker/bootstrap-colorpicker.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-wysihtml5/bootstrap-wysihtml5.zh-CN.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/js/bootstrap-markdown.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/locale/bootstrap-markdown.zh.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/js/markdown.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-markdown/js/to-markdown.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/ckeditor/ckeditor.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/input-mask/jquery.inputmask.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/input-mask/jquery.inputmask.date.extensions.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/input-mask/jquery.inputmask.extensions.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/datatables/jquery.dataTables.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/datatables/dataTables.bootstrap.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/chartjs/Chart.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.resize.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.pie.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/flot/jquery.flot.categories.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/ionslider/ion.rangeSlider.min.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-slider/bootstrap-slider.js"></script>
   <script
      src="${pageContext.request.contextPath}/plugins/bootstrap-datetimepicker/bootstrap-datetimepicker.min.js"></script>

   <script>
      $(document).ready(function() {
         // 选择框
         $(".select2").select2();

         // WYSIHTML5编辑器
         $(".textarea").wysihtml5({
            locale : 'zh-CN'
         });
      });

      // 设置激活菜单
      function setSidebarActive(tagUri) {
         var liObj = $("#" + tagUri);
         if (liObj.length > 0) {
            liObj.parent().parent().addClass("active");
            liObj.addClass("active");
         }
      }

   </script>
   

</body>

</html>
```

#### 工具类编写

##### DateUtils

```java
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    //日期类型转换字符串类型
    public static String dateToString(Date date,String patt){
        SimpleDateFormat sdf = new SimpleDateFormat(patt);
        String format = sdf.format(date);
        return format;
    }

    //日期类型转换字符串类型
    public static Date stringToDate(String date,String patt) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(patt);
        Date parse = sdf.parse(date);
        return parse;
    }
}
```

##### BCryptPasswordEncoderUtils

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptPasswordEncoderUtils {

    private static BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static String encodePassword(String password){
        return passwordEncoder.encode(password);
    }

    public static void main(String[] args) {
        System.out.println(encodePassword("root"));;
    }
}
```

#### 产品功能

##### 展示商品列表

###### 1.编写实体类

- Orders

```java
import cn.jazz.utils.DateUtils;

import java.util.Date;
import java.util.List;

public class Orders {
    private String id;
    private String orderNum;
    private Date orderTime;
    private String orderTimeStr; //orderTime页面显示格式化
    private int orderStatus;
    private String orderStatusStr; //orderStatus页面显示格式化
    private int peopleCount;
    private Product product;
    private List<Traveller> travellers;
    private Member member;
    private Integer payType;
    private String payTypeStr;
    private String orderDesc;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }

    public Date getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Date orderTime) {
        this.orderTime = orderTime;
    }

    public String getOrderTimeStr() {
        if (orderTime!=null){
            orderTimeStr = DateUtils.dateToString(orderTime,"yyyy-MM-dd HH:mm");
        }
        return orderTimeStr;
    }

    public void setOrderTimeStr(String orderTimeStr) {
        this.orderTimeStr = orderTimeStr;
    }

    public int getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(int orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderStatusStr() {
        //订单状态(0 未支付 1 已支付)
        if (orderStatus==0) orderStatusStr="未支付";
        else if (orderStatus==1) orderStatusStr="已支付";
        return orderStatusStr;
    }

    public void setOrderStatusStr(String orderStatusStr) {
        this.orderStatusStr = orderStatusStr;
    }

    public int getPeopleCount() {
        return peopleCount;
    }

    public void setPeopleCount(int peopleCount) {
        this.peopleCount = peopleCount;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public List<Traveller> getTravellers() {
        return travellers;
    }

    public void setTravellers(List<Traveller> travellers) {
        this.travellers = travellers;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public String getPayTypeStr() {
        //支付方式(0 支付宝 1 微信 2其它)
        if (payType==0) payTypeStr="支付宝";
        else if (payType==1) payTypeStr="微信";
        else if (payType==2) payTypeStr="其它";
        return payTypeStr;
    }

    public void setPayTypeStr(String payTypeStr) {
        this.payTypeStr = payTypeStr;
    }

    public String getOrderDesc() {
        return orderDesc;
    }

    public void setOrderDesc(String orderDesc) {
        this.orderDesc = orderDesc;
    }
}

```

###### 2.编写持久层

- IProductDao

```java
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

}
```

###### 3.编写业务层

- IProductService

```java
import cn.jazz.domain.Product;

import java.util.List;


public interface IProductService {

    public List<Product> findAll() throws Exception;

}
```

- ProductServiceImpl

```java
import cn.jazz.dao.IProductDao;
import cn.jazz.domain.Product;
import cn.jazz.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductServiceImpl implements IProductService {

    @Autowired
    private IProductDao productDao;

    @Override
    public List<Product> findAll() throws Exception {
        return productDao.findAll();
    }
}

```

###### 4.编写控制器

- ProductController

```java
import cn.jazz.domain.Product;
import cn.jazz.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private IProductService productService;


    @RequestMapping("findAll.do")
    public ModelAndView findAll() throws Exception {
        ModelAndView mv = new ModelAndView();
        List<Product> ps = productService.findAll();
        mv.addObject("productList",ps);
        mv.setViewName("product-list");
        return mv;
    }
}
```

##### 新增商品

###### 1.持久层代码

- IProductDao

```java
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
     * 新增一条产品信息
     * @param product
     */
    @Insert("insert into PRODUCT(productnum,productname,cityname,departuretime,productprice,productdesc,productstatus) values(#{productNum},#{productName},#{cityName},#{departureTime},#{productPrice},#{productDesc},#{productStatus})")
    public void save(Product product);
}
```

###### 2.业务层代码

- IProductService

```java
import cn.jazz.domain.Product;

import java.util.List;


public interface IProductService {

    public List<Product> findAll() throws Exception;

    public void save(Product product) throws Exception;
}
```

- ProductServiceImpl

```java
import cn.jazz.dao.IProductDao;
import cn.jazz.domain.Product;
import cn.jazz.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductServiceImpl implements IProductService {

    @Autowired
    private IProductDao productDao;

    @Override
    public List<Product> findAll() throws Exception {
        return productDao.findAll();
    }

    @Override
    public void save(Product product) throws Exception {
        productDao.save(product);
    }
}
```

###### 3.编写控制器

- ProductController

```java
import cn.jazz.domain.Product;
import cn.jazz.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private IProductService productService;

    @RequestMapping("/add.do")
    public String add(Product product) throws Exception {
        productService.save(product);
        return "redirect:findAll.do";
    }

    @RequestMapping("findAll.do")
    public ModelAndView findAll() throws Exception {
        ModelAndView mv = new ModelAndView();
        List<Product> ps = productService.findAll();
        mv.addObject("productList",ps);
        mv.setViewName("product-list");
        return mv;
    }
}
```

##### 删除商品



##### 修改商品信息



#### 订单功能

##### 展示订单列表

###### 1.编写实体类

- Orders

```java
import cn.jazz.utils.DateUtils;

import java.util.Date;
import java.util.List;

public class Orders {
    private String id;
    private String orderNum;
    private Date orderTime;
    private String orderTimeStr; //orderTime页面显示格式化
    private int orderStatus;
    private String orderStatusStr; //orderStatus页面显示格式化
    private int peopleCount;
    private Product product;
    private List<Traveller> travellers;
    private Member member;
    private Integer payType;
    private String payTypeStr;
    private String orderDesc;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }

    public Date getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Date orderTime) {
        this.orderTime = orderTime;
    }

    public String getOrderTimeStr() {
        if (orderTime!=null){
            orderTimeStr = DateUtils.dateToString(orderTime,"yyyy-MM-dd HH:mm");
        }
        return orderTimeStr;
    }

    public void setOrderTimeStr(String orderTimeStr) {
        this.orderTimeStr = orderTimeStr;
    }

    public int getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(int orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderStatusStr() {
        //订单状态(0 未支付 1 已支付)
        if (orderStatus==0) orderStatusStr="未支付";
        else if (orderStatus==1) orderStatusStr="已支付";
        return orderStatusStr;
    }

    public void setOrderStatusStr(String orderStatusStr) {
        this.orderStatusStr = orderStatusStr;
    }

    public int getPeopleCount() {
        return peopleCount;
    }

    public void setPeopleCount(int peopleCount) {
        this.peopleCount = peopleCount;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public List<Traveller> getTravellers() {
        return travellers;
    }

    public void setTravellers(List<Traveller> travellers) {
        this.travellers = travellers;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public String getPayTypeStr() {
        //支付方式(0 支付宝 1 微信 2其它)
        if (payType==0) payTypeStr="支付宝";
        else if (payType==1) payTypeStr="微信";
        else if (payType==2) payTypeStr="其它";
        return payTypeStr;
    }

    public void setPayTypeStr(String payTypeStr) {
        this.payTypeStr = payTypeStr;
    }

    public String getOrderDesc() {
        return orderDesc;
    }

    public void setOrderDesc(String orderDesc) {
        this.orderDesc = orderDesc;
    }
}
```

- Member

```java
public class Member {
    private String id;
    private String name;
    private String nickname;
    private String phoneNum;
    private String email;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
```

- Traveller

```java
public class Traveller {
    private String id;
    private String name;
    private String sex;
    private String phoneNum;
    private Integer credentialsType;
    private String credentialsTypeStr;
    private String credentialsNum;
    private Integer travellerType;
    private String travellerTypeStr;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public Integer getCredentialsType() {
        return credentialsType;
    }

    public void setCredentialsType(Integer credentialsType) {
        this.credentialsType = credentialsType;
    }

    public String getCredentialsTypeStr() {
        return credentialsTypeStr;
    }

    public void setCredentialsTypeStr(String credentialsTypeStr) {
        this.credentialsTypeStr = credentialsTypeStr;
    }

    public String getCredentialsNum() {
        return credentialsNum;
    }

    public void setCredentialsNum(String credentialsNum) {
        this.credentialsNum = credentialsNum;
    }

    public Integer getTravellerType() {
        return travellerType;
    }

    public void setTravellerType(Integer travellerType) {
        this.travellerType = travellerType;
    }

    public String getTravellerTypeStr() {
        return travellerTypeStr;
    }

    public void setTravellerTypeStr(String travellerTypeStr) {
        this.travellerTypeStr = travellerTypeStr;
    }
}
```

###### 2.编写持久层

- IOrdersDao

```java
import cn.jazz.domain.Orders;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface IOrdersDao {

    @Select("select * from ORDERS")
    @Results({
            @Result(id=true,column = "id",property = "id"),
            @Result(column = "orderNum",property = "orderNum"),
            @Result(column = "orderTime",property = "orderTime"),
            @Result(column = "orderStatus",property = "orderStatus"),
            @Result(column = "peopleCount",property = "peopleCount"),
            @Result(column = "payType",property = "payType"),
            @Result(column = "orderDesc",property = "orderDesc"),
            @Result(column = "productId",property = "product",one = @One(select = "cn.jazz.dao.IProductDao.findById"))
    })
    public List<Orders> findAll() throws Exception;

}
```

###### 3.编写业务层

- IOrdersService

```java
import cn.jazz.domain.Orders;

import java.util.List;

public interface IOrdersService {

    public List<Orders> findAll(int page,int size) throws Exception;

}
```

- OrdersServiceImpl

```java
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
}
```

###### 4.编写控制器

- OrdersController

```java
import cn.jazz.domain.Orders;
import cn.jazz.service.IOrdersService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrdersController {

    @Autowired
    private IOrdersService ordersService;

    /*
    **未分页
    @RequestMapping("/findAll.do")
    public ModelAndView findAll() throws Exception {
        ModelAndView mv = new ModelAndView();
        List<Orders> orders = ordersService.findAll();
        mv.addObject("ordersList",orders);
        mv.setViewName("orders-list");
        return mv;
    }*/

    @RequestMapping("/findAll.do")
    public ModelAndView findAll(@RequestParam(name = "page",required = true,defaultValue = "1") int page,@RequestParam(name = "size",required = true,defaultValue = "4") int size) throws Exception {
        ModelAndView mv = new ModelAndView();
        List<Orders> orders = ordersService.findAll(page,size);
        //pageInfo就是将分页查询到的数据封装到一个PageBean
        PageInfo pageInfo = new PageInfo(orders);
        mv.addObject("pageInfo", pageInfo);
        mv.setViewName("orders-page-list");
        return mv;
    }
}
```

##### 展示订单详情

###### 1.持久层代码

- IMemberDao

```java
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
```

- ITravellerDao

```java
import cn.jazz.domain.Traveller;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ITravellerDao {

    /**
     * 根据订单Id查询对应的游客信息
     * @param id
     * @return
     */
    @Select("select * from TRAVELLER where id in (select travellerId from ORDER_TRAVELLER where orderId=#{id})")
    public List<Traveller> findById(String id) throws Exception;

}
```

- IOrdersDao

```java
import cn.jazz.domain.Member;
import cn.jazz.domain.Orders;
import cn.jazz.domain.Product;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface IOrdersDao {

    /**
     * 查询所有订单信息
     * @return
     * @throws Exception
     */
    @Select("select * from ORDERS")
    @Results({
            @Result(id=true,column = "id",property = "id"),
            @Result(column = "orderNum",property = "orderNum"),
            @Result(column = "orderTime",property = "orderTime"),
            @Result(column = "orderStatus",property = "orderStatus"),
            @Result(column = "peopleCount",property = "peopleCount"),
            @Result(column = "payType",property = "payType"),
            @Result(column = "orderDesc",property = "orderDesc"),
            @Result(column = "productId",property = "product",one = @One(select = "cn.jazz.dao.IProductDao.findById"))
    })
    public List<Orders> findAll() throws Exception;

    /**
     * 根据订单Id查询订单详情
     * @param id
     * @return
     */
    @Select("select * from ORDERS where id=#{id}")
    @Results({
            @Result(id=true,column = "id",property = "id"),
            @Result(column = "orderNum",property = "orderNum"),
            @Result(column = "orderTime",property = "orderTime"),
            @Result(column = "orderStatus",property = "orderStatus"),
            @Result(column = "peopleCount",property = "peopleCount"),
            @Result(column = "payType",property = "payType"),
            @Result(column = "orderDesc",property = "orderDesc"),
            @Result(column = "productId",property = "product",javaType = Product.class,one = @One(select = "cn.jazz.dao.IProductDao.findById")),
            @Result(column = "memberId",property = "member",javaType = Member.class,one = @One(select = "cn.jazz.dao.IMemberDao.findById")),
            @Result(column = "id",property = "travellers",javaType = java.util.List.class,many = @Many(select = "cn.jazz.dao.ITravellerDao.findById"))
    })
    public Orders findById(String id);
}
```

###### 2.业务层代码

- OrdersServiceImpl

```java
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
```

###### 3.编写控制器

```java
import cn.jazz.domain.Orders;
import cn.jazz.service.IOrdersService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrdersController {

    @Autowired
    private IOrdersService ordersService;

    /**
     * 查询订单列表
     * @param page
     * @param size
     * @return
     * @throws Exception
     */
    @RequestMapping("/findAll.do")
    public ModelAndView findAll(@RequestParam(name = "page",required = true,defaultValue = "1") int page,@RequestParam(name = "size",required = true,defaultValue = "4") int size) throws Exception {
        ModelAndView mv = new ModelAndView();
        List<Orders> orders = ordersService.findAll(page,size);
        //pageInfo就是将分页查询到的数据封装到一个PageBean
        PageInfo pageInfo = new PageInfo(orders);
        mv.addObject("pageInfo", pageInfo);
        mv.setViewName("orders-page-list");
        return mv;
    }

    /**
     * 查询订单详情
     * @param ordersId
     * @return
     * @throws Exception
     */
    @RequestMapping("/findDetail")
    public ModelAndView findDetail(@RequestParam(name = "id",required = true) String ordersId) throws Exception {
        ModelAndView mv = new ModelAndView();
        Orders orders = ordersService.findDeatil(ordersId);
        mv.addObject("orders",orders);
        mv.setViewName("orders-show");
        return mv;
    }
}
```

#### Spring-Security

- SpringSecurity：是 Spring 项目组中用来提供安全认证服务的框架
  - “认证”，是为用户建立一个他所声明的主体。主题一般式指用户，设备或可以在你系 统中执行动作的其他系 统。
  -  “授权”指的是一个用户能否在你的应用中执行某个操作，在到达授权判断之前，身份的主题已经由 身份验证 过程建立了

##### 快速入门

- 导入依赖

```xml
	<properties>
		<spring.version>5.0.2.RELEASE</spring.version>
		<spring.security.version>5.0.1.RELEASE</spring.security.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-core</artifactId>
			<version>${spring.version}</version>
		</dependency>
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-web</artifactId>
			<version>${spring.version}</version>
		</dependency>
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-webmvc</artifactId>
			<version>${spring.version}</version>
		</dependency>
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-context-support</artifactId>
			<version>${spring.version}</version>
		</dependency>
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-test</artifactId>
			<version>${spring.version}</version>
		</dependency>
		<dependency>
			<groupId>org.springframework</groupId>
			<artifactId>spring-jdbc</artifactId>
			<version>${spring.version}</version>
		</dependency>
    <!-- spring-security相关依赖 -->
	<dependency>
		<groupId>org.springframework.security</groupId>
		<artifactId>spring-security-web</artifactId>
		<version>${spring.security.version}</version>
	</dependency>
	<dependency>
		<groupId>org.springframework.security</groupId>
		<artifactId>spring-security-config</artifactId>
		<version>${spring.security.version}</version>
	</dependency>
	<dependency>
		<groupId>javax.servlet</groupId>
		<artifactId>javax.servlet-api</artifactId>
		<version>3.1.0</version>
		<scope>provided</scope>
	</dependency>
</dependencies>
<build>
	<plugins>
		<!-- java编译插件 -->
		<plugin>
			<groupId>org.apache.maven.plugins</groupId>
			<artifactId>maven-compiler-plugin</artifactId>
			<version>3.2</version>
			<configuration>
				<source>12</source>
				<target>12</target>
				<encoding>UTF-8</encoding>
			</configuration>
		</plugin>
		<plugin>
			<groupId>org.apache.tomcat.maven</groupId>
			<artifactId>tomcat7-maven-plugin</artifactId>
			<configuration>
				<!-- 指定端口 -->
				<port>8090</port>
				<!-- 请求路径 -->
				<path>/</path>
			</configuration>
		</plugin>
	</plugins>
</build>
```
- 配置web.xml
  - filter-name:springSecurityFilterChain这个名称是固定的，不可更改；spring框架根据这个filter-name获取特定的bean对象，并加载spring-security框架所需的各种filter

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns="http://java.sun.com/xml/ns/javaee"
	xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
	version="2.5">
	<display-name>SpringSecurity314</display-name>

	<context-param>
		<param-name>contextConfigLocation</param-name>
		<param-value>classpath:spring-security.xml</param-value>
	</context-param>
	<listener>
		<listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
	</listener>
	<filter>
		<filter-name>springSecurityFilterChain</filter-name>
		<filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
	</filter>
	<filter-mapping>
		<filter-name>springSecurityFilterChain</filter-name>
        <!-- 配置拦截路径 -->
		<url-pattern>/*</url-pattern>
	</filter-mapping>
    <!-- 配置默认首页文件类型 -->
	<welcome-file-list>
		<welcome-file>index.html</welcome-file>
		<welcome-file>index.htm</welcome-file>
		<welcome-file>index.jsp</welcome-file>
		<welcome-file>default.html</welcome-file>
		<welcome-file>default.htm</welcome-file>
		<welcome-file>default.jsp</welcome-file>
	</welcome-file-list>
</web-app>
```

- 配置spring-security.xml
  - spring-security框架有两种认证方式
    - 基于xml配置文件进行认证
    - 基于数据库进行认证
  - 快速入门使用简单的xml配置方式

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:security="http://www.springframework.org/schema/security"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans
          http://www.springframework.org/schema/beans/spring-beans.xsd
          http://www.springframework.org/schema/security
          http://www.springframework.org/schema/security/spring-security.xsd">        
          
	
	<!-- 配置不过滤的资源（静态资源及登录相关） -->
	<security:http security="none" pattern="/login.html" />
	<security:http security="none" pattern="/failer.html" />
	<security:http auto-config="true" use-expressions="false" >
		<!-- 配置资料连接，表示任意路径都需要ROLE_USER权限 -->
		<security:intercept-url pattern="/**" access="ROLE_USER" />
		<!-- 自定义登陆页面，login-page 自定义登陆页面 authentication-failure-url 用户权限校验失败之后才会跳转到这个页面，如果数据库中没有这个用户则不会跳转到这个页面。 
			default-target-url 登陆成功后跳转的页面。 注：登陆页面用户名固定 username，密码 password，action:login -->
		<security:form-login login-page="/login.html"
			login-processing-url="/login" username-parameter="username"
			password-parameter="password" authentication-failure-url="/failer.html"
			default-target-url="/success.html" 
			/>
		<!-- 登出， invalidate-session 是否删除session logout-url：登出处理链接 logout-success-url：登出成功页面 
			注：登出操作 只需要链接到 logout即可登出当前用户 -->
		<security:logout invalidate-session="true" logout-url="/logout"
			logout-success-url="/login.jsp" />
		<!-- 关闭CSRF,默认是开启的 -->
		<security:csrf disabled="true" />
	</security:http>
	<security:authentication-manager>
		<security:authentication-provider>
			<security:user-service>
				<security:user name="user" password="{noop}user"
					authorities="ROLE_USER" />
				<security:user name="admin" password="{noop}admin"
					authorities="ROLE_ADMIN" />
			</security:user-service>
		</security:authentication-provider>
	</security:authentication-manager>
</beans>
```

- 测试

  - 随意访问任意页面，会自动跳转到/login认证页面

  - 当访问index.html页面时发现会弹出登录窗口，即使没有建立下面的登录页面，但会跳到上面的登录页面
- 这是因为在spring-security.xml中设置http的auto-conﬁg=”true”时Spring Security会自动生成的。 

##### 使用数据库认证

- 在Spring Security中如果想要使用数据进行认证操作，有很多种操作方式，这里使用UserDetails、 UserDetailsService来完成操作。

- 编写实体类

  - UserInfo

  ```java
  import java.util.List;
  
  public class UserInfo {
  
      private String id;
      private String username;
      private String email;
      private String password;
      private String phoneNum;
      private int status;
      private String statusStr;
      private List<Role> roles;
  
      public String getId() {
          return id;
      }
  
      public void setId(String id) {
          this.id = id;
      }
  
      public String getUsername() {
          return username;
      }
  
      public void setUsername(String username) {
          this.username = username;
      }
  
      public String getEmail() {
          return email;
      }
  
      public void setEmail(String email) {
          this.email = email;
      }
  
      public String getPassword() {
          return password;
      }
  
      public void setPassword(String password) {
          this.password = password;
      }
  
      public String getPhoneNum() {
          return phoneNum;
      }
  
      public void setPhoneNum(String phoneNum) {
          this.phoneNum = phoneNum;
      }
  
      public int getStatus() {
          return status;
      }
  
      public void setStatus(int status) {
          this.status = status;
      }
  
      public String getStatusStr() {
          return statusStr;
      }
  
      public void setStatusStr(String statusStr) {
          this.statusStr = statusStr;
      }
  
      public List<Role> getRoles() {
          return roles;
      }
  
      public void setRoles(List<Role> roles) {
          this.roles = roles;
      }
  }
  ```

  - Role

  ```java
  import org.springframework.security.core.userdetails.User;
  
  import java.util.List;
  
  public class Role {
  
      private String id;
      private String roleName;
      private String roleDesc;
      private List<Permission> permissions;
      private List<User> users;
  
      public String getId() {
          return id;
      }
  
      public void setId(String id) {
          this.id = id;
      }
  
      public String getRoleName() {
          return roleName;
      }
  
      public void setRoleName(String roleName) {
          this.roleName = roleName;
      }
  
      public String getRoleDesc() {
          return roleDesc;
      }
  
      public void setRoleDesc(String roleDesc) {
          this.roleDesc = roleDesc;
      }
  
      public List<Permission> getPermissions() {
          return permissions;
      }
  
      public void setPermissions(List<Permission> permissions) {
          this.permissions = permissions;
      }
  
      public List<User> getUsers() {
          return users;
      }
  
      public void setUsers(List<User> users) {
          this.users = users;
      }
  }
  ```

- 自定义重写UserDetailsService接口以及实现类

  - IUserService

  ```java
  import org.springframework.security.core.userdetails.UserDetailsService;
  
  public interface IUserService extends UserDetailsService {
  }
  ```

  - UserServiceImpl
    - 使用已经封装好的UserDeatils接口的实现类User

  ```java
  import cn.jazz.dao.IUserDao;
  import cn.jazz.domain.Role;
  import cn.jazz.domain.UserInfo;
  import cn.jazz.service.IUserService;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.security.core.authority.SimpleGrantedAuthority;
  import org.springframework.security.core.userdetails.User;
  import org.springframework.security.core.userdetails.UserDetails;
  import org.springframework.security.core.userdetails.UsernameNotFoundException;
  import org.springframework.stereotype.Service;
  import org.springframework.transaction.annotation.Transactional;
  
  import java.util.ArrayList;
  import java.util.List;
  
  @Service("userService")
  @Transactional
  public class IUserServiceImpl implements IUserService {
  
      @Autowired
      private IUserDao userDao;
  
      @Override
      public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
          UserInfo userInfo = null;
          try {
              userInfo = userDao.findByUsername(username);
          } catch (Exception e) {
              e.printStackTrace();
          }
          List<Role> roles = userInfo.getRoles();
          List<SimpleGrantedAuthority> authorities = getAuthority(roles);
          User user = new User(userInfo.getUsername(),"{noop}"+userInfo.getPassword(),userInfo.getStatus()==1?true:false,true,true,true,authorities);
          return user;
      }
  
      private List<SimpleGrantedAuthority> getAuthority(List<Role> roles){
          List<SimpleGrantedAuthority> authorities = new ArrayList<>();
          for (Role role : roles) {
              authorities.add(new SimpleGrantedAuthority("ROLE_"+role.getRoleName()));
          }
          return authorities;
      }
  }
  ```

- 编写持久层

  - IUserDao

  ```java
  import cn.jazz.domain.UserInfo;
  import org.apache.ibatis.annotations.Many;
  import org.apache.ibatis.annotations.Result;
  import org.apache.ibatis.annotations.Results;
  import org.apache.ibatis.annotations.Select;
  
  import java.util.List;
  
  public interface IUserDao {
  
      /**
       * 根据用户名查询出对应的用户信息，并封装为UserInfo对象
       * @param username
       * @return
       * @throws Exception
       */
      @Select("select * from users where username=#{username}")
      @Results({
              @Result(id = true, property = "id", column = "id"),
              @Result(column = "username", property = "username"),
              @Result(column = "email", property = "email"),
              @Result(column = "password", property = "password"),
              @Result(column = "phoneNum", property = "phoneNum"),
              @Result(column = "status", property = "status"),
              @Result(column = "id", property = "roles", javaType = List.class,
                      many = @Many(select = "cn.jazz.dao.IRoleDao.findByUserId"))
      })
      public UserInfo findByUsername(String username) throws Exception;
  }
  ```

  - IRoleDao

  ```java
  import cn.jazz.domain.Role;
  import org.apache.ibatis.annotations.Select;
  
  import java.util.List;
  
  public interface IRoleDao {
  
      /**
       * 根据用户Id查询出用户的角色
       * @param userId
       * @return 用户角色的集合
       */
      @Select("select * from role where id in (select roleId from USERS_ROLE where userId=#{userId})")
      public List<Role> findByUserId(String userId) throws Exception;
  }
  ```

- 用户登录流程

  - spring-security负责了原来controller的工作
  - 业务层也实现了spring-security规范化的UserDetailsService接口
  - 最终结果封装为spring-security提供的User类

#### 用户功能

##### 查询用户

###### 1.编写控制器

- UserController

```java
import cn.jazz.domain.UserInfo;
import cn.jazz.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;

    @RequestMapping("/findAll.do")
    public ModelAndView findAll() throws Exception {
        ModelAndView mv = new ModelAndView();
        List<UserInfo> userInfoList = userService.findAll();
        mv.addObject("userList",userInfoList);
        mv.setViewName("user-list");
        return mv;
    }
}
```

###### 2.编写业务层

- IUserService

```java
import cn.jazz.domain.UserInfo;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface IUserService extends UserDetailsService {
    public List<UserInfo> findAll() throws Exception;
}
```

- IUserServiceImpl

```java
import cn.jazz.dao.IUserDao;
import cn.jazz.domain.Role;
import cn.jazz.domain.UserInfo;
import cn.jazz.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service("userService")
@Transactional
public class IUserServiceImpl implements IUserService {

    @Autowired
    private IUserDao userDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo userInfo = null;
        try {
            userInfo = userDao.findByUsername(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Role> roles = userInfo.getRoles();
        List<SimpleGrantedAuthority> authorities = getAuthority(roles);
        User user = new User(userInfo.getUsername(),"{noop}"+userInfo.getPassword(),userInfo.getStatus()==1?true:false,true,true,true,authorities);
        return user;
    }

    private List<SimpleGrantedAuthority> getAuthority(List<Role> roles){
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_"+role.getRoleName()));
        }
        return authorities;
    }

    @Override
    public List<UserInfo> findAll() throws Exception {
        return userDao.findAll();
    }
}
```

###### 3.编写持久层

- IUserDao

```java
import cn.jazz.domain.UserInfo;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface IUserDao {

    /**
     * 根据用户名查询出对应的用户信息，并封装为UserInfo对象
     * @param username
     * @return
     * @throws Exception
     */
    @Select("select * from users where username=#{username}")
    @Results({
            @Result(id = true, property = "id", column = "id"),
            @Result(column = "username", property = "username"),
            @Result(column = "email", property = "email"),
            @Result(column = "password", property = "password"),
            @Result(column = "phoneNum", property = "phoneNum"),
            @Result(column = "status", property = "status"),
            @Result(column = "id", property = "roles", javaType = List.class,
                    many = @Many(select = "cn.jazz.dao.IRoleDao.findByUserId"))
    })
    public UserInfo findByUsername(String username) throws Exception;

    /**
     * 查询所有用户信息
     * @return
     */
    @Select("select * from users")
    public List<UserInfo> findAll() throws Exception;
}
```

###### 4.修改UserInfo实体类代码

```java
public String getStatusStr() {
    //状态 0未开启 1开启
    if (status==0) statusStr="未开启";
    else if (status==1) statusStr="开启";
    return statusStr;
}
```

##### 添加用户

- 难点：对用户的密码进行加密

  - 使用spring-security提供的加密类BCryptPasswordEncoder进行加密
  - 在spring-security.xml配置文件中配置加密类和加密方式

  ```xml
      <!-- 切换成数据库中的用户名和密码 -->
      <security:authentication-manager>
          <security:authentication-provider user-service-ref="userService">
              <!-- 配置加密的方式 -->
              <security:password-encoder ref="passwordEncoder"/>
          </security:authentication-provider>
      </security:authentication-manager>
  
      <!-- 配置加密类 -->
      <bean id="passwordEncoder" class="org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder"/>
  ```

###### 1.编写控制器

```java
import cn.jazz.domain.UserInfo;
import cn.jazz.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;

    @RequestMapping("/findAll.do")
    public ModelAndView findAll() throws Exception {
        ModelAndView mv = new ModelAndView();
        List<UserInfo> userInfoList = userService.findAll();
        mv.addObject("userList",userInfoList);
        mv.setViewName("user-list");
        return mv;
    }

    @RequestMapping("/save.do")
    public String save(UserInfo userInfo) throws Exception {
        userService.save(userInfo);
        return "redirect:findAll.do";
    }
}
```

###### 2.编写业务层

- IUserService

```java
import cn.jazz.domain.UserInfo;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface IUserService extends UserDetailsService {
    public List<UserInfo> findAll() throws Exception;

    public void save(UserInfo userInfo) throws Exception;
}
```

- IUserServiceImpl

```java
import cn.jazz.dao.IUserDao;
import cn.jazz.domain.Role;
import cn.jazz.domain.UserInfo;
import cn.jazz.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service("userService")
@Transactional
public class IUserServiceImpl implements IUserService {

    @Autowired
    private IUserDao userDao;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder; //加密类

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo userInfo = null;
        try {
            userInfo = userDao.findByUsername(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Role> roles = userInfo.getRoles();
        List<SimpleGrantedAuthority> authorities = getAuthority(roles);
        User user = new User(userInfo.getUsername(),userInfo.getPassword(),userInfo.getStatus()==1?true:false,true,true,true,authorities);
        return user;
    }

    /**
     * 功能：将角色类型封装
     * @param roles
     * @return
     */
    private List<SimpleGrantedAuthority> getAuthority(List<Role> roles){
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_"+role.getRoleName()));
        }
        return authorities;
    }

    @Override
    public List<UserInfo> findAll() throws Exception {
        return userDao.findAll();
    }

    @Override
    public void save(UserInfo userInfo) throws Exception {
        //对明文密码进行加密
        userInfo.setPassword(passwordEncoder.encode(userInfo.getPassword()));
        userDao.save(userInfo);
    }
}
```

###### 3.编写持久层

```java
import cn.jazz.domain.UserInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface IUserDao {

    /**
     * 根据用户名查询出对应的用户信息，并封装为UserInfo对象
     * @param username
     * @return
     * @throws Exception
     */
    @Select("select * from users where username=#{username}")
    @Results({
            @Result(id = true, property = "id", column = "id"),
            @Result(column = "username", property = "username"),
            @Result(column = "email", property = "email"),
            @Result(column = "password", property = "password"),
            @Result(column = "phoneNum", property = "phoneNum"),
            @Result(column = "status", property = "status"),
            @Result(column = "id", property = "roles", javaType = List.class,
                    many = @Many(select = "cn.jazz.dao.IRoleDao.findByUserId"))
    })
    public UserInfo findByUsername(String username) throws Exception;

    /**
     * 查询所有用户信息
     * @return
     */
    @Select("select * from users")
    public List<UserInfo> findAll() throws Exception;

    /**
     * 插入一条用户记录
     * @param userInfo
     */
    @Insert("insert into USERS(Email,Username,Password,Phonenum,Status) values(#{email},#{username},#{password},#{phoneNum},#{status})")
    public void save(UserInfo userInfo) throws Exception;
}
```

###### 4.编写加密工具类

- BCryptPasswordEncoderUtils

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptPasswordEncoderUtils {

    private static BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static String encodePassword(String password){
        return passwordEncoder.encode(password);
    }

    public static void main(String[] args) {
        System.out.println(encodePassword("123"));;
    }
}
```

##### 用户详情

###### 1.编写控制器

- UserController

```java
import cn.jazz.domain.UserInfo;
import cn.jazz.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;

    @RequestMapping("/findAll.do")
    public ModelAndView findAll() throws Exception {
        ModelAndView mv = new ModelAndView();
        List<UserInfo> userInfoList = userService.findAll();
        mv.addObject("userList",userInfoList);
        mv.setViewName("user-list");
        return mv;
    }

    @RequestMapping("/save.do")
    public String save(UserInfo userInfo) throws Exception {
        userService.save(userInfo);
        return "redirect:findAll.do";
    }

    @RequestMapping("/findDetails.do")
    public ModelAndView findDetails(String id) throws Exception {
        ModelAndView mv = new ModelAndView();
        UserInfo userInfo = userService.findDetails(id);
        mv.addObject("user",userInfo);
        mv.setViewName("user-show");
        return mv;
    }
}
```

###### 2.编写业务层

- IUserService

```java
import cn.jazz.domain.UserInfo;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface IUserService extends UserDetailsService {
    public List<UserInfo> findAll() throws Exception;

    public void save(UserInfo userInfo) throws Exception;

    public UserInfo findDetails(String id) throws Exception;
}
```

- IUserServiceImpl

```java
import cn.jazz.dao.IUserDao;
import cn.jazz.domain.Role;
import cn.jazz.domain.UserInfo;
import cn.jazz.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service("userService")
@Transactional
public class IUserServiceImpl implements IUserService {

    @Autowired
    private IUserDao userDao;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder; //加密类

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo userInfo = null;
        try {
            userInfo = userDao.findByUsername(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Role> roles = userInfo.getRoles();
        List<SimpleGrantedAuthority> authorities = getAuthority(roles);
        User user = new User(userInfo.getUsername(),userInfo.getPassword(),userInfo.getStatus()==1?true:false,true,true,true,authorities);
        return user;
    }

    /**
     * 功能：将角色类型封装
     * @param roles
     * @return
     */
    private List<SimpleGrantedAuthority> getAuthority(List<Role> roles){
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_"+role.getRoleName()));
        }
        return authorities;
    }

    @Override
    public List<UserInfo> findAll() throws Exception {
        return userDao.findAll();
    }

    @Override
    public void save(UserInfo userInfo) throws Exception {
        //对明文密码进行加密
        userInfo.setPassword(passwordEncoder.encode(userInfo.getPassword()));
        userDao.save(userInfo);
    }

    @Override
    public UserInfo findDetails(String id) throws Exception {
        return userDao.findById(id);
    }
}
```

###### 3.编写持久层

- IUserDao

```java
import cn.jazz.domain.UserInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface IUserDao {

    /**
     * 根据用户名查询出对应的用户信息，并封装为UserInfo对象
     * @param username
     * @return
     * @throws Exception
     */
    @Select("select * from users where username=#{username}")
    @Results({
            @Result(id = true, property = "id", column = "id"),
            @Result(column = "username", property = "username"),
            @Result(column = "email", property = "email"),
            @Result(column = "password", property = "password"),
            @Result(column = "phoneNum", property = "phoneNum"),
            @Result(column = "status", property = "status"),
            @Result(column = "id", property = "roles", javaType = List.class,
                    many = @Many(select = "cn.jazz.dao.IRoleDao.findByUserId"))
    })
    public UserInfo findByUsername(String username) throws Exception;

    /**
     * 查询所有用户信息
     * @return
     */
    @Select("select * from users")
    public List<UserInfo> findAll() throws Exception;

    /**
     * 插入一条用户记录
     * @param userInfo
     */
    @Insert("insert into USERS(Email,Username,Password,Phonenum,Status) values(#{email},#{username},#{password},#{phoneNum},#{status})")
    public void save(UserInfo userInfo) throws Exception;

    /**
     * 根据用户ID查询用户的详情(包括角色和权限)
     * @param id
     * @return
     * @throws Exception
     */
    @Select("select * from users where id=#{id}")
    @Results({
            @Result(id = true, property = "id", column = "id"),
            @Result(column = "username", property = "username"),
            @Result(column = "email", property = "email"),
            @Result(column = "password", property = "password"),
            @Result(column = "phoneNum", property = "phoneNum"),
            @Result(column = "status", property = "status"),
            @Result(column = "id", property = "roles", javaType = List.class,
                    many = @Many(select = "cn.jazz.dao.IRoleDao.findRolesByUserId"))
    })
    public UserInfo findById(String id) throws Exception;
}
```

- IRoleDao

```java
import cn.jazz.domain.Role;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface IRoleDao {

    /**
     * 根据用户Id查询出用户的角色
     * @param userId
     * @return 用户角色的集合
     */
    @Select("select * from role where id in (select roleId from USERS_ROLE where userId=#{userId})")
    public List<Role> findByUserId(String userId) throws Exception;

    /**
     * 根据用户Id查询出用户的角色和对应的权限信息
     * @param userId
     * @return 用户角色的集合
     */
    @Select("select * from role where id in (select roleId from USERS_ROLE where userId=#{userId})")
    @Results({
            @Result(id=true,column="id",property="id"),
            @Result(column="roleName",property="roleName"),
            @Result(column="roleDesc",property="roleDesc"),
            @Result(column="id",property="permissions",javaType=List.class,
                    many=@Many(select="cn.jazz.dao.IPermissionDao.findByRoleId"))
    })
    public List<Role> findRolesByUserId(String userId) throws Exception;
}
```

- IPermissionDao

```java
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
```

#### 角色功能

##### 查询角色

###### 1.编写控制器

- RoleController

```java
import cn.jazz.domain.Permission;
import cn.jazz.domain.Role;
import cn.jazz.service.IRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private IRoleService roleService;

    @RequestMapping("/findAll.do")
    public ModelAndView findAll() throws Exception {
        ModelAndView mv = new ModelAndView();
        List<Role> roles = roleService.findAll();
        mv.addObject("roleList",roles);
        mv.setViewName("role-list");
        return mv;
    }
}
```

###### 2.编写业务层

- IRoleService

```java
import cn.jazz.domain.Permission;
import cn.jazz.domain.Role;

import java.util.List;

public interface IRoleService {

    public List<Role> findAll() throws Exception;

}
```

- RoleServiceImpl

```java
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

}
```

###### 3.编写持久层

- IRoleDao

```java
import cn.jazz.domain.Permission;
import cn.jazz.domain.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface IRoleDao {

    /**
     * 根据用户Id查询出用户的角色
     * @param userId
     * @return 用户角色的集合
     */
    @Select("select * from role where id in (select roleId from USERS_ROLE where userId=#{userId})")
    public List<Role> findByUserId(String userId) throws Exception;

    /**
     * 根据用户Id查询出用户的角色和对应的权限信息
     * @param userId
     * @return 用户角色的集合
     */
    @Select("select * from role where id in (select roleId from USERS_ROLE where userId=#{userId})")
    @Results({
            @Result(id=true,column="id",property="id"),
            @Result(column="roleName",property="roleName"),
            @Result(column="roleDesc",property="roleDesc"),
            @Result(column="id",property="permissions",javaType=List.class,
                    many=@Many(select="cn.jazz.dao.IPermissionDao.findByRoleId"))
    })
    public List<Role> findRolesByUserId(String userId) throws Exception;

    /**
     * 查询所有角色信息
     * @return
     * @throws Exception
     */
    @Select("select * from role")
    public List<Role> findAll() throws Exception;
}
```

##### 添加角色

###### 1.编写控制器

- RoleController

```java
import cn.jazz.domain.Permission;
import cn.jazz.domain.Role;
import cn.jazz.service.IRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private IRoleService roleService;

    @RequestMapping("/findAll.do")
    public ModelAndView findAll() throws Exception {
        ModelAndView mv = new ModelAndView();
        List<Role> roles = roleService.findAll();
        mv.addObject("roleList",roles);
        mv.setViewName("role-list");
        return mv;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping("/save.do")
    public String save(Role role) throws Exception {
        roleService.save(role);
        return "redirect:findAll.do";
    }
}
```

###### 2.编写业务层

- IRoleService

```java
import cn.jazz.domain.Permission;
import cn.jazz.domain.Role;

import java.util.List;

public interface IRoleService {

    public List<Role> findAll() throws Exception;

    public void save(Role role) throws Exception;
}
```

- IRoleServiceImpl

```java
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
}
```

###### 3.编写持久层

```java
import cn.jazz.domain.Permission;
import cn.jazz.domain.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface IRoleDao {

    /**
     * 根据用户Id查询出用户的角色
     * @param userId
     * @return 用户角色的集合
     */
    @Select("select * from role where id in (select roleId from USERS_ROLE where userId=#{userId})")
    public List<Role> findByUserId(String userId) throws Exception;

    /**
     * 根据用户Id查询出用户的角色和对应的权限信息
     * @param userId
     * @return 用户角色的集合
     */
    @Select("select * from role where id in (select roleId from USERS_ROLE where userId=#{userId})")
    @Results({
            @Result(id=true,column="id",property="id"),
            @Result(column="roleName",property="roleName"),
            @Result(column="roleDesc",property="roleDesc"),
            @Result(column="id",property="permissions",javaType=List.class,
                    many=@Many(select="cn.jazz.dao.IPermissionDao.findByRoleId"))
    })
    public List<Role> findRolesByUserId(String userId) throws Exception;

    /**
     * 查询所有角色信息
     * @return
     * @throws Exception
     */
    @Select("select * from role")
    public List<Role> findAll() throws Exception;

    /**
     * 插入一条角色记录
     * @param role
     * @throws Exception
     */
    @Insert("insert into role(rolename,roledesc) values(#{roleName},#{roleDesc})")
    public void save(Role role) throws Exception;

}
```

#### 权限功能

##### 查询权限

###### 1.编写控制器

- PermissionController

```java
import cn.jazz.domain.Permission;
import cn.jazz.service.IPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/permission")
public class PermissionController {

    @Autowired
    private IPermissionService permissionService;

    @RequestMapping("/findAll.do")
    public ModelAndView findAll() throws Exception {
        ModelAndView mv = new ModelAndView();
        List<Permission> permissions = permissionService.findAll();
        mv.addObject("permissionList",permissions);
        mv.setViewName("permission-list");
        return mv;
    }

}
```

###### 2.编写业务层

- IPermissionService

```java
import cn.jazz.domain.Permission;

import java.util.List;

public interface IPermissionService {

    public List<Permission> findAll() throws Exception;

}
```

- IPermissionServiceImpl

```java
import cn.jazz.dao.IPermissionDao;
import cn.jazz.domain.Permission;
import cn.jazz.service.IPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PermissionServiceIpl implements IPermissionService {

    @Autowired
    private IPermissionDao permissionDao;

    @Override
    public List<Permission> findAll() throws Exception {
        return permissionDao.findAll();
    }
}
```

###### 3.编写持久层

- IPermissionDao

```java
import cn.jazz.domain.Permission;
import org.apache.ibatis.annotations.Insert;
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

    /**
     * 查询所有权限信息
     * @return
     * @throws Exception
     */
    @Select("select * from permission")
    public List<Permission> findAll() throws Exception;

}
```

##### 添加权限

###### 1.编写控制器

- PermissionController

```java
import cn.jazz.domain.Permission;
import cn.jazz.service.IPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/permission")
public class PermissionController {

    @Autowired
    private IPermissionService permissionService;

    @RequestMapping("/findAll.do")
    public ModelAndView findAll() throws Exception {
        ModelAndView mv = new ModelAndView();
        List<Permission> permissions = permissionService.findAll();
        mv.addObject("permissionList",permissions);
        mv.setViewName("permission-list");
        return mv;
    }

    @RequestMapping("/save.do")
    public String save(Permission permission) throws Exception {
        permissionService.save(permission);
        return "redirect:findAll.do";
    }
}
```

###### 2.编写业务层

- IPermissionService

```java
import cn.jazz.domain.Permission;

import java.util.List;

public interface IPermissionService {

    public List<Permission> findAll() throws Exception;

    public void save(Permission permission) throws Exception;
}
```

- IPermissionServiceImpl

```java
import cn.jazz.dao.IPermissionDao;
import cn.jazz.domain.Permission;
import cn.jazz.service.IPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PermissionServiceIpl implements IPermissionService {

    @Autowired
    private IPermissionDao permissionDao;

    @Override
    public List<Permission> findAll() throws Exception {
        return permissionDao.findAll();
    }

    @Override
    public void save(Permission permission) throws Exception {
        permissionDao.save(permission);
    }
}
```

###### 3.编写持久层

- IPermissionDao

```java
import cn.jazz.domain.Permission;
import org.apache.ibatis.annotations.Insert;
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

    /**
     * 查询所有权限信息
     * @return
     * @throws Exception
     */
    @Select("select * from permission")
    public List<Permission> findAll() throws Exception;

    /**
     * 插入一条权限记录
     * @param permission
     * @throws Exception
     */
    @Insert("insert into permission(permissionName,url) values(#{permissionName},#{url})")
    public void save(Permission permission) throws Exception;
}
```

#### 权限关联功能

##### 用户添加角色

- 功能分析

  - 获取用户信息，并查询还能添加哪些角色(即该用户未添加的角色)，将结果展示在user-role-add.jsp页面
  - 获取页面选择的用户id和角色id**数组**，添加多条用户角色关联表的记录

- 功能一

  - UserController

  ```java
  @RequestMapping("/findUserByIdAndAllRole.do")
      public ModelAndView findUserByIdAndAllRole(@RequestParam(name = "id",required = true) String userId) throws Exception {
          ModelAndView mv = new ModelAndView();
          UserInfo userInfo = userService.findById(userId);
          List<Role> ortherRoles = userService.findOrtherRole(userId);
          mv.addObject("user",userInfo);
          mv.addObject("roleList",ortherRoles);
          mv.setViewName("user-role-add");
          return mv;
      }
  ```

  - UserServiceImpl

  ```java
  /**
       * 查询当前用户不具有的角色信息
       * @param userId
       * @return
       * @throws Exception
       */
      @Override
      public List<Role> findOrtherRole(String userId) throws Exception {
          return userDao.findOrtherRoleById(userId);
      }
  ```

  - IUserDao

  ```java
  /**
   * 查询用户不具有的角色信息
   * @param userId
   * @return
   * @throws Exception
   */
  @Select("select * from role where id not in (select roleId from users_role where userId=#{userId})")
  public List<Role> findOrtherRoleById(String userId) throws Exception;
  ```

- 功能二

  - UserController

  ```java
  @RequestMapping("/addRoleToUser.do")
      public String addRoleToUser(@RequestParam(name = "userId",required = true) String userId,@RequestParam(name = "ids",required = true) String[] roleIds) throws Exception {
          userService.addRoleToUser(userId,roleIds);
          return "redirect:findAll.do";
      }
  ```

  - UserServiceImpl

  ```java
  /**
   * 给用户添加一个或多个角色
   * @param userId
   * @param roleIds
   * @throws Exception
   */
  @Override
  public void addRoleToUser(String userId, String[] roleIds) throws Exception {
      for (String roleId : roleIds) {
          userDao.addRoleToUser(userId,roleId);
      }
  }
  ```

  - IUserDao

  ```java
  /**
   * 插入一条用户与角色关联信息
   * @param userId
   * @param roleId
   * @throws Exception
   */
  @Select("insert into USERS_ROLE(userId,RoleId) values(#{userId},#{roleId})")
  public void addRoleToUser(@Param("userId") String userId,@Param("roleId") String roleId) throws Exception;
  ```

##### 角色添加权限

- 功能分析
  - 获取用户信息，并查询还能添加哪些角色(即该用户未添加的角色)，将结果展示在user-role-add.jsp页面
  - 获取页面选择的用户id和角色id**数组**，添加多条用户角色关联表的记录

- 功能一

  - RoleController

  ```java
  @RequestMapping("/findRoleByIdAndAllPermission.do")
  public ModelAndView findRoleByIdAndAllPermission(@RequestParam(name = "id",required = true) String roleId) throws Exception {
      ModelAndView mv = new ModelAndView();
      Role role = roleService.findDetail(roleId);
      List<Permission> permissions = roleService.findOrtherPermission(roleId);
      mv.addObject("role",role);
      mv.addObject("permissionList",permissions);
      mv.setViewName("role-permission-add");
      return mv;
  }
  ```

  - RoleServiceImpl

  ```java
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
  ```

  - IRoleDao

  ```java
  /**
   * 根据角色ID查询可添加的权限信息
   * @param roleId
   * @return
   * @throws Exception
   */
  @Select("select * from permission where id not in (select permissionId from role_permission where roleId=#{roleId})")
  public List<Permission> findOrtherPermissionById(String roleId) throws Exception;
  ```

- 功能二

  - RoleController

  ```java
  @RequestMapping("/addPermissionToRole.do")
  public String addPermissionToRole(@RequestParam(name = "roleId",required = true)String roleId,@RequestParam(name = "ids",required = true)String[] permissionIds) throws Exception {
      roleService.addPermissionToRole(roleId,permissionIds);
      return "redirect:findAll.do";
  }
  ```

  - RoleServiceImpl

  ```java
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
  ```

  - IRoleDao

  ```java
  /**
   * 插入一条角色关联权限记录
   * @param roleId
   * @param permissionId
   * @throws Exception
   */
  @Select("insert into role_permission(permissionId,roleId) values(#{permissionId},#{roleId})")
  public void addPermissionToRole(@Param("roleId") String roleId,@Param("permissionId") String permissionId) throws Exception;
  ```

#### 权限控制功能

##### 服务器端控制

- 在服务器端我们可以通过Spring security提供的注解对方法来进行权限控制。
  - Spring Security在方法的权限控制上 支持三种类型的注解
    - JSR-250注解
    - @Secured注解
    - 支持表达式的注解
  - 这三种注解默认都是没有启用的，需要 单独通过global-method-security元素的对应属性进行启用

###### 1.开启注解使用 

- 配置文件 

  ```xml
  <security:global-method-security jsr250-annotations="enabled"/>
  <security:global-method-security secured-annotations="enabled"/>
  <security:global-method-security pre-post-annotations="disabled"/> 
  ```

- 注解开启

  ```
  @EnableGlobalMethodSecurity ：Spring Security默认是禁用注解的，要想开启注解，需要在继承 WebSecurityConﬁgurerAdapter的类上加@EnableGlobalMethodSecurity注解，并在该类中将 AuthenticationManager定义为Bean
  ```

###### 2.JSR-250注解 

- 使用JSR-250注解需要导入依赖

- @RolesAllowed表示访问对应方法时所应该具有的角色

```java
 示例： 
     @RolesAllowed({"USER", "ADMIN"})  
     该方法只要具有"USER", "ADMIN"任意一种权限就可以访问。
     这里可以省略前缀ROLE_，实际的权限是ROLE_ADMIN 
```

- @PermitAll表示允许所有的角色进行访问，也就是说不进行权限控制 
- @DenyAll是和PermitAll相反的，表示无论什么角色都不能访问

###### 3.@Secured注解

- @Secured注解标注的方法进行权限控制的支持，其值默认为disabled

```java
  示例：   
  @Secured("IS_AUTHENTICATED_ANONYMOUSLY")   
  public Account readAccount(Long id);   
  @Secured("ROLE_TELLER") 
```

- @Secured与@RolesAllowed的区别
  - @Secured不能省略"ROLE_"前缀
  - @Secured是spring-securoty内置的注解，不需要导入第三方依赖

###### 4.支持表达式的注解

- @PreAuthorize 在方法调用之前,基于表达式的计算结果来限制对方法的访问

```java
示例： 
@PreAuthorize("#userId == authentication.principal.userId or hasAuthority(‘ADMIN’)")
void changePassword(@P("userId") long userId ){} 
这里表示在changePassword方法执行之前，判断方法参数userId的值是否等于principal中保存的当前用户的 userId，或者当前用户是否具有ROLE_ADMIN权限，两种符合其一，就可以访问该方法。
```

- @PostAuthorize 允许方法调用,但是如果表达式计算结果为false,将抛出一个安全性异

```java
示例： 
@PostAuthorize 
User getUser("returnObject.userId == authentication.principal.userId or hasPermission(returnObject, 'ADMIN')");
```

- 常用表达式

  - ```java
    hasRole('Role_Admin') //单个角色
    ```

  - ```java
    hasAnyRole('ROLE_ADMIN','ROLE_USER') //多个角色
    ```

##### 页面端控制

- 在jsp页面中我们可以使用spring security提供的权限标签来进行权限控制 

###### 1.导入

- 在spring-security.xml配置文件中开启SPEL表达式的支持

```xml
<!--
   配置具体的规则
   auto-config="true" 不用自己编写登录的页面，框架提供默认登录页面
   use-expressions="true"    是否使用SPEL表达式
-->
<security:http auto-config="true" use-expressions="true">
```

- 导入maven依赖

```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-taglibs</artifactId>
    <version>version</version> 
</dependency> 
```

- JSP页面头导入

```jsp
<%@taglib uri="http://www.springframework.org/security/tags" prefix="security"%>
```

###### 2.常用标签

- authentication 
  - property： 只允许指定Authentication所拥有的属性，可以进行属性的级联获取，如“principle.username”， 不允许直接通过方法进行调用
  - htmlEscape：表示是否需要将html进行转义。默认为true
  - scope：与var属性一起使用，用于指定存放获取的结果的属性名的作用范围，默认我pageContext。Jsp中拥 有的作用范围都进行进行指定
  - var： 用于指定一个属性名，这样当获取到了authentication的相关信息后会将其以var指定的属性名进行存 放，默认是存放在pageConext中
  - 获取用户名不能直接使用“username”，而应该使用“principal.username”
  - 其中principal代表当前操作的用户

```jsp
<security:authentication property="" htmlEscape="" scope="" var=""/>
```

- authorize 
  - access： 需要使用表达式来判断权限，当表达式的返回结果为true时表示拥有对应的权
  - method：method属性是配合url属性一起使用的，表示用户应当具有指定url指定method访问的权限， method的默认值为GET，可选值为http请求的7种方法
  - url：url表示如果用户拥有访问指定url的权限即表示可以显示authorize标签包含的内容
  - var：用于指定将权限鉴定的结果存放在pageContext的哪个属性中

```jsp
<security:authorize access="" method="" url="" var=""></security:authorize>
```

- accesscontrollist (不常用)

  - accesscontrollist标签是用于鉴定ACL权限的。其一共定义了三个属性：hasPermission、domainObject和var， 其中前两个是必须指定的

  ```jsp
  <security:accesscontrollist hasPermission="" domainObject="" var=""></security:accesscontrollist> 
  ```

  - hasPermission：hasPermission属性用于指定以逗号分隔的权限列表
  - domainObject：domainObject用于指定对应的域对象
  - var：var则是用以将鉴定的结果以指定的属性名存入pageContext中，以供同一页面的其它地方使用

#### 日志功能

##### 基于AOP实现日志记录

- 配置准备

  - web.xml

  ```xml
  <listener>
          <!-- 配置RequestContextListener后，可以获取HttpRequestServlet对象 -->
          <listener-class>org.springframework.web.context.request.RequestContextListener</listener-class>
  </listener>
  ```

- 实体类SysLog

```java
import cn.jazz.utils.DateUtils;

import java.util.Date;

public class SysLog {
    private String id;
    private Date visitTime;
    private String visitTimeStr;
    private String username;
    private String ip;
    private String url;
    private Long executionTime;
    private String method;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getVisitTime() {
        return visitTime;
    }

    public void setVisitTime(Date visitTime) {
        this.visitTime = visitTime;
    }

    public String getVisitTimeStr() {
        if (visitTime!=null){
            visitTimeStr = DateUtils.dateToString(visitTime,"yyyy-MM-dd HH:mm");
        }
        return visitTimeStr;
    }

    public void setVisitTimeStr(String visitTimeStr) {
        this.visitTimeStr = visitTimeStr;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
```

- LogAop

```java
import cn.jazz.domain.SysLog;
import cn.jazz.service.ISysLogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;

@Component //容器注入声明，任意类型
@Aspect //切面声明
public class LogAop {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private ISysLogService sysLogService;

    private Date visitTime; //开始时间
    private Class clazz; //访问的类
    private Method method; //访问的方法

    @Before("execution(* cn.jazz.controller.*.*(..))")
    public void doBefore(JoinPoint jp) throws NoSuchMethodException {

        visitTime = new Date(); //当前时间就是开始访问的时间

        clazz = jp.getTarget().getClass(); //获取要访问的类对象

        //获取要访问的方法对象
        String methodName = jp.getSignature().getName(); //获取要访问的方法的名称
        Object[] args = jp.getArgs(); //获取访问方法的参数,只有参数类型的集合
        if (args==null || args.length==0){
            method = clazz.getMethod(methodName); //只能获取无参的方法对象
        }else {
            Class[] classArgs = new Class[args.length]; //创建一个空的Class数组
            for (int i = 0; i < args.length; i++) {
                classArgs[i]=args[i].getClass();
            }
            method = clazz.getMethod(methodName,classArgs);
        }

    }

    @After("execution(* cn.jazz.controller.*.*(..))")
    public void doAfter(JoinPoint jp) throws Exception {
        if (clazz!=null && method!=null && clazz!=LogAop.class){
            //获取访问者的的IP地址
            String ip = request.getRemoteAddr();
            //获取访问时长
            long executionTime = new Date().getTime() - visitTime.getTime();

            //获取访问的url
            String url="";
            //获取访问类的RequestMapping注解对象
            RequestMapping classAnnotation = (RequestMapping) clazz.getAnnotation(RequestMapping.class);
            if (classAnnotation!=null){
                String classUrl = classAnnotation.value()[0];
                //获取访问方法的RequestMapping注解对象
                RequestMapping methodAnnotation = method.getAnnotation(RequestMapping.class);
                if (methodAnnotation!=null){
                    String methodUrl = methodAnnotation.value()[0];
                    //给访问url赋值
                    url=classUrl+methodUrl;
                }
            }

            //获取当前操作的用户名
            String username = "";
            //获取SecurityContext对象
            SecurityContext securityContext = SecurityContextHolder.getContext();
            //获取当前操作的用户Principal对象，可以强制转换为spring-security提供的User对象
            User user = (User) securityContext.getAuthentication().getPrincipal();
            //给访问者的用户名赋值
            username = user.getUsername();

            //将日志相关信息封装到SysLog对象
            SysLog sysLog = new SysLog();
            sysLog.setVisitTime(visitTime);
            sysLog.setExecutionTime(executionTime);
            sysLog.setIp(ip);
            sysLog.setUrl(url);
            sysLog.setUsername(username);
            sysLog.setMethod("[类名]"+clazz.getName()+"[方法名]"+method.getName());

            //调用service完成日志记录操作
            sysLogService.save(sysLog);
        }
    }
}
```

- SysLogService

```java
import cn.jazz.dao.ISysLogDao;
import cn.jazz.domain.SysLog;
import cn.jazz.service.ISysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SysLogServiceImpl implements ISysLogService {

    @Autowired
    private ISysLogDao sysLogDao;

    @Override
    public void save(SysLog sysLog) throws Exception {
        sysLogDao.save(sysLog);
    }
}
```

- SysLogDao

```java
import cn.jazz.domain.SysLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ISysLogDao {

    @Insert("insert into sysLog(visitTime,ip,url,username,executionTime,method) values(#{visitTime},#{ip},#{url},#{username},#{executionTime},#{method})")
    public void save(SysLog sysLog) throws Exception;
}
```

##### 日志查询

- sysLogController

```java
import cn.jazz.domain.SysLog;
import cn.jazz.service.ISysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/sysLog")
public class sysLogController {

    @Autowired
    private ISysLogService service;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping("/findAll.do")
    public ModelAndView findAll() throws Exception {
        ModelAndView mv = new ModelAndView();
        List<SysLog> sysLogList = service.findAll();
        mv.addObject("sysLogs",sysLogList);
        mv.setViewName("syslog-list");
        return mv;
    }
}
```

- SysLogService

```java
import cn.jazz.dao.ISysLogDao;
import cn.jazz.domain.SysLog;
import cn.jazz.service.ISysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SysLogServiceImpl implements ISysLogService {

    @Autowired
    private ISysLogDao sysLogDao;

    @Override
    public void save(SysLog sysLog) throws Exception {
        sysLogDao.save(sysLog);
    }

    @Override
    public List<SysLog> findAll() throws Exception {
        return sysLogDao.findAll();
    }
}
```

- SysLogDao

```java
import cn.jazz.domain.SysLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ISysLogDao {

    @Insert("insert into sysLog(visitTime,ip,url,username,executionTime,method) values(#{visitTime},#{ip},#{url},#{username},#{executionTime},#{method})")
    public void save(SysLog sysLog) throws Exception;

    @Select("select * from sysLog")
    public List<SysLog> findAll() throws Exception;
}
```







