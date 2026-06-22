@echo off
chcp 65001 >nul
title 学生成绩管理系统

:: ========================================
::  学生成绩管理系统 - 一键启动脚本
::  使用: run.bat 你的SQL密码
:: ========================================

set JAR=grade-system-1.0.0.jar
set PORT=8088

if "%~1"=="" (
    set /p DB_PASS="请输入SQL Server密码: "
) else (
    set DB_PASS=%~1
)

echo.
echo 正在启动学生成绩管理系统...
echo 数据库: localhost:1433
echo 端口: %PORT%
echo.

java -jar %JAR% --spring.datasource.password=%DB_PASS% --server.port=%PORT%

pause
