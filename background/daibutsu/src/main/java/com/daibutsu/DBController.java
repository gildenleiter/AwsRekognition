package com.daibutsu;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.daibutsu.dao.AwsDAO;

@RestController
public class DBController {

    @Autowired
    AwsDAO dao;
    
    /***
     * プロジェクト一覧を取得する
     * @param filePart
     * @return
     * @throws IOException
     */
    @CrossOrigin
    @RequestMapping(value = "api/getProjectAll", method = RequestMethod.GET)
	public ResponseEntity<List<Map<String, Object>>> getProjectAll() throws IOException {
    	List<Map<String, Object>> result = dao.selectProjectAll();
    	
        return ResponseEntity.ok(result);
    }
}
