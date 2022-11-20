<template>
  <v-card>
    <v-card-title class="font-weight-light primary white--text">
      {{ $t("profile.knownUsers.title") }}
    </v-card-title>
    <v-container>
      <UserList :userList="knownUsers" />
    </v-container>
  </v-card>
</template>

<script>
import UserList from "@/components/admin/usermanagement/UserList";
export default {
  name: "KnownUsers",
  components: { UserList },
  props: {
    knownUsers: Array,
  },
};
</script>

<style scoped></style>
