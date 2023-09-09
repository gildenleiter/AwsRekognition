package com.daibutsu.aws;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.CreateDatasetRequest;
import software.amazon.awssdk.services.rekognition.model.CreateDatasetResponse;
import software.amazon.awssdk.services.rekognition.model.CreateProjectRequest;
import software.amazon.awssdk.services.rekognition.model.CreateProjectResponse;
import software.amazon.awssdk.services.rekognition.model.CreateProjectVersionRequest;
import software.amazon.awssdk.services.rekognition.model.CreateProjectVersionResponse;
import software.amazon.awssdk.services.rekognition.model.CustomLabel;
import software.amazon.awssdk.services.rekognition.model.DatasetChanges;
import software.amazon.awssdk.services.rekognition.model.DatasetDescription;
import software.amazon.awssdk.services.rekognition.model.DatasetStatus;
import software.amazon.awssdk.services.rekognition.model.DatasetType;
import software.amazon.awssdk.services.rekognition.model.DeleteDatasetRequest;
import software.amazon.awssdk.services.rekognition.model.DeleteDatasetResponse;
import software.amazon.awssdk.services.rekognition.model.DeleteProjectRequest;
import software.amazon.awssdk.services.rekognition.model.DeleteProjectResponse;
import software.amazon.awssdk.services.rekognition.model.DeleteProjectVersionRequest;
import software.amazon.awssdk.services.rekognition.model.DeleteProjectVersionResponse;
import software.amazon.awssdk.services.rekognition.model.DescribeDatasetRequest;
import software.amazon.awssdk.services.rekognition.model.DescribeDatasetResponse;
import software.amazon.awssdk.services.rekognition.model.DescribeProjectVersionsRequest;
import software.amazon.awssdk.services.rekognition.model.DescribeProjectVersionsResponse;
import software.amazon.awssdk.services.rekognition.model.DescribeProjectsRequest;
import software.amazon.awssdk.services.rekognition.model.DescribeProjectsResponse;
import software.amazon.awssdk.services.rekognition.model.DetectCustomLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectCustomLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.ListDatasetEntriesRequest;
import software.amazon.awssdk.services.rekognition.model.ListDatasetEntriesResponse;
import software.amazon.awssdk.services.rekognition.model.OutputConfig;
import software.amazon.awssdk.services.rekognition.model.ProjectVersionDescription;
import software.amazon.awssdk.services.rekognition.model.ProjectVersionStatus;
import software.amazon.awssdk.services.rekognition.model.RekognitionException;
import software.amazon.awssdk.services.rekognition.model.S3Object;
import software.amazon.awssdk.services.rekognition.model.StartProjectVersionRequest;
import software.amazon.awssdk.services.rekognition.model.StartProjectVersionResponse;
import software.amazon.awssdk.services.rekognition.model.StopProjectVersionRequest;
import software.amazon.awssdk.services.rekognition.model.StopProjectVersionResponse;
import software.amazon.awssdk.services.rekognition.model.UpdateDatasetEntriesRequest;
import software.amazon.awssdk.services.rekognition.model.UpdateDatasetEntriesResponse;

@Service
public class AwsRekognitionCustomModelService {
	
	@Autowired
	private AwsProperties properties;
	
	private RekognitionClient buildRekognitionClient() {
		Region clientRegion = Region.AP_NORTHEAST_1;
		
    	AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
    			properties.getAccesskey(),
    			properties.getSecretkey());
  	
    	RekognitionClient rekClient = RekognitionClient.builder()
      		.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
              .region(clientRegion)
              .build();
    	
