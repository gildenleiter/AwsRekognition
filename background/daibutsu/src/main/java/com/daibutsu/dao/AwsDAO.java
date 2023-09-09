package com.daibutsu.dao;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AwsDAO {
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	public Integer registProject(String projectId, String projectName, String testDataName, String trainingDataName, String bucketName) {
		StringBuilder sql = new StringBuilder();
		
		sql.append("insert into project");
		sql.append("(");
		sql.append("    id");
		sql.append("    , project_name");
		sql.append("    , test_data_name");
		sql.append("    , training_data_name");
		sql.append("    , bucket_name");
		sql.append(") values (");
		sql.append("    '%1'");
		sql.append("    , '%2'");
		sql.append("    , '%3'");
		sql.append("    , '%4'");
		sql.append("    , '%5'");
		sql.append(");");
		
		String query = sql.toString();
		
		query= query.replace("%1", projectId);
		query= query.replace("%2", projectName);
		query= query.replace("%3", testDataName);
		query= query.replace("%4", trainingDataName);
		query= query.replace("%5", bucketName);
		
		return jdbcTemplate.update(query);
	}
	
	public List<Map<String, Object>> selectProjectAll() {
		StringBuilder sql = new StringBuilder();
		
		sql.append("select * from project");
		
		String query = sql.toString();
		
		return jdbcTemplate.queryForList(query);
	}
	
	public List<Map<String, Object>> selectProject(String projectId) {
		StringBuilder sql = new StringBuilder();
		
		sql.append("select * from project ");
		sql.append("where id = '%1'");
		
		String query = sql.toString();
		query= query.replace("%1", projectId);
		
		return jdbcTemplate.queryForList(query);
	}

	public Integer updateVersionName(String projectId, String versionName, String versionNameArn, String inferenceBucketName) {
		StringBuilder sql = new StringBuilder();
		
		sql.append("update project ");
		sql.append("SET version = '%1'");
		sql.append(", version_name = '%2'");
		sql.append(", inference_bucket_name = '%3'");
		sql.append("where id = '%4'");
		
		String query = sql.toString();
		query= query.replace("%1", versionName);
		query= query.replace("%2", versionNameArn);
		query= query.replace("%3", inferenceBucketName);
		query= query.replace("%4", projectId);
		
		return jdbcTemplate.update(query);		
	}

	public int deleteProject(String projectId) {
		StringBuilder sql = new StringBuilder();
		
		sql.append("delete from project ");
		sql.append("where id = '%1'");
		
		String query = sql.toString();
		query= query.replace("%1", projectId);
		
		return jdbcTemplate.update(query);		
		
	}
}
