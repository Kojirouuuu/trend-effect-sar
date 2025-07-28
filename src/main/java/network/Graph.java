package main.java.network;

/**
 * グラフ構造を表現するクラス
 * ネットワークのノードとエッジの情報を管理
 */
public class Graph {
    public int N;              // ノード数
    public int[] edgeList;   // 各ノードの隣接ノードリスト
    public int[] addressList;  // 各ノードのアドレス情報
    public int[] cursorList;   // 各ノードの現在の隣接ノード数

    /**
     * グラフの基本情報を表示
     */
    public void printGraphInfo() {
        System.out.println("--------------------------------");
        System.out.println("ノード数: " + N);
        
        if (N > 0 && cursorList != null) {
            int totalEdges = 0;
            int degreeI = 0;
            int maxDegree = 0;
            int minDegree = Integer.MAX_VALUE;
            
            for (int i = 0; i < N; i++) {
                degreeI = 0; // Reset degreeI for the current node
                for (int j = 0; j < cursorList[i] - addressList[i]; j++) {
                    totalEdges += 1;
                    degreeI += 1;
                }
                maxDegree = Math.max(maxDegree, degreeI);
                minDegree = Math.min(minDegree, degreeI);
            }
            
            System.out.println("総エッジ数: " + ((int)totalEdges / 2));
            System.out.println("最大次数: " + maxDegree);
            System.out.println("最小次数: " + minDegree);
            System.out.println("平均次数: " + (double)totalEdges / N);
            System.out.println("");
        }
    }

    /**
     * 指定されたノードの隣接ノードを表示
     * @param nodeId ノードID
     */
    public void printNodeNeighbors(int nodeId) {
        if (nodeId < 0 || nodeId >= N) {
            System.out.println("無効なノードID: " + nodeId);
            return;
        }
        
        System.out.print("ノード " + nodeId + " の隣接ノード: ");
        for (int i = addressList[nodeId]; i < cursorList[nodeId]; i++) {
            System.out.print(edgeList[i] + " ");
        }
        System.out.println();
    }
}
