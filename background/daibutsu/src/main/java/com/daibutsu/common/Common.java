package com.daibutsu.common;

import java.io.File;
import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public class Common {

	public static File multipartToFile(MultipartFile multipart, String fileName) throws IllegalStateException, IOException {
	    File convFile = new File(System.getProperty("java.io.tmpdir")+"/"+fileName);
	    multipart.transferTo(convFile);
	    return convFile;
	}
	
}
