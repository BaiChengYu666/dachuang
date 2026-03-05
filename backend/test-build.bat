@echo off
chcp 65001 >nul
echo ========================================
echo 老年人健康监测系统 - 编译测试脚本
echo ========================================
echo.

echo [1/4] 清理项目...
call mvn clean
if %errorlevel% neq 0 (
    echo ❌ 清理失败!
    pause
    exit /b 1
)
echo ✅ 清理完成
echo.

echo [2/4] 编译项目...
call mvn compile
if %errorlevel% neq 0 (
    echo ❌ 编译失败! 请检查错误信息
    pause
    exit /b 1
)
echo ✅ 编译成功
echo.

echo [3/4] 打包项目...
call mvn package -DskipTests
if %errorlevel% neq 0 (
    echo ❌ 打包失败!
    pause
    exit /b 1
)
echo ✅ 打包完成
echo.

echo [4/4] 项目构建成功! ✨
echo.
echo ========================================
echo 下一步操作:
echo ========================================
echo 1. 初始化数据库: mysql -u root -p ^< database_init.sql
echo 2. 配置数据库连接: 编辑 src/main/resources/application.properties
echo 3. 启动项目: mvn spring-boot:run
echo 4. 测试API: http://localhost:8080/api/elderly/list
echo ========================================
echo.
pause
