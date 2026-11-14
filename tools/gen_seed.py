#!/usr/bin/env python3
"""生成 sql/02_seed.sql（演示数据）。

密码哈希采用 PBKDF2-HMAC-SHA256，格式与后端 PasswordHasher 保持一致：
    pbkdf2_sha256$<iterations>$<saltBase64>$<hashBase64>

用法：
    python tools/gen_seed.py sql/02_seed.sql
"""
import base64
import hashlib
import os
import sys

ITERATIONS = 310000
DEFAULT_PWD = "123456"


def hash_password(password, salt=None):
    salt = salt or os.urandom(16)
    dk = hashlib.pbkdf2_hmac("sha256", password.encode("utf-8"), salt, ITERATIONS, dklen=32)
    return "pbkdf2_sha256${}${}${}".format(
        ITERATIONS,
        base64.b64encode(salt).decode(),
        base64.b64encode(dk).decode(),
    )


def q(value):
    """SQL 字符串字面量转义。"""
    if value is None:
        return "NULL"
    return "'" + str(value).replace("\\", "\\\\").replace("'", "''") + "'"


ADMINS = [
    ("admin", "123456", "平台管理员", "13800000001"),
]

MERCHANTS = [
    ("guo_shop", "123456", "13900000001", "果然多干果旗舰店", "新疆产地直采，主营核桃、巴旦木、葡萄干", "微信 guoranduo01"),
    ("nuts_shop", "123456", "13900000002", "坚果部落", "专注混合坚果与每日坚果礼盒", "微信 nuts_tribe"),
    ("dried_shop", "123456", "13900000003", "蜜饯小铺", "果干蜜饯、红枣、芒果干", "微信 mijian_shop"),
]

USERS = [
    ("user01", "13700000001", "核桃爱好者"),
    ("user02", "13700000002", "干果控"),
    ("user03", "13700000003", "养生青年"),
    ("user04", "13700000004", "送礼专业户"),
    ("user05", "13700000005", "办公室零食"),
    ("user06", "13700000006", "健身补给"),
]

# (parent_id, name, sort)
CATEGORIES = [
    (0, "坚果炒货", 1),
    (0, "果干蜜饯", 2),
    (0, "籽类", 3),
    (0, "礼盒装", 4),
    (1, "核桃", 1),
    (1, "巴旦木", 2),
    (1, "开心果", 3),
    (2, "葡萄干", 1),
    (2, "红枣", 2),
    (3, "瓜子", 1),
]

# (merchant_idx, category_idx, name, price, origin_price, origin_place, spec, stock, sales, detail)
PRODUCTS = [
    (1, 5, "新疆阿克苏薄皮核桃 500g", 39.90, 59.90, "新疆阿克苏", "500g/袋", 200, 860,
     "皮薄如纸，手剥即开，果仁饱满，冷榨锁鲜。"),
    (1, 5, "云南漾濞核桃仁 250g", 29.90, 45.00, "云南大理", "250g/罐", 150, 430,
     "去壳核桃仁，淡盐水烘烤，无添加蔗糖。"),
    (1, 6, "美国进口巴旦木 500g", 45.00, 68.00, "美国加州", "500g/袋", 180, 720,
     "原味烘焙，颗粒均匀，酥脆不油腻。"),
    (2, 6, "盐焗巴旦木 250g", 26.80, 36.00, "美国加州", "250g/袋", 220, 305,
     "盐焗工艺，咸香适口，独立小包装。"),
    (2, 7, "伊朗开心果 400g", 58.00, 78.00, "伊朗", "400g/袋", 90, 260,
     "自然开口率高，紫衣绿仁，原味烘焙。"),
    (2, 7, "盐焗开心果 500g", 62.00, 88.00, "伊朗", "500g/袋", 85, 210,
     "盐焗调味，适合追剧下酒。"),
    (1, 8, "新疆吐鲁番葡萄干 500g", 22.90, 32.00, "新疆吐鲁番", "500g/袋", 300, 1180,
     "无核白葡萄干，自然晾晒，酸甜软糯。"),
    (3, 8, "黑加仑葡萄干 250g", 25.50, 35.00, "新疆吐鲁番", "250g/袋", 160, 390,
     "黑加仑品种，果香浓郁，颗粒饱满。"),
    (3, 9, "新疆和田大枣 1kg", 49.90, 69.90, "新疆和田", "1kg/袋", 240, 950,
     "骏枣品种，肉厚核小，甜而不腻。"),
    (3, 9, "若羌灰枣 500g", 32.00, 45.00, "新疆若羌", "500g/袋", 200, 640,
     "个头小巧，皮薄肉甜，适合煲汤泡茶。"),
    (3, 10, "原味葵花籽 500g", 15.90, 22.00, "内蒙古巴彦淖尔", "500g/袋", 350, 1420,
     "大颗粒葵花籽，低温烘焙，原味不腻。"),
    (2, 10, "五香南瓜子 250g", 18.80, 26.00, "甘肃武威", "250g/袋", 280, 780,
     "五香卤制入味，籽仁饱满易剥。"),
    (1, 4, "每日坚果混合装 750g（30 袋）", 99.00, 149.00, "多产地拼配", "25g×30 袋", 120, 560,
     "六种坚果三种果干，独立分装，随身携带。"),
    (2, 4, "干果礼盒 高档送礼装 1.2kg", 168.00, 228.00, "多产地拼配", "1.2kg/盒", 60, 190,
     "含巴旦木、开心果、核桃、葡萄干与红枣，商务送礼首选。"),
    (1, 4, "春节年货坚果大礼包 2kg", 238.00, 328.00, "多产地拼配", "2kg/箱", 40, 95,
     "八款坚果果干组合，红色喜庆包装。"),
]

