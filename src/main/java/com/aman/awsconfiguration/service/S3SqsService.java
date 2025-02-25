package com.aman.awsconfiguration.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import software.amazon.awssdk.core.sync.RequestBody;
import java.io.IOException;

@Service
public class S3SqsService {

    private final S3Client s3Client;
    private final SqsClient sqsClient;

    @Value("${s3.bucket-name}")
    private String bucketName;

    @Value("${sqs.queue-name}")
    private String queueName;

    public S3SqsService(S3Client s3Client, SqsClient sqsClient) {
        this.s3Client = s3Client;
        this.sqsClient = sqsClient;
    }



public String uploadFile(MultipartFile file) throws IOException {
    // Upload file to S3
    String fileName = file.getOriginalFilename();
    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .build();

    PutObjectResponse response = s3Client.putObject(
            putObjectRequest,
            RequestBody.fromInputStream(file.getInputStream(), file.getSize())
    );

    // Send message to SQS
    String messageBody = "File uploaded: " + fileName;
    SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
            .queueUrl(queueName)
            .messageBody(messageBody)
            .build();

    SendMessageResponse sqsResponse = sqsClient.sendMessage(sendMessageRequest);

    return String.format(
            "File uploaded successfully. Message ID: %s, MD5 Of Message Body: %s",
            sqsResponse.messageId(),
            sqsResponse.md5OfMessageBody()
    );
}
}

