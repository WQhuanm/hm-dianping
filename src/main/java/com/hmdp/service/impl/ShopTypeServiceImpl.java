package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.SHOP_TYPE_KEY;
import static com.hmdp.utils.RedisConstants.SHOP_TYPE_TTL;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Result queryTypeList() {
        List<String> list = redisTemplate.opsForList().range(SHOP_TYPE_KEY, 0, -1);
        if (!list.isEmpty()) {
            List<ShopType> collect = list.stream()
                    .map(obj -> JSONUtil.toBean(obj, ShopType.class)).collect(Collectors.toList());
            return Result.ok(collect);
        }
        List<ShopType> typeList = query().orderByAsc("sort").list();
        list = typeList.stream().map(obj -> JSONUtil.toJsonStr(obj)).collect(Collectors.toList());
        redisTemplate.opsForList().rightPushAll(SHOP_TYPE_KEY, list);
        redisTemplate.expire(SHOP_TYPE_KEY, SHOP_TYPE_TTL, TimeUnit.HOURS);
        return Result.ok(typeList);
    }
}
