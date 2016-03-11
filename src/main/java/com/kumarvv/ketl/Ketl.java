package com.kumarvv.ketl;

import com.kumarvv.ketl.core.KetlProcessor;
import com.kumarvv.ketl.model.Def;
import com.kumarvv.ketl.model.Load;
import com.kumarvv.ketl.model.Status;
import com.kumarvv.ketl.utils.Chrono;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Ketl {
    private static final int STATUS_EVERY = 1000;

    protected Def loadFile(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Path path = Paths.get(filePath);
            final Def def = mapper.readValue(path.toFile(), Def.class);
            initCsvPaths(def, path);
            return def;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void initCsvPaths(final Def def, Path defPath) {
        if (def.getExtract().getCsv() != null && StringUtils.isNotEmpty(def.getExtract().getCsv().getFile())) {
            Path csvPath = defPath.getParent().resolve(def.getExtract().getCsv().getFile()).normalize();
            def.getExtract().getCsv().setFilePath(csvPath.toAbsolutePath().toString());
        }

        for (Load load : def.getLoads()) {
            if (load.getPre() != null && load.getPre().getCsv() != null && StringUtils.isNotEmpty(load.getPre().getCsv().getFile())) {
                Path csvPath = defPath.getParent().resolve(load.getPre().getCsv().getFile()).normalize();
                load.getPre().getCsv().setFilePath(csvPath.toAbsolutePath().toString());
            }
            if (load.getPost() != null && load.getPost().getCsv() != null && StringUtils.isNotEmpty(load.getPost().getCsv().getFile())) {
                Path csvPath = defPath.getParent().resolve(load.getPost().getCsv().getFile()).normalize();
                load.getPost().getCsv().setFilePath(csvPath.toAbsolutePath().toString());
            }
        }
    }

    protected void printStatEvery(Status status) {
        if (status.getRowsFound() % STATUS_EVERY == 0) {
            System.out.println("Found: " + status.getRowsFound() + ", Processed: " + status.getRowsProcessed());
        }
    }

    protected void printStat(Status status) {
        System.out.println("Found: " + status.getRowsFound() + ", Processed: " + status.getRowsProcessed());
    }

    public static void main(String[] args) {
        Chrono kch = Chrono.start("all");
//        String defPath = "src/main/resources/test.json";
//        String defPath = "src/main/resources/csv.json";
        String defPath = "/Users/Vijay/Dev/projects/ktacs/java/ktacs/ketl/master/country.json";

        Ketl me = new Ketl();
        Def def = me.loadFile(defPath);

        System.out.println("Processing " + def.getName());

        Status status = new Status((s) -> me.printStatEvery(s));
        KetlProcessor processor = new KetlProcessor(status, def);
        Thread t = new Thread(processor);
        t.run();

        try {
            t.join();
        } catch (InterruptedException ie) {}

        me.printStat(status);
        kch.stop();
        System.out.println("ALL DONE!");
    }

}
