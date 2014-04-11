package com.dieend.uvahunt.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utility {
	public static String convertStreamToString(InputStream input) throws IOException {

        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        //OutputStreamWriter output = new StringWriter(path);

        byte data[] = new byte[1024];

        int count;
        while ((count = input.read(data)) != -1) {
            output.write(data, 0, count);
        }

        output.flush();
        String ret = new String(output.toByteArray());
        output.close();
        return ret;
//        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
//        StringBuilder sb = new StringBuilder();
//
//        String line = null;
//        try {
//            while ((line = reader.readLine()) != null) {
//                sb.append(line + "\n");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                input.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return sb.toString();
    }
}
