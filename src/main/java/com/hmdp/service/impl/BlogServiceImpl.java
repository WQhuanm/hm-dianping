package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.Vo.ScrollVo;
import com.hmdp.dto.Result;
import com.hmdp.entity.Blog;
import com.hmdp.mapper.BlogMapper;
import com.hmdp.service.IBlogService;
import com.hmdp.service.IFollowService;
import com.hmdp.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.FEED_KEY;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    @Autowired
    private IFollowService followService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Result queryBlogById(Long id) {
        // 1.查询blog
        Blog blog = getById(id);
        if (blog == null) {
            return Result.fail("笔记不存在！");
        }
//        // 2.查询blog有关的用户
//        queryBlogUser(blog);

        return Result.ok(blog);
    }

    @Override
    public Result saveBlog(Blog blog) {
        Long userId = UserHolder.getUser().getId();
        blog.setUserId(userId);
        if (save(blog)) {
            List<Long> fans = followService.query().eq("follow_user_id", userId).list()
                    .stream().map(u -> u.getUserId()).collect(Collectors.toList());
            for (Long fan : fans) {
                redisTemplate.opsForZSet().add(FEED_KEY + fan, blog.getId().toString(), System.currentTimeMillis());
            }
            return Result.ok(blog.getId());
        }
        return Result.fail("保存博客失败");
    }

    @Override
    public Result queryBlogOfFollow(Long lastId, Integer offset) {
        String key = FEED_KEY + UserHolder.getUser().getId();
        Set<ZSetOperations.TypedTuple<String>> typedTuples = redisTemplate.opsForZSet().
                reverseRangeByScoreWithScores(key, 0, lastId, offset, 4);
        if (typedTuples == null || typedTuples.isEmpty()) {
            return Result.ok();
        }
        List<Long> ids = new ArrayList<>(typedTuples.size());
        for (ZSetOperations.TypedTuple<String> u : typedTuples) {
            ids.add(Long.valueOf(u.getValue()));
            if (lastId == u.getScore().longValue()) offset++;
            else {
                lastId = u.getScore().longValue();
                offset = 1;
            }
        }
        String idStr = StrUtil.join(",", ids);
        List<Blog> blogs = query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();
        ScrollVo vo = new ScrollVo();
        vo.setList(blogs);
        vo.setOffset(offset);
        vo.setMinTime(lastId);
        return Result.ok(vo);
    }
}
