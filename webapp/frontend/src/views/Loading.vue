<!-- Based on https://git.uibk.ac.at/csaw3618/ws2020_ps_swa_6_2/tree/master/src/frontend/src/pages/auth/Loading.vue -->
<template>
  <div>
    <h1>loading</h1>
    <v-progress-circular indeterminate color="primary"></v-progress-circular>
  </div>
</template>
<script>
export default {
  name: "Loading",
  components: {},
  mounted() {
    this.$store
      .dispatch("auth/authenticate")
      .then(() => {
        // Redirect using entry route
        const url = this.$store.getters.getEntryUrl || "/gamelobby";
        this.$router.push(url);
      })
      .catch(() => {
        // Token is invalid, login
        this.$router.push("/login");
      });
  },
};
</script>
