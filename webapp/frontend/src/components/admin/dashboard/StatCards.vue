<template>
  <v-row>
    <v-col cols="6" md="3" :key="index" v-for="(stat, index) in statMeta">
      <v-sheet rounded elevation="2" class="py-1">
        <h1 class="text-center hidden-sm-and-down">
          <v-icon class="display-3">{{ stat.icon }}</v-icon>
        </h1>
        <h2 class="text-center display-1 font-weight-bold">
          {{ stats[stat.key] }}
        </h2>
        <p class="text-center text-uppercase subtitle-2">
          {{ $t(stat.name) }}
        </p>
      </v-sheet>
    </v-col>
  </v-row>
</template>

<script>
export default {
  name: "StatCards",
  props: {
    stats: Object,
  },
  data() {
    return {
      statMeta: [
        {
          name: "dashboard.statCards.registeredUsers",
          key: "allRegisteredUsers",
          icon: "mdi-account-star-outline",
        },
        {
          name: "dashboard.statCards.activeUsers",
          key: "allActiveUsers",
          icon: "mdi-account-group-outline",
        },
        {
          name: "dashboard.statCards.gamesPlayed",
          key: "allGames",
          icon: "mdi-gamepad-square-outline",
        },
        {
          name: "dashboard.statCards.termsGuessed",
          key: "allTermsGuessed",
          icon: "mdi-head-question-outline",
        },
      ],
    };
  },
};
</script>
