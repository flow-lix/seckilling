package org.fancy.seckilling.redis;

import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RLongAdder;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.jcache.configuration.RedissonConfiguration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Component
public class RedisManager {

    private static RedisManager redisManager;

    private RedissonClient redisClient;

    @PostConstruct
    public void initRedisManager() throws IOException {
        setRedisManager(this);
        Config config = Config.fromYAML(
                RedisManager.class.getClassLoader().getResource("redisson-config.yaml"));
        this.redisClient = Redisson.create(config);
    }

    public static RedisManager getInstance() {
        return RedisManager.redisManager;
    }

    private RedisManager() {
    }

    public <V> RBucket<V> getBucket(String name) {
        return redisClient.getBucket(name);
    }

    public RLongAdder getLongAdder(String name) {
        return redisClient.getLongAdder(name);
    }

    private static void setRedisManager(RedisManager redisManager) {
        RedisManager.redisManager = redisManager;
    }


}
