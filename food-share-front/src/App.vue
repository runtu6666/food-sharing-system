<template>
  <router-view v-slot="{ Component }">
    <keep-alive include="HomeView">
      <component :is="Component" />
    </keep-alive>
  </router-view>
</template>

<script>
export default { name: 'App' }
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: 'Microsoft YaHei', sans-serif; }
</style>