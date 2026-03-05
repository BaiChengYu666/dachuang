"""
训练Transformer健康风险预测模型
"""
import pandas as pd
import numpy as np
import torch
import torch.nn as nn
from torch.utils.data import Dataset, DataLoader
from model import HealthTransformer
import matplotlib.pyplot as plt
from sklearn.model_selection import train_test_split

# 设置随机种子
torch.manual_seed(42)
np.random.seed(42)

class HealthDataset(Dataset):
    """健康数据集"""
    def __init__(self, features, labels):
        self.features = torch.FloatTensor(features)
        self.labels = torch.FloatTensor(labels)
    
    def __len__(self):
        return len(self.features)
    
    def __getitem__(self, idx):
        return self.features[idx], self.labels[idx]

def generate_vital_signs(row):
    """
    基于疾病和年龄生成合理的生理指标
    返回: [心率, 高压, 低压, 血氧, 体温]
    """
    age = row['Age']
    condition = row['Medical Condition']
    test_result = row['Test Results']
    
    # 基础值（正常情况）
    heart_rate = np.random.normal(75, 8)
    bp_high = np.random.normal(120, 10)
    bp_low = np.random.normal(80, 8)
    oxygen = np.random.normal(98, 1)
    temp = np.random.normal(36.6, 0.3)
    
    # 年龄因素
    if age > 70:
        heart_rate += np.random.uniform(5, 15)
        bp_high += np.random.uniform(10, 30)
    elif age > 60:
        heart_rate += np.random.uniform(0, 10)
        bp_high += np.random.uniform(5, 20)
    
    # 疾病因素
    if condition == 'Hypertension':  # 高血压
        bp_high += np.random.uniform(20, 50)
        bp_low += np.random.uniform(10, 30)
        heart_rate += np.random.uniform(5, 15)
    
    elif condition == 'Diabetes':  # 糖尿病
        bp_high += np.random.uniform(10, 30)
        heart_rate += np.random.uniform(5, 20)
    
    elif condition == 'Cancer':  # 癌症
        if test_result == 'Abnormal':
            heart_rate += np.random.uniform(10, 25)
            oxygen -= np.random.uniform(2, 8)
            temp += np.random.uniform(0.5, 2)
    
    elif condition == 'Asthma':  # 哮喘
        oxygen -= np.random.uniform(1, 5)
        heart_rate += np.random.uniform(5, 15)
    
    elif condition == 'Arthritis':  # 关节炎
        # 影响较小
        heart_rate += np.random.uniform(0, 5)
    
    elif condition == 'Obesity':  # 肥胖
        bp_high += np.random.uniform(10, 25)
        bp_low += np.random.uniform(5, 15)
        heart_rate += np.random.uniform(5, 10)
    
    # 测试结果因素
    if test_result == 'Abnormal':
        heart_rate += np.random.uniform(5, 15)
        if np.random.rand() > 0.5:
            oxygen -= np.random.uniform(1, 4)
        if np.random.rand() > 0.7:
            temp += np.random.uniform(0.3, 1.5)
    
    # 确保在合理范围内
    heart_rate = np.clip(heart_rate, 50, 150)
    bp_high = np.clip(bp_high, 80, 200)
    bp_low = np.clip(bp_low, 50, 120)
    oxygen = np.clip(oxygen, 85, 100)
    temp = np.clip(temp, 35.5, 39.5)
    
    return [heart_rate, bp_high, bp_low, oxygen, temp]

def calculate_risk_score(row, vital_signs):
    """
    计算风险评分（0-100）
    """
    score = 0
    age = row['Age']
    condition = row['Medical Condition']
    test_result = row['Test Results']
    
    hr, bp_h, bp_l, o2, temp = vital_signs
    
    # 生理指标异常
    if hr < 60 or hr > 100:
        score += 20
    if bp_h > 140 or bp_l > 90:
        score += 25
    if o2 < 95:
        score += 30
    if temp > 37.5 or temp < 36:
        score += 15
    
    # 疾病严重度
    severity_map = {
        'Cancer': 40,
        'Hypertension': 30,
        'Diabetes': 25,
        'Asthma': 20,
        'Obesity': 15,
        'Arthritis': 10
    }
    score += severity_map.get(condition, 10)
    
    # 年龄因素
    if age > 75:
        score += 15
    elif age > 65:
        score += 10
    elif age > 55:
        score += 5
    
    # 测试结果
    if test_result == 'Abnormal':
        score += 20
    elif test_result == 'Inconclusive':
        score += 10
    
    return min(score, 100)

def prepare_data(csv_path):
    """准备训练数据"""
    print("=" * 60)
    print("📊 加载数据集...")
    df = pd.read_csv(csv_path)
    print(f"✅ 加载完成: {len(df)} 条记录")
    
    print("\n📊 生成生理指标...")
    features_list = []
    labels_list = []
    
    for idx, row in df.iterrows():
        # 生成生理指标
        vital_signs = generate_vital_signs(row)
        
        # 性别 (Male=1, Female=0)
        gender = 1 if row['Gender'] == 'Male' else 0
        
        # 完整特征: [心率, 高压, 低压, 血氧, 体温, 年龄, 性别]
        features = vital_signs + [row['Age'], gender]
        
        # 计算风险评分
        risk_score = calculate_risk_score(row, vital_signs)
        
        features_list.append(features)
        labels_list.append(risk_score)
        
        if (idx + 1) % 1000 == 0:
            print(f"  已处理: {idx + 1}/{len(df)}")
    
    print(f"✅ 数据处理完成: {len(features_list)} 条训练样本")
    
    return np.array(features_list), np.array(labels_list)

