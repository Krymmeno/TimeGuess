<template>
  <v-container>
    <v-row justify="center">
      <v-col cols="12" xl="8" class="pt-16">
        <lottie-animation
          class="error-animation"
          path="json/500.json"
          :speed="1"
          :loop="false"
          :auto-play="true"
        />
      </v-col>
      <v-col cols="12" xl="8">
        <v-alert text color="error">
          <h3 class="headline">{{ $t("error.500.title") }}</h3>
          <div>
            {{ $t("error.500.text") }}
          </div>

          <v-divider class="my-4 error" style="opacity: 0.22"></v-divider>

          <v-row align="center" no-gutters>
            <v-spacer></v-spacer>
            <v-btn href="/gamelobby" color="error" outlined>
              {{ $t("error.500.back") }}
            </v-btn>
          </v-row>
        </v-alert>
      </v-col>
    </v-row>
  </v-container>
</template>
<script>
import LottieAnimation from "lottie-vuejs/src/LottieAnimation.vue";
export default {
  components: { LottieAnimation },
  props: {},
  data() {
    return {};
  },
  computed: {},
  watch: {},
  methods: {},
};
</script>

<style scoped>
@media only screen and (min-width: 960px) {
  .error-animation {
    width: 500px !important;
    height: 500px !important;
  }
}
</style>
