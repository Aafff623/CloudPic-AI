package com.yupi.yupicturebackend;

import org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

// 排除分库分表, 防止配置冲突, 方便后续的灵活控制
@SpringBootApplication(exclude = {ShardingSphereAutoConfiguration.class})
// 开启异步能力，后面可以用异步任务
@EnableAsync
// 告诉 MyBatis 去哪里找 Mapper
@MapperScan("com.yupi.yupicturebackend.mapper")
// 开启 AOP，让权限切面能生效
@EnableAspectJAutoProxy(exposeProxy = true)
public class YuPictureBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(YuPictureBackendApplication.class, args);
    }

}
