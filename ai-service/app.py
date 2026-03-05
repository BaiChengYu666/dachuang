"""
AI健康风险预测服务
Flask RESTful API
"""
from flask import Flask, request, jsonify
from flask_cors import CORS
import torch
from datetime import datetime
import os

from config import config
from model import HealthTransformer, predict_risk, detect_fall_risk, generate_forecast

# 创建Flask应用
app = Flask(__name__)
CORS(app, resources={r"/*": {"origins": "*", "methods": ["GET", "POST", "OPTIONS"], "allow_headers": "*"}})

# 全局模型实例
ai_model = None

# ============ 健康检查接口 ============

@app.route('/health', methods=['GET'])
def health_check():
    """服务健康检查"""
    return jsonify({
        'status': 'ok',
        'service': 'AI Health Risk Prediction Service',
        'version': '1.0.0',
        'timestamp': datetime.now().isoformat(),
        'model_loaded': ai_model is not None
    })

@app.route('/', methods=['GET'])
def index():
    """API根路径"""
    return jsonify({
        'message': '欢迎使用AI健康风险预测服务',
        'endpoints': {
            'health_check': '/health',
            'predict': '/predict',
            'predict_risk': '/predict/risk',
            'predict_behavior': '/predict/behavior',
            'batch_predict': '/predict/batch',
            'predict_forecast': '/predict/forecast'
        },
        'documentation': 'https://github.com/your-repo/ai-service'
    })

# ============ 预测接口 ============

@app.route('/predict', methods=['POST'])
def predict():
    """前端兼容接口"""
    try:
        data = request.json
        features = data.get('features', [])

        # 调用模型预测
        risk_score, risk_level, advice, details = predict_risk(features, ai_model)

        # 返回结果
        return jsonify({
            'risk_score': float(risk_score),
            'risk_level': risk_level,
            'advice': advice,
            'details': details
        })
    except Exception as e:
        return jsonify({
            'risk_score': 0,
            'risk_level': 'low',
            'advice': f'预测失败: {str(e)}',
            'details': {
                'abnormal_items': [],
                'warnings': [],
                'abnormal_count': 0
            }
        })

@app.route('/predict/risk', methods=['POST'])
def predict_health_risk():
    """健康风险预测（完整版）"""
    try:
        data = request.json

        if not data:
            return jsonify({
                'code': 400,
                'message': '请求体不能为空'
            }), 400

        # 提取特征
        heart_rate = float(data.get('heartRate', 72))
        bp_high = float(data.get('bloodPressureHigh', 120))
        bp_low = float(data.get('bloodPressureLow', 80))
        oxygen = float(data.get('bloodOxygen', 98))
        temp = float(data.get('bodyTemperature', 36.5))
        age = int(data.get('age', 75))
        gender_str = data.get('gender', 'female')

        # 性别转换
        gender = 1 if gender_str.lower() == 'male' else 0

        # 构建特征向量
        features = [heart_rate, bp_high, bp_low, oxygen, temp, age, gender]

        # 调用模型预测
        risk_score, risk_level, advice, details = predict_risk(features, ai_model)

        return jsonify({
            'code': 200,
            'message': '预测成功',
            'data': {
                'riskScore': int(risk_score),
                'riskLevel': risk_level,
                'advice': advice,
                'details': {
                    'abnormalItems': details['abnormal_items'],
                    'warnings': details['warnings'],
                    'abnormalCount': details['abnormal_count'],
                    'timestamp': datetime.now().isoformat()
                }
            }
        })

    except ValueError as e:
        return jsonify({
            'code': 400,
            'message': f'数据格式错误: {str(e)}'
        }), 400

    except Exception as e:
        app.logger.error(f'预测失败: {str(e)}')
        return jsonify({
            'code': 500,
            'message': f'服务器错误: {str(e)}'
        }), 500

@app.route('/predict/behavior', methods=['POST'])
def predict_behavior_anomaly():
    """行为异常检测 & 摔倒风险预测"""
    try:
        data = request.json

        if not data or 'behaviorData' not in data:
            return jsonify({
                'code': 400,
                'message': '缺少behaviorData字段'
            }), 400

        behavior_data = data['behaviorData']

        if not isinstance(behavior_data, list):
            return jsonify({
                'code': 400,
                'message': 'behaviorData必须是数组'
            }), 400

        # 调用摔倒风险检测
        fall_score, has_risk, suggestion, risk_factors = detect_fall_risk(behavior_data)

        return jsonify({
            'code': 200,
            'message': '检测成功',
            'data': {
                'fallRiskScore': int(fall_score),
                'hasRisk': has_risk,
                'riskFactors': risk_factors,
                'suggestion': suggestion,
                'timestamp': datetime.now().isoformat()
            }
        })

    except Exception as e:
        app.logger.error(f'行为检测失败: {str(e)}')
        return jsonify({
            'code': 500,
            'message': f'服务器错误: {str(e)}'
        }), 500