NOTICES = [
    ("平台上线公告", "优果仓干果电商平台正式上线，欢迎各位商家入驻，首批入驻商家可享受三个月免佣金政策。"),
    ("关于评价审核规则的说明", "为保障内容质量，用户评价与商家回复均需平台审核通过后才会对外展示，通常 24 小时内完成审核。"),
    ("双十一活动通知", "11 月 1 日至 11 月 11 日，全站干果满 99 减 20，礼盒类商品满 199 减 50。"),
]

# 行为数据：让 user01~user04 形成可用于协同过滤的共现关系
# (user_idx, product_idx) 浏览
BROWSE = [
    (1, 1), (1, 3), (1, 7), (1, 11),
    (2, 3), (2, 5), (2, 7), (2, 14),
    (3, 7), (3, 9), (3, 10), (3, 11),
    (4, 13), (4, 14), (4, 15),
    (5, 11), (5, 12), (5, 13),
]

FAVORITE = [
    (1, 3), (1, 7), (2, 3), (2, 14), (3, 9), (4, 14),
]

# (user_idx, merchant_idx, status, [(product_idx, qty), ...])
ORDERS = [
    (1, 1, 3, [(3, 1), (7, 2)]),
    (2, 2, 3, [(5, 1)]),
    (3, 3, 2, [(9, 1), (10, 1)]),
    (4, 1, 3, [(14, 1)]),
    (5, 2, 1, [(13, 1)]),
]


