package main.java.simulation;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * シミュレーション結果をファイルに書き出すユーティリティクラス
 */
public class SimulationDataWriter {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * パラメータ設定をJSONファイルに書き出す
     */
    public static void writeParams(String outputDir, Map<String, Object> params) throws IOException {
        // ディレクトリを作成
        Files.createDirectories(Paths.get(outputDir));
        
        // JSONファイルに書き出し
        String paramsFile = outputDir + "/params.json";
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(paramsFile), params);
        System.out.println("パラメータ設定を保存しました: " + paramsFile);
    }
    
    /**
     * シミュレーション結果をCSVファイルに書き出す
     */
    public static void writeResults(String outputDir, List<SimulationRun> results) throws IOException {
        // ディレクトリを作成
        Files.createDirectories(Paths.get(outputDir));
        
        // CSVファイルに書き出し
        String resultsFile = outputDir + "/results.csv";
        try (PrintWriter writer = new PrintWriter(new FileWriter(resultsFile))) {
            // ヘッダーを書き出し
            writer.println("run_id,transmission_rate,recovery_rate,network_type,network_size," +
                         "initial_infections,max_time,max_infected,final_infected,peak_time," +
                         "total_events,simulation_time");
            
            // 各シミュレーション結果を書き出し
            for (SimulationRun result : results) {
                writer.printf("%d,%.6f,%.6f,%d,%d,%d,%.6f,%d,%d,%.6f,%d,%.6f%n",
                    result.runId, result.transmissionRate, result.recoveryRate,
                    result.networkType, result.networkSize, result.initialInfections,
                    result.maxTime, result.maxInfected, result.finalInfected,
                    result.peakTime, result.totalEvents, result.simulationTime);
            }
        }
        System.out.println("シミュレーション結果を保存しました: " + resultsFile);
    }
    
    /**
     * シミュレーション状態をJSONファイルに書き出す
     */
    public static void writeStatus(String outputDir, Map<String, Object> status) throws IOException {
        // ディレクトリを作成
        Files.createDirectories(Paths.get(outputDir));
        
        // JSONファイルに書き出し
        String statusFile = outputDir + "/status.json";
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(statusFile), status);
        System.out.println("シミュレーション状態を保存しました: " + statusFile);
    }
    
    /**
     * シミュレーション実行結果を表すクラス
     */
    public static class SimulationRun {
        public final int runId;
        public final double transmissionRate;
        public final double recoveryRate;
        public final int networkType; // 0: BA, 1: ER, 2: RR
        public final int networkSize;
        public final int initialInfections;
        public final double maxTime;
        public final int maxInfected;
        public final int finalInfected;
        public final double peakTime;
        public final int totalEvents;
        public final double simulationTime;
        
        public SimulationRun(int runId, double transmissionRate, double recoveryRate,
                           int networkType, int networkSize, int initialInfections,
                           double maxTime, int maxInfected, int finalInfected,
                           double peakTime, int totalEvents, double simulationTime) {
            this.runId = runId;
            this.transmissionRate = transmissionRate;
            this.recoveryRate = recoveryRate;
            this.networkType = networkType;
            this.networkSize = networkSize;
            this.initialInfections = initialInfections;
            this.maxTime = maxTime;
            this.maxInfected = maxInfected;
            this.finalInfected = finalInfected;
            this.peakTime = peakTime;
            this.totalEvents = totalEvents;
            this.simulationTime = simulationTime;
        }
    }
} 