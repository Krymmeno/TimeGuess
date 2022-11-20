<template>
  <v-container>
    <v-row justify="center">
      <v-col cols="12" xl="8">
        <v-row>
          <v-col cols="9">
            <h1
              class="display-3 mt-16 mb-5"
              v-html="$t('userManagement.title')"
            ></h1>
          </v-col>
          <v-spacer />
          <v-col align-self="end">
            <CreateUser
              v-if="$store.getters['user/getUser'].role !== 'PLAYER'"
              @createUser="createUser"
              ref="createUser"
            />
          </v-col>
        </v-row>
        <v-divider class="mb-5"></v-divider>
        <UserList
          :user-list="userList"
          ref="userList"
          @toggleUser="toggleUser"
        ></UserList>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import UserList from "@/components/admin/usermanagement/UserList";
import CreateUser from "@/components/admin/usermanagement/CreateUser";
import {
  getActiveUsers,
  getUsers,
  createUser,
  updateUser,
} from "@/services/user.service";

export default {
  name: "UserManagement",
  components: { CreateUser, UserList },
  data() {
    return {
      userList: [],
    };
  },
  methods: {
    toggleUser(user) {
      user.active = !user.active;
      updateUser(user)
        .then(() => {
          this.$notify({
            title: this.$t("notification.success"),
            text: this.$t("notification.userSaved"),
            type: "success",
          });
          this.getUsers();
        })
        .catch((error) => {
          this.$notify({
            title: this.$t("notification.error") + error.response.status,
            text: this.$t("notification.userSaveFailure"),
            type: "error",
          });
          this.getUsers();
        });
    },
    createUser(user) {
      user.active = true;
      createUser(user)
        .then(() => {
          this.$refs.createUser.confirmCreation(true);
          this.getUsers();
        })
        .catch((error) => {
          if (error.response.status === 409) {
            this.$notify({
              title: this.$t("notification.error") + error.response.status,
              text: this.$t("notification.duplicateUsername"),
              type: "error",
            });
          }
        });
    },
    getUsers() {
      let user = this.$store.getters["user/getUser"];
      if (user.role !== "PLAYER") {
        getUsers()
          .then((users) => (this.userList = users))
          .catch((error) => {
            this.$notify({
              title: this.$t("notification.error") + error.response.status,
              text: this.$t("notification.getUserFailure"),
              type: "error",
            });
          });
      } else {
        getActiveUsers()
          .then((users) => (this.userList = users))
          .catch((error) => {
            this.$notify({
              title: this.$t("notification.error") + error.response.status,
              text: this.$t("notification.getUserFailure"),
              type: "error",
            });
          });
      }
    },
  },
  created() {
    this.getUsers();
  },
};
</script>

<style scoped></style>
