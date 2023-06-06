package top.keithfeng.config;

import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisMapperScannerConfig {

    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
        mapperScannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactory");

        //*.*.mapper 这个包名是所有的Mapper.java文件所在的路径，该包下面的子包里面的文件同样会扫描到。
        mapperScannerConfigurer.setBasePackage("top.keithfeng.mapper");

        return mapperScannerConfigurer;
    }


    /**
     * 目的防止驼峰命名规则
     *
     * @return
     */
    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return new ConfigurationCustomizer() {
            @Override
            public void customize(org.apache.ibatis.session.Configuration configuration) {
                configuration.setMapUnderscoreToCamelCase(true);
            }
        };
    }

}
