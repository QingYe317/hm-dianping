package com.hmdp;

import com.hmdp.service.impl.ShopServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class HmDianPingApplicationTests {

    @Resource
    private ShopServiceImpl shopService;

    @Test
    void testSaveShop() {
        // 把 id=1 的店铺写入缓存，逻辑过期时间 10 秒（方便观察）
        shopService.saveShop2Redis(1L, 10L);
    }

}
