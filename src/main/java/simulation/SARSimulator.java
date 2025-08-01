package main.java.simulation;

import main.java.network.Graph;
import java.util.*;

/**
 * SARモデルのGillespieシミュレーション
 * ネットワーク上での感染症の伝播をシミュレーション
 */
public class SARSimulator {
    private Graph network;
    private double transmissionRate;  // τ (感染率)
    private double recoveryRate;      // γ (回復率)
    private Set<Integer> infectedNodes;
    private Set<Integer> atRiskNodes;
    private Map<Integer, Double> infectionRates;
    private double totalInfectionRate;
    private double totalRecoveryRate;
    private double totalRate;
    
    // シミュレーション結果
    private List<Double> times;
    private List<Integer> susceptibleCounts;
    private List<Integer> infectedCounts;
    private List<Integer> recoveredCounts;
    
    public SARSimulator(Graph network, double transmissionRate, double recoveryRate) {
        this.network = network;
        this.transmissionRate = transmissionRate;
        this.recoveryRate = recoveryRate;
        this.infectedNodes = new HashSet<>();
        this.atRiskNodes = new HashSet<>();
        this.infectionRates = new HashMap<>();
        this.times = new ArrayList<>();
        this.susceptibleCounts = new ArrayList<>();
        this.infectedCounts = new ArrayList<>();
        this.recoveredCounts = new ArrayList<>();
    }
    
    /**
     * GillespieアルゴリズムによるSARシミュレーション
     * @param initialInfections 初期感染ノードのセット
     * @param maxTime 最大シミュレーション時間
     * @return シミュレーション結果
     */
    public SimulationResult runSimulation(Set<Integer> initialInfections, double maxTime) {
        // 初期化
        initializeSimulation(initialInfections);
        
        double currentTime = 0.0;
        Random random = new Random();
        
        // 初期状態を記録
        recordState(currentTime);
        
        while (currentTime < maxTime && totalRate > 0) {
            // 次のイベントまでの時間を計算
            double timeToNextEvent = exponentialVariate(totalRate, random);
            currentTime += timeToNextEvent;
            
            if (currentTime >= maxTime) {
                break;
            }
            
            // イベントの種類を決定
            double r = random.nextDouble() * totalRate;
            
            if (r < totalRecoveryRate) {
                // 回復イベント
                performRecoveryEvent(random);
            } else {
                // 感染イベント
                performInfectionEvent(random);
            }
            
            // 状態を記録
            recordState(currentTime);
        }
        
        return new SimulationResult(times, susceptibleCounts, infectedCounts, recoveredCounts);
    }
    
    /**
     * シミュレーションの初期化
     */
    private void initializeSimulation(Set<Integer> initialInfections) {
        infectedNodes.clear();
        atRiskNodes.clear();
        infectionRates.clear();
        
        // 初期感染ノードを設定
        infectedNodes.addAll(initialInfections);
        
        // 感染リスクのあるノードを特定
        updateAtRiskNodes();
        
        // 感染率を計算
        calculateInfectionRates();
        
        // 総率を計算
        updateTotalRates();
    }
    
    /**
     * 感染リスクのあるノードを更新
     */
    private void updateAtRiskNodes() {
        atRiskNodes.clear();
        
        for (int node = 0; node < network.N; node++) {
            if (!infectedNodes.contains(node)) {
                int infectedNeighbors = countInfectedNeighbors(node);
                if (infectedNeighbors > 0) {
                    atRiskNodes.add(node);
                }
            }
        }
    }
    
