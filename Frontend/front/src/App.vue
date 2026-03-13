<script setup>
import { RouterView } from 'vue-router';
import Footer from './components/Footer.vue';
import Header from './components/Header.vue';
</script>
<template>
  <v-app>
    <Header />
    <v-main style="height: 100%;">
      <RouterView />
    </v-main style="display: flex; flex-direction: column;">
    <Footer />
  </v-app>
</template>