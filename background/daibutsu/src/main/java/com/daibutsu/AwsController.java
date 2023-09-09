package com.daibutsu;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.daibutsu.aws.AwsRekognitionCustomModelService;
import com.daibutsu.aws.AwsS3Service;
import com.daibutsu.common.Common;
import com.daibutsu.dao.AwsDAO;

import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.services.rekognition.model.CreateDatasetResponse;
import software.amazon.awssdk.services.rekognition.model.CreateProjectResponse;
import software.amazon.awssdk.services.rekognition.model.CreateProjectVersionResponse;
import software.amazon.awssdk.services.rekognition.model.CustomLabel;
import software.amazon.awssdk.services.rekognition.model.DeleteDatasetResponse;
import software.amazon.awssdk.services.rekognition.model.DeleteProjectResponse;
import software.amazon.awssdk.services.rekognition.model.DescribeProjectVersionsResponse;
import software.amazon.awssdk.services.rekognition.model.ListDatasetEntriesResponse;
import software.amazon.awssdk.services.rekognition.model.StartProjectVersionResponse;
import software.amazon.awssdk.services.rekognition.model.StopProjectVersionResponse;
import software.amazon.awssdk.services.rekognition.model.UpdateDatasetEntriesResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@RestController
public class AwsController {
    @Autowired
    AwsS3Service awsS3Service;
    
    @Autowired
    AwsRekognitionCustomModelService customModelService;
    
    @Autowired
    AwsDAO dao;
    
    public AwsController() {
    }
    
    /***
     * カスタムモデルプロジェクトを作成する（トレーニング用データセット、テスト用データセットも含めて作成）
     * @return
     * @throws Exception
     */
    @CrossOrigin
    @RequestMapping(value = "api/createCustomModelProject", method = RequestMethod.POST)
    public ResponseEntity<Object> createCustomModelProject(@RequestBody String projectId) throws Exception {    	
    	CreateProjectResponse projectResult = customModelService.CreateCustomModelProject(projectId);
    	
    	String projectName = projectResult.projectArn();
    	
    	CreateDatasetResponse testDataSetResult = customModelService.CreateCustomModelTestDataSet(projectName);
    	String testDataName = testDataSetResult.datasetArn();
    	
    	CreateDatasetResponse trDataSetResult = customModelService.CreateCustomModelTrainingDataSet(projectName);
    	String trainingDataName = trDataSetResult.datasetArn();

    	System.out.print("Project：" + projectName + "\n");
    	System.out.print("Test Arn：" + testDataSetResult.datasetArn() + "\n");
    	System.out.print("TR Arn：" + trDataSetResult.datasetArn() + "\n");
    	
//    	return ResponseEntity.ok(projectResult.sdkHttpResponse());

    	// S3にバケットを作成する
    	String bucketName = "custom-labels-" + projectId.toLowerCase();
    	WaiterResponse<HeadBucketResponse> createBucketResult = awsS3Service.createBucket(bucketName);
    	if(createBucketResult.matched().response().get().sdkHttpResponse().isSuccessful()) {
        	// プロジェクト情報を登録する
        	dao.registProject(projectId, projectName, testDataName, trainingDataName, bucketName);
        	return ResponseEntity.ok("Create：" + projectId);
    	} else {
    		// バケット作成失敗した場合、プロジェクトを削除する
    		// TODO 削除処理、未実装
    		
    		return ResponseEntity.ok("Failed：" + projectId);
    	}
    	
    }
    
