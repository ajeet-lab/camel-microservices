/**
 *  Copyright 2005-2018 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package in.goindigo.smartcargo;

import javax.sql.DataSource;

import org.apache.camel.component.mybatis.MyBatisComponent;
import org.apache.camel.component.sjms2.Sjms2Component;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

@SpringBootApplication
public class Application {

	@Autowired
	DataSource dataSource;

	@Value("${amqp.username}")
	private String username;

	@Value("${amqp.password}")
	private String password;
	
	@Value("${amqp.url}")
	private String url;
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public SqlSessionFactory sqlSessionFactory() throws Exception {
		SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
		factoryBean.setDataSource(dataSource);
		factoryBean.setConfigLocation(new ClassPathResource("SqlMapConfig.xml"));
		return factoryBean.getObject();
	}

	@Bean(name = "mybatis")
	public MyBatisComponent myBatisComponent(SqlSessionFactory sqlSessionFactory) {
		MyBatisComponent result = new MyBatisComponent();
		result.setSqlSessionFactory(sqlSessionFactory);
		return result;
	}

	@Bean
	org.apache.qpid.jms.JmsConnectionFactory jmsConnectionFactory(){
		org.apache.qpid.jms.JmsConnectionFactory factory = new JmsConnectionFactory(username, password, url);
		return factory;
	}

	@Bean(name = "amqp")
	org.apache.camel.component.sjms2.Sjms2Component get(JmsConnectionFactory jmsConnectionFactory) {
		org.apache.camel.component.sjms2.Sjms2Component amqp = new Sjms2Component();
		amqp.setConnectionFactory(jmsConnectionFactory);
		return amqp;
	}

}
