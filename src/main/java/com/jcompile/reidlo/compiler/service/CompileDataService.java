package com.jcompile.reidlo.compiler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
@RequiredArgsConstructor
public class CompileDataService {

    @Transactional
    public List<String> compileAfterConvertFile(String beforeCompile) {
        log.info("Compile SVC compileAfterConvertFile beforeCompile : " + beforeCompile);
        try {
            String path = "C:\\compileData\\";
            File folder = new File(path);

            if(!folder.exists()) {
                if(folder.mkdirs()) {
                    log.info("Compile SVC compileAfterConvertFile folder : "+ folder + " are generated");
                }
            }

            String fileName = "Main" + ".java";
            File file = new File(path, fileName);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))){
                writer.write(beforeCompile);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Compile SVC compileAfterConvertFile error occurred : " + e.getMessage());
            }

            List<String> result = compileFile(file);
            log.info("Compile SVC compileAfterConvertFile compileFile result : " + result);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Compile SVC convertFile error occurred : " + e.getMessage());
        }
        return null;
    }

    private List<String> compileFile(File file) {
        try {
            String[] commands = new String[] {"java " + file.getName(),"exit"};

            ProcessBuilder pb = new ProcessBuilder("cmd");
            Map<String, String> env = pb.environment();

            pb.redirectErrorStream(true);
            pb.directory(new File("c:\\compileData"));
            log.info("PB environment : " + env);

            Process p = pb.start();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));

            for(String cmd : commands) {
                log.info("cmd : " + cmd);
                writer.write(cmd + "\n");
                writer.flush();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String outputLine = "";
            List<String> result = new ArrayList<>();

            while((outputLine = reader.readLine()) != null){
                result.add(outputLine);
                if(outputLine.equals("")){
                    result.remove(outputLine);
                    continue;
                }
                if(outputLine.contains("Microsoft Windows")){
                    result.remove(outputLine);
                    continue;
                }
                if(outputLine.equals("(c) Microsoft Corporation. All rights reserved.")) {
                    result.remove(outputLine);
                    continue;
                }
                if(outputLine.equals("c:\\compileData>java Main.java")) {
                    result.remove(outputLine);
                    continue;
                }
                if(outputLine.equals("c:\\compileData>exit")) {
                    result.remove(outputLine);
                    break;
                }
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}