/**
 * This class downloads the images for the survey objects.
 */

package edu.newpaltz.surveyit;

import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Downloads a file in a thread.
 */
public class DownloaderThread extends Thread {

    private static final int DOWNLOAD_BUFFER_SIZE = 4096;
    private String downloadUrl;

    /**
     * Instantiates a new DownloaderThread object.
     * @param inUrl String representing the URL of the file to be downloaded.
     */
    public DownloaderThread(String inUrl)
    {
        downloadUrl = "";
        if(inUrl != null)
        {
            downloadUrl = inUrl;
        }
    }

    /**
     * Connects to the URL of the file, begins the download, and writes the file to
     * the root of the SD card.
     */
    @Override
    public void run()
    {
        URL url;
        URLConnection conn;
        int lastSlash;
        String fileName;
        BufferedInputStream inStream;
        BufferedOutputStream outStream;
        File outFile;
        FileOutputStream fileStream;
        try
        {
            url = new URL(downloadUrl);
            conn = url.openConnection();
            conn.setUseCaches(false);
            // get the filename
            lastSlash = url.toString().lastIndexOf('/');
            fileName = "file.bin";
            if(lastSlash >=0)
            {
                fileName = url.toString().substring(lastSlash + 1);
            }
            if(fileName.equals(""))
            {
                fileName = "file.bin";
            }
            // start download
            inStream = new BufferedInputStream(conn.getInputStream());
            outFile = new File(Environment.getExternalStorageDirectory() + "/" + fileName);
            if(outFile.exists()) {
                inStream.close();
                return;
            }
            fileStream = new FileOutputStream(outFile);
            outStream = new BufferedOutputStream(fileStream, DOWNLOAD_BUFFER_SIZE);
            byte[] data = new byte[DOWNLOAD_BUFFER_SIZE];
            int bytesRead;
            while((bytesRead = inStream.read(data, 0, data.length)) >= 0)
            {
                outStream.write(data, 0, bytesRead);
            }
            outStream.close();
            fileStream.close();
            inStream.close();
        }
        catch(MalformedURLException e)        {            e.printStackTrace();        }
        catch(FileNotFoundException e)        {            e.printStackTrace();        }
        catch(Exception e)                    {            e.printStackTrace();        }
    }
}
