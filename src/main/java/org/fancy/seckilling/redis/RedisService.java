package org.fancy.seckilling.redis;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class RedisService {

    @Resource
    private RedisManager redisManager;

}
