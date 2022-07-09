import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;

public class TarReader {

    byte[] uncompressedBytes = new byte[20000];
    int fLength = 0;
    final int HEADER_SIZE = 512;
    public ArrayList<OffsetData> offsets;

    TarReader(File file){
        //TarInputStream tin = new TarInputStream();
        try{
            unzip(new FileInputStream(file));
            createOffsets();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    TarReader(InputStream in){
        try{
            unzip(in);
            createOffsets();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private void createOffsets()throws IOException{
        TarInputStream tin = new TarInputStream(new ByteArrayInputStream(uncompressedBytes));
        TarEntry entry;
        offsets = new ArrayList<OffsetData>();

        int currentOffset = 0;
        while((entry = tin.getNextEntry())!= null){
            OffsetData o = new OffsetData();
            o.setFileName(entry.getName());
            o.setStartOffset(currentOffset);
            
            currentOffset += HEADER_SIZE;
            o.setContentOffset(currentOffset);
            o.setContentLength(currentOffset + (int)entry.getSize());
            //round to nearest 512th byte
            currentOffset  = round((int)(currentOffset + entry.getSize()));
            o.setEof(currentOffset);

            offsets.add(o);
        }
    }

    //Rounds to next 512th int
    public int round(int n) {        
        int r = ((n/512) * 512);
        return (n > r)? r + 512: r;
    }

    private void unzip(InputStream fin) throws IOException{
        GZIPInputStream gzin = new GZIPInputStream(fin); 
        fLength = gzin.read(uncompressedBytes);
        gzin.close();
    }

    class OffsetData{
        String fileName;
        int startOffset;
        int contentOffset;
        int contentLength;
        int eof;

        OffsetData(){
            fileName = "";
            startOffset = -1;
            contentOffset = -1;
            contentLength = -1;
            eof = -1;
        }

        OffsetData(String f){
            fileName = f;
            startOffset = -1;
            contentOffset = -1;
            contentLength = -1;
            eof = -1;
        }

        OffsetData(String f, int sOffset, int cOffset, int lenOffset, int e){
            fileName = f;
            startOffset = sOffset;
            contentOffset = cOffset;
            contentLength = lenOffset;
            eof = e;
        }

        public void print(){
            System.out.print(fileName + ": ");
            System.out.print("start = " + startOffset);
            System.out.print(" contentOffset = " + contentOffset);
            System.out.print(" contentLength = " + contentLength );
            System.out.print(" EOF = " + eof);
            System.out.println();
        }
        

        


        public String getFileName(){
            return fileName;
        }

        public int getStartOffset(){
            return startOffset;
        }

        public int getContentOffset(){
            return contentOffset;
        }

        public int getContentLength(){
            return contentLength;
        }

        public int getEof(){
            return eof;
        }
        
        public void setFileName(String f){
            fileName = f;
        }

        public void setStartOffset(int offset){
            startOffset = offset;
        }

        public void setContentOffset(int offset){
            contentOffset = offset;
        }

        public void setContentLength(int l){
             contentLength = l;
        }
        public void setEof(int e){
            eof = e;
        }

    }
    
}
