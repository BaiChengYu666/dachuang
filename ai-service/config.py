"""
AI服务配置文件
"""
import os

class Config:
    # Flask配置
    DEBUG = True
    HOST = '0.0.0.0'
    PORT = 5000
    
    # 模型配置
    MODEL_PATH = 'model.pth'
    
    # 特征配置
    FEATURE_NAMES = [
        'heartRate',           # 心率
        'bloodPressureHigh',   # 高压
        'bloodPressureLow',    # 低压
        'bloodOxygen',         # 血氧
        'bodyTemperature',     # 体温
        'age',                 # 年龄
        'gender'               # 性别 (0=女, 1=男)
    ]
    
    # 正常范围
    NORMAL_RANGES = {
        'heartRate': (60, 100),
        'bloodPressureHigh': (90, 140),
        'bloodPressureLow': (60, 90),
        'bloodOxygen': (95, 100),
        'bodyTemperature': (36.0, 37.5)
    }
    
    # 风险等级阈值
    RISK_THRESHOLDS = {
        'low': 30,      # 0-30: 低风险
        'medium': 60,   # 30-60: 中等风险
        'high': 100     # 60-100: 高风险
    }
    
    # 健康建议模板
    ADVICE_TEMPLATES = {
        'low': [
            '健康状态良好，请继续保持规律作息',
            '各项指标正常，建议适当运动增强体质',
            '当前状况稳定，保持良好生活习惯即可'
        ],
        'medium': [
            '存在一定健康风险，建议定期监测',
            '部分指标偏离正常范围，请注意休息',
            '建议咨询医生，进行进一步检查'
        ],
        'high': [
            '健康风险较高，建议立即就医检查',
            '多项指标异常，请尽快前往医院',
            '情况较为严重，建议联系家人并就医'
        ]
    }
    
    # 行为异常检测阈值
    BEHAVIOR_THRESHOLDS = {
        'max_inactive_time': 180,    # 最长静坐时间(分钟)
        'min_daily_steps': 1000,     # 最少日步数
        'max_fall_risk_score': 70    # 摔倒风险阈值
    }

# 开发环境配置
class DevelopmentConfig(Config):
    DEBUG = True

# 生产环境配置
class ProductionConfig(Config):
    DEBUG = False
    
# 默认配置
config = DevelopmentConfig()