    /***
     * カスタムモデルプロジェクトを削除する
     * @return
     * @throws Exception
     */
    @CrossOrigin
    @RequestMapping(value = "api/deleteCustomModelProject", method = RequestMethod.POST)
    public ResponseEntity<Object> deleteCustomModelProject(@RequestBody String projectId) throws Exception {
    	List<Map<String, Object>> projectList = dao.selectProject(projectId);
    	if(projectList == null || projectList.size() == 0) {
    		return ResponseEntity.ok("Failed");
    	}
    	
    	String projectName = projectList.get(0).get("project_name").toString();
    	
    	// モデル削除
    	boolean deleteAllVersionResult = customModelService.deleteProjectAllVersion(projectName);
    	
    	// データセット削除
    	// トレーニングデータ削除
    	String trainingName = projectList.get(0).get("training_data_name").toString();
    	DeleteDatasetResponse deleteTrainingResult = customModelService.deleteCustomModelDataSet(trainingName);
    	
    	// テストデータ削除
    	String testName = projectList.get(0).get("test_data_name").toString();
    	DeleteDatasetResponse deleteTestResult = customModelService.deleteCustomModelDataSet(testName);
    	
    	// プロジェクト削除
    	DeleteProjectResponse deleteProjectResult = customModelService.DeleteCustomModelProject(projectName);
    	
    	// バケット削除
    	String bucketName = projectList.get(0).get("bucket_name").toString();
    	awsS3Service.deleteBucket(bucketName);
    	
    	String inferenceBucketName = projectList.get(0).get("inference_bucket_name").toString();
    	awsS3Service.deleteBucket(inferenceBucketName);
    	
    	// ＤＢからプロジェクトを削除
    	dao.deleteProject(projectId);
    	
    	return ResponseEntity.ok("Delete：" + projectId);
    }
    
    /***
     * カスタムモデルのデータセットにデータを登録する(トレーニング用)
     * @return
     * @throws Exception
     */
    @CrossOrigin
    @RequestMapping(value = "api/trainingDataSet", method = RequestMethod.POST)
    public ResponseEntity<String> UpdateTrainingDatasetEntries(
            @RequestParam("file") MultipartFile multipartFile,
            @RequestParam("projectId") String projectId,
            @RequestParam("label") String label
    ) throws Exception{
    	
    	List<Map<String, Object>> projectList = dao.selectProject(projectId);
    	if(projectList == null || projectList.size() == 0) {
    		return ResponseEntity.ok("Failed");
    	}
    	
    	File uploadFile = Common.multipartToFile(multipartFile, multipartFile.getOriginalFilename());
    	PutObjectResponse uploadResult = awsS3Service.UploadObject(uploadFile, projectList.get(0).get("bucket_name").toString());
    	if(!uploadResult.sdkHttpResponse().isSuccessful()) {
    		// アップロード失敗した場合
    		return ResponseEntity.ok("Failed：" + "アップロードに失敗しました。");
    	}
    	
    	// ラベル貼り付け
    	String filePath = createLabelFile(projectList.get(0).get("bucket_name").toString(), uploadFile.getName(), label);
    	
    	UpdateDatasetEntriesResponse result = customModelService.UpdateDatasetEntries(projectList.get(0).get("training_data_name").toString(), filePath);
    	
        return ResponseEntity.ok("Success");
    }

    /***
     * カスタムモデルのデータセットにデータを登録する(テスト用)
     * @return
     * @throws Exception
     */
    @CrossOrigin
    @RequestMapping(value = "api/testDataSet", method = RequestMethod.POST)
    public ResponseEntity<String> UpdateTestDatasetEntries(
            @RequestParam("file") MultipartFile multipartFile,
            @RequestParam("projectId") String projectId,
            @RequestParam("label") String label
    ) throws Exception{
    	
    	List<Map<String, Object>> projectList = dao.selectProject(projectId);
    	if(projectList == null || projectList.size() == 0) {
    		return ResponseEntity.ok("Failed");
    	}
    	
    	File uploadFile = Common.multipartToFile(multipartFile, multipartFile.getOriginalFilename());
    	PutObjectResponse uploadResult = awsS3Service.UploadObject(uploadFile, projectList.get(0).get("bucket_name").toString());
    	if(!uploadResult.sdkHttpResponse().isSuccessful()) {
    		// アップロード失敗した場合
    		return ResponseEntity.ok("Failed：" + "アップロードに失敗しました。");
    	}
    	
    	// ラベル貼り付け
    	String filePath = createLabelFile(projectList.get(0).get("bucket_name").toString(), uploadFile.getName(), label);
    	
    	UpdateDatasetEntriesResponse result = customModelService.UpdateDatasetEntries(projectList.get(0).get("test_data_name").toString(), filePath);
    	
        return ResponseEntity.ok("Success");
    }
    
