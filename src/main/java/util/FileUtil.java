package util;

import core.Bot;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Created by mlase on 12/8/2016.
 */
public class FileUtil {

    private FileUtil() {}
    private static FileUtil fileUtil;
    public static final String USER_FILE_PATH = "rupert_users";


    public static FileUtil instance() {
        if (fileUtil == null) {
            fileUtil = new FileUtil();
        }
        return fileUtil;
    }

     public synchronized boolean fileContainsAttribute(File file, String attribute) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            if (reader.readLine().contains(attribute)) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public synchronized File createUserFile(String fileName) {
        try {
            File file = new File(USER_FILE_PATH + "/" + fileName);
            if (file.createNewFile()) {
                return file;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized void modifyUserFileAttribute(File file, String attribute, boolean addAttribute) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            StringBuilder output = new StringBuilder();
            reader.close();
            if (line != null) {
                if (line.contains(",")) {
                    List<String> list = new ArrayList<>(Arrays.asList(line.split(",")));
                    if (addAttribute && !list.contains(attribute)) {
                        list.add(attribute);
                        list.forEach(s -> output.append(s).append(','));
                    } else {
                        list.remove(attribute);
                        list.forEach(s -> output.append(s).append(','));
                    }
                } else if (!line.equals(attribute) && addAttribute){
                    output.append(line).append(',').append(attribute);
                } else if (addAttribute) {
                    output.append(line);
                }
            } else if (addAttribute) {
                output.append(attribute);
            }

            String outString = output.toString();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            if (outString.length() > 1 && outString.charAt(outString.length() - 1) == ',') {
                outString = outString.substring(0, outString.length() - 1);
            }
            writer.write(outString);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
