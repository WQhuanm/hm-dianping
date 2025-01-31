package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Result queryShopById(Long id) {
        String key = CACHE_SHOP_KEY + id;
        String data = redisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(data)) {
            Shop shop = JSONUtil.toBean(data, Shop.class);
            return Result.ok(shop);
        }
        if (data != null) {
            return Result.fail("店铺不存在");
        }
        Shop shop = getById(id);
        if (shop == null) {//防止缓存穿透，对查询不到的数据写入空值
            redisTemplate.opsForValue().set(key,"",CACHE_NULL_TTL,TimeUnit.MINUTES);
            return Result.fail("店铺不存在");
        }
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        return Result.ok(shop);
    }

    @Override
    @Transactional//更新和删除缓存作为一个事务存在
    public Result updateShop(Shop shop) {
        if (shop.getId() == null) {
            return Result.fail(" 店铺id不能为空");
        }
        updateById(shop);
        redisTemplate.delete(CACHE_SHOP_KEY + shop.getId());
        return Result.ok();
    }
}
