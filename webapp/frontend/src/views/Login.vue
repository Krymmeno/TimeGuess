<template>
  <div
    :style="{
      'background-image':
        'url(' + require(`@/assets/BackgroundLogin.svg`) + ')',
    }"
    class="d-flex flex-column wrapper"
  >
    <div align="center" class="text-md-h2 text-h3 font-weight-thin mt-15">
      <strong>TIME</strong>GUESS
    </div>
    <v-card
      outlined
      class="ma-auto"
      :style="translation"
      width="35%"
      min-width="250px"
      elevation="5"
    >
      <h1 align="center">Login</h1>
      <LoginMask />
    </v-card>
  </div>
</template>

<script>
import LoginMask from "@/components/login/LoginMask";

export default {
  name: "Home",
  components: { LoginMask },
  props: {},
  data() {
    return {};
  },
  computed: {
    translation() {
      if (this.$vuetify.breakpoint.mdAndUp) {
        return { transform: "translate(-50%, 0%)" };
      }
      return {};
    },
  },
  watch: {},
  methods: {},
};
</script>
<style scoped>
.wrapper {
  width: 100%;
  height: 100%;
  background-size: cover;
}
</style>
