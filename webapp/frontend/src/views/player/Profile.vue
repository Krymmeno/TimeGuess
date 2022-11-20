<template>
  <v-container>
    <v-row justify="center">
      <v-col cols="12" xl="8">
        <h1 class="display-3 mt-16 mb-5">
          {{ user.firstName + " " + user.lastName + "'s " }}
          <span class="font-weight-thin">{{ $t("profile.title") }}</span>
        </h1>
        <v-divider class="mb-5"></v-divider>
        <v-row>
          <v-col cols="12" sm="6">
            <Information :user="user"> </Information>
          </v-col>
          <v-col cols="12" sm="6">
            <Statistics :statistics="statistics"></Statistics>
          </v-col>
        </v-row>
        <v-row>
          <v-col cols="12">
            <KnownUsers :knownUsers="knownUsers"></KnownUsers>
          </v-col>
        </v-row>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import Information from "@/components/player/profile/Information";
import Statistics from "@/components/player/profile/Statistics";
import { getUser } from "@/services/user.service";
import { getUserStatistics } from "@/services/statistics.service";
import KnownUsers from "@/components/player/profile/KnownUsers";

export default {
  name: "Profile",
  components: { KnownUsers, Statistics, Information },
  data() {
    return {
      user: { username: "", firstName: "", lastName: "", role: "" },
      statistics: {},
      knownUsers: [],
    };
  },
  methods: {
    async getUser() {
      let id = Number(this.$route.params.id);
      if (id) {
        try {
          let user = await getUser(id);
          if (user) {
            this.user = user;
          } else {
            this.$router.push({ path: "/errors/404" });
          }
        } catch {
          this.$notify({
            title: this.$t("notification.error"),
            text: this.$t("notification.getUserFailure"),
            type: "error",
          });
          this.$router.push({ path: "/errors/404" });
        }
      } else {
        let user = this.$store.getters["user/getUser"];
        if (user) {
          this.user = user;
        } else {
          this.$router.push({ path: "/errors/404" });
        }
      }
    },
    async loadData() {
      await this.getUser();
      getUserStatistics(this.user.userId).then((statistics) => {
        this.knownUsers = statistics.teammates;
        delete statistics.teammates;
        this.statistics = statistics;
      });
    },
  },
  mounted() {
    this.loadData();
  },
};
</script>

<style scoped></style>