@app.route('/predict/batch', methods=['POST'])
def batch_predict():
    """批量预测"""
    try:
        data = request.json
        records = data.get('records', [])

        if not records:
            return jsonify({
                'code': 400,
                'message': '请提供至少一条记录'
            }), 400

        results = []

        for record in records:
            heart_rate = float(record.get('heartRate', 72))
            bp_high = float(record.get('bloodPressureHigh', 120))
            bp_low = float(record.get('bloodPressureLow', 80))
            oxygen = float(record.get('bloodOxygen', 98))
            temp = float(record.get('bodyTemperature', 36.5))
            age = int(record.get('age', 75))
            gender = 1 if record.get('gender', 'female').lower() == 'male' else 0

            features = [heart_rate, bp_high, bp_low, oxygen, temp, age, gender]

            risk_score, risk_level, advice, details = predict_risk(features, ai_model)

            results.append({
                'riskScore': int(risk_score),
                'riskLevel': risk_level,
                'timestamp': record.get('timestamp', datetime.now().isoformat())
            })

        return jsonify({
            'code': 200,
            'message': f'批量预测成功，共{len(results)}条',
            'data': {
                'results': results,
                'count': len(results)
            }
        })

    except Exception as e:
        return jsonify({
            'code': 500,
            'message': f'批量预测失败: {str(e)}'
        }), 500

@app.route('/predict/forecast', methods=['POST'])
def predict_forecast():
    """生成未来24-48小时生理指标预测"""
    try:
        data = request.json
        if not data:
            return jsonify({'code': 400, 'message': '请求体不能为空'}), 400

        historical_data = data.get('historicalData', [])
        age = int(data.get('age', 68))
        gender_str = str(data.get('gender', 'female'))
        gender_val = 1 if gender_str.lower() == 'male' else 0
        hours = int(data.get('hours', 24))
        hours = max(1, min(hours, 48))

        forecast_list, attention_weights, summary = generate_forecast(
            historical_data, age, gender_val, ai_model, hours
        )

        return jsonify({
            'code': 200,
            'message': '预测成功',
            'data': {
                'forecast': forecast_list,
                'attentionWeights': attention_weights,
                'summary': summary,
                'hours': hours,
                'timestamp': datetime.now().isoformat()
            }
        })

    except Exception as e:
        app.logger.error(f'趋势预测失败: {str(e)}')
        return jsonify({'code': 500, 'message': f'预测失败: {str(e)}'}), 500


# ============ 错误处理 ============

@app.errorhandler(404)
def not_found(error):
    return jsonify({
        'code': 404,
        'message': 'API端点不存在'
    }), 404

@app.errorhandler(500)
def internal_error(error):
    return jsonify({
        'code': 500,
        'message': '服务器内部错误'
    }), 500

# ============ 启动服务 ============

def init_model():
    """初始化AI模型"""
    global ai_model

    print('=' * 60)
    print('🤖 正在初始化AI模型...')

    try:
        ai_model = HealthTransformer()

        if os.path.exists(config.MODEL_PATH):
            ai_model.load_state_dict(torch.load(config.MODEL_PATH))
            print(f'✅ 成功加载模型权重: {config.MODEL_PATH}')
        else:
            print('⚠️ 未找到预训练模型，使用随机初始化权重')
            print('💡 当前使用规则引擎作为后备方案')

        ai_model.eval()
        print('✅ 模型初始化完成')

    except Exception as e:
        print(f'⚠️ 模型加载失败: {e}')
        print('💡 将使用纯规则引擎进行预测')
        ai_model = None

    print('=' * 60)

if __name__ == '__main__':
    init_model()

    print('\n' + '=' * 60)
    print('🚀 AI健康风险预测服务启动中...')
    print(f'📍 服务地址: http://{config.HOST}:{config.PORT}')
    print(f'🔧 调试模式: {config.DEBUG}')
    print('=' * 60 + '\n')

    app.run(
        host=config.HOST,
        port=config.PORT,
        debug=config.DEBUG
    )