-- =============================================================
-- 优果仓 演示数据（由 tools/gen_seed.py 生成，请勿手工编辑）
-- 所有演示账号密码均为 123456
-- =============================================================
SET NAMES utf8mb4;
USE `youguocang`;

-- 管理员
INSERT INTO `t_admin` (`username`,`password`,`real_name`,`phone`,`status`) VALUES ('admin','pbkdf2_sha256$310000$aMOuAuPPrUp04Us0d51BTQ==$NRDYryFZvvlU+K6TRYFz4reB/u5hiv2K6K9EJUjRj9g=','平台管理员','13800000001',1);

-- 商家 + 店铺
INSERT INTO `t_merchant` (`id`,`username`,`password`,`phone`,`status`) VALUES (1,'guo_shop','pbkdf2_sha256$310000$qTr17DZUQuK+b0Ly7HzztQ==$KHPWvwR++ZFsbaJp9OFj8HV2W8UyRkYxQVazOLnTAWY=','13900000001',1);
INSERT INTO `t_shop` (`merchant_id`,`shop_name`,`intro`,`contact`,`status`) VALUES (1,'果然多干果旗舰店','新疆产地直采，主营核桃、巴旦木、葡萄干','微信 guoranduo01',1);
INSERT INTO `t_merchant` (`id`,`username`,`password`,`phone`,`status`) VALUES (2,'nuts_shop','pbkdf2_sha256$310000$yP1I/TMCE6eOZIEwt4BDiQ==$fX8UJF3RgPaWYrBvQXMzhSkYEH+w3Tr0te+iHVhtwWQ=','13900000002',1);
INSERT INTO `t_shop` (`merchant_id`,`shop_name`,`intro`,`contact`,`status`) VALUES (2,'坚果部落','专注混合坚果与每日坚果礼盒','微信 nuts_tribe',1);
INSERT INTO `t_merchant` (`id`,`username`,`password`,`phone`,`status`) VALUES (3,'dried_shop','pbkdf2_sha256$310000$SuBz7NFtskWudGdwMKshQA==$dpHmnd6rpaSZhcApj+M1Q3aJVKkKKG+kqhlZw9CPIls=','13900000003',1);
INSERT INTO `t_shop` (`merchant_id`,`shop_name`,`intro`,`contact`,`status`) VALUES (3,'蜜饯小铺','果干蜜饯、红枣、芒果干','微信 mijian_shop',1);

-- 用户
INSERT INTO `t_user` (`id`,`username`,`phone`,`password`,`nickname`,`status`) VALUES (1,'user01','13700000001','pbkdf2_sha256$310000$XFIFRJNOahVpcmSeLpbccQ==$O894qCVZGX+Yo2VNW550hJB2SYxFr8nT36/HzUghKSQ=','核桃爱好者',1);
INSERT INTO `t_user` (`id`,`username`,`phone`,`password`,`nickname`,`status`) VALUES (2,'user02','13700000002','pbkdf2_sha256$310000$GaaNjksM1Lchbr2H5tE3cg==$5MVmynApFwRliVBa0FGTg9a6gq5byBE1mpIV8GBrB6U=','干果控',1);
INSERT INTO `t_user` (`id`,`username`,`phone`,`password`,`nickname`,`status`) VALUES (3,'user03','13700000003','pbkdf2_sha256$310000$54ZweKDuFDxMy1iELIYzyA==$27QaolVY+NgeJfnFRgRVD1NQl4E5L5y+8AMnXS90Xvc=','养生青年',1);
INSERT INTO `t_user` (`id`,`username`,`phone`,`password`,`nickname`,`status`) VALUES (4,'user04','13700000004','pbkdf2_sha256$310000$mOnJDNeEI39ouEMSSN8TMw==$7YL9z0bAg2i1HKaNxqTyGXl8odSQcTu7FgpdQS1WqJ0=','送礼专业户',1);
INSERT INTO `t_user` (`id`,`username`,`phone`,`password`,`nickname`,`status`) VALUES (5,'user05','13700000005','pbkdf2_sha256$310000$XizsJUthkbiZXp9x5JA72Q==$q6N/S4ecGXxcIu5hl9KJJk4k7giFZOrlJRFHLU5AGBw=','办公室零食',1);
INSERT INTO `t_user` (`id`,`username`,`phone`,`password`,`nickname`,`status`) VALUES (6,'user06','13700000006','pbkdf2_sha256$310000$NyXginmfGe6361sIw/DsTQ==$ifqQpqVuhePvdtybyIPRw+qhz+FH7pTZYjxXRU3maVg=','健身补给',1);