    	return rekClient;
	}
	
	/***
	 * カスタムラベル状態確認
	 * @return
	 * @throws Exception
	 */
	public DescribeProjectVersionsResponse StatusCustomModel(String projectName, String versionName) throws Exception {
		DescribeProjectVersionsResponse result = null;
		
        try {
            RekognitionClient rekClient = buildRekognitionClient();
        	
            DescribeProjectVersionsRequest request = DescribeProjectVersionsRequest.builder()
        			.projectArn(projectName)
        			.versionNames(versionName)
                    .build();

            result = rekClient.describeProjectVersions(request);
            
        } catch (RekognitionException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
		return result;
	}
	
	/***
	 * カスタムラベルプロジェクト作成
	 * @return
	 * @throws Exception
	 */
	public CreateProjectResponse CreateCustomModelProject(String projectId) throws Exception {
		CreateProjectResponse result = null;
		
        try {
        	RekognitionClient rekClient = buildRekognitionClient();
	    	
	    	CreateProjectRequest request = CreateProjectRequest.builder()
	    			.projectName(projectId)
                    .build();
	    	
	    	result = rekClient.createProject(request);
	    	
        } catch (RekognitionException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
		return result;
	}
	
	/***
	 * カスタムラベルプロジェクト削除
	 * @param projectName
	 * @return
	 */
	public DeleteProjectResponse DeleteCustomModelProject(String projectName) {
		DeleteProjectResponse result = null;
		
        try {
        	RekognitionClient rekClient = buildRekognitionClient();
	    	
        	DeleteProjectRequest request = DeleteProjectRequest.builder()
	    			.projectArn(projectName)
                    .build();
	    	
	    	result = rekClient.deleteProject(request);
	    	
        } catch (RekognitionException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
		return result;
	}
	
	/***
	 * カスタムラベルデータセット作成(トレーニング用)
	 * @return
	 * @throws Exception
	 */
	public CreateDatasetResponse CreateCustomModelTrainingDataSet(String projectName) throws Exception {
		CreateDatasetResponse result = null;
		
        try {
        	RekognitionClient rekClient = buildRekognitionClient();

	    	CreateDatasetRequest request = CreateDatasetRequest.builder()
	    			.projectArn(projectName)
	    			.datasetType(DatasetType.TRAIN)
                    .build();
	    	
	    	result = rekClient.createDataset(request);
	    	
        } catch (RekognitionException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
		return result;
	}
	
	/***
	 * カスタムラベルデータセット作成(テスト用)
	 * @return
	 * @throws Exception
	 */
	public CreateDatasetResponse CreateCustomModelTestDataSet(String projectName) throws Exception {
		CreateDatasetResponse result = null;
		
        try {
        	RekognitionClient rekClient = buildRekognitionClient();

	    	CreateDatasetRequest request = CreateDatasetRequest.builder()
	    			.projectArn(projectName)
	    			.datasetType(DatasetType.TEST)
                    .build();
	    	
	    	result = rekClient.createDataset(request);
	    	
        } catch (RekognitionException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
		return result;
	}
	
	/***
	 * カスタムラベルデータセット削除
	 * @param dataSetName
	 * @return
	 * @throws Exception
	 */
	public DeleteDatasetResponse deleteCustomModelDataSet(String dataSetName) throws Exception {
		DeleteDatasetResponse result = null;
		
        try {
        	RekognitionClient rekClient = buildRekognitionClient();

        	DeleteDatasetRequest request = DeleteDatasetRequest.builder()
	    			.datasetArn(dataSetName)
                    .build();
	    	
	    	result = rekClient.deleteDataset(request);
	    	
        } catch (RekognitionException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
		return result;
	}
	
	/***
	 * カスタムラベルプロジェクト一覧取得
	 * @return
	 * @throws Exception
	 */
	public DescribeProjectsResponse GetCustomModelProject() throws Exception {
		DescribeProjectsResponse result = null;
		
        try {
        	RekognitionClient rekClient = buildRekognitionClient();
	    	
	    	DescribeProjectsRequest request = DescribeProjectsRequest.builder().build();
	    	
	    	result = rekClient.describeProjects(request);
	    	
        } catch (RekognitionException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
		return result;
	}
	
	/***
	 * カスタムデータセットの登録一覧を取得する
	 * @return
	 * @throws Exception
	 */
	public ListDatasetEntriesResponse GetListDatasetEntries(String dataSetName) throws Exception {
		ListDatasetEntriesResponse result = null;
		
        try {
        	RekognitionClient rekClient = buildRekognitionClient();
	    	
	    	ListDatasetEntriesRequest request = ListDatasetEntriesRequest.builder()
	    			.datasetArn(dataSetName)
	    			.build();

        	result = rekClient.listDatasetEntries(request);
        	
        } catch (RekognitionException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
		return result;
	}
	
	/***
	 * カスタムラベルデータセット更新
	 * @return
	 * @throws Exception
	 */
	public UpdateDatasetEntriesResponse UpdateDatasetEntries(String datasetArn, String updateFile) throws Exception {
		UpdateDatasetEntriesResponse result = null;
		
        try {
        	RekognitionClient rekClient = buildRekognitionClient();
	    	
        	// データ登録できる状態まで待つ
	    	boolean updatedStart = false;
            do {

                DescribeDatasetRequest describeDatasetRequest = DescribeDatasetRequest.builder()
                        .datasetArn(datasetArn).build();
                DescribeDatasetResponse describeDatasetResponse = rekClient.describeDataset(describeDatasetRequest);

                DatasetDescription datasetDescription = describeDatasetResponse.datasetDescription();

                DatasetStatus status = datasetDescription.status();
                
                switch (status) {
                	case CREATE_COMPLETE:
	                case UPDATE_COMPLETE:
	                	System.out.print("Dataset Updated Start\n");
	                	updatedStart = true;
	                    break;
	
	                case UPDATE_IN_PROGRESS:
	                    Thread.sleep(5000);
	                    break;
	
	                case UPDATE_FAILED:
	                    String error = "Dataset update failed: " + datasetDescription.statusAsString() + " "
	                            + datasetDescription.statusMessage() + " " + datasetArn;
	                    System.out.print(error);
	                    throw new Exception(error);
	
	                default:
	                    String unexpectedError = "Unexpected update state: " + datasetDescription.statusAsString() + " "
	                            + datasetDescription.statusMessage() + " " + datasetArn;
	                    System.out.print(unexpectedError);
	                    throw new Exception(unexpectedError);
                }

            } while (updatedStart == false);
        	
        	// データ登録処理
            InputStream sourceStream = new FileInputStream(updateFile);
            SdkBytes sourceBytes = SdkBytes.fromInputStream(sourceStream);
            
            DatasetChanges datasetChanges = DatasetChanges.builder()
                    .groundTruth(sourceBytes).build();
	    	
	    	UpdateDatasetEntriesRequest request = UpdateDatasetEntriesRequest.builder()
	    			.changes(datasetChanges)
	    			.datasetArn(datasetArn)
	    			.build();
	    	
	    	result = rekClient.updateDatasetEntries(request);
	    	
            // データ登録完了まで待つ
	    	boolean updated = false;
            do {

                DescribeDatasetRequest describeDatasetRequest = DescribeDatasetRequest.builder()
                        .datasetArn(datasetArn).build();
                DescribeDatasetResponse describeDatasetResponse = rekClient.describeDataset(describeDatasetRequest);

                DatasetDescription datasetDescription = describeDatasetResponse.datasetDescription();

                DatasetStatus status = datasetDescription.status();

                switch (status) {
	                case UPDATE_COMPLETE:
	                	System.out.print("Dataset Updated End\n");
	                    updated = true;
	                    break;
	
	                case UPDATE_IN_PROGRESS:
	                    Thread.sleep(5000);
	                    break;
	
	                case UPDATE_FAILED:
	                    String error = "Dataset update failed: " + datasetDescription.statusAsString() + " "
	                            + datasetDescription.statusMessage() + " " + datasetArn;
	                    System.out.print(error);
	                    throw new Exception(error);
	
	                default:
	                    String unexpectedError = "Unexpected update state: " + datasetDescription.statusAsString() + " "
	                            + datasetDescription.statusMessage() + " " + datasetArn;
	                    System.out.print(unexpectedError);
	                    throw new Exception(unexpectedError);
                }

            } while (updated == false);
	    	
        } catch (RekognitionException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
		return result;
	}
	
	/***
	 * カスタムモデルを作成
	 * @return
	 * @throws Exception
	 */
	public CreateProjectVersionResponse CreateProjectVersion(String projectName, String bucketName, String versionName) throws Exception {
		CreateProjectVersionResponse result = null;
		
        try {
        	RekognitionClient rekClient = buildRekognitionClient();
	    	
	    	OutputConfig outputConfig = OutputConfig.builder()
	    			.s3Bucket(bucketName)
	    			.s3KeyPrefix("")
	    			.build();
	    	
	    	CreateProjectVersionRequest request = CreateProjectVersionRequest.builder()
	    			.projectArn(projectName)
	    			.versionName(versionName)
	    			.outputConfig(outputConfig)
	    			.build();

        	result = rekClient.createProjectVersion(request);
        	
        	boolean checkStatus = false;        	
        	do {
	        	DescribeProjectVersionsRequest describeProjectVersionsRequest = DescribeProjectVersionsRequest.builder()
	        			.projectArn(projectName)
	        			.versionNames(versionName)
	                    .build();
	        	
	        	DescribeProjectVersionsResponse describeProjectVersionsResponse = rekClient.describeProjectVersions(describeProjectVersionsRequest);
	        	ProjectVersionStatus status = describeProjectVersionsResponse.projectVersionDescriptions().get(0).status();
	        	
	        	switch(status) {
	        	case TRAINING_COMPLETED:
	        		checkStatus = true;
	        		break;
				default:
            		// １分待つ
            		Thread.sleep(60000);
					checkStatus = false;
					break;
	        	}
        	} while (checkStatus == false);

        } catch (RekognitionException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
		return result;
	}
	
	/***
	 * カスタムモデルを全削除
	 * @return
	 * @throws Exception
	 */
	public boolean deleteProjectAllVersion(String projectName) throws Exception {
		boolean result = false;
		
        try {
        	RekognitionClient rekClient = buildRekognitionClient();
	    	
        	DescribeProjectVersionsRequest describeProjectVersionsRequest = DescribeProjectVersionsRequest.builder()
        			.projectArn(projectName)
                    .build();

        	DescribeProjectVersionsResponse describeProjectVersionsResponse = rekClient.describeProjectVersions(describeProjectVersionsRequest);
            List<ProjectVersionDescription> versionDescriptions = describeProjectVersionsResponse.projectVersionDescriptions();
            for (ProjectVersionDescription version : versionDescriptions) {
            	boolean checkStatus = false;
            	String versionNameArn = version.projectVersionArn();
            	String[] splitVersionNameArn = versionNameArn.split("/");
            	String versionName = splitVersionNameArn[3];
            	
                do {
                    describeProjectVersionsRequest = DescribeProjectVersionsRequest.builder()
                			.projectArn(projectName)
                			.versionNames(versionName)
                            .build();
                	
                    describeProjectVersionsResponse = rekClient.describeProjectVersions(describeProjectVersionsRequest);
                    
                    if(describeProjectVersionsResponse.projectVersionDescriptions().size() > 0) {
	                    ProjectVersionStatus status = describeProjectVersionsResponse.projectVersionDescriptions().get(0).status();
	                    
		            	switch(status) {
		            	case TRAINING_IN_PROGRESS:
		            	case STARTING:
		            	case STOPPING:
		            		// １分待つ
		            		Thread.sleep(60000);
		            		checkStatus = false;
		            		break;
		            	case RUNNING:
		            		// カスタムラベル停止
		            		StopCustomModel(versionName);
		            		break;
		            	case UNKNOWN_TO_SDK_VERSION:
		                    String unexpectedError = "Unexpected status: " + status + " " + versionName;
		                    System.out.print(unexpectedError);
		                    throw new Exception(unexpectedError);
						default:
							checkStatus = true;
							break;
		            		
		            	}
                    } else {
						checkStatus = true;
						break;
                    }
                } while (checkStatus == false);
            	
            	
            	DeleteProjectVersionRequest request = DeleteProjectVersionRequest.builder()
    	    			.projectVersionArn(versionNameArn)
    	    			.build();
            	
            	DeleteProjectVersionResponse deleteProjectVersionResponse = rekClient.deleteProjectVersion(request);
                do {
                    describeProjectVersionsRequest = DescribeProjectVersionsRequest.builder()
                			.projectArn(projectName)
                			.versionNames(versionName)
                            .build();
                	
                    describeProjectVersionsResponse = rekClient.describeProjectVersions(describeProjectVersionsRequest);
                    
                    if(describeProjectVersionsResponse.projectVersionDescriptions().size() > 0) {
	                    ProjectVersionStatus status = describeProjectVersionsResponse.projectVersionDescriptions().get(0).status();
	                    
		            	switch(status) {
		            	case DELETING:
		            		// １秒待つ
		            		Thread.sleep(1000);
		            		checkStatus = false;
		            		break;
		            	case UNKNOWN_TO_SDK_VERSION:
							checkStatus = true;
							break;
						default:
							checkStatus = true;
							break;
		            		
		            	}
                    } else {
						checkStatus = true;
						break;
                    }
                } while (checkStatus == false);
            }
        	
        } catch (RekognitionException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
		
		return result;
	}
	
	/***
	 * 過去のカスタムモデルを削除
	 * @return
	 * @throws Exception
	 */
	public boolean deletePostProjectVersion(String projectName, String currentVersionNameArn) throws Exception {
		boolean result = false;
		
        try {
        	RekognitionClient rekClient = buildRekognitionClient();
	    	
        	DescribeProjectVersionsRequest describeProjectVersionsRequest = DescribeProjectVersionsRequest.builder()
        			.projectArn(projectName)
                    .build();

        	DescribeProjectVersionsResponse describeProjectVersionsResponse = rekClient.describeProjectVersions(describeProjectVersionsRequest);
            List<ProjectVersionDescription> versionDescriptions = describeProjectVersionsResponse.projectVersionDescriptions();
            for (ProjectVersionDescription version : versionDescriptions) {
            	boolean checkStatus = false;
            	String versionNameArn = version.projectVersionArn();
            	String[] splitVersionNameArn = versionNameArn.split("/");
            	String versionName = splitVersionNameArn[3];
            	
            	if(versionName.equals(currentVersionNameArn)) {
            		continue;
            	}
            	
                do {
                    describeProjectVersionsRequest = DescribeProjectVersionsRequest.builder()
                			.projectArn(projectName)
                			.versionNames(versionName)
                            .build();
                	
                    describeProjectVersionsResponse = rekClient.describeProjectVersions(describeProjectVersionsRequest);
                    
                    if(describeProjectVersionsResponse.projectVersionDescriptions().size() > 0) {
	                    ProjectVersionStatus status = describeProjectVersionsResponse.projectVersionDescriptions().get(0).status();
	                    
		            	switch(status) {
		            	case TRAINING_IN_PROGRESS:
		            	case STARTING:
		            	case STOPPING:
		            		// １分待つ
		            		Thread.sleep(60000);
		            		checkStatus = false;
		            		break;
		            	case RUNNING:
		            		// カスタムラベル停止
		            		StopCustomModel(versionName);
		            		break;
		            	case UNKNOWN_TO_SDK_VERSION:
		                    String unexpectedError = "Unexpected status: " + status + " " + versionName;
		                    System.out.print(unexpectedError);
		                    throw new Exception(unexpectedError);
						default:
							checkStatus = true;
							break;
		            		
		            	}
                    } else {
						checkStatus = true;
						break;
                    }
                } while (checkStatus == false);
            	
            	
            	DeleteProjectVersionRequest request = DeleteProjectVersionRequest.builder()
    	    			.projectVersionArn(versionNameArn)
    	    			.build();
            	
            	DeleteProjectVersionResponse deleteProjectVersionResponse = rekClient.deleteProjectVersion(request);
                do {
                    describeProjectVersionsRequest = DescribeProjectVersionsRequest.builder()
                			.projectArn(projectName)
                			.versionNames(versionName)
                            .build();
                	
                    describeProjectVersionsResponse = rekClient.describeProjectVersions(describeProjectVersionsRequest);
                    
                    if(describeProjectVersionsResponse.projectVersionDescriptions().size() > 0) {
	                    ProjectVersionStatus status = describeProjectVersionsResponse.projectVersionDescriptions().get(0).status();
	                    
		            	switch(status) {
		            	case DELETING:
		            		// １秒待つ
		            		Thread.sleep(1000);
		            		checkStatus = false;
		            		break;
		            	case UNKNOWN_TO_SDK_VERSION:
							checkStatus = true;
							break;
						default:
							checkStatus = true;
							break;
		            		
		            	}
                    } else {
						checkStatus = true;
						break;
                    }
                } while (checkStatus == false);
            }
        	
        } catch (RekognitionException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
		
		return result;
	}
	
	
	/***
	 * カスタムラベル開始
	 * @return
	 * @throws Exception
	 */
	public StartProjectVersionResponse StartCustomModel(String projectVersionArn) throws Exception {
		StartProjectVersionResponse result = null;
        try {
        	RekognitionClient rekClient = buildRekognitionClient();
	    	
        	StartProjectVersionRequest request = StartProjectVersionRequest.builder()
	    			.projectVersionArn(projectVersionArn)
	    			.minInferenceUnits(1)
	    			.build();

        	result = rekClient.startProjectVersion(request);
        	
        } catch (RekognitionException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
		return result;
	}
	
	/***
	 * カスタムラベル停止
	 * @return
	 * @throws Exception
	 */
	public StopProjectVersionResponse StopCustomModel(String projectVersionArn) throws Exception {
		StopProjectVersionResponse result = null;
        
        try {
        	RekognitionClient rekClient = buildRekognitionClient();
        	
        	StopProjectVersionRequest request = StopProjectVersionRequest.builder()
        			.projectVersionArn(projectVersionArn)
        			.build();
        			
        	result = rekClient.stopProjectVersion(request);
        	
        } catch (RekognitionException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        
		return result;
	}
	
	/***
	 * 画像解析実施
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
    public List<CustomLabel> CustomModel(String projectVersionArn, String bucketName, String fileName) throws Exception {
    	List<CustomLabel> result = null;

        try {
        	RekognitionClient rekClient = buildRekognitionClient();
        	
            S3Object s3Object = S3Object.builder()
                    .bucket(bucketName)
                    .name(fileName)
                    .build();
            
            // Create an Image object for the source image
            Image s3Image = Image.builder()
                    .s3Object(s3Object)
                    .build();
        	
        	
            DetectCustomLabelsRequest request = DetectCustomLabelsRequest.builder()
            		.image(s3Image)
                    .projectVersionArn(projectVersionArn)
                    .build();
        	
        	DetectCustomLabelsResponse response = rekClient.detectCustomLabels(request);
        	result = response.customLabels();
            
        } catch (RekognitionException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
        
        return result;
    }
    
	/***
	 * データ一覧取得
	 * @param datasetArn
	 * @return
	 * @throws Exception
	 */
    public ListDatasetEntriesResponse getListDatasetEntries(String datasetArn) throws Exception {
    	ListDatasetEntriesResponse result = null;
    	try {
        	RekognitionClient rekClient = buildRekognitionClient();
        	ListDatasetEntriesRequest request = ListDatasetEntriesRequest.builder().datasetArn(datasetArn).build();
        	result = rekClient.listDatasetEntries(request);
        } catch (RekognitionException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    	
    	return result;
    }
}
