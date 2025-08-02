package network;

import main.java.network.Graph;
import main.java.network.topology.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class NetworkTopologyTest {
    
    @DisplayName("ランダムレギュラーグラフ（RR）の生成テスト")
    @ParameterizedTest(name = "ノード数={0}, 次数={1}")
    @CsvSource({
        "10, 2",
        "20, 4",
        "16, 3"
    })
    public void testRR(int N, int k) {
        assertDoesNotThrow(() -> {
            Graph graph = RR.generateRR(N, k);
            graph.printGraphInfo();
            
            // 基本的な検証
            assertNotNull(graph);
            assertEquals(N, graph.N);
            
            // 各ノードの次数がkであることを確認
            for (int i = 0; i < N; i++) {
                int degree = graph.cursorList[i] - graph.addressList[i];
                assertEquals(k, degree, "ノード " + i + " の次数が " + k + " ではありません: " + degree);
            }
        });
    }
    
    @DisplayName("ERモデルの生成テスト")
    @ParameterizedTest(name = "ノード数={0}, 確率={1}")
    @CsvSource({
        "10, 0.3",
        "20, 0.2",
        "30, 0.1"
    })
    public void testER(int N, double p) {
        assertDoesNotThrow(() -> {
            Graph graph = ER.generateER(N, p);
            graph.printGraphInfo();
            
            // 基本的な検証
            assertNotNull(graph);
            assertEquals(N, graph.N);
            
            // エッジが存在することを確認
            assertTrue(graph.edgeList.length > 0);
        });
    }
    
    @DisplayName("BAモデルの生成テスト")
    @ParameterizedTest(name = "ノード数={0}, 初期ノード数={1}, 接続数={2}")
    @CsvSource({
        "20, 5, 2",
        "30, 6, 3",
        "25, 6, 2"
    })
    public void testBA(int N, int m0, int m) {
        assertDoesNotThrow(() -> {
            Graph graph = BA.generateBA(N, m0, m);
            graph.printGraphInfo();
            
            // 基本的な検証
            assertNotNull(graph);
            assertEquals(N, graph.N);
            
            // 初期ノードの次数がm0-1以上であることを確認
            for (int i = 0; i < m0; i++) {
                int degree = graph.cursorList[i] - graph.addressList[i];
                assertTrue(degree >= m0 - 1, "初期ノード " + i + " の次数が不足: " + degree);
            }
            
            // 新規ノードの次数がm以上であることを確認（重複接続の可能性があるため）
            for (int i = m0; i < N; i++) {
                int degree = graph.cursorList[i] - graph.addressList[i];
                assertTrue(degree >= m, "新規ノード " + i + " の次数が " + m + " 未満です: " + degree);
            }
        });
    }
    
    @Test
    @DisplayName("RRモデルのエラーケーステスト")
    public void testRRErrorCases() {
        // 次数がノード数以上の場合
        assertThrows(IllegalArgumentException.class, () -> {
            RR.generateRR(10, 10);
        });
        
        // N*kが奇数の場合
        assertThrows(IllegalArgumentException.class, () -> {
            RR.generateRR(11, 3);
        });
        
        // 負の次数の場合
        assertThrows(IllegalArgumentException.class, () -> {
            RR.generateRR(10, -1);
        });
    }
    
    @Test
    @DisplayName("ERモデルのエラーケーステスト")
    public void testERErrorCases() {
        // 無効な確率
        assertThrows(IllegalArgumentException.class, () -> {
            ER.generateER(10, 1.5);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            ER.generateER(10, -0.1);
        });
        
        // 無効なノード数
        assertThrows(IllegalArgumentException.class, () -> {
            ER.generateER(0, 0.5);
        });
    }
    
    @Test
    @DisplayName("BAモデルのエラーケーステスト")
    public void testBAErrorCases() {
        // 無効な初期ノード数
        assertThrows(IllegalArgumentException.class, () -> {
            BA.generateBA(10, 0, 2);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            BA.generateBA(10, 15, 2);
        });
        
        // 無効な接続数
        assertThrows(IllegalArgumentException.class, () -> {
            BA.generateBA(10, 5, -1);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            BA.generateBA(10, 5, 6);
        });
    }
    
    @Test
    @DisplayName("各モデルの統計情報テスト")
    public void testGraphStatistics() {
        // RRテスト
        Graph rrGraph = RR.generateRR(20, 4);
        System.out.println("\n=== RRグラフ統計 ===");
        rrGraph.printGraphInfo();
        
        // ERテスト
        Graph erGraph = ER.generateER(20, 0.2);
        System.out.println("\n=== ERグラフ統計 ===");
        erGraph.printGraphInfo();
        
        // BAテスト
        Graph baGraph = BA.generateBA(20, 5, 2);
        System.out.println("\n=== BAグラフ統計 ===");
        baGraph.printGraphInfo();
        
        // 基本的な検証
        assertNotNull(rrGraph);
        assertNotNull(erGraph);
        assertNotNull(baGraph);
        
        assertEquals(20, rrGraph.N);
        assertEquals(20, erGraph.N);
        assertEquals(20, baGraph.N);
    }
} 