-- 分类（先父后子，便于取 id）
INSERT INTO `t_category` (`id`,`parent_id`,`name`,`sort`,`status`) VALUES (1,0,'坚果炒货',1,1);
INSERT INTO `t_category` (`id`,`parent_id`,`name`,`sort`,`status`) VALUES (2,0,'果干蜜饯',2,1);
INSERT INTO `t_category` (`id`,`parent_id`,`name`,`sort`,`status`) VALUES (3,0,'籽类',3,1);
INSERT INTO `t_category` (`id`,`parent_id`,`name`,`sort`,`status`) VALUES (4,0,'礼盒装',4,1);
INSERT INTO `t_category` (`id`,`parent_id`,`name`,`sort`,`status`) VALUES (5,1,'核桃',1,1);
INSERT INTO `t_category` (`id`,`parent_id`,`name`,`sort`,`status`) VALUES (6,1,'巴旦木',2,1);
INSERT INTO `t_category` (`id`,`parent_id`,`name`,`sort`,`status`) VALUES (7,1,'开心果',3,1);
INSERT INTO `t_category` (`id`,`parent_id`,`name`,`sort`,`status`) VALUES (8,2,'葡萄干',1,1);
INSERT INTO `t_category` (`id`,`parent_id`,`name`,`sort`,`status`) VALUES (9,2,'红枣',2,1);
INSERT INTO `t_category` (`id`,`parent_id`,`name`,`sort`,`status`) VALUES (10,3,'瓜子',1,1);

