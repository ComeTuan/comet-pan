package top.nomelin.cometpan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("top.nomelin.cometpan.mapper")
@EnableTransactionManagement
@EnableScheduling
@EnableCaching
public class CometPanApplication {
    public static void main(String[] args) {
        SpringApplication.run(CometPanApplication.class, args);
    }

}
