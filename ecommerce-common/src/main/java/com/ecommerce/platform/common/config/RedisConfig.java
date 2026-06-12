package com.ecommerce.platform.common.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.support.spring.data.redis.GenericFastJsonRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        FastJson2RedisSerializer jsonSerializer = new FastJson2RedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    public static class FastJson2RedisSerializer extends GenericFastJsonRedisSerializer {
        @Override
        public Object deserialize(byte[] bytes) {
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            return super.deserialize(bytes);
        }

        @Override
        public byte[] serialize(Object object) {
            if (object == null) {
                return new byte[0];
            }
            if (object instanceof String) {
                return ((String) object).getBytes();
            }
            return JSON.toJSONBytes(object);
        }
    }
}
