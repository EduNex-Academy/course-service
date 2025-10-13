package org.edunex.courseservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3Service s3Service;

    private final String bucketName = "test-bucket";
    private final String cloudfrontDomain = "test-distribution.cloudfront.net";
    private final Long testModuleId = 1L;
    private final Long testCourseId = 1L;
    private MultipartFile testFile;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3Service, "bucketName", bucketName);
        ReflectionTestUtils.setField(s3Service, "cloudfrontDomain", cloudfrontDomain);
        
        // Create a test file
        byte[] content = "test file content".getBytes(StandardCharsets.UTF_8);
        testFile = new MockMultipartFile(
            "test-file", 
            "test.pdf", 
            "application/pdf", 
            content
        );
    }

    @Test
    @DisplayName("Should upload file successfully and return object key")
    void uploadFile_shouldUploadFileAndReturnKey() throws IOException {
        // Act
        String objectKey = s3Service.uploadFile(testFile, testModuleId);

        // Assert
        assertThat(objectKey).isNotNull();
        assertThat(objectKey).startsWith("module-" + testModuleId + "/");
        assertThat(objectKey).endsWith(".pdf");
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("Should throw exception when upload fails")
    void uploadFile_whenS3ClientThrowsException_shouldThrowException() throws IOException {
        // Arrange
        doThrow(S3Exception.builder().build())
            .when(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> s3Service.uploadFile(testFile, testModuleId));
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("Should download file successfully and return response entity")
    void downloadFile_shouldReturnResponseEntity() {
        // Arrange
        String testObjectKey = "module-1/test-file.pdf";
        byte[] fileContent = "test file content".getBytes(StandardCharsets.UTF_8);
        
        // Mock S3 response
        GetObjectResponse objectResponse = GetObjectResponse.builder()
            .contentType("application/pdf")
            .build();
        
        AbortableInputStream inputStream = AbortableInputStream.create(
            new ByteArrayInputStream(fileContent)
        );
        
        ResponseInputStream<GetObjectResponse> responseInputStream = 
            new ResponseInputStream<>(objectResponse, inputStream);
        
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseInputStream);

        // Act
        ResponseEntity<InputStreamResource> response = s3Service.downloadFile(testObjectKey);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType().toString()).contains("application/pdf");
        verify(s3Client).getObject(any(GetObjectRequest.class));
    }

    @Test
    @DisplayName("Should throw 404 when file not found")
    void downloadFile_whenFileNotFound_shouldThrow404() {
        // Arrange
        String testObjectKey = "module-1/non-existent.pdf";
        
        S3Exception notFoundException = (S3Exception) S3Exception.builder()
            .statusCode(404)
            .build();
            
        when(s3Client.getObject(any(GetObjectRequest.class))).thenThrow(notFoundException);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, 
            () -> s3Service.downloadFile(testObjectKey));
        
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(s3Client).getObject(any(GetObjectRequest.class));
    }

    @Test
    @DisplayName("Should delete file successfully")
    void deleteFile_shouldDeleteFileFromS3() {
        // Arrange
        String testObjectKey = "module-1/test-file.pdf";
        DeleteObjectResponse mockResponse = DeleteObjectResponse.builder().build();
        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(mockResponse);

        // Act
        s3Service.deleteFile(testObjectKey);

        // Assert
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("Should throw exception when delete fails")
    void deleteFile_whenS3ClientThrowsException_shouldThrowException() {
        // Arrange
        String testObjectKey = "module-1/test-file.pdf";
        doThrow(S3Exception.builder().build())
            .when(s3Client).deleteObject(any(DeleteObjectRequest.class));

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> s3Service.deleteFile(testObjectKey));
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("Should return true when file exists")
    void doesFileExist_whenFileExists_shouldReturnTrue() {
        // Arrange
        String testObjectKey = "module-1/test-file.pdf";
        HeadObjectResponse headObjectResponse = HeadObjectResponse.builder().build();
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenReturn(headObjectResponse);

        // Act
        boolean result = s3Service.doesFileExist(testObjectKey);

        // Assert
        assertThat(result).isTrue();
        verify(s3Client).headObject(any(HeadObjectRequest.class));
    }

    @Test
    @DisplayName("Should return false when file does not exist")
    void doesFileExist_whenFileDoesNotExist_shouldReturnFalse() {
        // Arrange
        String testObjectKey = "module-1/non-existent.pdf";
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenThrow(NoSuchKeyException.class);

        // Act
        boolean result = s3Service.doesFileExist(testObjectKey);

        // Assert
        assertThat(result).isFalse();
        verify(s3Client).headObject(any(HeadObjectRequest.class));
    }

    @Test
    @DisplayName("Should get CloudFront URL for valid object key")
    void getCloudFrontUrl_shouldReturnUrl() {
        // Arrange
        String testObjectKey = "module-1/test-file.pdf";
        String expectedUrl = "https://" + cloudfrontDomain + "/" + testObjectKey;

        // Act
        String url = s3Service.getCloudFrontUrl(testObjectKey);

        // Assert
        assertThat(url).isEqualTo(expectedUrl);
    }

    @Test
    @DisplayName("Should return null for empty object key")
    void getCloudFrontUrl_whenObjectKeyEmpty_shouldReturnNull() {
        // Act
        String url = s3Service.getCloudFrontUrl("");

        // Assert
        assertThat(url).isNull();
    }

    @Test
    @DisplayName("Should upload course thumbnail successfully")
    void uploadCourseThumbnail_shouldUploadAndReturnKey() throws IOException {
        // Arrange
        MockMultipartFile thumbnailFile = new MockMultipartFile(
            "thumbnail", 
            "thumbnail.jpg", 
            "image/jpeg", 
            "test image content".getBytes()
        );

        // Act
        String objectKey = s3Service.uploadCourseThumbnail(thumbnailFile, testCourseId);

        // Assert
        assertThat(objectKey).isNotNull();
        assertThat(objectKey).startsWith("course-thumbnails/" + testCourseId);
        assertThat(objectKey).contains(".jpeg");
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("Should throw exception when thumbnail is not an image")
    void uploadCourseThumbnail_whenNotImage_shouldThrowException() {
        // Arrange
        MockMultipartFile nonImageFile = new MockMultipartFile(
            "file", 
            "document.pdf", 
            "application/pdf", 
            "test content".getBytes()
        );

        // Act & Assert
        assertThrows(ResponseStatusException.class, 
            () -> s3Service.uploadCourseThumbnail(nonImageFile, testCourseId));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
}