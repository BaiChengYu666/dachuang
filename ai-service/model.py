"""
健康风险预测Transformer模型
"""
import torch
import torch.nn as nn
import numpy as np
from config import config
import random

class HealthTransformer(nn.Module):
    """
    基于Transformer的健康风险预测模型
    输入: 7维特征 [心率, 高压, 低压, 血氧, 体温, 年龄, 性别]
    输出: 风险评分 0-100
    """
    def __init__(self, input_dim=7, hidden_dim=64, num_heads=4, num_layers=2):
        super(HealthTransformer, self).__init__()
        
        # 输入嵌入层
        self.embedding = nn.Linear(input_dim, hidden_dim)
        self.pos_encoding = nn.Parameter(torch.randn(1, 10, hidden_dim))
        
        # Transformer编码器
        encoder_layer = nn.TransformerEncoderLayer(
            d_model=hidden_dim,
            nhead=num_heads,
            dim_feedforward=hidden_dim * 4,
            dropout=0.1,
            batch_first=True
        )
        self.transformer = nn.TransformerEncoder(encoder_layer, num_layers=num_layers)
        
        # 输出层
        self.fc1 = nn.Linear(hidden_dim, 32)
        self.fc2 = nn.Linear(32, 1)
        self.relu = nn.ReLU()
        self.sigmoid = nn.Sigmoid()
    
    def forward(self, x):
        """
        前向传播
        x: (batch_size, seq_len, input_dim)
        """
        # 嵌入
        x = self.embedding(x)  # (batch, seq, hidden)
        
        # 添加位置编码
        x = x + self.pos_encoding[:, :x.size(1), :]
        
        # Transformer编码
        x = self.transformer(x)  # (batch, seq, hidden)
        
        # 取最后一个时间步
        x = x[:, -1, :]  # (batch, hidden)
        
        # 全连接层
        x = self.relu(self.fc1(x))
        x = self.fc2(x)
        
        # 输出0-100的风险评分
        risk = self.sigmoid(x) * 100
        
        return risk


def predict_risk(features, model=None):
    """
    预测健康风险
    
    Args:
        features: [心率, 高压, 低压, 血氧, 体温, 年龄, 性别]
        model: Transformer模型（可选）
    
    Returns:
        risk_score: 风险评分 0-100
        risk_level: 风险等级 low/medium/high
        advice: 健康建议
        details: 详细分析
    """
    
    # 使用规则引擎计算风险（模型未加载时的后备方案）
    risk_score, details = calculate_rule_based_risk(features)
    
    # 如果有模型，可以结合模型预测
    if model is not None:
        try:
            # 准备输入数据
            x = torch.tensor([features], dtype=torch.float32).unsqueeze(1)
            with torch.no_grad():
                model_score = model(x).item()
            
            # 结合规则和模型（加权平均）
            risk_score = risk_score * 0.6 + model_score * 0.4
        except Exception as e:
            print(f"模型预测失败，使用规则引擎: {e}")
    
    # 确定风险等级
    if risk_score < config.RISK_THRESHOLDS['low']:
        risk_level = 'low'
        advice_list = config.ADVICE_TEMPLATES['low']
    elif risk_score < config.RISK_THRESHOLDS['medium']:
        risk_level = 'medium'
        advice_list = config.ADVICE_TEMPLATES['medium']
    else:
        risk_level = 'high'
        advice_list = config.ADVICE_TEMPLATES['high']
    
    # 随机选择建议
    advice = random.choice(advice_list)
    
    # 添加具体建议
    if details['abnormal_items']:
        advice += f"\n异常指标: {', '.join(details['abnormal_items'])}"
    
    return risk_score, risk_level, advice, details


