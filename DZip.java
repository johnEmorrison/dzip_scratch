import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

public class DZip{
    
    public final static int GZIP_MAGIC = 0x8b1f;

    private final static int FTEXT      = 1;    // Extra text
    private final static int FHCRC      = 2;    // Header CRC
    private final static int FEXTRA     = 4;    // Extra field
    private final static int FNAME      = 8;    // File name
    private final static int FCOMMENT   = 16;   // File comment
    
    CRC32 crc = new CRC32();
    InputStream this_in;
    Inflater inf;
    byte[] buf;
    InflaterInputStream infS;
    int len;

    public DZip(InputStream in, int length) throws IOException {
        this_in = in;
        inf = new Inflater(true);
        infS = new InflaterInputStream(this_in, new Inflater(true), length);
        buf = new byte[length];

    }

    public void read(byte b[])throws IOException{
      int n;
      try{
            while((n = inf.inflate(b)) == 0){
                if(n == -1){

                }else{
                    fill();
                }
            }
        }catch(DataFormatException e){
            e.printStackTrace();
        }
    }

    private void fill() throws IOException{
         this.len = this_in.read(buf, 0, buf.length);
        
        //System.out.println(buf.length);
        for(int i= 0; i < buf.length; i++){
            //System.out.print(buf[i] + "  ");
        }

        if (this.len == -1){
            throw new EOFException("Unexpected end of zlib input stream");
        }
        inf.setInput(buf, 0, buf.length);

    }

    public int readHeader() throws IOException{        
            CheckedInputStream crc_in = new CheckedInputStream(this_in, this.crc);
            crc.reset();
            
            if(readUShort(crc_in) == GZIP_MAGIC){
                System.out.println("Magic");
            }
            //Checks if valid compression
            if(readUByte(crc_in) == 8){
                System.out.println("Compression supported");
            }
            int flg = readUByte(crc_in);
            skipBytes(crc_in, 6);
            //Amount of bytes read so far 
            //GZIP_MAGIC = 2 
            //Valid Compression = 1 byte
            //flg = 1 byte
            int headerSize = 2 + 2 + 6;
            
            //skip optional extra field
            if((flg & FEXTRA) == FEXTRA){
                int m = readUShort(crc_in);
                skipBytes(crc_in, m);
                headerSize += m + 2;
                System.out.println("Skipping optional extra field");
            }

            if((flg & FNAME) == FNAME){
                int cByte = 0;
                do{
                    
                    headerSize++;
                    
                }while(readUByte(crc_in) != 0);
            }

            if((flg & FCOMMENT) == FCOMMENT){
                do{
                    headerSize++;
                }while(readUInt(crc_in) != 0);
            }

            if ((flg & FHCRC) == FHCRC) {
                int v = (int)crc.getValue() & 0xffff;
                if (readUShort(crc_in) != v) {
                    throw new ZipException("Corrupt GZIP header");
                }
                headerSize += 2;
            }

            crc.reset();

            System.out.println("HeaderSize: " + headerSize);
            //readToEnd(crc_in);

        return headerSize;
    }

    private boolean readTrailer() throws IOException {
        InputStream in = this_in;
        int n = inf.getRemaining();
        if (n > 0) {
            in = new SequenceInputStream(
                        new ByteArrayInputStream(buf, len - n, n), in);
        }
        // Uses left-to-right evaluation order
        if ((readUInt(in) != crc.getValue()) ||
            // rfc1952; ISIZE is the input size modulo 2^32
            (readUInt(in) != (inf.getBytesWritten() & 0xffffffffL)))
            throw new ZipException("Corrupt GZIP trailer");

        // If there are more bytes available in "in" or
        // the leftover in the "inf" is > 26 bytes:
        // this.trailer(8) + next.header.min(10) + next.trailer(8)
        // try concatenated case
        if (this_in.available() > 0 || n > 26) {
            int m = 8;                  // this.trailer
            try {
                m += readHeader();    // next.header
            } catch (IOException ze) {
                return true;  // ignore any malformed, do nothing
            }
            inf.reset();
            if (n > m)
                inf.setInput(buf, len - n + m, n - m);
            return false;
        }
        return true;
    }

    private void readToEnd(InputStream in) throws IOException{
        int cByte = 0;
        while((cByte = readUByte(in)) != -1){  
            System.out.print("Byte: " + cByte);
            System.out.println("   Char: " + (char)cByte);
        }
    }


    private long readUInt(InputStream in) throws IOException {
        long s = readUShort(in);
        return ((long)readUShort(in) << 16) | s;
    }

    /*
     * Reads unsigned short in Intel byte order.
     */
    private int readUShort(InputStream in) throws IOException {
        int b = readUByte(in);
        return ((int)readUByte(in) << 8) | b;
    }

    /*
     * Reads unsigned byte.
     */
    private int readUByte(InputStream in) throws IOException {
        int b = in.read();
        if (b == -1) {
            throw new EOFException();
        }
        if (b < -1 || b > 255) {
            // Report on this.in, not argument in; see read{Header, Trailer}.
            /*throw new IOException(in.getClass().getName()
                + ".read() returned value out of range -1..255: " + b);*/
            throw new IOException(".read() return value out of range -1..255: " + b);
            
        }
        return b;
    }

    private void skipBytes(InputStream in, int len) throws IOException{
        while(len > 0){
            int n = in.read();
            if(n == -1)
                throw new EOFException();
            len -= n;
        }
    }

}
