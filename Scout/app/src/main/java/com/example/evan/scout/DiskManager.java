package com.example.evan.scout;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiskManager {
    public static String readFile(Activity context, String name) {
        BufferedReader file;
        try {
            file = new BufferedReader(new InputStreamReader(new FileInputStream(
                    new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/MatchData/" + name))));
        } catch (IOException ioe) {
            Log.e("File Error", "Failed To Open File");
            Toast.makeText(context, "Failed To Open File", Toast.LENGTH_LONG).show();
            return null;
        }
        String text;
        try {
            text = file.readLine();
        } catch (IOException ioe) {
            Log.e("File Error", "Failed To Read From File");
            Toast.makeText(context, "Failed To Read From File", Toast.LENGTH_LONG).show();
            return null;
        }
        Log.i("JSON after read", text);
        return text;
    }

    public static List<File> writeToDisk(Map<String, String> data) throws IOException {
        //first we save to a file so if something goes wrong we have backups.  We use external storage so it is not deleted when app is reinstalled.
        //storage path: /sdcard/Android/MatchData
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Log.e("File Error", "External Storage not Mounted");
            Utils.toastText("External Storage Not Mounted", Toast.LENGTH_LONG);
            throw new IOException();
        }
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/MatchData");
        if (!dir.mkdir()) {
            Log.i("File Info", "Failed to make Directory.  Unimportant");
        }
        List<File> returnList = new ArrayList<>();
        //we loop through all the data points, write them to files, and save their files to be renamed later
        for (Map.Entry<String, String> entry : data.entrySet()) {
            File file;
            PrintWriter fileWriter;
            try {
                //we first name the file with the prefix "UNSENT_".  If all goes well, it is renamed without the prefix, but if something fails it will still have it.
                file = new File(dir, "UNSENT_" + entry.getKey());
                fileWriter = new PrintWriter(file);
            } catch (IOException ioe) {
                Log.e("File Error", "Failed to open file");
                Utils.toastText("Failed To Open File", Toast.LENGTH_LONG);
                throw new IOException();
            }


            fileWriter.print(entry.getValue());
            fileWriter.close();
            if (fileWriter.checkError()) {
                Log.e("File Error", "Failed to Write to File");
                Utils.toastText("Failed To Save Match Data To File", Toast.LENGTH_LONG);
                throw new IOException();
            }
            returnList.add(file);
        }
        return returnList;
    }

    public static File writeToDisk(final String name, final String data) throws IOException {
        return writeToDisk(new HashMap<String, String>(){{put(name, data);}}).get(0);
    }
}