def main(dst):
    lines = []
    add = lines.append
    add("-- =============================================================")
    add("-- 优果仓 演示数据（由 tools/gen_seed.py 生成，请勿手工编辑）")
    add("-- 所有演示账号密码均为 123456")
    add("-- =============================================================")
    add("SET NAMES utf8mb4;")
    add("USE `youguocang`;")
    add("")

    add("-- 管理员")
    for name, pwd, real, phone in ADMINS:
        add("INSERT INTO `t_admin` (`username`,`password`,`real_name`,`phone`,`status`) VALUES "
            f"({q(name)},{q(hash_password(pwd))},{q(real)},{q(phone)},1);")
    add("")

    add("-- 商家 + 店铺")
    for idx, (name, pwd, phone, shop_name, intro, contact) in enumerate(MERCHANTS, start=1):
        add(f"INSERT INTO `t_merchant` (`id`,`username`,`password`,`phone`,`status`) VALUES "
            f"({idx},{q(name)},{q(hash_password(pwd))},{q(phone)},1);")
        add(f"INSERT INTO `t_shop` (`merchant_id`,`shop_name`,`intro`,`contact`,`status`) VALUES "
            f"({idx},{q(shop_name)},{q(intro)},{q(contact)},1);")
    add("")

    add("-- 用户")
    for idx, (name, phone, nickname) in enumerate(USERS, start=1):
        add(f"INSERT INTO `t_user` (`id`,`username`,`phone`,`password`,`nickname`,`status`) VALUES "
            f"({idx},{q(name)},{q(phone)},{q(hash_password(DEFAULT_PWD))},{q(nickname)},1);")
    add("")

    add("-- 分类（先父后子，便于取 id）")
    cat_ids = {}
    next_id = 0
    for parent, name, sort in CATEGORIES:
        next_id += 1
        cat_ids[name] = next_id
        parent_id = cat_ids[CATEGORIES[parent - 1][1]] if parent else 0
        add(f"INSERT INTO `t_category` (`id`,`parent_id`,`name`,`sort`,`status`) VALUES "
            f"({next_id},{parent_id},{q(name)},{sort},1);")
    add("")

    add("-- 商品")
    for idx, (m_idx, c_idx, name, price, origin_price, place, spec, stock, sales, detail) in enumerate(PRODUCTS, start=1):
        cat_name = CATEGORIES[c_idx - 1][1]
        add(f"INSERT INTO `t_product` (`id`,`merchant_id`,`shop_id`,`category_id`,`name`,`price`,"
            f"`origin_price`,`origin_place`,`spec`,`stock`,`sales_count`,`main_image`,`detail`,`status`) VALUES "
            f"({idx},{m_idx},{m_idx},{cat_ids[cat_name]},{q(name)},{price},{origin_price},{q(place)},"
            f"{q(spec)},{stock},{sales},{q('/img/product/%d.jpg' % idx)},{q(detail)},1);")
    add("")

    add("-- 公告")
    for title, content in NOTICES:
        add(f"INSERT INTO `t_notice` (`title`,`content`,`publisher_id`,`status`) VALUES "
            f"({q(title)},{q(content)},1,1);")
    add("")

    add("-- 收货地址")
    add("INSERT INTO `t_address` (`user_id`,`receiver`,`phone`,`province`,`city`,`district`,`detail`,`is_default`) VALUES "
        "(1,'张伟','13700000001','江苏省','南京市','鼓楼区','中山北路 100 号 3 栋 502',1),"
        "(2,'李娜','13700000002','浙江省','杭州市','西湖区','文三路 200 号 A 座 1201',1),"
        "(3,'王强','13700000003','广东省','深圳市','南山区','科技园南区 8 栋 1102',1);")
    add("")

    add("-- 浏览行为（推荐算法数据来源）")
    for u, p in BROWSE:
        add(f"INSERT INTO `t_browse_log` (`user_id`,`product_id`,`source`) VALUES ({u},{p},{1 + (p % 4)});")
    add("")

    add("-- 收藏")
    for u, p in FAVORITE:
        add(f"INSERT INTO `t_favorite` (`user_id`,`product_id`) VALUES ({u},{p});")
    add("")

    add("-- 购物车（user05 保留两条待结算）")
    add("INSERT INTO `t_cart_item` (`user_id`,`product_id`,`quantity`,`checked`) VALUES "
        "(5,13,2,1),(5,11,1,1),(2,3,1,0);")
    add("")

    add("-- 订单与明细")
    order_id = 0
    for u, m, status, items in ORDERS:
        order_id += 1
        total = 0
        detail_rows = []
        for p, qty in items:
            price = PRODUCTS[p - 1][3]
            amount = round(price * qty, 2)
            total += amount
            detail_rows.append((p, qty, price, amount))
        order_no = "YG2026111400%02d" % order_id
        add(f"INSERT INTO `t_order` (`id`,`order_no`,`user_id`,`merchant_id`,`status`,`total_amount`,`pay_amount`,"
            f"`receiver`,`receiver_phone`,`receiver_addr`,`paid_at`) VALUES "
            f"({order_id},{q(order_no)},{u},{m},{status},{round(total, 2)},{round(total, 2)},"
            f"'张伟','13700000001','江苏省南京市鼓楼区中山北路 100 号 3 栋 502','2026-11-10 10:00:00');")
        for p, qty, price, amount in detail_rows:
            add(f"INSERT INTO `t_order_item` (`order_id`,`product_id`,`product_name`,`product_image`,`spec`,"
                f"`price`,`quantity`,`amount`) VALUES "
                f"({order_id},{p},{q(PRODUCTS[p - 1][2])},{q('/img/product/%d.jpg' % p)},"
                f"{q(PRODUCTS[p - 1][6])},{price},{qty},{amount});")
    add("")

    add("-- 评价（1 条待审核、1 条已通过）")
    add("INSERT INTO `t_review` (`order_item_id`,`order_id`,`user_id`,`product_id`,`merchant_id`,`score`,`content`,`status`) VALUES "
        "(1,1,1,3,1,5,'核桃很新鲜，壳薄好剥，回购第三次了。',1),"
        "(3,2,2,5,2,4,'开心果开口率不错，就是盐味稍重。',0);")
    add("")

    add("-- 商品共现（离线任务示例产出，真实环境由 CooccurJob 刷新）")
    add("INSERT INTO `t_product_cooccur` (`product_id`,`related_id`,`co_count`) VALUES "
        "(3,7,2),(7,3,2),(3,11,1),(11,3,1),(7,11,1),(11,7,1),"
        "(5,7,1),(7,5,1),(5,14,1),(14,5,1),(9,10,1),(10,9,1),(13,14,1),(14,13,1);")
    add("")

    add("-- 操作日志样例")
    add("INSERT INTO `t_operation_log` (`operator_type`,`operator_id`,`operator_name`,`action`,`target`,`detail`,`ip`) VALUES "
        "(1,1,'admin','REVIEW_AUDIT','评价#1','审核通过','127.0.0.1'),"
        "(2,1,'guo_shop','PRODUCT_ONLINE','商品#1','上架商品','127.0.0.1'),"
        "(1,1,'admin','NOTICE_PUBLISH','公告#1','发布平台公告','127.0.0.1');")
    add("")

    with open(dst, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))
    print(f"wrote {dst}, {len(lines)} lines")


if __name__ == "__main__":
    main(sys.argv[1] if len(sys.argv) > 1 else "sql/02_seed.sql")
