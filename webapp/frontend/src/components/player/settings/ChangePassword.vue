<template>
  <div class="text-center">
    <v-dialog v-model="dialog" max-width="500px">
      <template v-slot:activator="{ on, attrs }">
        <v-btn color="primary" dark v-bind="attrs" v-on="on">
          {{ $t("userSettings.changePassword.changePassword") }}
        </v-btn>
      </template>

      <v-card>
        <v-card-title class="headline">
          {{ $t("userSettings.changePassword.changePassword") }}
        </v-card-title>

        <v-divider></v-divider>
        <v-form v-model="valid" class="pa-4" ref="passForm">
          <v-text-field
            v-model="oldPassword"
            type="password"
            :label="$t('userSettings.changePassword.oldPassword')"
            :rules="rules.general"
            required
          ></v-text-field>
          <v-text-field
            v-model="newPassword"
            type="password"
            :label="$t('userSettings.changePassword.newPassword')"
            :rules="rules.general"
            @change="$refs.newPass.validate()"
            required
          ></v-text-field>
          <v-text-field
            v-model="newPassword2"
            type="password"
            :label="$t('userSettings.changePassword.newPassword2')"
            ref="newPass"
            :rules="rules.general.concat(rules.newPassword)"
            required
          ></v-text-field>
        </v-form>
        <v-alert :value="failure" type="error" align="center" class="ma-4"
          >{{ $t("userSettings.changePassword.changePasswordFailure") }}
        </v-alert>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn
            color="primary"
            text
            :disabled="!valid"
            @click="
              $emit('changePassword', user.username, oldPassword, newPassword)
            "
          >
            {{ $t("userSettings.changePassword.change") }}
          </v-btn>
          <v-btn
            color="red"
            text
            @click="
              dialog = false;
              oldPassword = '';
              newPassword = '';
              newPassword2 = '';
            "
          >
            {{ $t("userSettings.changePassword.cancel") }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
export default {
  name: "ChangePassword",
  components: {},
  props: {
    user: Object,
  },
  data() {
    return {
      dialog: false,
      oldPassword: "",
      newPassword: "",
      newPassword2: "",
      valid: false,
      failure: false,
      rules: {
        general: [(v) => !!v || this.$t("login.emptyForm")],
        newPassword: [
          () =>
            this.newPassword === this.newPassword2 ||
            this.$t("userSettings.changePassword.passwordsNotEqual"),
        ],
      },
    };
  },
  methods: {
    changeStatus(status) {
      if (status) {
        this.dialog = false;
        this.$notify({
          title: this.$t("notification.success"),
          text: this.$t("notification.passwordChanged"),
          type: "success",
        });
        this.failure = false;
        this.oldPassword = "";
        this.newPassword = "";
        this.newPassword2 = "";
      } else {
        this.failure = true;
      }
    },
  },
};
</script>

<style scoped></style>
