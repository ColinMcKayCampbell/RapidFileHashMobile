package colin.mckay.campbell.rfh_mobile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Created by Colin on 26/04/2018.
 * This class holds the files found by AsyncFileScan.
 * It contains the hashing function used against each file, a handle to the file and the resulting hash string
 */

public class myFile {
    public myFile(File f){
        fileHandle = f;
    }

    public void doHash() // do hash mich
    {
        byte[] digest;
        byte[] buffer = new byte[4096]; // Default buffer size of 4kB
        try {
            // File reading
            FileInputStream inFile = new FileInputStream(fileHandle); // Open file stream
            MessageDigest SHA = MessageDigest.getInstance("SHA-256"); // Initialise SHA-256 digest
            while (inFile.available() > 4096) { // Hash file until less than 4kB is left
                inFile.read(buffer);
                SHA.update(buffer);
            }
            buffer = new byte[inFile.available()]; // Set buffer to amount of remaining file
            inFile.read(buffer);
            inFile.close();
            // End of file reading
            digest = SHA.digest(buffer); // Finalise hash
            hash = String.format("%064x", new BigInteger(1, digest)); // Maths I don't understand to convert digest object to human readable SHA-256 hex string
        } catch (Exception e) {
            e.printStackTrace(System.err);
            hash = "File failed to open. Manual inspection required.";
        }
    }


    String hash;
    File fileHandle;
}