def calculate_rule_based_risk(features):
    """
    基于规则的风险评分（专家系统）
    
    Args:
        features: [心率, 高压, 低压, 血氧, 体温, 年龄, 性别]
    
    Returns:
        risk_score: 风险评分
        details: 详细分析
    """
    heart_rate, bp_high, bp_low, oxygen, temp, age, gender = features
    
    risk = 0
    abnormal_items = []
    warnings = []
    
    # 1. 心率异常检测
    hr_min, hr_max = config.NORMAL_RANGES['heartRate']
    if heart_rate < hr_min:
        risk += 20
        abnormal_items.append('心率过低')
        warnings.append(f'心率{heart_rate}bpm低于正常范围，可能存在心动过缓')
    elif heart_rate > hr_max:
        risk += 25
        abnormal_items.append('心率过高')
        warnings.append(f'心率{heart_rate}bpm高于正常范围，建议休息并监测')
    
    # 2. 血压异常检测
    bph_min, bph_max = config.NORMAL_RANGES['bloodPressureHigh']
    bpl_min, bpl_max = config.NORMAL_RANGES['bloodPressureLow']
    
    if bp_high > bph_max:
        risk += 30
        abnormal_items.append('高压偏高')
        warnings.append(f'收缩压{bp_high}mmHg偏高，可能有高血压风险')
    elif bp_high < bph_min:
        risk += 20
        abnormal_items.append('高压偏低')
        warnings.append(f'收缩压{bp_high}mmHg偏低，注意低血压症状')
    
    if bp_low > bpl_max:
        risk += 25
        abnormal_items.append('低压偏高')
        warnings.append(f'舒张压{bp_low}mmHg偏高')
    elif bp_low < bpl_min:
        risk += 15
        abnormal_items.append('低压偏低')
        warnings.append(f'舒张压{bp_low}mmHg偏低')
    
    # 3. 血氧检测
    o2_min, o2_max = config.NORMAL_RANGES['bloodOxygen']
    if oxygen < o2_min:
        risk += 35
        abnormal_items.append('血氧偏低')
        warnings.append(f'血氧{oxygen}%低于正常，可能缺氧，建议就医')
    
    # 4. 体温检测
    temp_min, temp_max = config.NORMAL_RANGES['bodyTemperature']
    if temp > temp_max:
        risk += 20
        abnormal_items.append('体温偏高')
        warnings.append(f'体温{temp}°C偏高，注意是否发热')
    elif temp < temp_min:
        risk += 15
        abnormal_items.append('体温偏低')
        warnings.append(f'体温{temp}°C偏低，注意保暖')
    
    # 5. 年龄因素
    if age > 80:
        risk += 10
        warnings.append('高龄人群，建议加强日常监护')
    elif age > 70:
        risk += 5
    
    # 6. 综合风险评估
    # 多项指标异常时，风险叠加
    if len(abnormal_items) >= 3:
        risk += 15
        warnings.append('多项指标异常，建议立即就医')
    
    # 限制最高分
    risk = min(risk, 100)
    
    details = {
        'abnormal_items': abnormal_items,
        'warnings': warnings,
        'abnormal_count': len(abnormal_items)
    }
    
    return risk, details


def detect_fall_risk(behavior_data):
    """
    摔倒风险检测
    
    Args:
        behavior_data: 行为数据列表
        [
            {"activity": "walking", "duration": 30, "timestamp": "..."},
            {"activity": "sitting", "duration": 120, "timestamp": "..."}
        ]
    
    Returns:
        fall_risk_score: 摔倒风险评分 0-100
        has_risk: 是否有风险
        suggestion: 建议
    """
    
    fall_risk_score = 0
    risk_factors = []
    
    # 统计各类活动时长
    activity_stats = {
        'sitting': 0,
        'walking': 0,
        'standing': 0,
        'falling': 0
    }
    
    for item in behavior_data:
        activity = item.get('activity', '').lower()
        duration = item.get('duration', 0)
        
        if activity in activity_stats:
            activity_stats[activity] += duration
    
    # 1. 检测久坐不动
    if activity_stats['sitting'] > config.BEHAVIOR_THRESHOLDS['max_inactive_time']:
        fall_risk_score += 30
        risk_factors.append('长时间静坐')
    
    # 2. 检测活动量过少
    if activity_stats['walking'] < 10:
        fall_risk_score += 20
        risk_factors.append('活动量不足')
    
    # 3. 检测是否有摔倒记录
    if activity_stats['falling'] > 0:
        fall_risk_score += 50
        risk_factors.append('近期有摔倒记录')
    
    # 综合评估
    has_risk = fall_risk_score > config.BEHAVIOR_THRESHOLDS['max_fall_risk_score']
    
    if has_risk:
        suggestion = f"检测到摔倒风险因素: {', '.join(risk_factors)}。建议增加活动，避免久坐，注意行走安全。"
    else:
        suggestion = "当前行为模式正常，继续保持适度活动。"
    
    return fall_risk_score, has_risk, suggestion, risk_factors


