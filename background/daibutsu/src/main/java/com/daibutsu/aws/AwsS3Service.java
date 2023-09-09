package com.daibutsu.aws;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.waiters.S3Waiter;

@Service
public class AwsS3Service {
	
	@Autowired
	private AwsProperties properties;
	
    // Return a byte array
    private static byte[] getObjectFile(String filePath) {

        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;

        try {
            File file = new File(filePath);
            bytesArray = new byte[(int) file.length()];
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytesArray);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bytesArray;
    }
	
    public WaiterResponse<HeadBucketResponse> createBucket(String bucketName) throws IOException {
		Region clientRegion = Region.AP_NORTHEAST_1;
		WaiterResponse<HeadBucketResponse> result = null;
		
    	try {
        	AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
        			properties.getAccesskey(),
        			properties.getSecretkey());
        	
        	S3Client s3Client = S3Client.builder()
    				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
    				.region(clientRegion)
    				.build();
        	
            S3Waiter s3Waiter = s3Client.waiter();
            
            
            ListBucketsResponse listBucketsResponse = s3Client.listBuckets();
            for(Bucket bucket : listBucketsResponse.buckets()) {
            	if(bucketName.equals(bucket.name())) {
            		return null;
            	}
            }
            
            CreateBucketRequest bucketRequest = CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            s3Client.createBucket(bucketRequest);
            HeadBucketRequest bucketRequestWait = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            // Wait until the bucket is created and print out the response.
            WaiterResponse<HeadBucketResponse> waiterResponse = s3Waiter.waitUntilBucketExists(bucketRequestWait);
            waiterResponse.matched().response().ifPresent(System.out::println);
            System.out.println(bucketName +" is ready");
        	
            result = waiterResponse;
            
        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    	
    	return result;
    }
    
	public PutObjectResponse UploadObject(String bucketName, String filePath) throws IOException {
		PutObjectResponse result = null;
		
		Region clientRegion = Region.AP_NORTHEAST_1;
		
        try {
        	String fileName = new File(filePath).getName();
        	
        	AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
        			properties.getAccesskey(),
        			properties.getSecretkey());

        	S3Client s3Client = S3Client.builder()
        				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
        				.region(clientRegion)
        				.build();
            
        	PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();
            
            result = s3Client.putObject(putObjectRequest, RequestBody.fromBytes(getObjectFile(filePath)));
            
        } catch (S3Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
		return result;
	}
    
	public PutObjectResponse UploadObject() throws IOException {
		PutObjectResponse result = null;
		String bucketName = "rekotest1";
		
		Region clientRegion = Region.AP_NORTHEAST_1;
		
        try {
        	// ファイル名
        	String fileName = "C:\\Users\\mori\\Desktop\\仏像AI開発\\東大寺大仏\\NaraDaibutu1.jpg";
        	
        	AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
        			properties.getAccesskey(),
        			properties.getSecretkey());

        	S3Client s3Client = S3Client.builder()
        				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
        				.region(clientRegion)
        				.build();
            
        	PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key("test.jpg")
                    .build();
            
            result = s3Client.putObject(putObjectRequest, RequestBody.fromBytes(getObjectFile(fileName)));
            
        } catch (S3Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
		return result;
	}
	
	public PutObjectResponse UploadObject(File uploadFile) throws IOException {
		PutObjectResponse result = null;
		String bucketName = "rekotest1";
		
		Region clientRegion = Region.AP_NORTHEAST_1;
		
        try {        	
        	AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
        			properties.getAccesskey(),
        			properties.getSecretkey());

        	S3Client s3Client = S3Client.builder()
        				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
        				.region(clientRegion)
        				.build();
            
        	
        	
        	PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(uploadFile.getName())
                    .build();
            
            result = s3Client.putObject(putObjectRequest, RequestBody.fromFile(uploadFile));
            
        } catch (S3Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
		return result;
	}

	public PutObjectResponse UploadObject(File uploadFile, String bucketName) {
		PutObjectResponse result = null;
		
		Region clientRegion = Region.AP_NORTHEAST_1;
		
        try {        	
        	AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
        			properties.getAccesskey(),
        			properties.getSecretkey());

        	S3Client s3Client = S3Client.builder()
        				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
        				.region(clientRegion)
        				.build();
            
        	PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(uploadFile.getName())
                    .build();
            
            result = s3Client.putObject(putObjectRequest, RequestBody.fromFile(uploadFile));
            
        } catch (S3Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        
		return result;
	}

	public DeleteBucketResponse deleteBucket(String bucketName) {
		Region clientRegion = Region.AP_NORTHEAST_1;
		DeleteBucketResponse result = null;
		
    	try {
        	AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
        			properties.getAccesskey(),
        			properties.getSecretkey());
        	
        	S3Client s3Client = S3Client.builder()
    				.credentialsProvider(StaticCredentialsProvider.create(awsCreds))
    				.region(clientRegion)
    				.build();
        	
            ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder().bucket(bucketName).build();
            ListObjectsV2Response listObjectsV2Response;

            do {
                listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request);
                for (S3Object s3Object : listObjectsV2Response.contents()) {
                	s3Client.deleteObject(DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(s3Object.key())
                            .build());
                }

                listObjectsV2Request = ListObjectsV2Request.builder().bucket(bucketName)
                        .continuationToken(listObjectsV2Response.nextContinuationToken())
                        .build();

            } while(listObjectsV2Response.isTruncated());
        	
            DeleteBucketRequest bucketRequest = DeleteBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            
            result = s3Client.deleteBucket(bucketRequest);
            
        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    	
    	return result;
	}
}
