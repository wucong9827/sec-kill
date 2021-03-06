## 秒杀项目

参考：[从零开始打造简易秒杀系统](https://github.com/qqxx6661/miaosha)

### 秒杀项目的目的：

- 严格防止超卖：库存100件你卖了120件，等着辞职吧
- 防止黑产：防止不怀好意的人群通过各种技术手段把你本该下发给群众的利益全收入了囊中。
- 保证用户体验：高并发下，别网页打不开了，支付不成功了，购物车进不去了，地址改不了了。这个问题非常之大，涉及到各种技术，也不是一下子就能讲完的，甚至根本就没法讲完。

**开发高并发系统时有三把利器用来保护系统**：缓存、降级和限流

- `缓存` 缓存的目的是提升系统访问速度和增大系统处理容量
- `降级` 降级是当服务出现问题或者影响到核心流程时，需要暂时屏蔽掉，待高峰或者问题解决后再打开
- `限流` 限流的目的是通过对并发访问/请求进行限速，或者对一个时间窗口内的请求进行限速来保护系统，一旦达到限制速率则可以拒绝服务、排队或等待、降级等处理

#### 项目使用数据库表结构：

```sql
-- ----------------------------
-- Table structure for stock
-- ----------------------------
DROP TABLE IF EXISTS `stock`;
CREATE TABLE `stock` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL DEFAULT '' COMMENT '名称',
  `count` int(11) NOT NULL COMMENT '库存',
  `sale` int(11) NOT NULL COMMENT '已售',
  `version` int(11) NOT NULL COMMENT '乐观锁，版本号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of stock
-- ----------------------------
INSERT INTO `stock` VALUES ('1', 'iphone', '50', '0', '0');
INSERT INTO `stock` VALUES ('2', 'mac', '10', '0', '0');

-- ----------------------------
-- Table structure for stock_order
-- ----------------------------
DROP TABLE IF EXISTS `stock_order`;
CREATE TABLE `stock_order` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `sid` int(11) NOT NULL COMMENT '库存ID',
  `name` varchar(30) NOT NULL DEFAULT '' COMMENT '商品名称',
  `user_id` int(11) NOT NULL DEFAULT '0',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of stock_order
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES ('1', '张三');
```

- 使用Jmeter进行压力测试，模拟并发请求，测试秒杀的功能

### 秒杀功能0.1⭐️

简单的通过`1.查库存 2. 更新库存 3. 生成订单`来实现的，没有加锁。

存在的问题：

- 没有使用数据库锁，在高并发下导致卖出量不正确，订单量 > 卖出量，出现**超卖问题**。⚡️
- 因为存在同一时间多个请求同时读到相同的数据

#### 解决方案1:( v0.11乐观锁)⚡️

- 乐观锁：一个最简单的办法就是，给每个商品库存一个版本号version字段，通过版本号来确认是否能够卖出商品。(处理速度快，不用像悲观锁那样长时间等待)
- 在实际减库存的SQL操作中，首先判断version是否是我们查询库存时候的version，如果是，扣减库存，成功抢购。如果发现version变了，则不更新数据库，返回抢购失败。
- 该方案可以让sale==订单数量，**防止了超卖问题**。
- 存在问题，**商品数量没有完全被抢完**，收益没有最大化。⚡️

### 接口限流：

- **系统安全防护措施**：
  - 令牌桶限流
  - 单用户访问频率限流
  - 抢购接口隐藏
- 在面临**高并发的请购请求**时，我们如果不对接口进行限流，可能会对后台系统造成极大的压力。尤其是对于下单的接口，过多的请求**打到数据库**会对系统的稳定性造成影响。
- 秒杀系统会尽量选择独立于公司其他后端系统之外进行**单独部署**，以免秒杀业务崩溃影响到其他系统。⭐️

**令牌桶算法：**

<img src="https://mmbiz.qpic.cn/mmbiz_png/qm3R3LeH8rbBpMpQhf1UW7z1w90B1505RgjM2dX8VDTC5HjQNeeia6tvyOuj7PwQIC8gubNm7cujZsaKibqwBHWw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1" alt="img" style="zoom:73%;" />

可控制发送到网络上数据的数目，并允许突发数据的发送。大小固定的令牌桶可自行以恒定的速率源源不断地产生令牌。如果令牌不被消耗，或者被消耗的速度小于产生的速度，令牌就会不断地增多，直到把桶填满。后面再产生的令牌就会从桶中溢出。最后桶中可以保存的最大令牌数永远不会超过桶的大小。

**漏桶算法**：

![img](https://mmbiz.qpic.cn/mmbiz_png/qm3R3LeH8rbBpMpQhf1UW7z1w90B1505HNIvKkaZcXY4NKBzbgsxxcBTBvY6ISmOWqm8dalZWmic2IswgXyEiaCw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

水（请求）先进入到漏桶里，漏桶以一定的速度出水，当水流入速度过大会直接溢出，可以看出漏桶算法能强行限制数据的传输速率。

#### 令牌桶算法与漏桶算法区别

漏桶算法能够强行限制数据的传输速率，而令牌桶算法在能够限制数据的平均传输速率外，**还允许某种程度的突发传输**。在令牌桶算法中，只要令牌桶中存在令牌，那么就允许突发地传输数据直到达到用户配置的门限，**因此它适合于具有突发特性的流量**。

令牌桶：当桶内有很多令牌，可以一次允许较多的请求

漏桶：任何时候都不允许快

#### 令牌桶工具

- Guava 的 RateLimiter 实现令牌桶（[实现原理](https://segmentfault.com/a/1190000012875897)）
- 在0.11版本上加上令牌桶算法，限流作用。
- 阻塞式获取令牌（让所有的商品都卖出，且没有超卖，但是用户的请求等待时间较长），非阻塞式获取令牌（请求响应快，但是商品由于乐观锁的并发的问题，还是无法全部被卖出）

### 0.11总结：

在**海量请求**的场景下，如果使用乐观锁**，会导致大量的请求返回抢购失败，用户体验极差。

然而使用悲观锁，比如数据库事务，则可以让数据库一个个处理库存数修改，修改成功后再迎接下一个请求，所以在不同情况下，应该根据实际情况使用悲观锁和乐观锁。

**两种锁各有优缺点，不能单纯的定义哪个好于哪个。**

- 乐观锁比较适合数据修改比较少，读取比较频繁的场景，即使出现了少量的冲突，这样也省去了大量的锁的开销，故而提高了系统的吞吐量。
- 但是如果经常发生冲突（写数据比较多的情况下），上层应用不不断的retry，这样反而降低了性能，对于这种情况使用悲观锁就更合适。

#### 悲观锁版本⚡️

- 使用 select ... for update 对查询的row进行加锁操作，让前面的请求都能成功，
- 可以做到防止超卖，并且订单数量 == 商品数量
- 容易阻塞后面的请求，使用体验不好，存在长时间的等待情况

### 秒杀下单流程图：

<img src="https://mmbiz.qpic.cn/mmbiz_png/qm3R3LeH8raYmr3dsqIWbpw1JJguysYX496pf9Ihz5thAYNNfe3V89rmVyicQa67PNeGxaUXWdR8C8XK4pTVruQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1" alt="img" style="zoom:80%;" />

### 0.2版本：抢购接口隐藏 + 单用户限制频率⭐️

- 为了防脚本自动化秒杀，将接口隐藏起来，不让恶意用户提前看到
- **可以防住的是直接请求接口的人，但是只要坏蛋们把脚本写复杂一点，先去请求一个验证值，再立刻请求抢购，也是能够抢购成功的。**不过坏蛋们请求验证值接口，**也需要在抢购时间开始后**，才能请求接口拿到验证值，然后才能申请抢购接口。**理论上来说在访问接口的时间上受到了限制，并且我们还能通过在验证值接口增加更复杂的逻辑，让获取验证值的接口并不快速返回验证值，进一步拉平普通用户和坏蛋们的下单时刻**。所以接口加盐还是有用的！
- **抢购接口隐藏（接口加盐）的具体做法**：⚡️( V 0.2 )
  - 每次点击秒杀按钮，先从服务器获取一个秒杀验证值（接口内判断是否到秒杀时间）。
  - Redis以缓存用户ID和商品ID为Key，秒杀地址为Value缓存验证值
  - 用户请求秒杀商品的时候，要带上秒杀验证值进行校验。

- **单用户限制频率**⚡️( V 0.21 )
  - 假设做好了接口隐藏，总有无聊的人会写一个复杂的脚本，先请求hash值，再立刻请求购买，如果你的app下单按钮做的很差，大家都要开抢后0.5秒才能请求成功，那可能会让脚本依然能够在大家前面抢购成功。我们需要在做一个额外的措施，来限制单个用户的抢购频率。
  - 其实很简单的就能想到用**redis给每个用户做访问统计**，甚至是**带上商品id，对单个商品做访问统计**，这都是可行的。我们先实现一个对用户的访问频率限制，我们在用户申请下单时，检查用户的访问次数，超过访问次数，则不让他下单！

### 0.3版本：添加缓存与数据库双写问题的争议⭐️

- **缓存热点数据**

  - 在秒杀实际的业务中，一定有很多需要做缓存的场景，比如售卖的商品，包括名称，详情等。访问量很大的数据，可以算是“热点”数据了，尤其是一些读取量远大于写入量的数据，更应该被缓存，而不应该让请求打到数据库上。

- ### 哪类数据适合缓存

  - 缓存量大但又不常变化的数据，比如详情，评论等。对于那些经常变化的数据，其实并不适合缓存，一方面会增加系统的复杂性（缓存的更新，缓存脏数据），另一方面也给系统带来一定的不稳定性（缓存系统的维护）。

- **缓存优缺点：**
  上缓存的优点：

  - 能够缩短服务的响应时间，给用户带来更好的体验。
  - 能够增大系统的吞吐量，依然能够提升用户体验。
  - 减轻数据库的压力，防止高峰期数据库被压垮，导致整个线上服务BOOM！

  上了缓存，也会引入很多额外的问题：

  - 缓存有多种选型，是内存缓存，memcached还是redis，你是否都熟悉，如果不熟悉，无疑增加了维护的难度（本来是个纯洁的数据库系统）。
  - 缓存系统也要考虑分布式，比如redis的分布式缓存还会有很多坑，无疑增加了系统的复杂性。
  - 在特殊场景下，如果对缓存的准确性有非常高的要求，就必须考虑**「缓存和数据库的一致性问题」**。

**「大部分观点认为，做缓存不应该是去更新缓存，而是应该删除缓存，然后由下个请求去去缓存，发现不存在后再读取数据库，写入缓存。」**

- **更新数据库和删缓存操作的先后顺序**⚡️

- 想实现基础的缓存数据库双写一致的逻辑，那么在大多数情况下，在不想做过多设计，增加太大工作量的情况下，请**「先更新数据库，再删缓存!」**

- 基本情况下只能要求满足最终一致性

- **先删除缓存，再更新数据库中避免脏数据？**⚡️

  - 采用延时双删策略。

  - （1）先淘汰缓存

    （2）再写数据库（这两步和原来一样）

    （3）休眠1秒，再次淘汰缓存

    这么做，可以将1秒内所造成的缓存脏数据，再次删除。

- #### 删缓存失败了怎么办：重试机制⚡️

  - <img src="https://mmbiz.qpic.cn/mmbiz_png/qm3R3LeH8rYAtxneteWLTnqeGPlfdvLLiaiazgneWWCZ784uQa7LLNp1NnpY5nuUMr34oJ9DcVTJNGicN3PtG8fXw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1" alt="img" style="zoom:67%;" />

- > 流程如下图所示：
  >
  > （1）更新数据库数据
  >
  > （2）数据库会将操作信息写入binlog日志当中
  >
  > （3）订阅程序提取出所需要的数据以及key
  >
  > （4）另起一段非业务代码，获得该信息
  >
  > （5）尝试删除缓存操作，发现删除失败
  >
  > （6）将这些信息发送至消息队列
  >
  > （7）重新从消息队列中获得该数据，重试操作。

  **「而读取binlog的中间件，可以采用阿里开源的canal」**

### 0.4版本： 订单异步处理⭐️

在秒杀系统用户进行抢购的过程中，由于在同一时间会有大量请求涌入服务器，如果每个请求都立即访问数据库进行扣减库存 **+** 写入订单的操作，对数据库的压力是巨大的。

如何减轻数据库的压力呢，**「我们将每一条秒杀的请求存入消息队列（例如RabbitMQ）中，放入消息队列后，给用户返回类似“抢购请求发送成功”的结果。而在消息队列中，我们将收到的下订单请求一个个的写入数据库中」**，比起多线程同步修改数据库的操作，大大缓解了数据库的连接压力，最主要的好处就表现在数据库连接的减少：

- 同步方式：大量请求快速占满数据库框架开启的数据库连接池，同时修改数据库，导致数据库读写性能骤减。
- 异步方式：一条条消息以顺序的方式写入数据库，连接数几乎不变（当然，也取决于消息队列消费者的数量）。

**「这种实现可以理解为是一中流量削峰：让数据库按照他的处理能力，从消息队列中拿取消息进行处理。」**

##### **完善用户体验**⚡️

- 用户点击了提交订单，收到了消息：您的订单已经提交成功。然后用户啥也没看见，也没有订单号，用户开始慌了，点到了自己的个人中心——已付款。发现居然没有订单！（因为可能还在队列中处理）

  这样的话，用户可能马上就要开始投诉了！太不人性化了，我们不能只为了开发方便，舍弃了用户体验！

  所以我们要改进一下，如何改进呢？其实很简单：

  - 让前端在提交订单后，显示一个“排队中”，**「就像我们在小米官网抢小米手机那样」**
  - 同时，前端不断请求 检查用户和商品是否已经有订单 的接口，如果得到订单已经处理完成的消息，页面跳转抢购成功。

