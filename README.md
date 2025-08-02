# SARシミュレーション (Susceptible-Infected-Recovered)

ネットワーク上での感染症伝播をシミュレーションするJavaプロジェクトです。Gillespieアルゴリズムを使用して、様々なネットワークトポロジー（BA、ER、RR）での感染症の伝播を解析できます。

## 機能

- **ネットワークトポロジー**: BA（Barabási-Albert）、ER（Erdős-Rényi）、RR（Regular Random）ネットワーク
- **感染症モデル**: SAR（Susceptible-Infected-Recovered）モデル
- **シミュレーション**: Gillespieアルゴリズムによる確率的シミュレーション
- **結果出力**: JSON、CSV形式での結果保存
- **バッチ実行**: 複数パラメータでの一括シミュレーション
- **テスト機能**: JUnit 5による包括的なテストスイート

## ファイル構造

```
trend-effect-sar/
├── src/main/java/
│   ├── network/
│   │   ├── Graph.java              # ネットワークグラフクラス
│   │   └── topology/
│   │       ├── BA.java             # Barabási-Albertネットワーク
│   │       ├── ER.java             # Erdős-Rényiネットワーク
│   │       └── RR.java             # Regular Randomネットワーク
│   └── simulation/
│       ├── SARSimulator.java       # SARシミュレーター
│       ├── SARExample.java         # 使用例
│       ├── BatchSimulationRunner.java  # バッチ実行
│       └── SimulationDataWriter.java   # 結果書き出し
├── src/test/java/
│   ├── network/
│   │   └── NetworkTopologyTest.java    # ネットワークトポロジーテスト
│   └── simulation/
│       └── SimulationDataWriterTest.java # データ書き出しテスト
├── data/                           # シミュレーション結果（gitignore）
├── scripts/
│   └── run_batch_simulation.sh     # 実行スクリプト
└── pom.xml                        # Maven設定
```

## 使用方法

### 1. 単一シミュレーション

```bash
# コンパイル
mvn compile

# 実行例
mvn exec:java -Dexec.mainClass="main.java.simulation.SARExample"
```

### 2. バッチシミュレーション

```bash
# 実行スクリプトを使用
./scripts/run_batch_simulation.sh

# または直接実行
mvn exec:java -Dexec.mainClass="main.java.simulation.BatchSimulationRunner"
```

## 出力ファイル

バッチシミュレーション実行後、以下のファイルが生成されます：

- `data/sim1/params.json`: シミュレーションパラメータ設定
- `data/sim1/results.csv`: シミュレーション結果（CSV形式）
- `data/sim1/status.json`: 実行状態とシステム情報

### パラメータ設定例

```json
{
  "network_types": ["BA", "ER", "RR"],
  "network_sizes": [50, 100, 200],
  "transmission_rates": [0.2, 0.3, 0.4, 0.5],
  "recovery_rates": [0.1, 0.15, 0.2, 0.25],
  "initial_infections": [1, 2, 3, 5],
  "max_time": 50.0,
  "samples_per_config": 10
}
```

### 結果CSV形式

```csv
run_id,transmission_rate,recovery_rate,network_type,network_size,initial_infections,max_time,max_infected,final_infected,peak_time,total_events,simulation_time
1,0.200000,0.100000,0,50,1,50.000000,15,0,12.345,156,0.123
...
```

## 依存関係

- Java 11以上
- Maven 3.6以上
- Jackson (JSON処理)
- JUnit 5 (テスト)

## インストール

```bash
# リポジトリをクローン
git clone <repository-url>
cd trend-effect-sar

# 依存関係をインストール
mvn install
```

## 開発

### コンパイル

```bash
mvn compile
```

### テスト

```bash
# 全テスト実行
mvn test

# 特定のテストクラス実行
mvn test -Dtest=SimulationDataWriterTest
mvn test -Dtest=NetworkTopologyTest

# テスト結果の詳細表示
mvn test -Dsurefire.useFile=false
```

### クリーン

```bash
mvn clean
```

## テスト詳細

### SimulationDataWriterTest
- **パラメータ書き出しテスト**: JSON形式での設定保存
- **結果書き出しテスト**: CSV形式でのシミュレーション結果保存
- **ステータス書き出しテスト**: 実行状態のJSON保存
- **エッジケーステスト**: 空データの処理
- **ディレクトリ作成テスト**: 自動ディレクトリ作成機能

### NetworkTopologyTest
- **RR（Regular Random）テスト**: ランダムレギュラーグラフ生成
- **ER（Erdős-Rényi）テスト**: ランダムグラフ生成
- **BA（Barabási-Albert）テスト**: スケールフリーネットワーク生成
- **エラーケーステスト**: 無効なパラメータの処理
- **統計情報テスト**: グラフ特性の検証

### テスト実行結果
```bash
Tests run: 20, Failures: 0, Errors: 0, Skipped: 0
```

## ライセンス

このプロジェクトはMITライセンスの下で公開されています。
