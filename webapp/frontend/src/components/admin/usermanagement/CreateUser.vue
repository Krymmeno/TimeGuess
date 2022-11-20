<template>
  <v-dialog v-model="dialog" max-width="500px">
    <template v-slot:activator="{ on, attrs }">
      <v-btn color="green" fab small class="mb-2" v-bind="attrs" v-on="on">
        <v-icon> mdi-plus </v-icon>
      </v-btn>
    </template>
    <v-card>
      <v-form
        v-model="valid"
        @submit.prevent="$emit('createUser', user)"
        ref="form"
      >
        <v-card-title>
          <span class="headline">{{
            $t("userManagement.createUser.title")
          }}</span>
        </v-card-title>

        <v-card-text>
          <v-container>
            <v-row>
              <v-col
                cols="12"
                sm="6"
                v-for="attribute in attributes"
                :key="attribute.value"
              >
                <v-text-field
                  v-model="user[attribute.value]"
                  :label="attribute.text"
                  :type="
                    attribute.value === 'password'
                      ? showPass
                        ? 'text'
                        : 'password'
                      : ''
                  "
                  :append-icon="
                    attribute.value === 'password'
                      ? showPass
                        ? 'mdi-eye'
                        : 'mdi-eye-off'
                      : ''
                  "
                  :rules="
                    rules.general.concat(
                      attribute.value === 'password' ? rules.password : []
                    )
                  "
                  @click:append="showPass = !showPass"
                ></v-text-field>
              </v-col>
              <v-col>
                <v-select
                  v-model="user.role"
                  :items="roles"
                  :rules="rules.general"
                  chips
                />
              </v-col>
            </v-row>
          </v-container>
        </v-card-text>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="blue darken-1" text @click="close">
            {{ $t("userManagement.createUser.cancel") }}
          </v-btn>
          <v-btn color="blue darken-1" text :disabled="!valid" type="submit">
            {{ $t("userManagement.createUser.save") }}
          </v-btn>
        </v-card-actions>
      </v-form>
    </v-card>
  </v-dialog>
</template>

<script>
export default {
  name: "CreateUser",
  components: {},
  data() {
    return {
      showPass: false,
      dialog: false,
      valid: false,
      attributes: [
        {
          text: this.$t("userManagement.userList.username"),
          value: "username",
          type: "text",
        },
        {
          text: this.$t("userManagement.userList.firstName"),
          value: "firstName",
          type: "text",
        },
        {
          text: this.$t("userManagement.userList.lastName"),
          value: "lastName",
          type: "text",
        },
        {
          text: this.$t("userManagement.userList.password"),
          value: "password",
          type: "password",
        },
      ],
      rules: {
        general: [(v) => !!v || this.$t("login.emptyForm")],
        password: [
          (v) =>
            (v && v.length > 4) ||
            this.$t("userManagement.userList.passwordToShort"),
        ],
      },
      user: {
        username: "",
        firstname: "",
        lastname: "",
        password: "",
        role: "",
      },
    };
  },
  methods: {
    close() {
      this.dialog = false;
      this.user = {};
      this.showPass = false;
      this.$refs.form.resetValidation();
    },
    confirmCreation(status) {
      if (status) {
        this.close();
        this.$notify({
          title: this.$t("notification.success"),
          text: this.$t("notification.userCreated"),
          type: "success",
        });
      } else {
        this.$notify({
          title: this.$t("notification.error"),
          text: this.$t("notification.creationFailed"),
          type: "error",
        });
      }
    },
  },
  computed: {
    roles: function () {
      let user = this.$store.getters["user/getUser"];
      if (user != null && user.role === "ADMIN") {
        return ["ADMIN", "GAMEMANAGER", "PLAYER"];
      } else {
        return ["PLAYER"];
      }
    },
  },
};
</script>

<style scoped></style>
