package de.pandaserv.music.server.ssh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read OpenSSH key files.
 * 
 * OpenSSH key files have the following format:
 * [length (uint32)|format string|length (uint32)|exponent|length (uint32)|modulus]
 * 
 * @author ich
 */
//TODO: error checking
public class OpenSSHKeyReader {
    public class KeyParseException extends Exception {

        public KeyParseException(String msg) {
            super(msg);
        }
        
    }
    
    static Logger logger = LoggerFactory.getLogger(OpenSSHKeyReader.class);
    
    private byte[] data;
    private int pos;
    
    public OpenSSHKeyReader(byte[] data) throws KeyParseException {
        this.data = data;
        pos = 0;
        
        int offset = readUInt32();
        pos += offset; // skip identifier string
        logger.debug("skipped {} bytes at the start of input", pos);
    }
    
    /**
     * Return the exponent and modulus of the public key.
     * The first invocation returns the exponent, the second invocation
     * returns the modulus. Calling this function more than two times is an error.
     * @return 
     */
    public byte[] getKeyElement() throws KeyParseException {
        int len = readUInt32();
        logger.debug("found field with length {} at ({}/{})", 
                new Object[]{len, pos, data.length});
        
        if (len < 0 || len > data.length - pos) {
            throw new KeyParseException("invalid length field: " + len);
        }
        byte[] ret = new byte[len];
        
        System.arraycopy(data, pos, ret, 0, len);
        pos += len;
        return ret;
    }
    
    private int readUInt32() throws KeyParseException {
        if (pos >= data.length - 4) {
            throw new KeyParseException("no more data");
        }
        int ret = 0;
        for (int i = 0; i < 4; i++) {
            ret += (data[pos+i] << 8*(3-i));
        }
        pos += 4;
        return ret;
    }
}
