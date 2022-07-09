
import com.amazonaws.regions.Regions;

import java.io.File;
import java.io.InputStream;




public class Main{
    public static void main(String[] args) { 
        String fileName = "./resources/Dzip.tar.gz";
        String key = "Dzip.tar.gz";
        String bucketName = "a-test-bucket-0001";

        try{
            
            //Creates new S3 client
            S3 s3 = new S3( Regions.US_EAST_1, bucketName );
            
            //Upload file to bucket for testing 
            //s3.UploadObj(new File(fileName));

            //Downloads s3 Object and returns as InputStream to be fed into TarReader
            InputStream s3In = s3.DownloadObj(key);

            //Unzips tar.gz file to take note of specific file offsets to look up
            TarReader tr = new TarReader(s3In);
            TarReader.OffsetData offset = tr.offsets.get(3);

            DZip dzip = new DZip(new File(fileName));
            
            //the offset within the tar to find while compressed
            dzip.seek( offset.getContentOffset() );
            
            byte[] dest = new byte[20000];

            //reads to the length of eof(end of file within tar) minus the offset given to seek
            //so it dosent read past eof
            int n = dzip.read(dest, 0, offset.getEof() - offset.getContentOffset());
            
            for(byte b : dest){
                
                //Removes padding
                if(b == 0)continue;

                System.out.print((char)b);
            }

        }catch(Exception e){
            e.printStackTrace();
        }     
    
    }

}