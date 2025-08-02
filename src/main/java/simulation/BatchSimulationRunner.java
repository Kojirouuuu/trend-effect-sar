package main.java.simulation;

import main.java.network.Graph;
import main.java.network.topology.BA;
import main.java.network.topology.ER;
import main.java.network.topology.RR;
import java.util.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

/**
 * 複数のシミュレーションを実行し、結果をファイルに書き出すバッチ実行クラス
 */
public class BatchSimulationRunner {
    
    public static void main(String[] args) {
        System.out.println("=== バッチシミュレーション実行 ===");
        
        try {
            // シミュレーション設定
            String outputDir = "data/sim1";
            Map<String, Object> params = createSimulationParams();
            
            // パラメータ設定を保存
            SimulationDataWriter.writeParams(outputDir, params);
            
            // シミュレーション実行
            List<SimulationDataWriter.SimulationRun> results = runBatchSimulations(params);
            
            // 結果を保存
            SimulationDataWriter.writeResults(outputDir, results);
            
            // 状態情報を保存
            Map<String, Object> status = createStatusInfo();
            SimulationDataWriter.writeStatus(outputDir, status);
            
            System.out.println("バッチシミュレーション完了！");
            
        } catch (IOException e) {
            System.err.println("ファイル書き出しエラー: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * シミュレーションパラメータを作成
     */
    private static Map<String, Object> createSimulationParams() {
        Map<String, Object> params = new HashMap<>();
        
        // ネットワーク設定
        params.put("network_types", Arrays.asList("BA", "ER", "RR"));
        params.put("network_sizes", Arrays.asList(50, 100, 200));
        
        // BAネットワークパラメータ
        params.put("ba_m0", 5);
        params.put("ba_m", 2);
        
        // ERネットワークパラメータ
        params.put("er_probabilities", Arrays.asList(0.05, 0.1, 0.15));
        
        // RRネットワークパラメータ
        params.put("rr_degrees", Arrays.asList(3, 4, 6));
        
        // 感染パラメータ
        params.put("transmission_rates", Arrays.asList(0.2, 0.3, 0.4, 0.5));
        params.put("recovery_rates", Arrays.asList(0.1, 0.15, 0.2, 0.25));
        
        // シミュレーション設定
        params.put("initial_infections", Arrays.asList(1, 2, 3, 5));
        params.put("max_time", 50.0);
        params.put("samples_per_config", 10);
        
        return params;
    }
    
    /**
     * バッチシミュレーションを実行
     */
    private static List<SimulationDataWriter.SimulationRun> runBatchSimulations(Map<String, Object> params) {
        List<SimulationDataWriter.SimulationRun> results = new ArrayList<>();
        int runId = 0;
        
        @SuppressWarnings("unchecked")
        List<String> networkTypes = (List<String>) params.get("network_types");
        @SuppressWarnings("unchecked")
        List<Integer> networkSizes = (List<Integer>) params.get("network_sizes");
        @SuppressWarnings("unchecked")
        List<Double> transmissionRates = (List<Double>) params.get("transmission_rates");
        @SuppressWarnings("unchecked")
        List<Double> recoveryRates = (List<Double>) params.get("recovery_rates");
        @SuppressWarnings("unchecked")
        List<Integer> initialInfections = (List<Integer>) params.get("initial_infections");
        
        double maxTime = (Double) params.get("max_time");
        int samplesPerConfig = (Integer) params.get("samples_per_config");
        
        for (String networkType : networkTypes) {
            for (int networkSize : networkSizes) {
                for (double transmissionRate : transmissionRates) {
                    for (double recoveryRate : recoveryRates) {
                        for (int initialInfection : initialInfections) {
                            
                            // 各設定で複数回シミュレーション実行
                            for (int sample = 0; sample < samplesPerConfig; sample++) {
                                runId++;
                                
                                System.out.printf("実行 %d: %s, N=%d, τ=%.2f, γ=%.2f, 初期感染=%d, サンプル=%d%n",
                                    runId, networkType, networkSize, transmissionRate, recoveryRate, 
                                    initialInfection, sample + 1);
                                
                                // ネットワーク生成
                                Graph network = generateNetwork(networkType, networkSize, params);
                                
                                // シミュレーション実行
                                long startTime = System.currentTimeMillis();
                                SimulationDataWriter.SimulationRun result = runSingleSimulation(
                                    runId, network, networkType, networkSize, transmissionRate, 
                                    recoveryRate, initialInfection, maxTime);
                                long endTime = System.currentTimeMillis();
                                
                                // 実行時間を設定
                                result = new SimulationDataWriter.SimulationRun(
                                    result.runId, result.transmissionRate, result.recoveryRate,
                                    result.networkType, result.networkSize, result.initialInfections,
                                    result.maxTime, result.maxInfected, result.finalInfected,
                                    result.peakTime, result.totalEvents, (endTime - startTime) / 1000.0
                                );
                                
                                results.add(result);
                            }
                        }
                    }
                }
            }
        }
        
        return results;
    }
    
    /**
     * 単一のシミュレーションを実行
     */
    private static SimulationDataWriter.SimulationRun runSingleSimulation(
            int runId, Graph network, String networkType, int networkSize,
            double transmissionRate, double recoveryRate, int initialInfection, double maxTime) {
        
        // SARシミュレーターを作成
        SARSimulator simulator = new SARSimulator(network, transmissionRate, recoveryRate);
        
        // 初期感染ノードを設定
        Set<Integer> initialInfections = new HashSet<>();
        Random random = new Random(runId); // 再現性のためrunIdをシードに使用
        while (initialInfections.size() < initialInfection) {
            initialInfections.add(random.nextInt(networkSize));
        }
        
        // シミュレーション実行
        SARSimulator.SimulationResult result = simulator.runSimulation(initialInfections, maxTime);
        
        // 統計情報を計算
        int maxInfected = result.infectedCounts.stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
        
        int finalInfected = result.infectedCounts.get(result.infectedCounts.size() - 1);
        
        // 感染ピーク時間を計算
        int peakIndex = 0;
        for (int i = 1; i < result.infectedCounts.size(); i++) {
            if (result.infectedCounts.get(i) > result.infectedCounts.get(peakIndex)) {
                peakIndex = i;
            }
        }
        double peakTime = result.times.get(peakIndex);
        
        // ネットワークタイプを数値に変換
        int networkTypeInt = getNetworkTypeInt(networkType);
        
        return new SimulationDataWriter.SimulationRun(
            runId, transmissionRate, recoveryRate, networkTypeInt, networkSize,
            initialInfection, maxTime, maxInfected, finalInfected, peakTime,
            result.times.size(), 0.0 // 実行時間は後で設定
        );
    }
    
    /**
     * ネットワークを生成
     */
    private static Graph generateNetwork(String networkType, int networkSize, Map<String, Object> params) {
        Random random = new Random(42); // 再現性のため固定シード
        
        switch (networkType) {
            case "BA":
                int m0 = (Integer) params.get("ba_m0");
                int m = (Integer) params.get("ba_m");
                return BA.generateBA(networkSize, m0, m);
                
            case "ER":
                @SuppressWarnings("unchecked")
                List<Double> erProbs = (List<Double>) params.get("er_probabilities");
                double prob = erProbs.get(random.nextInt(erProbs.size()));
                return ER.generateER(networkSize, prob);
                
            case "RR":
                @SuppressWarnings("unchecked")
                List<Integer> rrDegrees = (List<Integer>) params.get("rr_degrees");
                int degree = rrDegrees.get(random.nextInt(rrDegrees.size()));
                return RR.generateRR(networkSize, degree);
                
            default:
                throw new IllegalArgumentException("未知のネットワークタイプ: " + networkType);
        }
    }
    
    /**
     * ネットワークタイプを数値に変換
     */
    private static int getNetworkTypeInt(String networkType) {
        switch (networkType) {
            case "BA": return 0;
            case "ER": return 1;
            case "RR": return 2;
            default: return -1;
        }
    }
    
    /**
     * システム状態情報を作成
     */
    private static Map<String, Object> createStatusInfo() {
        Map<String, Object> status = new HashMap<>();
        
        // システム情報
        Runtime runtime = Runtime.getRuntime();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        
        status.put("timestamp", new Date().toString());
        status.put("java_version", System.getProperty("java.version"));
        status.put("os_name", System.getProperty("os.name"));
        status.put("os_version", System.getProperty("os.version"));
        status.put("available_processors", runtime.availableProcessors());
        status.put("max_memory_mb", runtime.maxMemory() / (1024 * 1024));
        status.put("total_memory_mb", runtime.totalMemory() / (1024 * 1024));
        status.put("free_memory_mb", runtime.freeMemory() / (1024 * 1024));
        status.put("heap_memory_usage_mb", memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024));
        
        return status;
    }
} 