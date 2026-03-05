#!/bin/bash
# Unity WebGL 一键部署脚本
# 使用方法：在 Unity 完成构建后，双击运行此脚本

set -e

PROJECT_DIR="C:/Users/emo/Desktop/大创项目/people"
BUILD_DIR="$PROJECT_DIR/WebGLBuild"
DEPLOY_DIR="$PROJECT_DIR/WebGLDeploy"

echo "=============================="
echo "  Unity WebGL 部署脚本"
echo "=============================="

# 检查构建产物
if [ ! -f "$BUILD_DIR/index.html" ]; then
  echo ""
  echo "❌ 未找到构建产物：$BUILD_DIR/index.html"
  echo ""
  echo "请先在 Unity 中完成 WebGL 构建："
  echo "  File → Build Settings → WebGL → Switch Platform → Build"
  echo "  输出目录选择：$BUILD_DIR"
  echo ""
  exit 1
fi

echo "✅ 找到 Unity WebGL 构建产物"
echo ""

# 准备部署目录（复制并添加 vercel.json）
rm -rf "$DEPLOY_DIR"
cp -r "$BUILD_DIR" "$DEPLOY_DIR"

cat > "$DEPLOY_DIR/vercel.json" << 'EOF'
{
  "headers": [
    {
      "source": "/(.*)",
      "headers": [
        { "key": "Cross-Origin-Opener-Policy", "value": "same-origin" },
        { "key": "Cross-Origin-Embedder-Policy", "value": "require-corp" },
        { "key": "Cross-Origin-Resource-Policy", "value": "cross-origin" }
      ]
    },
    {
      "source": "/Build/(.*)\\.gz",
      "headers": [
        { "key": "Content-Encoding", "value": "gzip" }
      ]
    },
    {
      "source": "/Build/(.*)\\.br",
      "headers": [
        { "key": "Content-Encoding", "value": "br" }
      ]
    }
  ]
}
EOF

echo "✅ 已生成 vercel.json（配置跨域头和压缩响应头）"
echo ""

# 登录 Vercel（已登录则跳过）
echo ">>> 检查 Vercel 登录状态..."
if ! npx vercel whoami &>/dev/null; then
  echo "请在浏览器中完成 Vercel 登录："
  npx vercel login
fi

echo ""
echo ">>> 开始部署到 Vercel..."
cd "$DEPLOY_DIR"

# 部署到现有项目（重新部署到 unity-web-gl 项目）
npx vercel --prod --yes --name unity-web-gl 2>&1

echo ""
echo "=============================="
echo "  ✅ 部署完成！"
echo "=============================="
