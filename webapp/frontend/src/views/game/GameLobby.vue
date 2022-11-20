<template>
  <v-container>
    <h1
      class="display-3 text-center mt-16 mb-10"
      v-html="$t('gameLobby.title')"
    ></h1>
    <h2 class="headline text-center mb-5">{{ $t("gameLobby.subtitle") }}</h2>
    <v-row justify="center">
      <v-col cols="12" xl="8">
        <h3 class="display-1" v-html="$t('gameLobby.gameInvites.title')"></h3>
        <v-divider class="mb-5"></v-divider>
        <GameCards :games="gameInvites" />
        <v-divider class="my-16"></v-divider>
        <div class="text-center">
          <CreateGameButton />
        </div>
        <v-divider class="my-16"></v-divider>
        <h3 class="display-1 mb-5" v-html="$t('gameLobby.stats.title')"></h3>
        <StatCards :stats="stats"></StatCards>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import GameCards from "@/components/game/gamelobby/GameCards.vue";
import CreateGameButton from "@/components/game/gamelobby/CreateGameButton";
import WS from "@/services/ws.service";
import { getInvites } from "@/services/invites.service";
import { getGameStatistics } from "@/services/statistics.service";
import StatCards from "@/components/game/gamelobby/StatCards";
export default {
  name: "GameLobby",
  components: { StatCards, GameCards, CreateGameButton },
  data() {
    return {
      gameInvites: [],
      subscription: null,
      stats: {},
    };
  },
  mounted() {
    this.updateInvites();
    this.getStats();
    if (WS.connected) {
      this.subscribeToInvites();
    } else {
      WS.connect(
        {
          Authorization: `Bearer ${this.$store.getters["auth/getToken"]}`,
        },
        () => {
          this.subscribeToInvites();
        },
        (error) => {
          console.log(error);
        }
      );
    }
  },
  methods: {
    updateInvites() {
      getInvites(this.$store.getters["user/getUser"].userId).then(
        (response) => {
          this.gameInvites = response;
          console.log(response);
        }
      );
    },
    getStats() {
      getGameStatistics().then((data) => {
        this.stats = data;
      });
    },
    subscribeToInvites() {
      this.subscription = WS.subscribe(
        `/topic/invites/${this.$store.getters["user/getUser"].userId}`,
        (tick) => {
          if (tick.body === "INVITES_UPDATE") {
            this.updateInvites();
          }
        }
      );
    },
  },
  beforeRouteLeave(to, from, next) {
    this.subscription.unsubscribe();
    next();
  },
};
</script>

<style scoped></style>
