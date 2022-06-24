import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class Main{
    public static void main(String[] args) { 
        String fileName = "testing.tar.gz";

        
        try{
            FileInputStream fclone = new FileInputStream(fileName);
            int len = fclone.readAllBytes().length;
            DZip dzip = new DZip(new FileInputStream(fileName), len);
            dzip.readHeader();
            byte[] buf = new byte[len];
            dzip.read(buf);
            System.out.println(buf.length);
            for(int i =0;i < buf.length; i++){
                System.out.print(buf[i] + "  ");
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        //gzip(fileName);
    
    }


    public static void gzip(String fName){
        try{
            GZIPInputStream gin = new GZIPInputStream(new FileInputStream(fName));
            int b;
            while((b = gin.read())!=-1){
                System.out.print((char)b);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }

}