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

--创建产品表
CREATE TABLE PRODUCT(  
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
insert into PRODUCT
  (id,
   productnum,
   productname,
   cityname,
   departuretime,
   productprice,
   productdesc,
   productstatus)
values
  ('676C5BD1D35E429A8C2E114939C5685A',
   'itcast-002',
   '北京三日游',
   '北京',
   to_timestamp('1010-2018 10:10:00.000000', 'dd-mm-yyyy hh24:mi:ss.ff'),
   1200,
   '不错的旅行',
   1);
insert into PRODUCT
  (id,
   productnum,
   productname,
   cityname,
   departuretime,
   productprice,
   productdesc,
   productstatus)
values
  ('12B7ABF2A4C544568B0A7C69F36BF8B7',
   'itcast-003',
   '上海五日游',
   '上海',
   to_timestamp('2504-2018 14:30:00.000000', 'dd-mm-yyyy hh24:mi:ss.ff'),
   1800,
   '魔都我来了',
   0);
insert into PRODUCT
  (id,
   productnum,
   productname,
   cityname,
   departuretime,
   productprice,
   productdesc,
   productstatus)
values
  ('9F71F01CB448476DAFB309AA6DF9497F',
   'itcast-001',
   '北京三日游',
   '北京',
   to_timestamp('1010-2018 10:10:00.000000', 'dd-mm-yyyy hh24:mi:ss.ff'),
   1200,
   '不错的旅行',
   1);
   
   -- 创建会员表
CREATE TABLE member(
       id varchar2(32) default SYS_GUID() PRIMARY KEY,
       NAME VARCHAR2(20),
       nickname VARCHAR2(20),
       phoneNum VARCHAR2(20),
       email VARCHAR2(20) 
);
--插入会员数据
insert into MEMBER (id, name, nickname, phonenum, email)
values ('E61D65F673D54F68B0861025C69773DB', '张三', '小三', '18888888888', 'zs@163.com');
   
   --创建订单表
CREATE TABLE orders(
  id varchar2(32) default SYS_GUID() PRIMARY KEY,
  orderNum VARCHAR2(20) NOT NULL UNIQUE,
  orderTime timestamp,
  peopleCount INT,
  orderDesc VARCHAR2(500),
  payType INT,
  orderStatus INT,
  productId varchar2(32),
  memberId varchar2(32),
  FOREIGN KEY (productId) REFERENCES product(id),
  FOREIGN KEY (memberId) REFERENCES member(id)
);
--插入订单数据
insert into ORDERS (id, ordernum, ordertime, peoplecount, orderdesc, paytype, orderstatus, productid, memberid)
values ('0E7231DC797C486290E8713CA3C6ECCC', '12345', to_timestamp('02-03-2018 12:00:00.000000', 'dd-mm-yyyy hh24:mi:ss.ff'), 2, '没什么', 0, 1, '676C5BD1D35E429A8C2E114939C5685A', 'E61D65F673D54F68B0861025C69773DB');
insert into ORDERS (id, ordernum, ordertime, peoplecount, orderdesc, paytype, orderstatus, productid, memberid)
values ('5DC6A48DD4E94592AE904930EA866AFA', '54321', to_timestamp('02-03-2018 12:00:00.000000', 'dd-mm-yyyy hh24:mi:ss.ff'), 2, '没什么', 0, 1, '676C5BD1D35E429A8C2E114939C5685A', 'E61D65F673D54F68B0861025C69773DB');
insert into ORDERS (id, ordernum, ordertime, peoplecount, orderdesc, paytype, orderstatus, productid, memberid)
values ('2FF351C4AC744E2092DCF08CFD314420', '67890', to_timestamp('02-03-2018 12:00:00.000000', 'dd-mm-yyyy hh24:mi:ss.ff'), 2, '没什么', 0, 1, '12B7ABF2A4C544568B0A7C69F36BF8B7', 'E61D65F673D54F68B0861025C69773DB');
insert into ORDERS (id, ordernum, ordertime, peoplecount, orderdesc, paytype, orderstatus, productid, memberid)
values ('A0657832D93E4B10AE88A2D4B70B1A28', '98765', to_timestamp('02-03-2018 12:00:00.000000', 'dd-mm-yyyy hh24:mi:ss.ff'), 2, '没什么', 0, 1, '12B7ABF2A4C544568B0A7C69F36BF8B7', 'E61D65F673D54F68B0861025C69773DB');
insert into ORDERS (id, ordernum, ordertime, peoplecount, orderdesc, paytype, orderstatus, productid, memberid)
values ('E4DD4C45EED84870ABA83574A801083E', '11111', to_timestamp('02-03-2018 12:00:00.000000', 'dd-mm-yyyy hh24:mi:ss.ff'), 2, '没什么', 0, 1, '12B7ABF2A4C544568B0A7C69F36BF8B7', 'E61D65F673D54F68B0861025C69773DB');
insert into ORDERS (id, ordernum, ordertime, peoplecount, orderdesc, paytype, orderstatus, productid, memberid)
values ('96CC8BD43C734CC2ACBFF09501B4DD5D', '22222', to_timestamp('02-03-2018 12:00:00.000000', 'dd-mm-yyyy hh24:mi:ss.ff'), 2, '没什么', 0, 1, '12B7ABF2A4C544568B0A7C69F36BF8B7', 'E61D65F673D54F68B0861025C69773DB');
insert into ORDERS (id, ordernum, ordertime, peoplecount, orderdesc, paytype, orderstatus, productid, memberid)
values ('55F9AF582D5A4DB28FB4EC3199385762', '33333', to_timestamp('02-03-2018 12:00:00.000000', 'dd-mm-yyyy hh24:mi:ss.ff'), 2, '没什么', 0, 1, '9F71F01CB448476DAFB309AA6DF9497F', 'E61D65F673D54F68B0861025C69773DB');
insert into ORDERS (id, ordernum, ordertime, peoplecount, orderdesc, paytype, orderstatus, productid, memberid)
values ('CA005CF1BE3C4EF68F88ABC7DF30E976', '44444', to_timestamp('02-03-2018 12:00:00.000000', 'dd-mm-yyyy hh24:mi:ss.ff'), 2, '没什么', 0, 1, '9F71F01CB448476DAFB309AA6DF9497F', 'E61D65F673D54F68B0861025C69773DB');
insert into ORDERS (id, ordernum, ordertime, peoplecount, orderdesc, paytype, orderstatus, productid, memberid)
values ('3081770BC3984EF092D9E99760FDABDE', '55555', to_timestamp('02-03-2018 12:00:00.000000', 'dd-mm-yyyy hh24:mi:ss.ff'), 2, '没什么', 0, 1, '9F71F01CB448476DAFB309AA6DF9497F', 'E61D65F673D54F68B0861025C69773DB');



-- 创建旅客表
CREATE TABLE traveller(
  id varchar2(32) default SYS_GUID() PRIMARY KEY,
  NAME VARCHAR2(20),
  sex VARCHAR2(20),
  phoneNum VARCHAR2(20),
  credentialsType INT,
  credentialsNum VARCHAR2(50),
  travellerType INT
);
--插入旅客数据
insert into TRAVELLER (id, name, sex, phonenum, credentialstype, credentialsnum, travellertype)
values ('3FE27DF2A4E44A6DBC5D0FE4651D3D3E', '张龙', '男', '13333333333', 0, '123456789009876543', 0);
insert into TRAVELLER (id, name, sex, phonenum, credentialstype, credentialsnum, travellertype)
values ('EE7A71FB6945483FBF91543DBE851960', '张小龙', '男', '15555555555', 0, '987654321123456789', 1);


-- 创建订单与旅客中间表
CREATE TABLE order_traveller(
  orderId varchar2(32),
  travellerId varchar2(32),
  PRIMARY KEY (orderId,travellerId),
  FOREIGN KEY (orderId) REFERENCES orders(id),
  FOREIGN KEY (travellerId) REFERENCES traveller(id)
);
-- 插入相关数据
insert into ORDER_TRAVELLER (orderId, travellerId)
values ('0E7231DC797C486290E8713CA3C6ECCC', '3FE27DF2A4E44A6DBC5D0FE4651D3D3E');
insert into ORDER_TRAVELLER (orderId, travellerId)
values ('2FF351C4AC744E2092DCF08CFD314420', '3FE27DF2A4E44A6DBC5D0FE4651D3D3E');
insert into ORDER_TRAVELLER (orderId, travellerId)
values ('3081770BC3984EF092D9E99760FDABDE', 'EE7A71FB6945483FBF91543DBE851960');
insert into ORDER_TRAVELLER (orderId, travellerId)
values ('55F9AF582D5A4DB28FB4EC3199385762', 'EE7A71FB6945483FBF91543DBE851960');
insert into ORDER_TRAVELLER (orderId, travellerId)
values ('5DC6A48DD4E94592AE904930EA866AFA', '3FE27DF2A4E44A6DBC5D0FE4651D3D3E');
insert into ORDER_TRAVELLER (orderId, travellerId)
values ('96CC8BD43C734CC2ACBFF09501B4DD5D', 'EE7A71FB6945483FBF91543DBE851960');
insert into ORDER_TRAVELLER (orderId, travellerId)
values ('A0657832D93E4B10AE88A2D4B70B1A28', '3FE27DF2A4E44A6DBC5D0FE4651D3D3E');
insert into ORDER_TRAVELLER (orderId, travellerId)
values ('CA005CF1BE3C4EF68F88ABC7DF30E976', 'EE7A71FB6945483FBF91543DBE851960');
insert into ORDER_TRAVELLER (orderId, travellerId)
values ('E4DD4C45EED84870ABA83574A801083E', 'EE7A71FB6945483FBF91543DBE851960');

-- 用户表
drop table users;
CREATE TABLE users(
id varchar2(32) default SYS_GUID() PRIMARY KEY,
email VARCHAR2(50) UNIQUE NOT NULL,
username VARCHAR2(50),
PASSWORD VARCHAR2(100),
phoneNum VARCHAR2(20),
STATUS INT
);

--插入用户信息    注：密码是加密后的root
insert into users(id,email,username,PASSWORD,phoneNum,STATUS) values('001','root@163.com','root','$2a$10$ahg5mqaG8ZiYytsdmLJyPe15MlOXev32wpNXbLhjp9MrVetixqD8K','0000000001',1);

-- 角色表
drop table role;
CREATE TABLE role(
id varchar2(32) default SYS_GUID() PRIMARY KEY,
roleName VARCHAR2(50) ,
roleDesc VARCHAR2(50)
);

--插入角色信息
insert into role(id,roleName,roleDesc) values('111','ADMIN','管理员');
insert into role(id,roleName,roleDesc) values('222','USER','用户');
-- 注意：手动创建角色后不可以使用，需要到utils模块找到BCryptPasswordEncoderUtils类，将密码加密后重新设置后才可访问


-- 用户角色关联表
drop table users_role;
CREATE TABLE users_role(
userId varchar2(32),
roleId varchar2(32),
PRIMARY KEY(userId,roleId),
FOREIGN KEY (userId) REFERENCES users(id),
FOREIGN KEY (roleId) REFERENCES role(id)
);

--插入角色用户关联信息
insert into users_role(userId,roleId) values('001','111');
insert into users_role(userId,roleId) values('001','222');

-- 资源权限表
drop table permission;
CREATE TABLE permission(
id varchar2(32) default SYS_GUID() PRIMARY KEY,
permissionName VARCHAR2(50) ,
url VARCHAR2(50)
);

--插入资源权限信息
insert into permission(id,permissionName,url) values('p-001','user findAll','/user/findAll.do');
insert into permission(id,permissionName,url) values('p-002','product findAll','/product/findAll.do');

-- 角色权限关联表
drop table role_permission;
CREATE TABLE role_permission(
permissionId varchar2(32),
roleId varchar2(32),
PRIMARY KEY(permissionId,roleId),
FOREIGN KEY (permissionId) REFERENCES permission(id),
FOREIGN KEY (roleId) REFERENCES role(id)
);

--插入角色关联权限信息
insert into role_permission(permissionId,roleId) values('p-001','111');
insert into role_permission(permissionId,roleId) values('p-002','111');
insert into role_permission(permissionId,roleId) values('p-002','222');

--日志表
CREATE TABLE sysLog(
    id VARCHAR2(32) default SYS_GUID() PRIMARY KEY, 
    visitTime timestamp,
    username VARCHAR2(50),
    ip VARCHAR2(30),
    url VARCHAR2(50),
    executionTime int, 
    method VARCHAR2(200)
)

-- 查询各表
select * from product;
select * from member;
select * from traveller;
select * from orders;
select * from order_traveller;
select * from users;
select * from role;
select * from users_role;
select * from permission;
select * from role_permission;
select * from sysLog;
   
   
   
