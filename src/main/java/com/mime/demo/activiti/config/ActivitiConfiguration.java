package com.mime.demo.activiti.config;


import org.activiti.engine.*;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;


import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * @author SlumDuck
 * @create 2018-03-08 11:10
 * @desc 工作流启动配置
 */
@Configuration
@AutoConfigureAfter(PlatformTransactionManager.class)
public class ActivitiConfiguration {

    @Resource
    private PlatformTransactionManager transactionManager;

    @Resource
    private DataSource dataSource;

    @Bean("processEngineConfiguration")
    public ProcessEngineConfiguration processEngineConfiguration(){
        ProcessEngineConfiguration processEngineConfiguration = new SpringProcessEngineConfiguration();
        //设置dataSource
        processEngineConfiguration.setDataSource(dataSource);
        processEngineConfiguration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        processEngineConfiguration.setJobExecutorActivate(false);
        ((SpringProcessEngineConfiguration)processEngineConfiguration).setTransactionManager(transactionManager);
        return processEngineConfiguration;
    }

    /**
     * 工厂bean用于创建processEngine
     * @return
     */
    @Bean("processEngineFactoryBean")
    public ProcessEngineFactoryBean processEngineFactoryBean(){
        ProcessEngineFactoryBean processEngineFactoryBean = new ProcessEngineFactoryBean();
        processEngineFactoryBean.setProcessEngineConfiguration((ProcessEngineConfigurationImpl) processEngineConfiguration());
        return processEngineFactoryBean;
    }

    /**
     * 获取processEngine实例
     * @return
     */
    @Bean
    public ProcessEngine processEngine() throws Exception{

        return ((ProcessEngine)processEngineFactoryBean().getObject());
    }
    /******************************activiti 七大服务接口******************************/

    /**
     * 用户、组信息服务
     * @return
     * @throws Exception
     */
    @Bean("identityService")
    public IdentityService identityService() throws Exception{
        return processEngine().getIdentityService();
    }

    /**
     * 仓库服务：加载流程模型、部署流程模型
     * @return
     * @throws Exception
     */
    @Bean("repositoryService")
    public RepositoryService repositoryService() throws Exception{
        return processEngine().getRepositoryService();
    }

    /**
     * 运行时服务：启动流程
     * @return
     * @throws Exception
     */
    @Bean("runtimeService")
    public RuntimeService runtimeService() throws Exception{
        return processEngine().getRuntimeService();
    }

    /**
     * 任务服务：签收、办理、查询
     * @return
     * @throws Exception
     */
    @Bean("taskService")
    public TaskService taskService() throws Exception{
        return processEngine().getTaskService();
    }

    /**
     * 动态表单服务
     * @return
     * @throws Exception
     */
    @Bean("formService")
    public FormService formService() throws Exception{
        return processEngine().getFormService();
    }

    /**
     * 历史记录服务
     * @return
     * @throws Exception
     */
    @Bean("historyService")
    public HistoryService historyService() throws Exception{
        return processEngine().getHistoryService();
    }

    /**
     * 管理服务
     * @return
     * @throws Exception
     */
    @Bean("managementService")
    public ManagementService managementService() throws Exception{
        return processEngine().getManagementService();
    }

    /******************************restful 服务需要加载的bean******************************/
    /*
    @Bean("restResponseFactory")
    public RestResponseFactory restResponseFactory(){
        return new RestResponseFactory();
    }

    @Bean("contentTypeResolver")
    public ContentTypeResolver contentTypeResolver(){
        return new DefaultContentTypeResolver();
    }
    */
}
