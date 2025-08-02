package simulation;

import main.java.simulation.SimulationDataWriter;
import main.java.simulation.SimulationDataWriter.SimulationRun;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * SimulationDataWriterクラスのテスト
 */
public class SimulationDataWriterTest {
    
    @TempDir
    Path tempDir;
    
    private String testOutputDir;
    
    @BeforeEach
    void setUp() {
        testOutputDir = tempDir.toString() + "/test_output";
    }
    
    @AfterEach
    void tearDown() throws IOException {
        // テスト後に作成されたファイルを削除
        if (Files.exists(Path.of(testOutputDir))) {
            Files.walk(Path.of(testOutputDir))
                .sorted((a, b) -> b.compareTo(a))
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        // 無視
                    }
                });
        }
    }
    
    @Test
    void testWriteParams() throws IOException {
        // テスト用のパラメータを作成
        Map<String, Object> params = new HashMap<>();
        params.put("transmissionRate", 0.3);
        params.put("recoveryRate", 0.1);
        params.put("networkSize", 1000);
        params.put("initialInfections", 10);
        params.put("maxTime", 100.0);
        
        // パラメータを書き出し
        SimulationDataWriter.writeParams(testOutputDir, params);
        
        // ファイルが作成されたことを確認
        File paramsFile = new File(testOutputDir + "/params.json");
        assertTrue(paramsFile.exists(), "パラメータファイルが作成されていません");
        assertTrue(paramsFile.length() > 0, "パラメータファイルが空です");
        
        // ファイルの内容を確認
        String content = Files.readString(paramsFile.toPath());
        assertTrue(content.contains("\"transmissionRate\""), "transmissionRateが含まれていません");
        assertTrue(content.contains("\"recoveryRate\""), "recoveryRateが含まれていません");
        assertTrue(content.contains("\"networkSize\""), "networkSizeが含まれていません");
    }
    
    @Test
    void testWriteResults() throws IOException {
        // テスト用のシミュレーション結果を作成
        List<SimulationRun> results = new ArrayList<>();
        results.add(new SimulationRun(1, 0.3, 0.1, 0, 1000, 10, 50.0, 500, 200, 25.0, 1000, 1.5));
        results.add(new SimulationRun(2, 0.4, 0.2, 1, 1000, 10, 60.0, 600, 300, 30.0, 1200, 2.0));
        
        // 結果を書き出し
        SimulationDataWriter.writeResults(testOutputDir, results);
        
        // ファイルが作成されたことを確認
        File resultsFile = new File(testOutputDir + "/results.csv");
        assertTrue(resultsFile.exists(), "結果ファイルが作成されていません");
        assertTrue(resultsFile.length() > 0, "結果ファイルが空です");
        
        // ファイルの内容を確認
        List<String> lines = Files.readAllLines(resultsFile.toPath());
        assertEquals(3, lines.size(), "ヘッダー + 2行のデータがあるはずです");
        
        // ヘッダーの確認
        assertTrue(lines.get(0).contains("run_id,transmission_rate,recovery_rate"));
        
        // データ行の確認
        assertTrue(lines.get(1).contains("1,0.300000,0.100000,0,1000,10,50.000000,500,200,25.000000,1000,1.500000"));
        assertTrue(lines.get(2).contains("2,0.400000,0.200000,1,1000,10,60.000000,600,300,30.000000,1200,2.000000"));
    }
    
    @Test
    void testWriteStatus() throws IOException {
        // テスト用のステータス情報を作成
        Map<String, Object> status = new HashMap<>();
        status.put("totalRuns", 10);
        status.put("completedRuns", 5);
        status.put("currentRun", 6);
        status.put("startTime", "2024-01-01T10:00:00");
        status.put("estimatedCompletion", "2024-01-01T12:00:00");
        
        // ステータスを書き出し
        SimulationDataWriter.writeStatus(testOutputDir, status);
        
        // ファイルが作成されたことを確認
        File statusFile = new File(testOutputDir + "/status.json");
        assertTrue(statusFile.exists(), "ステータスファイルが作成されていません");
        assertTrue(statusFile.length() > 0, "ステータスファイルが空です");
        
        // ファイルの内容を確認
        String content = Files.readString(statusFile.toPath());
        assertTrue(content.contains("\"totalRuns\""), "totalRunsが含まれていません");
        assertTrue(content.contains("\"completedRuns\""), "completedRunsが含まれていません");
        assertTrue(content.contains("\"currentRun\""), "currentRunが含まれていません");
    }
    
    @Test
    void testWriteParamsWithEmptyMap() throws IOException {
        // 空のマップでテスト
        Map<String, Object> emptyParams = new HashMap<>();
        
        SimulationDataWriter.writeParams(testOutputDir, emptyParams);
        
        File paramsFile = new File(testOutputDir + "/params.json");
        assertTrue(paramsFile.exists(), "空のパラメータでもファイルが作成されるべきです");
        
        String content = Files.readString(paramsFile.toPath());
        assertTrue(content.trim().equals("{}") || content.trim().equals("{ }"), 
                  "空のマップは空のJSONオブジェクトになるべきです: " + content.trim());
    }
    
    @Test
    void testWriteResultsWithEmptyList() throws IOException {
        // 空のリストでテスト
        List<SimulationRun> emptyResults = new ArrayList<>();
        
        SimulationDataWriter.writeResults(testOutputDir, emptyResults);
        
        File resultsFile = new File(testOutputDir + "/results.csv");
        assertTrue(resultsFile.exists(), "空の結果でもファイルが作成されるべきです");
        
        List<String> lines = Files.readAllLines(resultsFile.toPath());
        assertEquals(1, lines.size(), "ヘッダーのみが存在するべきです");
        assertTrue(lines.get(0).contains("run_id,transmission_rate,recovery_rate"));
    }
    
    @Test
    void testSimulationRunConstructor() {
        // SimulationRunクラスのコンストラクタをテスト
        SimulationRun run = new SimulationRun(1, 0.3, 0.1, 0, 1000, 10, 50.0, 500, 200, 25.0, 1000, 1.5);
        
        assertEquals(1, run.runId);
        assertEquals(0.3, run.transmissionRate, 0.001);
        assertEquals(0.1, run.recoveryRate, 0.001);
        assertEquals(0, run.networkType);
        assertEquals(1000, run.networkSize);
        assertEquals(10, run.initialInfections);
        assertEquals(50.0, run.maxTime, 0.001);
        assertEquals(500, run.maxInfected);
        assertEquals(200, run.finalInfected);
        assertEquals(25.0, run.peakTime, 0.001);
        assertEquals(1000, run.totalEvents);
        assertEquals(1.5, run.simulationTime, 0.001);
    }
    
    @Test
    void testDirectoryCreation() throws IOException {
        // 存在しないディレクトリでも作成されることをテスト
        String nonExistentDir = testOutputDir + "/subdir/nested";
        
        Map<String, Object> params = new HashMap<>();
        params.put("test", "value");
        
        SimulationDataWriter.writeParams(nonExistentDir, params);
        
        File paramsFile = new File(nonExistentDir + "/params.json");
        assertTrue(paramsFile.exists(), "ネストしたディレクトリでもファイルが作成されるべきです");
    }
} 