-- 商品
INSERT INTO `t_product` (`id`,`merchant_id`,`shop_id`,`category_id`,`name`,`price`,`origin_price`,`origin_place`,`spec`,`stock`,`sales_count`,`main_image`,`detail`,`status`) VALUES (1,1,1,5,'新疆阿克苏薄皮核桃 500g',39.9,59.9,'新疆阿克苏','500g/袋',200,860,'/img/product/1.jpg','皮薄如纸，手剥即开，果仁饱满，冷榨锁鲜。',1);
INSERT INTO `t_product` (`id`,`merchant_id`,`shop_id`,`category_id`,`name`,`price`,`origin_price`,`origin_place`,`spec`,`stock`,`sales_count`,`main_image`,`detail`,`status`) VALUES (2,1,1,5,'云南漾濞核桃仁 250g',29.9,45.0,'云南大理','250g/罐',150,430,'/img/product/2.jpg','去壳核桃仁，淡盐水烘烤，无添加蔗糖。',1);
INSERT INTO `t_product` (`id`,`merchant_id`,`shop_id`,`category_id`,`name`,`price`,`origin_price`,`origin_place`,`spec`,`stock`,`sales_count`,`main_image`,`detail`,`status`) VALUES (3,1,1,6,'美国进口巴旦木 500g',45.0,68.0,'美国加州','500g/袋',180,720,'/img/product/3.jpg','原味烘焙，颗粒均匀，酥脆不油腻。',1);
INSERT INTO `t_product` (`id`,`merchant_id`,`shop_id`,`category_id`,`name`,`price`,`origin_price`,`origin_place`,`spec`,`stock`,`sales_count`,`main_image`,`detail`,`status`) VALUES (4,2,2,6,'盐焗巴旦木 250g',26.8,36.0,'美国加州','250g/袋',220,305,'/img/product/4.jpg','盐焗工艺，咸香适口，独立小包装。',1);
INSERT INTO `t_product` (`id`,`merchant_id`,`shop_id`,`category_id`,`name`,`price`,`origin_price`,`origin_place`,`spec`,`stock`,`sales_count`,`main_image`,`detail`,`status`) VALUES (5,2,2,7,'伊朗开心果 400g',58.0,78.0,'伊朗','400g/袋',90,260,'/img/product/5.jpg','自然开口率高，紫衣绿仁，原味烘焙。',1);
INSERT INTO `t_product` (`id`,`merchant_id`,`shop_id`,`category_id`,`name`,`price`,`origin_price`,`origin_place`,`spec`,`stock`,`sales_count`,`main_image`,`detail`,`status`) VALUES (6,2,2,7,'盐焗开心果 500g',62.0,88.0,'伊朗','500g/袋',85,210,'/img/product/6.jpg','盐焗调味，适合追剧下酒。',1);
INSERT INTO `t_product` (`id`,`merchant_id`,`shop_id`,`category_id`,`name`,`price`,`origin_price`,`origin_place`,`spec`,`stock`,`sales_count`,`main_image`,`detail`,`status`) VALUES (7,1,1,8,'新疆吐鲁番葡萄干 500g',22.9,32.0,'新疆吐鲁番','500g/袋',300,1180,'/img/product/7.jpg','无核白葡萄干，自然晾晒，酸甜软糯。',1);
INSERT INTO `t_product` (`id`,`merchant_id`,`shop_id`,`category_id`,`name`,`price`,`origin_price`,`origin_place`,`spec`,`stock`,`sales_count`,`main_image`,`detail`,`status`) VALUES (8,3,3,8,'黑加仑葡萄干 250g',25.5,35.0,'新疆吐鲁番','250g/袋',160,390,'/img/product/8.jpg','黑加仑品种，果香浓郁，颗粒饱满。',1);
INSERT INTO `t_product` (`id`,`merchant_id`,`shop_id`,`category_id`,`name`,`price`,`origin_price`,`origin_place`,`spec`,`stock`,`sales_count`,`main_image`,`detail`,`status`) VALUES (9,3,3,9,'新疆和田大枣 1kg',49.9,69.9,'新疆和田','1kg/袋',240,950,'/img/product/9.jpg','骏枣品种，肉厚核小，甜而不腻。',1);
INSERT INTO `t_product` (`id`,`merchant_id`,`shop_id`,`category_id`,`name`,`price`,`origin_price`,`origin_place`,`spec`,`stock`,`sales_count`,`main_image`,`detail`,`status`) VALUES (10,3,3,9,'若羌灰枣 500g',32.0,45.0,'新疆若羌','500g/袋',200,640,'/img/product/10.jpg','个头小巧，皮薄肉甜，适合煲汤泡茶。',1);
INSERT INTO `t_product` (`id`,`merchant_id`,`shop_id`,`category_id`,`name`,`price`,`origin_price`,`origin_place`,`spec`,`stock`,`sales_count`,`main_image`,`detail`,`status`) VALUES (11,3,3,10,'原味葵花籽 500g',15.9,22.0,'内蒙古巴彦淖尔','500g/袋',350,1420,'/img/product/11.jpg','大颗粒葵花籽，低温烘焙，原味不腻。',1);
INSERT INTO `t_product` (`id`,`merchant_id`,`shop_id`,`category_id`,`name`,`price`,`origin_price`,`origin_place`,`spec`,`stock`,`sales_count`,`main_image`,`detail`,`status`) VALUES (12,2,2,10,'五香南瓜子 250g',18.8,26.0,'甘肃武威','250g/袋',280,780,'/img/product/12.jpg','五香卤制入味，籽仁饱满易剥。',1);
INSERT INTO `t_product` (`id`,`merchant_id`,`shop_id`,`category_id`,`name`,`price`,`origin_price`,`origin_place`,`spec`,`stock`,`sales_count`,`main_image`,`detail`,`status`) VALUES (13,1,1,4,'每日坚果混合装 750g（30 袋）',99.0,149.0,'多产地拼配','25g×30 袋',120,560,'/img/product/13.jpg','六种坚果三种果干，独立分装，随身携带。',1);
INSERT INTO `t_product` (`id`,`merchant_id`,`shop_id`,`category_id`,`name`,`price`,`origin_price`,`origin_place`,`spec`,`stock`,`sales_count`,`main_image`,`detail`,`status`) VALUES (14,2,2,4,'干果礼盒 高档送礼装 1.2kg',168.0,228.0,'多产地拼配','1.2kg/盒',60,190,'/img/product/14.jpg','含巴旦木、开心果、核桃、葡萄干与红枣，商务送礼首选。',1);
INSERT INTO `t_product` (`id`,`merchant_id`,`shop_id`,`category_id`,`name`,`price`,`origin_price`,`origin_place`,`spec`,`stock`,`sales_count`,`main_image`,`detail`,`status`) VALUES (15,1,1,4,'春节年货坚果大礼包 2kg',238.0,328.0,'多产地拼配','2kg/箱',40,95,'/img/product/15.jpg','八款坚果果干组合，红色喜庆包装。',1);

-- 公告
INSERT INTO `t_notice` (`title`,`content`,`publisher_id`,`status`) VALUES ('平台上线公告','优果仓干果电商平台正式上线，欢迎各位商家入驻，首批入驻商家可享受三个月免佣金政策。',1,1);
INSERT INTO `t_notice` (`title`,`content`,`publisher_id`,`status`) VALUES ('关于评价审核规则的说明','为保障内容质量，用户评价与商家回复均需平台审核通过后才会对外展示，通常 24 小时内完成审核。',1,1);
INSERT INTO `t_notice` (`title`,`content`,`publisher_id`,`status`) VALUES ('双十一活动通知','11 月 1 日至 11 月 11 日，全站干果满 99 减 20，礼盒类商品满 199 减 50。',1,1);

