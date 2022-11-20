<template>
  <v-row class="justify-center">
    <v-col cols="12" sm="6" md="4">
      <v-sheet rounded elevation="2" class="py-5">
        <h1 class="text-center hidden-sm-and-down">
          <v-icon class="display-2">mdi-account-star-outline</v-icon>
        </h1>
        <h2 class="display-1 text-center">
          {{ stats.allRegisteredUsers }}
        </h2>
        <h3 class="text-uppercase subtitle-2 text-center">
          {{ $t("gameLobby.stats.texts.users") }}
        </h3>
      </v-sheet>
    </v-col>
    <v-col cols="12" sm="6" md="4">
      <v-sheet rounded elevation="2" class="py-5">
        <h1 class="text-center hidden-sm-and-down">
          <v-icon class="display-2">mdi-gamepad-square-outline</v-icon>
        </h1>
        <h2 class="display-1 text-center">
          {{ stats.allGames }}
        </h2>
        <h3 class="text-uppercase subtitle-2 text-center">
          {{ $t("gameLobby.stats.texts.games") }}
        </h3>
      </v-sheet>
    </v-col>
    <v-col cols="12" sm="6" md="4">
      <v-sheet rounded elevation="2" class="py-5">
        <h1 class="text-center hidden-sm-and-down">
          <v-icon class="display-2">mdi-head-question-outline</v-icon>
        </h1>
        <h2 class="display-1 text-center">
          {{ stats.allTermsGuessed }}
        </h2>
        <h3 class="text-uppercase subtitle-2 text-center">
          {{ $t("gameLobby.stats.texts.terms") }}
        </h3>
      </v-sheet>
    </v-col>
    <v-col cols="12" class="text-center">
      <v-btn to="/dashboard" rounded color="primary" dark>
        {{ $t("gameLobby.stats.viewAll") }}
      </v-btn>
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
