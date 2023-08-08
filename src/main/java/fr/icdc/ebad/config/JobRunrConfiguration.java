package fr.icdc.ebad.config;

import org.jobrunr.jobs.mappers.JobMapper;
import org.jobrunr.spring.autoconfigure.JobRunrAutoConfiguration;
import org.jobrunr.spring.autoconfigure.storage.JobRunrSqlStorageAutoConfiguration;
import org.jobrunr.storage.StorageProvider;
import org.jobrunr.storage.StorageProviderUtils;
import org.jobrunr.storage.sql.common.DefaultSqlStorageProvider;
import org.jobrunr.storage.sql.h2.H2StorageProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;

@Configuration
@Import({JobRunrSqlStorageAutoConfiguration.class, JobRunrAutoConfiguration.class})
public class JobRunrConfiguration {

    private final DataSource dataSource;

    public JobRunrConfiguration(DataSource dataSource) {
        this.dataSource = dataSource;
    }
//    @Bean
//    public StorageProvider storageProvider(JobMapper jobMapper) {
//
//        DefaultSqlStorageProvider storageProvider = new DefaultSqlStorageProvider(dataSource, new AnsiDialect(), StorageProviderUtils.DatabaseOptions.CREATE);
//        storageProvider.setJobMapper(jobMapper);
//        return storageProvider;
//    }

    @Bean
    public StorageProvider storageProvider2(JobMapper jobMapper) {

        DefaultSqlStorageProvider storageProvider = new H2StorageProvider(dataSource, StorageProviderUtils.DatabaseOptions.CREATE);
        storageProvider.setJobMapper(jobMapper);
        return storageProvider;
    }
}
