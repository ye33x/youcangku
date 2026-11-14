#!/usr/bin/env python3
"""项目结构自检：在没有 JDK / MySQL 的环境下，尽可能提前发现结构性问题。

检查项：
  1. Java 包声明与目录结构一致
  2. 文件名与 public 类型名一致
  3. 项目内部 import 指向的类文件真实存在
  4. Java 源文件花括号配平
  5. SQL 表名唯一，外键引用的表与列存在
  6. SQL 索引引用的列存在
  7. 前端 router 里 import 的 .vue 文件存在

用法：
    python tools/check_structure.py
退出码 0 表示全部通过，1 表示存在问题。
"""
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JAVA_SRC = os.path.join(ROOT, "server", "src", "main", "java")
SQL_DIR = os.path.join(ROOT, "sql")
WEB_SRC = os.path.join(ROOT, "web", "src")

errors = []
warnings = []


# ----------------------------------------------------------------------
# Java
# ----------------------------------------------------------------------
def check_java():
    if not os.path.isdir(JAVA_SRC):
        errors.append(f"缺少目录: {JAVA_SRC}")
        return
    files = []
    for dirpath, _, filenames in os.walk(JAVA_SRC):
        for name in filenames:
            if name.endswith(".java"):
                files.append(os.path.join(dirpath, name))

    defined = set()
    bodies = {}
    for path in files:
        rel = os.path.relpath(path, JAVA_SRC).replace(os.sep, "/")
        # rel 形如 com/youguocang/common/ApiResult.java
        expected_package = os.path.dirname(rel).replace("/", ".")
        with open(path, encoding="utf-8") as fh:
            text = fh.read()
        bodies[rel] = text

        m = re.search(r"^package\s+([\w.]+);", text, re.MULTILINE)
        if not m:
            errors.append(f"[java] {rel}: 缺少 package 声明")
        elif m.group(1) != expected_package:
            errors.append(f"[java] {rel}: package 为 {m.group(1)}，按路径应为 {expected_package}")

        stem = os.path.splitext(os.path.basename(rel))[0]
        defined.add(f"{expected_package}.{stem}")

        public_types = re.findall(r"^public\s+(?:final\s+|abstract\s+)?(?:class|interface|record|enum)\s+(\w+)",
                                  text, re.MULTILINE)
        if not public_types:
            errors.append(f"[java] {rel}: 没有 public 类型")
        elif stem not in public_types:
            errors.append(f"[java] {rel}: 文件名为 {stem}，public 类型为 {public_types}")

        opens = text.count("{")
        closes = text.count("}")
        if opens != closes:
            errors.append(f"[java] {rel}: 花括号不配平（{{={opens}, }}={closes}）")

    for rel, text in bodies.items():
        for imp in re.findall(r"^import\s+(com\.youguocang\.[\w.]+);", text, re.MULTILINE):
            if imp not in defined:
                errors.append(f"[java] {rel}: import {imp} 找不到对应源文件")

    print(f"  Java 文件: {len(files)}")


# ----------------------------------------------------------------------
# SQL
# ----------------------------------------------------------------------
def split_tables(sql):
    """返回 {表名: 表定义体}。"""
    pattern = re.compile(r"CREATE TABLE\s+`(\w+)`\s*\((.*?)\n\)\s*ENGINE", re.S)
    return {name: body for name, body in pattern.findall(sql)}


def check_sql():
    schema_path = os.path.join(SQL_DIR, "01_schema.sql")
    if not os.path.exists(schema_path):
        errors.append("缺少 sql/01_schema.sql")
        return
    with open(schema_path, encoding="utf-8") as fh:
        sql = fh.read()

    tables = split_tables(sql)
    if not tables:
        errors.append("[sql] 未解析到任何 CREATE TABLE")
        return

    # 表名唯一
    declared = re.findall(r"CREATE TABLE\s+`(\w+)`", sql)
    dup = {name for name in declared if declared.count(name) > 1}
    for name in dup:
        errors.append(f"[sql] 表 {name} 重复定义")

    columns = {}
    for table, body in tables.items():
        cols = set(re.findall(r"^\s*`(\w+)`\s+[A-Za-z]", body, re.MULTILINE))
        if not cols:
            errors.append(f"[sql] 表 {table} 未解析到列")
        columns[table] = cols

    for table, body in tables.items():
        for col, ref_table, ref_col in re.findall(
                r"FOREIGN KEY\s*\(`(\w+)`\)\s*REFERENCES\s*`(\w+)`\s*\(`(\w+)`\)", body):
            if col not in columns[table]:
                errors.append(f"[sql] {table}.{col}: 外键列不存在")
            if ref_table not in columns:
                errors.append(f"[sql] {table}.{col}: 外键指向的表 {ref_table} 不存在")
            elif ref_col not in columns[ref_table]:
                errors.append(f"[sql] {table}.{col}: 外键指向的列 {ref_table}.{ref_col} 不存在")

        for idx_cols in re.findall(r"(?:UNIQUE\s+)?KEY\s+`\w+`\s*\(([^)]*)\)", body):
            for col in re.findall(r"`(\w+)`", idx_cols):
                if col not in columns[table]:
                    errors.append(f"[sql] {table}: 索引引用了不存在的列 {col}")

    print(f"  SQL 表: {len(tables)}")

    seed_path = os.path.join(SQL_DIR, "02_seed.sql")
    if not os.path.exists(seed_path):
        warnings.append("缺少 sql/02_seed.sql（可运行 tools/gen_seed.py 生成）")
        return
    with open(seed_path, encoding="utf-8") as fh:
        seed = fh.read()
    for table in set(re.findall(r"INSERT INTO\s+`(\w+)`", seed)):
        if table not in tables:
            errors.append(f"[sql] 种子数据插入了未定义的表 {table}")


# ----------------------------------------------------------------------
# 前端
# ----------------------------------------------------------------------
def check_web():
    router = os.path.join(WEB_SRC, "router", "index.js")
    if not os.path.exists(router):
        warnings.append("未找到 web/src/router/index.js，跳过前端检查")
        return
    with open(router, encoding="utf-8") as fh:
        text = fh.read()
    count = 0
    for rel in re.findall(r"from\s+'(\.\./[^']+\.vue)'", text):
        target = os.path.normpath(os.path.join(os.path.dirname(router), rel))
        count += 1
        if not os.path.exists(target):
            errors.append(f"[web] router 引用了不存在的页面 {rel}")
    print(f"  前端路由页面: {count}")


def main():
    print("优果仓 结构自检")
    print("-" * 50)
    check_java()
    check_sql()
    check_web()
    print("-" * 50)

    for msg in warnings:
        print(f"WARN  {msg}")
    if errors:
        for msg in errors:
            print(f"ERROR {msg}")
        print(f"\n共 {len(errors)} 个问题")
        return 1

    print("全部检查通过")
    return 0


if __name__ == "__main__":
    sys.exit(main())
