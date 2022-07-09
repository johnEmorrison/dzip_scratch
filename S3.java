import java.io.File;
import java.io.InputStream;
import java.util.List;

import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

class S3{ 
    
    AmazonS3 s3;
    String bucketName;

    S3(Regions region, String bName)throws BucketDoesNotExistException{
        s3 = AmazonS3ClientBuilder.standard().withRegion(region).build();
        bucketName = bName;
    }

    public List<Bucket> ListBuckets(){
        return s3.listBuckets();
    }

    public List<S3ObjectSummary>listObjects(){
        ObjectListing objListing = s3.listObjects(bucketName);
        return objListing.getObjectSummaries();
    }

    public void printObjects(){
        List<S3ObjectSummary> objects = listObjects();
        for(S3ObjectSummary os: objects){
            System.out.println(os.getKey());            
        }
    }

    public void UploadObj(File f){
        s3.putObject(bucketName, f.getName(), f);
    }

    public InputStream DownloadObj(String key){
        S3Object obj = s3.getObject(new GetObjectRequest(bucketName, key) );
        InputStream in = obj.getObjectContent();
        return in;
    }

    public void DeleteObj(String key)throws SdkClientException{
        s3.deleteObject(bucketName, key);
    }

    class BucketDoesNotExistException extends Exception{
        BucketDoesNotExistException(String errorMessage){
            super(errorMessage);
        }
    }

}