    /***
     * カスタムモデル作成
     * @return
     * @throws Exception
     */
    @CrossOrigin
    @RequestMapping(value = "api/createModel", method = RequestMethod.POST)
    public ResponseEntity<String> CreateModel(
            @RequestParam("projectId") String projectId,
            @RequestParam("versionName") String versionName
    ) throws Exception{
    	List<Map<String, Object>> projectList = dao.selectProject(projectId);
    	if(projectList == null || projectList.size() == 0) {
    		return ResponseEntity.ok("Failed");
    	}
    	
    	String projectName = projectList.get(0).get("project_name").toString();
    	String bucketName = projectList.get(0).get("bucket_name").toString();
    	
    	// モデル作成
    	CreateProjectVersionResponse createProjectVersionResponse = customModelService.CreateProjectVersion(projectName, bucketName, versionName);
    	
    	// S3にバケットを作成する
    	String inferenceBucketName = "custom-labels-" + projectId.toLowerCase() + "-" + "inference";
    	WaiterResponse<HeadBucketResponse> createBucketResult = awsS3Service.createBucket(inferenceBucketName);
    	
    	String versionNameArn = createProjectVersionResponse.projectVersionArn();
    	
    	// プロジェクト一覧に最新モデルを更新する
    	dao.updateVersionName(projectId, versionName, versionNameArn, inferenceBucketName);
    	
    	// 過去のモデルを削除する
    	boolean deleteProjectVersionResult = customModelService.deletePostProjectVersion(projectName, versionName);
    	
    	return ResponseEntity.ok("Success");
    }
    
    /***
     * カスタムモデル状態を取得する
     * @return
     * @throws Exception
     */
    @CrossOrigin
    @RequestMapping(value = "api/statusCustomModel", method = RequestMethod.POST)
    public ResponseEntity<Object> statusCustomModel(
    		@RequestParam("projectId") String projectId
    ) throws Exception {
    	List<Map<String, Object>> projectList = dao.selectProject(projectId);
    	if(projectList == null || projectList.size() == 0) {
    		return ResponseEntity.ok("Failed");
    	}
    	
    	String projectName = projectList.get(0).get("project_name").toString();
    	String versionName = projectList.get(0).get("version").toString();
    	DescribeProjectVersionsResponse result = customModelService.StatusCustomModel(projectName, versionName);
    	
    	return ResponseEntity.ok(result.projectVersionDescriptions().get(0).status());
    }
    
    /***
     * カスタムモデルを起動する
     * @return
     * @throws Exception
     */
    @CrossOrigin
    @RequestMapping(value = "api/startCustomModel", method = RequestMethod.POST)
    public ResponseEntity<Object> startCustomModel(
    		@RequestParam("projectId") String projectId
    		) throws Exception {
    	List<Map<String, Object>> projectList = dao.selectProject(projectId);
    	if(projectList == null || projectList.size() == 0) {
    		return ResponseEntity.ok("Failed");
    	}
    	
    	String projectVersionArn = projectList.get(0).get("version_name").toString();
    	
    	StartProjectVersionResponse result = customModelService.StartCustomModel(projectVersionArn);
        
    	
        return ResponseEntity.ok(result.statusAsString());
    }
    
    /***
     * カスタムモデルを停止する
     * @return
     * @throws Exception
     */
    @CrossOrigin
    @RequestMapping(value = "api/stopCustomModel", method = RequestMethod.POST)
    public ResponseEntity<Object> stopCustomModel(
    		@RequestParam("projectId") String projectId
    		) throws Exception {
    	List<Map<String, Object>> projectList = dao.selectProject(projectId);
    	if(projectList == null || projectList.size() == 0) {
    		return ResponseEntity.ok("Failed");
    	}
    	
    	String projectVersionArn = projectList.get(0).get("version_name").toString();
    	
    	StopProjectVersionResponse result = customModelService.StopCustomModel(projectVersionArn);
    	
        return ResponseEntity.ok(result.statusAsString());
    }