def train_model(features, labels, epochs=50, batch_size=32):
    """训练模型"""
    print("\n" + "=" * 60)
    print("🤖 开始训练Transformer模型...")
    print("=" * 60)
    
    # 划分训练集和测试集
    X_train, X_test, y_train, y_test = train_test_split(
        features, labels, test_size=0.2, random_state=42
    )
    
    print(f"\n📊 数据划分:")
    print(f"  训练集: {len(X_train)} 条")
    print(f"  测试集: {len(X_test)} 条")
    
    # 创建数据加载器
    train_dataset = HealthDataset(X_train, y_train)
    test_dataset = HealthDataset(X_test, y_test)
    
    train_loader = DataLoader(train_dataset, batch_size=batch_size, shuffle=True)
    test_loader = DataLoader(test_dataset, batch_size=batch_size)
    
    # 创建模型
    model = HealthTransformer(input_dim=7, hidden_dim=64, num_heads=4, num_layers=2)
    
    # 损失函数和优化器
    criterion = nn.MSELoss()
    optimizer = torch.optim.Adam(model.parameters(), lr=0.001)
    
    # 训练历史
    train_losses = []
    test_losses = []
    
    print(f"\n🔧 训练参数:")
    print(f"  Epochs: {epochs}")
    print(f"  Batch Size: {batch_size}")
    print(f"  Learning Rate: 0.001")
    print(f"  优化器: Adam")
    
    print("\n" + "=" * 60)
    print("开始训练...")
    print("=" * 60)
    
    # 训练循环
    for epoch in range(epochs):
        model.train()
        train_loss = 0
        
        for features_batch, labels_batch in train_loader:
            # 添加序列维度 (batch, seq_len=1, features)
            features_batch = features_batch.unsqueeze(1)
            
            # 前向传播
            outputs = model(features_batch).squeeze()
            loss = criterion(outputs, labels_batch)
            
            # 反向传播
            optimizer.zero_grad()
            loss.backward()
            optimizer.step()
            
            train_loss += loss.item()
        
        # 计算平均损失
        train_loss /= len(train_loader)
        train_losses.append(train_loss)
        
        # 测试集评估
        model.eval()
        test_loss = 0
        with torch.no_grad():
            for features_batch, labels_batch in test_loader:
                features_batch = features_batch.unsqueeze(1)
                outputs = model(features_batch).squeeze()
                loss = criterion(outputs, labels_batch)
                test_loss += loss.item()
        
        test_loss /= len(test_loader)
        test_losses.append(test_loss)
        
        # 打印进度
        if (epoch + 1) % 5 == 0 or epoch == 0:
            print(f"Epoch [{epoch+1}/{epochs}] - "
                  f"训练损失: {train_loss:.4f} | "
                  f"测试损失: {test_loss:.4f}")
    
    print("\n" + "=" * 60)
    print("✅ 训练完成！")
    print("=" * 60)
    
    # 保存模型
    torch.save(model.state_dict(), 'model.pth')
    print("\n💾 模型已保存: model.pth")
    
    # 绘制训练曲线
    plt.figure(figsize=(10, 5))
    plt.plot(train_losses, label='训练损失')
    plt.plot(test_losses, label='测试损失')
    plt.xlabel('Epoch')
    plt.ylabel('损失 (MSE)')
    plt.title('Transformer训练曲线')
    plt.legend()
    plt.grid(True)
    plt.savefig('training_curve.png')
    print("📊 训练曲线已保存: training_curve.png")
    
    # 模型评估
    print("\n" + "=" * 60)
    print("📊 模型评估:")
    print("=" * 60)
    
    model.eval()
    predictions = []
    actuals = []
    
    with torch.no_grad():
        for features_batch, labels_batch in test_loader:
            features_batch = features_batch.unsqueeze(1)
            outputs = model(features_batch).squeeze()
            predictions.extend(outputs.numpy())
            actuals.extend(labels_batch.numpy())
    
    predictions = np.array(predictions)
    actuals = np.array(actuals)
    
    # 计算指标
    mae = np.mean(np.abs(predictions - actuals))
    rmse = np.sqrt(np.mean((predictions - actuals) ** 2))
    
    print(f"  平均绝对误差 (MAE): {mae:.2f}")
    print(f"  均方根误差 (RMSE): {rmse:.2f}")
    
    # 测试几个样本
    print("\n📋 测试样本预测:")
    print("-" * 60)
    for i in range(min(5, len(predictions))):
        print(f"  样本 {i+1}: 预测={predictions[i]:.1f}, 实际={actuals[i]:.1f}, "
              f"误差={abs(predictions[i]-actuals[i]):.1f}")
    
    return model, train_losses, test_losses

if __name__ == '__main__':
    # 准备数据
    features, labels = prepare_data('healthcare_dataset.csv')
    
    # 训练模型
    model, train_losses, test_losses = train_model(
        features, labels,
        epochs=50,
        batch_size=32
    )
    
    print("\n" + "=" * 60)
    print("🎉 训练完成！")
    print("=" * 60)
    print("\n📝 下一步:")
    print("  1. 重启AI服务: python app.py")
    print("  2. 模型会自动加载 model.pth")
    print("  3. 前端评估将使用训练好的Transformer!")
    print("\n" + "=" * 60)