-- 收货地址
INSERT INTO `t_address` (`user_id`,`receiver`,`phone`,`province`,`city`,`district`,`detail`,`is_default`) VALUES (1,'张伟','13700000001','江苏省','南京市','鼓楼区','中山北路 100 号 3 栋 502',1),(2,'李娜','13700000002','浙江省','杭州市','西湖区','文三路 200 号 A 座 1201',1),(3,'王强','13700000003','广东省','深圳市','南山区','科技园南区 8 栋 1102',1);

-- 浏览行为（推荐算法数据来源）
INSERT INTO `t_browse_log` (`user_id`,`product_id`,`source`) VALUES (1,1,2);
INSERT INTO `t_browse_log` (`user_id`,`product_id`,`source`) VALUES (1,3,4);
INSERT INTO `t_browse_log` (`user_id`,`product_id`,`source`) VALUES (1,7,4);
INSERT INTO `t_browse_log` (`user_id`,`product_id`,`source`) VALUES (1,11,4);
INSERT INTO `t_browse_log` (`user_id`,`product_id`,`source`) VALUES (2,3,4);
INSERT INTO `t_browse_log` (`user_id`,`product_id`,`source`) VALUES (2,5,2);
INSERT INTO `t_browse_log` (`user_id`,`product_id`,`source`) VALUES (2,7,4);
INSERT INTO `t_browse_log` (`user_id`,`product_id`,`source`) VALUES (2,14,3);
INSERT INTO `t_browse_log` (`user_id`,`product_id`,`source`) VALUES (3,7,4);
INSERT INTO `t_browse_log` (`user_id`,`product_id`,`source`) VALUES (3,9,2);
INSERT INTO `t_browse_log` (`user_id`,`product_id`,`source`) VALUES (3,10,3);
INSERT INTO `t_browse_log` (`user_id`,`product_id`,`source`) VALUES (3,11,4);
INSERT INTO `t_browse_log` (`user_id`,`product_id`,`source`) VALUES (4,13,2);
INSERT INTO `t_browse_log` (`user_id`,`product_id`,`source`) VALUES (4,14,3);
INSERT INTO `t_browse_log` (`user_id`,`product_id`,`source`) VALUES (4,15,4);
INSERT INTO `t_browse_log` (`user_id`,`product_id`,`source`) VALUES (5,11,4);
INSERT INTO `t_browse_log` (`user_id`,`product_id`,`source`) VALUES (5,12,1);
INSERT INTO `t_browse_log` (`user_id`,`product_id`,`source`) VALUES (5,13,2);

-- 收藏
INSERT INTO `t_favorite` (`user_id`,`product_id`) VALUES (1,3);
INSERT INTO `t_favorite` (`user_id`,`product_id`) VALUES (1,7);
INSERT INTO `t_favorite` (`user_id`,`product_id`) VALUES (2,3);
INSERT INTO `t_favorite` (`user_id`,`product_id`) VALUES (2,14);
INSERT INTO `t_favorite` (`user_id`,`product_id`) VALUES (3,9);
INSERT INTO `t_favorite` (`user_id`,`product_id`) VALUES (4,14);

-- 购物车（user05 保留两条待结算）
INSERT INTO `t_cart_item` (`user_id`,`product_id`,`quantity`,`checked`) VALUES (5,13,2,1),(5,11,1,1),(2,3,1,0);

