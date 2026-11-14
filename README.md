# 优果仓 · 干果电商平台

面向干果/坚果垂类的三端电商平台骨架：**用户端、商家端、管理端**三套角色，隔离清晰；
核心差异化是**可解释的个性化推荐**（冷启动降级 + 基于物品的协同过滤 + 每条推荐附带理由）。

> 本仓库是可直接继续开发的工程骨架：数据库设计完整、后端分层与鉴权打通、推荐算法可单测，
> 业务接口按模块逐步补齐。各模块完成度见 [docs/01-架构与模块对照.md](docs/01-架构与模块对照.md)。

## 技术栈

| 层 | 选型 |
|---|---|
| 后端 | Spring Boot 3.2.5、JDK 17、MyBatis-Plus 3.5.7、MySQL 8.0 |
| 鉴权 | JWT（jjwt 0.12.5）+ 路径前缀角色校验 |
| 密码 | PBKDF2-HMAC-SHA256（JDK 原生，无额外依赖） |
| 前端 | Vue 3、Vite 5、Vue Router 4、Element Plus、Axios |

## 目录结构

```
youcangku/
├── server/                      # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/java/com/youguocang/
│       ├── common/              # ApiResult、异常、枚举
│       ├── config/              # MyBatis-Plus、Web 配置
│       ├── security/            # JWT、密码哈希、登录态、拦截器
│       ├── entity/ mapper/      # 数据层
│       ├── recommend/           # ★ 推荐模块（算法 + 离线任务）
│       ├── service/             # 登录、操作日志
│       └── controller/          # publicapi / user / merchant / admin
├── web/                         # Vue 3 前端
├── sql/                         # 01_schema.sql、02_seed.sql
├── tools/                       # 演示数据生成、结构自检
└── docs/                        # 架构、数据库、推荐设计
```

## 快速开始

### 1. 准备数据库

```bash
mysql -uroot -p < sql/01_schema.sql   # 建库 + 21 张表
mysql -uroot -p < sql/02_seed.sql     # 演示数据
```

### 2. 启动后端

```bash
cd server
# 修改 src/main/resources/application.yml 中的数据库账号密码
mvn spring-boot:run
```

后端默认 `http://localhost:8080`。

### 3. 启动前端

```bash
cd web
npm install
npm run dev
```

前端默认 `http://localhost:5173`，`/api` 已代理到 8080。

### 4. 演示账号

| 角色 | 账号 | 密码 | 登录后可访问 |
|---|---|---|---|
| 用户 | `user01` | `123456` | `/user/home` 推荐首页 |
| 商家 | `guo_shop` | `123456` | 商家接口（页面待补） |
| 管理员 | `admin` | `123456` | `/admin/reviews` 内容审核 |

## 核心接口示例

```bash
# 登录
curl -X POST http://localhost:8080/api/public/auth/login \
  -H "Content-Type: application/json" \
  -d '{"role":"USER","account":"user01","password":"123456"}'

# 首页推荐（带推荐理由）
curl http://localhost:8080/api/user/recommend/home \
  -H "Authorization: Bearer <token>"

# 商品检索
curl "http://localhost:8080/api/user/products?keyword=核桃&minPrice=20&maxPrice=50&sort=price_asc" \
  -H "Authorization: Bearer <token>"
```

推荐接口返回示例：

```json
{
  "productId": 7,
  "name": "新疆吐鲁番葡萄干 500g",
  "price": 22.90,
  "strategy": "ITEM_CF",
  "strategyLabel": "基于物品的协同过滤",
  "reason": "因为你浏览过「新疆阿克苏薄皮核桃 500g」，同类买家也常一起看"
}
```

## 推荐模块

设计说明见 [docs/03-推荐模块设计.md](docs/03-推荐模块设计.md)。

- **离线**：`CooccurrenceJob` 每日 03:00 由行为表重建商品共现矩阵（`t_product_cooccur`）
- **在线**：`ItemCfRecommender` 按余弦相似度打分，`RecommendService` 决定策略并生成理由
- **降级**：无行为 → 冷启动热门榜；行为不足 → 协同过滤 + 热门补位；行为充足 → 纯协同过滤
- **可解释**：每条推荐记录贡献最大的来源商品，理由能落到具体商品上

```bash
cd server
mvn -q -Dtest=ItemCfRecommenderTest test
```

## 结构自检

本仓库的骨架代码在无 JDK/MySQL 环境下也可做结构校验（包路径、文件名、内部依赖、SQL 外键与索引、前端路由）：

```bash
python tools/check_structure.py
```

演示数据由脚本生成，密码哈希与后端算法一致，可直接登录：

```bash
python tools/gen_seed.py sql/02_seed.sql
```

## 说明与边界

- 本项目定位是**课程/专业实践的工程骨架**，不是可直接上线的生产系统。
- 已在 **JDK 17 + Maven 3.10 + MySQL 8.4** 环境实测通过：
  - `mvn compile` 编译 66 个源文件无错误
  - `mvn test` 6 个推荐算法单测全部通过
  - 启动服务后，登录、推荐、商品检索接口联调正常，越权访问（用户 token 调管理端）返回 403
- 已建表但尚未接入接口的模块：售后、订单流转、店铺信息、商家统计、公告、通知、推荐结果日志（`t_recommend_log`）。

## 踩坑记录

- **推荐得分全为 0**：共现表双向存储，但代码只按用户已看商品查共现行，
  候选商品的归一化分母因此算成 0，所有候选被过滤，推荐静默退化为热门补位。
  已改为独立聚合查询求 `N_x`，并补了回归测试。细节见 [docs/03](docs/03-推荐模块设计.md#三相似度与打分)。

## 参考

功能需求来自《优果仓功能》需求文档（用户端 7 模块 / 商家端 5 模块 / 管理端 7 模块）。

## License

MIT
