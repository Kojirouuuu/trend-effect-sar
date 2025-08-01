package main.java.simulation;

import main.java.network.Graph;
import main.java.network.topology.BA;
import main.java.network.topology.ER;
import main.java.network.topology.RR;
import java.util.*;

/**
 * SARシミュレーションの使用例とテスト
 */
public class SARExample {
    
    public static void main(String[] args) {
        System.out.println("=== SARシミュレーション例 ===");
        
        // 1. BAネットワークでのシミュレーション
        testBANetwork();
        
        // 2. ERネットワークでのシミュレーション
        testERNetwork();
        
        // 3. RRネットワークでのシミュレーション
        testRRNetwork();
    }
    
    /**
     * BAネットワークでのSARシミュレーション
     */
    private static void testBANetwork() {
        System.out.println("\n--- BAネットワークでのSARシミュレーション ---");
        
        // BAネットワークを生成
        Graph baNetwork = BA.generateBA(100, 5, 2);
        System.out.println("BAネットワーク生成完了: " + baNetwork.N + " ノード");
        
        // SARシミュレーターを作成
        double transmissionRate = 0.3;  // 感染率
        double recoveryRate = 0.1;      // 回復率
        SARSimulator simulator = new SARSimulator(baNetwork, transmissionRate, recoveryRate);
        
        // 初期感染ノードを設定（ランダムに5個選択）
        Set<Integer> initialInfections = new HashSet<>();
        Random random = new Random(42);
        while (initialInfections.size() < 5) {
            initialInfections.add(random.nextInt(baNetwork.N));
        }
        
        System.out.println("初期感染ノード: " + initialInfections);
        
        // シミュレーション実行
        double maxTime = 50.0;
        SARSimulator.SimulationResult result = simulator.runSimulation(initialInfections, maxTime);
        
        // 結果を表示
        result.printResults();
        
        // 統計情報を表示
        printStatistics(result);
    }
    
    /**
     * ERネットワークでのSARシミュレーション
     */
    private static void testERNetwork() {
        System.out.println("\n--- ERネットワークでのSARシミュレーション ---");
        
        // ERネットワークを生成
        Graph erNetwork = ER.generateER(100, 0.1);
        System.out.println("ERネットワーク生成完了: " + erNetwork.N + " ノード");
        
        // SARシミュレーターを作成
        double transmissionRate = 0.4;  // 感染率
        double recoveryRate = 0.15;     // 回復率
        SARSimulator simulator = new SARSimulator(erNetwork, transmissionRate, recoveryRate);
        
        // 初期感染ノードを設定
        Set<Integer> initialInfections = new HashSet<>();
        Random random = new Random(123);
        while (initialInfections.size() < 3) {
            initialInfections.add(random.nextInt(erNetwork.N));
        }
        
        System.out.println("初期感染ノード: " + initialInfections);
        
        // シミュレーション実行
        double maxTime = 40.0;
        SARSimulator.SimulationResult result = simulator.runSimulation(initialInfections, maxTime);
        
        // 結果を表示
        result.printResults();
        
        // 統計情報を表示
        printStatistics(result);
    }
    
    /**
     * RRネットワークでのSARシミュレーション
     */
    private static void testRRNetwork() {
        System.out.println("\n--- RRネットワークでのSARシミュレーション ---");
        
        // RRネットワークを生成
        Graph rrNetwork = RR.generateRR(80, 4);
        System.out.println("RRネットワーク生成完了: " + rrNetwork.N + " ノード");
        
        // SARシミュレーターを作成
        double transmissionRate = 0.25; // 感染率
        double recoveryRate = 0.08;     // 回復率
        SARSimulator simulator = new SARSimulator(rrNetwork, transmissionRate, recoveryRate);
        
        // 初期感染ノードを設定
        Set<Integer> initialInfections = new HashSet<>();
        Random random = new Random(456);
        while (initialInfections.size() < 4) {
            initialInfections.add(random.nextInt(rrNetwork.N));
        }
        
        System.out.println("初期感染ノード: " + initialInfections);
        
        // シミュレーション実行
        double maxTime = 60.0;
        SARSimulator.SimulationResult result = simulator.runSimulation(initialInfections, maxTime);
        
        // 結果を表示
        result.printResults();
        
        // 統計情報を表示
        printStatistics(result);
    }
    
    /**
     * シミュレーション結果の統計情報を表示
     */
    private static void printStatistics(SARSimulator.SimulationResult result) {
        System.out.println("\n=== 統計情報 ===");
        
        // 最大感染数
        int maxInfected = result.infectedCounts.stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
        System.out.println("最大感染数: " + maxInfected);
        
        // 最終感染数
        int finalInfected = result.infectedCounts.get(result.infectedCounts.size() - 1);
        System.out.println("最終感染数: " + finalInfected);
        
        // 感染ピーク時間
        int peakIndex = 0;
        for (int i = 1; i < result.infectedCounts.size(); i++) {
            if (result.infectedCounts.get(i) > result.infectedCounts.get(peakIndex)) {
                peakIndex = i;
            }
        }
        double peakTime = result.times.get(peakIndex);
        System.out.println("感染ピーク時間: " + String.format("%.3f", peakTime));
        
        // 総シミュレーション時間
        double totalTime = result.times.get(result.times.size() - 1);
        System.out.println("総シミュレーション時間: " + String.format("%.3f", totalTime));
        
        // イベント数
        System.out.println("記録されたイベント数: " + result.times.size());
    }
    
    /**
     * パラメータの影響を比較するテスト
     */
    public static void parameterComparisonTest() {
        System.out.println("\n=== パラメータ比較テスト ===");
        
        // BAネットワークで異なるパラメータをテスト
        Graph network = BA.generateBA(50, 4, 2);
        Set<Integer> initialInfections = Set.of(0, 1);
        
        double[] transmissionRates = {0.2, 0.4, 0.6};
        double[] recoveryRates = {0.1, 0.2, 0.3};
        
        for (double tau : transmissionRates) {
            for (double gamma : recoveryRates) {
                System.out.printf("\nτ=%.1f, γ=%.1f でのシミュレーション:%n", tau, gamma);
                
                SARSimulator simulator = new SARSimulator(network, tau, gamma);
                SARSimulator.SimulationResult result = simulator.runSimulation(initialInfections, 30.0);
                
                int maxInfected = result.infectedCounts.stream()
                        .mapToInt(Integer::intValue)
                        .max()
                        .orElse(0);
                System.out.printf("最大感染数: %d%n", maxInfected);
            }
        }
    }
} 