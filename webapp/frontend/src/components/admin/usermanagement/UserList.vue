<template>
  <v-container>
    <v-row>
      <v-col cols="5">
        <v-text-field
          v-model="search"
          class="mb-4"
          append-icon="mdi-magnify"
          label="Search"
          single-line
          outlined
          hide-details
          dense
        ></v-text-field>
      </v-col>
    </v-row>
    <v-data-table
      :headers="computedHeaders"
      :items="userList"
      sort-by="username"
      :search="search"
      mobile-breakpoint="860"
    >
      <template v-slot:item.modify="{ item }">
        <v-btn
          small
          icon
          class="mr-2"
          color="green"
          @click="modifyUser(item)"
          :disabled="
            user.userId !== item.userId &&
            item.role !== 'PLAYER' &&
            user.role === 'GAMEMANAGER'
          "
        >
          <v-icon>mdi-pencil</v-icon>
        </v-btn>
      </template>
      <template v-slot:item.view="{ item }">
        <v-btn small icon class="mr-2" color="primary" @click="viewUser(item)">
          <v-icon>mdi-eye</v-icon>
        </v-btn>
      </template>
      <template v-slot:item.disable="{ item }" v-if="user.role !== 'PLAYER'">
        <v-switch
          :input-value="item.active"
          :disabled="
            (user.userId !== item.userId &&
              item.role !== 'PLAYER' &&
              user.role === 'GAMEMANAGER') ||
            (user.userId === item.userId && user.role === 'ADMIN') ||
            !$listeners.toggleUser
          "
          @change="$emit('toggleUser', item)"
        />
      </template>
    </v-data-table>
  </v-container>
</template>

<script>
export default {
  name: "UserList",
  components: {},
  props: {
    userList: Array,
  },
  data() {
    return {
      search: "",
      dialogDelete: false,
      userToDelete: {},
      user:
        this.$store.getters["user/getUser"] != null
          ? this.$store.getters["user/getUser"]
          : "",
      headers: [
        {
          text: this.$t("userManagement.userList.ID"),
          align: "start",
          value: "userId",
          class: "font-weight-bold subtitle-1",
        },
        {
          text: this.$t("userManagement.userList.username"),
          value: "username",
          class: "font-weight-bold subtitle-1",
        },
        {
          text: this.$t("userManagement.userList.firstName"),
          value: "firstName",
          class: "font-weight-bold subtitle-1",
        },
        {
          text: this.$t("userManagement.userList.lastName"),
          value: "lastName",
          class: "font-weight-bold subtitle-1",
        },
        {
          text: this.$t("userManagement.userList.roles"),
          value: "role",
          class: "font-weight-bold subtitle-1",
        },
        {
          text: this.$t("userManagement.userList.active"),
          value: "disable",
          class: "font-weight-bold subtitle-1",
          hiddenForPlayers: true,
        },
        { text: "", value: "modify", sortable: false, hiddenForPlayers: true },
        { text: "", value: "view", sortable: false },
      ],
    };
  },
  methods: {
    modifyUser(user) {
      this.$router.push({ path: `/settings/${user.userId}` });
    },
    viewUser(user) {
      this.$router.push({ path: `/profile/${user.userId}` });
    },
    confirmDeletion(status) {
      if (status) {
        this.$notify({
          title: this.$t("notification.success"),
          text: this.$t("notification.confirmDeletion"),
          type: "success",
        });
      } else {
        this.$notify({
          title: this.$t("notification.error"),
          text: this.$t("notification.deletionFailed"),
          type: "error",
        });
      }
    },
  },
  computed: {
    computedHeaders: function () {
      if (this.user.role !== "PLAYER") {
        return this.headers;
      } else {
        return this.headers.filter((header) => !header.hiddenForPlayers);
      }
    },
  },
};
</script>

<style scoped></style>
