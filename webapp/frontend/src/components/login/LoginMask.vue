<template>
  <v-container id="loginForm">
    <v-form ref="loginForm" v-model="valid" @submit.prevent="doLogin">
      <v-text-field
        v-model="user.name"
        :label="$t('login.username')"
        :rules="rules.general"
        required
      ></v-text-field>
      <v-text-field
        v-model="user.password"
        type="password"
        :label="$t('login.password')"
        :rules="rules.general"
        required
      ></v-text-field>
      <v-btn
        id="loginButton"
        align="center"
        class="blue white--text"
        type="submit"
        >{{ $t("login.loginButton") }}</v-btn
      >
      <v-alert :value="loginFailure" type="error" align="center" class="ma-4"
        >{{ $t("login.wrongPassword") }}
      </v-alert>
    </v-form>
  </v-container>
</template>

<script>
export default {
  name: "LoginMask",
  data() {
    return {
      user: {
        name: "",
        password: "",
      },
      valid: true,
      rules: {
        general: [
          (v) => !!v || this.$t("login.emptyForm"),
          () => !this.loginFailure || "",
        ],
      },
      loginFailure: false,
    };
  },
  methods: {
    doLogin() {
      this.$store
        .dispatch("auth/login", this.user)
        .then(() => {
          this.$router.push("/gamelobby");
        })
        .catch(() => {
          // Credentials are invalid
          this.loginFailure = true;
          this.$refs.loginForm.validate();
        });
    },
  },
};
</script>

<style scoped>
#loginButton {
  width: 100%;
  margin-top: 12px;
}
</style>
