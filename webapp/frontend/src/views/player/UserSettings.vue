<template>
  <v-container class="fill-height align-content-start">
    <v-row justify="center" class="fill-height">
      <v-col cols="12" xl="8" class="fill-height">
        <h1 class="display-3 mt-16 mb-5" v-html="$t('userSettings.title')"></h1>
        <v-divider class="mb-5"></v-divider>
        <v-row justify="center">
          <v-col cols="auto">
            <div style="text-align: center">
              <v-avatar size="100">
                <img
                  :src="
                    hashedFirstName ===
                      '0B2B6CCDE82B83E44767B72E663098971CED044F' &&
                    hashedLastName ===
                      'F941E1206ABD4A2D8889DA67BE10151F429D95DC'
                      ? '/seifenspender.jpg'
                      : '/no_avatar.png'
                  "
                  alt="Profile"
                />
              </v-avatar>
              <div class="text-h6 font-weight-light">
                {{ user.username }}
              </div>
            </div>
          </v-col>
        </v-row>
        <v-row class="justify-center">
          <v-col cols="12" md="6">
            <v-form v-model="valid">
              <v-row>
                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="user.firstName"
                    :rules="rules.general"
                    :label="$t('userSettings.firstname')"
                    required
                  ></v-text-field>
                </v-col>
                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="user.lastName"
                    :rules="rules.general"
                    :label="$t('userSettings.lastname')"
                    required
                  ></v-text-field>
                </v-col>
              </v-row>
              <v-row>
                <v-col cols="12" md="6">
                  <v-text-field
                    :v-model="user.username"
                    :value="user.username"
                    disabled
                    :rules="rules.general"
                    :label="$t('userSettings.username')"
                    required
                  ></v-text-field>
                </v-col>
                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="user.password"
                    :type="showPass ? 'text' : 'password'"
                    :append-icon="showPass ? 'mdi-eye' : 'mdi-eye-off'"
                    :rules="rules.password"
                    @click:append="showPass = !showPass"
                    :label="$t('userSettings.password')"
                    required
                  ></v-text-field>
                </v-col>
              </v-row>
              <v-row>
                <v-col cols="12" md="6">
                  <v-select
                    v-model="user.role"
                    :items="roles"
                    chips
                    :disabled="
                      $store.getters['user/getUser'].role !== 'ADMIN' ||
                      $store.getters['user/getUser'].username ===
                        this.user.username
                    "
                  />
                </v-col>
              </v-row>
              <v-row>
                <v-col cols="12" md="6">
                  <v-switch
                    v-model="user.active"
                    :disabled="
                      user.userId === $store.getters['user/getUser'].userId
                    "
                    :label="
                      user.active
                        ? $t('userSettings.active')
                        : $t('userSettings.inActive')
                    "
                  />
                </v-col>
              </v-row>
              <v-row class="grow align-content-end">
                <v-col cols="auto">
                  <v-btn color="primary" @click="save" :disabled="!valid">
                    {{ $t("userSettings.save") }}
                  </v-btn>
                </v-col>
              </v-row>
            </v-form>
          </v-col>
        </v-row>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import { getUser, updateUser } from "@/services/user.service";
import sha1 from "sha1";

export default {
  name: "UserSettings",
  components: {},
  data() {
    return {
      user: { username: "", firstName: "", lastName: "", role: "" },
      showPass: false,
      roles: ["ADMIN", "GAMEMANAGER", "PLAYER"],
      valid: false,
      rules: {
        general: [(v) => !!v || this.$t("login.emptyForm")],
        password: [
          (v) =>
            (v && v.length > 4) ||
            !v ||
            this.$t("userManagement.userList.passwordToShort"),
        ],
      },
    };
  },
  methods: {
    save() {
      console.log(this.user);
      updateUser(this.user)
        .then((response) => {
          console.log(response);
          this.user = response;
          this.$notify({
            title: this.$t("notification.success"),
            text: this.$t("notification.userSaved"),
            type: "success",
          });
        })
        .catch((error) => {
          if (error.response.status === 409) {
            this.$notify({
              title: this.$t("notification.error") + error.response.status,
              text: this.$t("notification.duplicateUsername"),
              type: "error",
            });
          } else {
            this.$notify({
              title: this.$t("notification.error") + error.response.status,
              text: this.$t("notification.userSaveFailure"),
              type: "error",
            });
          }
        });
    },
    getUser() {
      let id = Number(this.$route.params.id);
      if (id) {
        getUser(id)
          .then((user) => {
            if (user) {
              this.user = user;
            } else {
              this.$router.push({ path: "/errors/404" });
            }
          })
          .catch(() => {
            this.$notify({
              title: this.$t("notification.error"),
              text: this.$t("notification.getUserFailure"),
              type: "error",
            });
            this.$router.push({ path: "/errors/404" });
          });
      } else {
        let user = this.$store.getters["user/getUser"];
        if (user) {
          this.user = user;
        } else {
          this.$router.push({ path: "/errors/404" });
        }
      }
    },
  },
  created() {
    this.getUser();
  },
  computed: {
    hashedFirstName: function () {
      return sha1(this.user.firstName).toUpperCase();
    },
    hashedLastName: function () {
      return sha1(this.user.lastName).toUpperCase();
    },
  },
};
</script>

<style scoped></style>