def generate_forecast(historical_data, age, gender_val, model=None, hours=24):
    """
    Generate hourly physiological forecast for next N hours using trend extrapolation.

    Args:
        historical_data: list of dicts with keys heartRate/bloodPressureHigh/bloodPressureLow/bloodOxygen/bodyTemperature
        age: int
        gender_val: 0 (female) or 1 (male)
        model: HealthTransformer (optional)
        hours: forecast horizon 1-48

    Returns:
        forecast_list: list of hourly predicted dicts
        attention_weights: list of importance weights for historical points
        summary: dict with trend summary
    """
    if not historical_data:
        return [], [], {}

    # Convert to numpy array columns: [hr, bp_high, bp_low, oxygen, temp]
    hist_arr = np.array([
        [
            float(d.get('heartRate', 72)),
            float(d.get('bloodPressureHigh', 120)),
            float(d.get('bloodPressureLow', 80)),
            float(d.get('bloodOxygen', 98)),
            float(d.get('bodyTemperature', 36.5))
        ]
        for d in historical_data
    ])

    n_hist = len(hist_arr)
    t = np.arange(n_hist, dtype=float)

    normal_ranges = [
        (55.0, 105.0),   # heart rate
        (100.0, 155.0),  # bp_high
        (60.0, 100.0),   # bp_low
        (90.0, 100.0),   # blood oxygen
        (36.0, 37.5)     # temperature
    ]
    noise_scales = [1.5, 2.0, 1.5, 0.3, 0.08]

    # Compute damped linear trend for each metric
    trends = []
    for i in range(5):
        slope = float(np.polyfit(t, hist_arr[:, i], 1)[0]) * 0.2 if n_hist > 1 else 0.0
        trends.append(slope)

    # Seed based on last observation for reproducibility
    np.random.seed(int(np.sum(hist_arr[-1]) * 100) % 10000)
    last = hist_arr[-1].copy()
    forecast_list = []

    for h in range(hours):
        new_point = []
        for i in range(5):
            mean_normal = (normal_ranges[i][0] + normal_ranges[i][1]) / 2.0
            # Trend with mean reversion
            val = last[i] + trends[i] - 0.05 * (last[i] - mean_normal)
            val += np.random.normal(0, noise_scales[i])
            val = float(np.clip(val, normal_ranges[i][0], normal_ranges[i][1]))
            new_point.append(round(val, 1))

        features_full = new_point + [float(age), float(gender_val)]
        risk_score, risk_level, _, _ = predict_risk(features_full, model)

        forecast_list.append({
            'hour': h + 1,
            'heartRate': int(round(new_point[0])),
            'bloodPressureHigh': int(round(new_point[1])),
            'bloodPressureLow': int(round(new_point[2])),
            'bloodOxygen': round(new_point[3], 1),
            'bodyTemperature': round(new_point[4], 1),
            'riskScore': round(float(risk_score), 1),
            'riskLevel': risk_level
        })
        last = np.array(new_point)

    # Compute attention weights: recent points weighted more, anomalies boosted
    weights = np.exp(np.linspace(-2.0, 0.0, n_hist))
    for i in range(n_hist):
        row = hist_arr[i]
        anomaly = 0.0
        if row[0] < 60 or row[0] > 100: anomaly += 0.4
        if row[1] > 140 or row[1] < 100: anomaly += 0.3
        if row[2] > 95 or row[2] < 65:  anomaly += 0.2
        if row[3] < 95:                  anomaly += 0.5
        if row[4] > 37.3 or row[4] < 36.0: anomaly += 0.3
        weights[i] += anomaly
    attention_weights = (weights / weights.sum()).tolist()

    # Summary
    last_hist = hist_arr[-1]
    last_fc = forecast_list[-1]

    def trend_dir(cur, fut, thresh=5):
        return 'rising' if fut > cur + thresh else 'falling' if fut < cur - thresh else 'stable'

    summary = {
        'hrTrend': trend_dir(last_hist[0], last_fc['heartRate']),
        'bpTrend': trend_dir(last_hist[1], last_fc['bloodPressureHigh']),
        'maxRisk': max(p['riskScore'] for p in forecast_list),
        'avgRisk': round(sum(p['riskScore'] for p in forecast_list) / hours, 1),
        'highRiskHours': [p['hour'] for p in forecast_list if p['riskLevel'] == 'high']
    }

    return forecast_list, attention_weights, summary


# 测试代码
if __name__ == '__main__':
    print("=" * 50)
    print("健康风险预测模型测试")
    print("=" * 50)
    
    # 测试1: 正常健康状态
    print("\n【测试1: 正常健康老人】")
    normal_features = [72, 120, 80, 98, 36.5, 75, 0]
    score, level, advice, details = predict_risk(normal_features)
    print(f"风险评分: {score:.1f}")
    print(f"风险等级: {level}")
    print(f"建议: {advice}")
    print(f"异常项: {details['abnormal_items']}")
    
    # 测试2: 高风险状态
    print("\n【测试2: 高风险老人】")
    high_risk_features = [105, 160, 100, 88, 38.2, 82, 1]
    score, level, advice, details = predict_risk(high_risk_features)
    print(f"风险评分: {score:.1f}")
    print(f"风险等级: {level}")
    print(f"建议: {advice}")
    print(f"异常项: {details['abnormal_items']}")
    print(f"警告: {details['warnings']}")
    
    # 测试3: 摔倒风险检测
    print("\n【测试3: 摔倒风险检测】")
    behavior_data = [
        {"activity": "sitting", "duration": 200},
        {"activity": "walking", "duration": 5}
    ]
    fall_score, has_risk, suggestion, factors = detect_fall_risk(behavior_data)
    print(f"摔倒风险评分: {fall_score}")
    print(f"是否有风险: {has_risk}")
    print(f"风险因素: {factors}")
    print(f"建议: {suggestion}")
