<template>
  <v-card>
    <v-card-title class="font-weight-light primary white--text">
      {{ $t("profile.information.title") }}
    </v-card-title>
    <v-card>
      <v-card-title class="mb-5">
        {{ user.username }}
        <v-spacer />
        <v-chip
          :color="chipColor(user.role)"
          class="mx-2 mb-3 text-h6 font-weight-light"
          >{{ user.role }}</v-chip
        >
      </v-card-title>
      <v-card-subtitle class="pb-0 font-weight-bold">
        {{ $t("profile.information.name") }}
      </v-card-subtitle>
      <v-card-text class="text-h6 font-weight-light mb-2">
        {{ user.firstName }} {{ user.lastName }}
      </v-card-text>
    </v-card>
  </v-card>
</template>

<script>
export default {
  name: "Information",
  props: {
    user: Object,
  },
  methods: {
    chipColor(role) {
      let color;
      switch (role) {
        case "ADMIN":
          color = "red";
          break;
        case "GAMEMANAGER":
          color = "orange";
          break;
        default:
          color = "green";
      }
      return color;
    },
  },
};
</script>

<style scoped></style>