    /***
     * 画像を推論する
     * @param filePart
     * @return
     * @throws Exception
     */
    @CrossOrigin
    @RequestMapping(value = "api/customModel", method = RequestMethod.POST)
    public ResponseEntity<Object> customModel(
            @RequestParam("file") MultipartFile multipartFile,
            @RequestParam("projectId") String projectId
    		) throws Exception {
    	
    	List<Map<String, Object>> projectList = dao.selectProject(projectId);
    	if(projectList == null || projectList.size() == 0) {
    		return ResponseEntity.ok("Failed");
    	}
    	
    	String projectVersionArn = projectList.get(0).get("version_name").toString();
    	String inference_bucket_name = projectList.get(0).get("inference_bucket_name").toString();
    	
    	// S3に画像をアップロード
    	File uploadFile = Common.multipartToFile(multipartFile, multipartFile.getOriginalFilename());
    	PutObjectResponse uploadResult = awsS3Service.UploadObject(uploadFile, inference_bucket_name);
    	if(!uploadResult.sdkHttpResponse().isSuccessful()) {
    		// アップロード失敗した場合
    		return ResponseEntity.ok("Failed：" + "アップロードに失敗しました。");
    	}
    	
    	List<CustomLabel> result = customModelService.CustomModel(projectVersionArn, inference_bucket_name, multipartFile.getOriginalFilename());
    	
        return ResponseEntity.ok(result.get(0).toString());
    }
    
    /***
     * ラベル一覧取得
     * @param projectId
     * @return
     * @throws Exception
     */
    @CrossOrigin
    @RequestMapping(value = "api/getListLabel", method = RequestMethod.POST)
    public ResponseEntity<Object> getListLabel(
    		@RequestParam("projectId") String projectId
    		) throws Exception {
    	return null;
    }
    
    /***
     * データ一覧取得
     * @param projectId
     * @return
     * @throws Exception
     */
    @CrossOrigin
    @RequestMapping(value = "api/getTrainingData", method = RequestMethod.POST)
    public ResponseEntity<Object> getTrainingData(
    		@RequestParam("projectId") String projectId
    		) throws Exception {
    	
    	List<Map<String, Object>> projectList = dao.selectProject(projectId);
    	if(projectList == null || projectList.size() == 0) {
    		return ResponseEntity.ok("Failed");
    	}
    	
    	String datasetArn = projectList.get(0).get("training_data_name").toString();
    	
    	ListDatasetEntriesResponse result = customModelService.getListDatasetEntries(datasetArn);
    	
    	return ResponseEntity.ok(result.datasetEntries());
    }
    
    /***
     * データ一覧取得
     * @param projectId
     * @return
     * @throws Exception
     */
    @CrossOrigin
    @RequestMapping(value = "api/getTestData", method = RequestMethod.POST)
    public ResponseEntity<Object> getTestData(
    		@RequestParam("projectId") String projectId
    		) throws Exception {
    	List<Map<String, Object>> projectList = dao.selectProject(projectId);
    	if(projectList == null || projectList.size() == 0) {
    		return ResponseEntity.ok("Failed");
    	}
    	
    	String datasetArn = projectList.get(0).get("test_data_name").toString();
    	
    	ListDatasetEntriesResponse result = customModelService.getListDatasetEntries(datasetArn);
    	
    	return ResponseEntity.ok(result.datasetEntries());
    }
    
	private String createLabelFile(String bucket_name, String fileName, String label) {
		String result = "";
		String sample = "{\"source-ref\":\"s3://" + bucket_name + "/" + fileName + "\",\"TestProject-train_" + label + "\":1,\"TestProject-train_" + label + "-metadata\":{\"confidence\":1,\"job-name\":\"labeling-job/TestProject-train_" + label + "\",\"class-name\":\"" + label + "\",\"human-annotated\":\"yes\",\"creation-date\":\"2022-05-17T15:38:44.259Z\",\"type\":\"groundtruth/image-classification\"},\"cl-metadata\":{\"is_labeled\":true}}";
		
		try {
            // FileWriterクラスのオブジェクトを生成する
            FileWriter file = new FileWriter("lbl.json");
            // PrintWriterクラスのオブジェクトを生成する
            PrintWriter pw = new PrintWriter(new BufferedWriter(file));
            
            //ファイルに書き込む
            pw.println(sample);
            
            //ファイルパス取得
            Path p1 = Paths.get("");
            Path p2 = p1.toAbsolutePath();
            result = p2.toString() + "\\" + "lbl.json";
            
            //ファイルを閉じる
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		return result;
	}
    
}
