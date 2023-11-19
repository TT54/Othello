package fr.tt54.othello.data.complexity;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AlphaBetaComplexityLoader {

    public static final String DATA_FOLDER = "/alphabeta_complexity_analysis/";

    public static List<long[]> loadDatas(String resourceFolder) throws IOException {
        File folder = new File(AlphaBetaComplexityLoader.class.getResource(resourceFolder).getFile());
        if(!folder.isDirectory())
            return new ArrayList<>();

        List<long[]> durations = new ArrayList<>();

        for (File file : folder.listFiles()){
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line = reader.readLine();

            while(line != null){
                line = line.substring(1, line.length() - 1);
                String[] datas = line.replace(" ", "").split(",");
                long[] times = new long[datas.length];
                for(int i = 0; i < datas.length; i++){
                    times[i] = Long.parseLong(datas[i]);
                }
                durations.add(times);

                line = reader.readLine();
            }

            reader.close();
        }

        return durations;
    }

}
