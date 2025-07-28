#!/bin/bash

echo "=== RR、ER、BAの生成テストを実行します ==="

# プロジェクトのルートディレクトリに移動
cd "$(dirname "$0")/.."

# コンパイル
echo "コンパイル中..."
javac -cp "lib/*" -d bin src/main/java/network/*.java src/main/java/network/topology/*.java

if [ $? -ne 0 ]; then
    echo "コンパイルエラーが発生しました"
    exit 1
fi

echo "コンパイル完了"

# テスト実行
echo "テストを実行中..."
java -cp "bin:lib/*" org.junit.platform.console.ConsoleLauncher --class-path bin --select-class main.java.network.test

echo "=== テスト実行完了 ===" 