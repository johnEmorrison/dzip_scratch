import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import net.jzran.*;

public class DZip {
    File gzFile;
    int SPAN = 1048576;
    SeekableInputStream sis;
    RandomAccessGZip.Index index;

    DZip(File fName) throws FileNotFoundException, IOException, ClassNotFoundException{
         gzFile = fName;
        //int SPAN = 1048576;
        sis = new FileSeekableInputStream(new RandomAccessFile(gzFile, "r"));
        index = RandomAccessGZip.index(sis, SPAN);
        ByteArrayOutputStream indexData = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(indexData);
        
        oos.writeObject(index);
        oos.close();
        indexData.close();
        byte[] indexBytes = indexData.toByteArray();
        
        index = (RandomAccessGZip.Index)new ObjectInputStream(new ByteArrayInputStream(indexBytes)).readObject();
        index.open(sis);
    }

    public void seek(int offset) throws IOException{
        index.seek(offset);
    }

    public int read(byte[] dest, int offset, int length) throws IOException{
        return index.read(dest, offset, length);
    }

    public int read() throws IOException{
        return index.read();
    }

    public void close()throws IOException{
        sis.close();
        index.close(); 
    }

}
