#!/bin/bash

# バッチシミュレーション実行スクリプト

echo "=== SARバッチシミュレーション実行 ==="

# Mavenでプロジェクトをビルド
echo "プロジェクトをビルド中..."
mvn clean compile

if [ $? -ne 0 ]; then
    echo "ビルドエラーが発生しました"
    exit 1
fi

# バッチシミュレーションを実行
echo "バッチシミュレーションを実行中..."
mvn exec:java -Dexec.mainClass="main.java.simulation.BatchSimulationRunner"

if [ $? -eq 0 ]; then
    echo "シミュレーション完了！"
    echo "結果ファイル:"
    echo "  - data/sim1/params.json (パラメータ設定)"
    echo "  - data/sim1/results.csv (シミュレーション結果)"
    echo "  - data/sim1/status.json (実行状態)"
else
    echo "シミュレーション実行中にエラーが発生しました"
    exit 1
fi 