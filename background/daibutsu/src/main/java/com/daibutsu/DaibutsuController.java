package com.daibutsu;
import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.daibutsu.aws.AwsRekognitionCustomModelService;
import com.daibutsu.aws.AwsS3Service;
import com.daibutsu.common.Common;
import com.daibutsu.dao.AwsDAO;

import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.rekognition.model.DeleteProjectResponse;
import software.amazon.awssdk.services.rekognition.model.DescribeProjectsResponse;
import software.amazon.awssdk.services.rekognition.model.ListDatasetEntriesResponse;
import software.amazon.awssdk.services.rekognition.model.UpdateDatasetEntriesResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@RestController
public class DaibutsuController {
    @Autowired
    AwsS3Service awsS3Service;
    
    @Autowired
    AwsRekognitionCustomModelService customModelService;
    
    @Autowired
    AwsDAO dao;
    
    public DaibutsuController() {
    }
    
    /***
     * S3にフォルダを作成する
     * @param filePart
     * @return
     * @throws IOException
     */
    @CrossOrigin
    @RequestMapping(value = "api/CreateBucket", method = RequestMethod.GET)
	public SdkHttpResponse CreateBucket() throws IOException {
    	WaiterResponse<HeadBucketResponse> result = awsS3Service.createBucket("testproject20220702");
    	
        return result.matched().response().get().sdkHttpResponse();
    }
    
    /***
     * S3にファイルをアップロードする(ファイルパス)
     * @param filePart
     * @return
     * @throws IOException
     */
    @CrossOrigin
    @RequestMapping(value = "api/uploadCustomModelData", method = RequestMethod.GET)
	public SdkHttpResponse UploadCustomModelData() throws IOException {
    	String bucketName = "custom-labels-console-ap-northeast-1-2f64e22fc7/assets/TestProject/1652800417";
    	String filePath = "C:\\Users\\mori\\Desktop\\仏像AI開発\\KAMAKURA\\AWS用\\トレーニング用\\アップ済\\KAMAKURA_TR_001.jpg";
    	PutObjectResponse result = awsS3Service.UploadObject(bucketName, filePath);
    	
        return result.sdkHttpResponse();
    }
    
    /***
     * 
     * @param filePart
     * @return
     * @throws IOException
     */
    @CrossOrigin
    @RequestMapping(value = "api/uploadS3", method = RequestMethod.POST)
	public SdkHttpResponse uploadS3(@RequestPart("file") MultipartFile filePart) throws IOException {
    	File uploadFile = Common.multipartToFile(filePart, filePart.getOriginalFilename());
    	
    	PutObjectResponse result = awsS3Service.UploadObject(uploadFile);
    	
    	return result.sdkHttpResponse();
    }
    
    /***
     * S3にファイルをアップロードする(テスト)
     * @param filePart
     * @return
     * @throws IOException
     */
    @CrossOrigin
    @RequestMapping(value = "api/uploadCustomModelDataTest", method = RequestMethod.GET)
	public SdkHttpResponse UploadCustomModelDataTest() throws IOException {
    	PutObjectResponse result = awsS3Service.UploadObject();
    	
        return result.sdkHttpResponse();
    }
    
    /***
     * カスタムモデルプロジェクトを削除する（トレーニング用データセット、テスト用データセットも含めて削除）
     * @return
     * @throws Exception
     */
    @CrossOrigin
    @RequestMapping(value = "api/deleteCustomModelProject", method = RequestMethod.GET)
    public ResponseEntity<Object> deleteCustomModelProject() throws Exception {
    	// TODO　テスト用データセット削除
    	// TODO　トレーニング用データセット削除
    	
    	// プロジェクトを削除
    	DeleteProjectResponse deleteResult = customModelService.DeleteCustomModelProject("arn:aws:rekognition:ap-northeast-1:879800508542:project/testproject3/1663479975230");
    	
    	return ResponseEntity.ok("test");
    }
    
    
    /***
     * カスタムモデルプロジェクト一覧を取得する
     * @return
     * @throws Exception
     */
    @CrossOrigin
    @RequestMapping(value = "api/getCustomModelProject", method = RequestMethod.GET)
    public ResponseEntity<Object> getCustomModelProject() throws Exception {
    	DescribeProjectsResponse result = customModelService.GetCustomModelProject();
    	
        return ResponseEntity.ok(result.projectDescriptions());
    }
    
    /***
     * カスタムデータセットの登録一覧を取得する
     * @return
     * @throws Exception
     */
    @CrossOrigin
    @RequestMapping(value = "api/getListDatasetEntries", method = RequestMethod.GET)
    public ResponseEntity<Object> ListDatasetEntries() throws Exception {
    	String dataSetName = "arn:aws:rekognition:ap-northeast-1:415555380271:project/TestProject2/dataset/train/1657279851335";

    	ListDatasetEntriesResponse result = customModelService.GetListDatasetEntries(dataSetName);
    	
        return ResponseEntity.ok(result.datasetEntries());
    }
    
    
    /***
     * カスタムモデルのデータセットにデータを登録する(トレーニング用)
     * @return
     * @throws Exception
     */
    @CrossOrigin
    @RequestMapping(value = "api/updateDatasetEntries", method = RequestMethod.GET)
    public SdkHttpResponse UpdateDatasetEntries() throws Exception {
    	String dataSetName = "arn:aws:rekognition:ap-northeast-1:415555380271:project/TestProject2/dataset/train/1657279851335";
    	
    	String updateFile = "C:\\Users\\mori\\Desktop\\lbl_heian.json";
    	
    	UpdateDatasetEntriesResponse result = customModelService.UpdateDatasetEntries(dataSetName, updateFile);
    	
        return result.sdkHttpResponse();
    }
    
    /***
     * カスタムモデルのデータセットにデータを登録する(テスト用)
     * @return
     * @throws Exception
     */
    @CrossOrigin
    @RequestMapping(value = "api/updateDatasetEntries/test", method = RequestMethod.GET)
    public SdkHttpResponse UpdateDatasetEntriesTest() throws Exception {
    	String dataSetName = "arn:aws:rekognition:ap-northeast-1:415555380271:project/TestProject2/dataset/test/1657279850973";
    	
    	String updateFile = "C:\\Users\\mori\\Desktop\\仏像AI開発\\json\\test\\lbl_heian.json";
    	
    	UpdateDatasetEntriesResponse result = customModelService.UpdateDatasetEntries(dataSetName, updateFile);
    	
        return result.sdkHttpResponse();
    }
    
}
