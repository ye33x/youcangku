<template>
  <el-container>
    <el-header class="app-header">
      <span class="brand">优果仓</span>
      <nav class="nav">
        <router-link to="/user/home">首页推荐</router-link>
        <router-link to="/admin/reviews">内容审核</router-link>
        <router-link to="/login">登录</router-link>
      </nav>
    </el-header>
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
// 根组件只负责骨架；三端页面通过路由区分
</script>

<style>
.app-header {
  display: flex;
  align-items: center;
  gap: 24px;
  border-bottom: 1px solid #e5e7eb;
}
.brand {
  font-weight: 700;
  font-size: 18px;
}
.nav {
  display: flex;
  gap: 16px;
}
.nav a {
  color: #374151;
  text-decoration: none;
}
</style>
