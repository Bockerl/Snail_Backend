import pandas as pd
import matplotlib.pyplot as plt
import json

with open('build/reports/jmh/results.json') as f:
    data = json.load(f)
label_map = {
    "com.bockerl.snailmember.jmh.benchMark.KafkaSendBenchmark.sendWithExecutor": "Executor 방식",
    "com.bockerl.snailmember.jmh.benchMark.KafkaSendBenchmark.sendWithCompletableFuture": "CompletableFuture 방식",
}
df = pd.DataFrame(data)
df = df[['benchmark', 'primaryMetric']]
df['benchmark'] = df['benchmark'].map(label_map)
df['score'] = df['primaryMetric'].apply(lambda x: x['score'])
df['error'] = df['primaryMetric'].apply(lambda x: x['scoreError'])


print(df)
df['ops_per_sec'] = df['score'] * 1000
base = df.loc[df['benchmark'] == "Executor 방식", 'ops_per_sec'].values[0]
comp = df.loc[df['benchmark'] == "CompletableFuture 방식", 'ops_per_sec'].values[0]
improvement = ((comp - base) / base) * 100
print(f"CompletableFuture는 Executor보다 약 {improvement:.1f}% 빠름")

plt.savefig("benchmark_comparison.png", dpi=150)

# 그래프
plt.rcParams['font.family'] = 'AppleGothic'
plt.rcParams['axes.unicode_minus'] = False
plt.figure(figsize=(10, 6))
plt.barh(df['benchmark'], df['score'], xerr=df['error'], color='skyblue')
plt.xlabel("ops/ms (처리량)")
plt.title("Kafka 전송 처리량 비교 (JMH 벤치마크)")
plt.grid(True, axis='x', linestyle='--', alpha=0.7)
plt.tight_layout()
plt.show()