-- 订单与明细
INSERT INTO `t_order` (`id`,`order_no`,`user_id`,`merchant_id`,`status`,`total_amount`,`pay_amount`,`receiver`,`receiver_phone`,`receiver_addr`,`paid_at`) VALUES (1,'YG202611140001',1,1,3,90.8,90.8,'张伟','13700000001','江苏省南京市鼓楼区中山北路 100 号 3 栋 502','2026-11-10 10:00:00');
INSERT INTO `t_order_item` (`order_id`,`product_id`,`product_name`,`product_image`,`spec`,`price`,`quantity`,`amount`) VALUES (1,3,'美国进口巴旦木 500g','/img/product/3.jpg','500g/袋',45.0,1,45.0);
INSERT INTO `t_order_item` (`order_id`,`product_id`,`product_name`,`product_image`,`spec`,`price`,`quantity`,`amount`) VALUES (1,7,'新疆吐鲁番葡萄干 500g','/img/product/7.jpg','500g/袋',22.9,2,45.8);
INSERT INTO `t_order` (`id`,`order_no`,`user_id`,`merchant_id`,`status`,`total_amount`,`pay_amount`,`receiver`,`receiver_phone`,`receiver_addr`,`paid_at`) VALUES (2,'YG202611140002',2,2,3,58.0,58.0,'张伟','13700000001','江苏省南京市鼓楼区中山北路 100 号 3 栋 502','2026-11-10 10:00:00');
INSERT INTO `t_order_item` (`order_id`,`product_id`,`product_name`,`product_image`,`spec`,`price`,`quantity`,`amount`) VALUES (2,5,'伊朗开心果 400g','/img/product/5.jpg','400g/袋',58.0,1,58.0);
INSERT INTO `t_order` (`id`,`order_no`,`user_id`,`merchant_id`,`status`,`total_amount`,`pay_amount`,`receiver`,`receiver_phone`,`receiver_addr`,`paid_at`) VALUES (3,'YG202611140003',3,3,2,81.9,81.9,'张伟','13700000001','江苏省南京市鼓楼区中山北路 100 号 3 栋 502','2026-11-10 10:00:00');
INSERT INTO `t_order_item` (`order_id`,`product_id`,`product_name`,`product_image`,`spec`,`price`,`quantity`,`amount`) VALUES (3,9,'新疆和田大枣 1kg','/img/product/9.jpg','1kg/袋',49.9,1,49.9);
INSERT INTO `t_order_item` (`order_id`,`product_id`,`product_name`,`product_image`,`spec`,`price`,`quantity`,`amount`) VALUES (3,10,'若羌灰枣 500g','/img/product/10.jpg','500g/袋',32.0,1,32.0);
INSERT INTO `t_order` (`id`,`order_no`,`user_id`,`merchant_id`,`status`,`total_amount`,`pay_amount`,`receiver`,`receiver_phone`,`receiver_addr`,`paid_at`) VALUES (4,'YG202611140004',4,1,3,168.0,168.0,'张伟','13700000001','江苏省南京市鼓楼区中山北路 100 号 3 栋 502','2026-11-10 10:00:00');
INSERT INTO `t_order_item` (`order_id`,`product_id`,`product_name`,`product_image`,`spec`,`price`,`quantity`,`amount`) VALUES (4,14,'干果礼盒 高档送礼装 1.2kg','/img/product/14.jpg','1.2kg/盒',168.0,1,168.0);
INSERT INTO `t_order` (`id`,`order_no`,`user_id`,`merchant_id`,`status`,`total_amount`,`pay_amount`,`receiver`,`receiver_phone`,`receiver_addr`,`paid_at`) VALUES (5,'YG202611140005',5,2,1,99.0,99.0,'张伟','13700000001','江苏省南京市鼓楼区中山北路 100 号 3 栋 502','2026-11-10 10:00:00');
INSERT INTO `t_order_item` (`order_id`,`product_id`,`product_name`,`product_image`,`spec`,`price`,`quantity`,`amount`) VALUES (5,13,'每日坚果混合装 750g（30 袋）','/img/product/13.jpg','25g×30 袋',99.0,1,99.0);

-- 评价（1 条待审核、1 条已通过）
INSERT INTO `t_review` (`order_item_id`,`order_id`,`user_id`,`product_id`,`merchant_id`,`score`,`content`,`status`) VALUES (1,1,1,3,1,5,'核桃很新鲜，壳薄好剥，回购第三次了。',1),(3,2,2,5,2,4,'开心果开口率不错，就是盐味稍重。',0);

-- 商品共现（离线任务示例产出，真实环境由 CooccurJob 刷新）
INSERT INTO `t_product_cooccur` (`product_id`,`related_id`,`co_count`) VALUES (3,7,2),(7,3,2),(3,11,1),(11,3,1),(7,11,1),(11,7,1),(5,7,1),(7,5,1),(5,14,1),(14,5,1),(9,10,1),(10,9,1),(13,14,1),(14,13,1);

-- 操作日志样例
INSERT INTO `t_operation_log` (`operator_type`,`operator_id`,`operator_name`,`action`,`target`,`detail`,`ip`) VALUES (1,1,'admin','REVIEW_AUDIT','评价#1','审核通过','127.0.0.1'),(2,1,'guo_shop','PRODUCT_ONLINE','商品#1','上架商品','127.0.0.1'),(1,1,'admin','NOTICE_PUBLISH','公告#1','发布平台公告','127.0.0.1');
