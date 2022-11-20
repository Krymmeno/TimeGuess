<template>
  <v-app>
    <NavBar
      v-if="
        this.$route.path !== '/login' && !this.$route.path.startsWith('/errors')
      "
    />
    <notifications position="top right" class="vue-notf" />
    <v-main>
      <router-view :key="$route.fullPath" />
    </v-main>
    <Footer v-if="this.$route.path !== '/login'" />
  </v-app>
</template>

<script>
import NavBar from "@/components/navigation/NavBar";
import Footer from "@/components/navigation/Footer";
export default {
  name: "App",
  components: { Footer, NavBar },
};
</script>

<style scoped>
.vue-notf {
  margin-top: 75px;
  margin-right: 5px;
  z-index: 10000 !important;
}
.vue-notification {
  margin-right: 75px;
}
</style>
<style>
.v-card__title {
  word-break: normal !important;
}
</style>