    /**
     * 指定ノードの感染隣接ノード数をカウント
     */
    private int countInfectedNeighbors(int node) {
        int count = 0;
        int start = network.addressList[node];
        int end = network.cursorList[node];
        
        for (int i = start; i < end; i++) {
            int neighbor = network.edgeList[i];
            if (infectedNodes.contains(neighbor)) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * 感染率を計算
     */
    private void calculateInfectionRates() {
        infectionRates.clear();
        
        for (int node : atRiskNodes) {
            int infectedNeighbors = countInfectedNeighbors(node);
            double rate = transmissionRate * infectedNeighbors;
            infectionRates.put(node, rate);
        }
    }
    
    /**
     * 総率を更新
     */
    private void updateTotalRates() {
        totalInfectionRate = infectionRates.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        totalRecoveryRate = recoveryRate * infectedNodes.size();
        totalRate = totalInfectionRate + totalRecoveryRate;
    }
    
    /**
     * 回復イベントを実行
     */
    private void performRecoveryEvent(Random random) {
        // ランダムに感染ノードを選択
        List<Integer> infectedList = new ArrayList<>(infectedNodes);
        int recoveredNode = infectedList.get(random.nextInt(infectedList.size()));
        
        // 感染ノードから削除
        infectedNodes.remove(recoveredNode);
        
        // 隣接ノードの感染率を更新
        updateNeighborInfectionRates(recoveredNode);
        
        // 総率を更新
        updateTotalRates();
    }
    
    /**
     * 感染イベントを実行
     */
    private void performInfectionEvent(Random random) {
        // 感染リスクのあるノードから選択
        List<Integer> atRiskList = new ArrayList<>(atRiskNodes);
        double[] probabilities = new double[atRiskList.size()];
        
        for (int i = 0; i < atRiskList.size(); i++) {
            probabilities[i] = infectionRates.get(atRiskList.get(i));
        }
        
        int infectedNode = selectNodeByProbability(atRiskList, probabilities, random);
        
        // 感染ノードに追加
        infectedNodes.add(infectedNode);
        atRiskNodes.remove(infectedNode);
        infectionRates.remove(infectedNode);
        
        // 隣接ノードの感染率を更新
        updateNeighborInfectionRates(infectedNode);
        
        // 総率を更新
        updateTotalRates();
    }
    
    /**
     * 隣接ノードの感染率を更新
     */
    private void updateNeighborInfectionRates(int node) {
        int start = network.addressList[node];
        int end = network.cursorList[node];
        
        for (int i = start; i < end; i++) {
            int neighbor = network.edgeList[i];
            
            if (!infectedNodes.contains(neighbor)) {
                int infectedNeighbors = countInfectedNeighbors(neighbor);
                
                if (infectedNeighbors > 0) {
                    atRiskNodes.add(neighbor);
                    infectionRates.put(neighbor, transmissionRate * infectedNeighbors);
                } else {
                    atRiskNodes.remove(neighbor);
                    infectionRates.remove(neighbor);
                }
            }
        }
    }
    
    /**
     * 確率に基づいてノードを選択
     */
    private int selectNodeByProbability(List<Integer> nodes, double[] probabilities, Random random) {
        double totalProb = 0.0;
        for (double prob : probabilities) {
            totalProb += prob;
        }
        
        double r = random.nextDouble() * totalProb;
        double cumulativeProb = 0.0;
        
        for (int i = 0; i < nodes.size(); i++) {
            cumulativeProb += probabilities[i];
            if (r <= cumulativeProb) {
                return nodes.get(i);
            }
        }
        
        return nodes.get(nodes.size() - 1);
    }
    
    /**
     * 指数分布の乱数を生成
     */
    private double exponentialVariate(double rate, Random random) {
        return -Math.log(1 - random.nextDouble()) / rate;
    }
    
    /**
     * 現在の状態を記録
     */
    private void recordState(double time) {
        times.add(time);
        susceptibleCounts.add(network.N - infectedNodes.size());
        infectedCounts.add(infectedNodes.size());
        recoveredCounts.add(0); // SARモデルでは回復ノードは追跡しない
    }
    
    /**
     * シミュレーション結果クラス
     */
    public static class SimulationResult {
        public final List<Double> times;
        public final List<Integer> susceptibleCounts;
        public final List<Integer> infectedCounts;
        public final List<Integer> recoveredCounts;
        
        public SimulationResult(List<Double> times, List<Integer> susceptibleCounts, 
                              List<Integer> infectedCounts, List<Integer> recoveredCounts) {
            this.times = times;
            this.susceptibleCounts = susceptibleCounts;
            this.infectedCounts = infectedCounts;
            this.recoveredCounts = recoveredCounts;
        }
        
        public void printResults() {
            System.out.println("=== SARシミュレーション結果 ===");
            System.out.println("時間\t感受性\t感染\t回復");
            for (int i = 0; i < times.size(); i++) {
                System.out.printf("%.3f\t%d\t%d\t%d%n", 
                    times.get(i), susceptibleCounts.get(i), 
                    infectedCounts.get(i), recoveredCounts.get(i));
            }
        }
    }
} 