<template>
  <v-container>
    <v-row justify="center">
      <v-col cols="12" xl="8">
        <h1 class="display-3 mt-16 mb-5">Dashboard</h1>
        <v-divider class="mb-5"></v-divider>
        <v-container>
          <StatCards :stats="stats.gameStats" />
          <v-row>
            <v-col cols="12" md="6">
              <v-sheet elevation="2" rounded>
                <h3 class="heading pa-2 primary rounded-t white--text">
                  {{ $t("dashboard.gameRoundsPerDay") }}
                </h3>
                <LineChart
                  :height="300"
                  :chart-data="dayChart.chartData"
                  :options="dayChart.options"
                />
              </v-sheet>
            </v-col>
            <v-col cols="12" md="6">
              <v-sheet elevation="2" rounded>
                <h3 class="heading pa-2 primary rounded-t white--text">
                  {{ $t("dashboard.gamesPerTopic") }}
                </h3>
                <PieChart
                  :height="300"
                  :chart-data="topicChart.chartData"
                  :options="topicChart.options"
                />
              </v-sheet>
            </v-col>
          </v-row>
          <v-row>
            <v-col cols="12" md="6">
              <v-sheet elevation="2" rounded>
                <h3 class="heading pa-2 primary rounded-t white--text">
                  {{ $t("dashboard.gamesWonPerUser") }}
                </h3>
                <BarChart
                  :height="300"
                  :chart-data="highScoreChart.chartData"
                  :options="highScoreChart.options"
                  :plugin="plugin"
                />
              </v-sheet>
            </v-col>
            <v-col cols="12" md="6">
              <v-sheet elevation="2" rounded>
                <h3 class="heading pa-2 primary rounded-t white--text">
                  {{ $t("dashboard.termsGuessedPerTopic") }}
                </h3>
                <DoughnutChart
                  :height="300"
                  :chart-data="gamesWonChart.chartData"
                  :options="gamesWonChart.options"
                />
              </v-sheet>
            </v-col>
          </v-row>
          <ManagementCards
            v-if="$store.getters['user/getUser'].role !== 'PLAYER'"
            :topics="stats.topics"
            :activeGames="stats.gameRooms"
            :timeFlips="stats.timeFlips"
          />
        </v-container>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import LineChart from "@/components/admin/dashboard/LineChart.vue";
import StatCards from "@/components/admin/dashboard/StatCards.vue";
import ManagementCards from "@/components/admin/dashboard/ManagementCards";
import { getGameStatistics } from "@/services/statistics.service";
import { getTopics } from "@/services/topics.service";
import { getGameRooms } from "@/services/gameroom.service";
import { getTimeFlips } from "@/services/timeflip.service";
import DoughnutChart from "@/components/admin/dashboard/DoughnutChart";
import BarChart from "@/components/admin/dashboard/BarChart";
import PieChart from "@/components/admin/dashboard/PieChart";

export default {
  name: "Dashboard",
  components: {
    PieChart,
    BarChart,
    DoughnutChart,
    ManagementCards,
    LineChart,
    StatCards,
  },
  data: () => ({
    dayChart: {
      chartData: {},
    },
    topicChart: {
      chartData: {},
    },
    highScoreChart: {
      chartData: {},
    },
    gamesWonChart: {
      chartData: {},
    },
    stats: {
      topics: [],
      gameStats: {},
      gameRooms: [],
      timeFlips: [],
    },
  }),
  methods: {
    fetchData() {
      getGameStatistics().then((data) => {
        this.stats.gameStats = data;
        this.loadStatistics();
      });
      if (this.$store.getters["user/getUser"].role !== "PLAYER") {
        getTopics().then((topics) => {
          this.stats.topics = topics;
        });
        getTimeFlips().then((timeFlips) => {
          this.stats.timeFlips = timeFlips;
        });
        getGameRooms().then((gameRooms) => {
          this.stats.gameRooms = gameRooms;
        });
      }
    },
    loadStatistics() {
      this.loadDayChart();
      this.loadGamesWonChart();
      this.loadHighScoreChart();
      this.loadTopicChart();
    },
    loadDayChart() {
      this.dayChart.chartData = {
        labels: Object.getOwnPropertyNames(
          this.stats.gameStats.gameRoundsPerDay
        ).slice(0, -1), //Ignore __ob__ property
        datasets: [
          {
            label: this.$t("dashboard.gameRounds"),
            data: [],
            borderColor: "#0060ff",
            backgroundColor: "transparent",
          },
        ],
      };
      for (const label of this.dayChart.chartData.labels) {
        this.dayChart.chartData.datasets[0].data.push(
          this.stats.gameStats.gameRoundsPerDay[label]
        );
      }
    },
    loadTopicChart() {
      this.topicChart.chartData = {
        labels: Object.getOwnPropertyNames(
          this.stats.gameStats.gamesPerTopic
        ).slice(0, -1), //Ignore __ob__ property
        datasets: [
          {
            label: this.$t("stats.games"),
            backgroundColor: [],
            data: [],
          },
        ],
      };
      for (const label of this.topicChart.chartData.labels) {
        this.topicChart.chartData.datasets[0].data.push(
          this.stats.gameStats.gamesPerTopic[label]
        );
        this.topicChart.chartData.datasets[0].backgroundColor.push(
          "hsl(" + this.topicColors[label] + ", 100%, 80%)"
        );
      }
    },
    loadHighScoreChart() {
      this.highScoreChart.chartData = {
        labels: Object.getOwnPropertyNames(this.stats.gameStats.gamesWonPerUser)
          .slice(0, -1)
          .slice(0, 5), //Ignore __ob__ property
        datasets: [
          {
            label: this.$t("statistic.wonGames"),
            backgroundColor: "#0060ff",
            data: [],
          },
        ],
      };
      for (const label of this.highScoreChart.chartData.labels) {
        this.highScoreChart.chartData.datasets[0].data.push(
          this.stats.gameStats.gamesWonPerUser[label]
        );
      }
    },
    loadGamesWonChart() {
      this.gamesWonChart.chartData = {
        labels: [this.$t("statistic.correct"), this.$t("statistic.wrong")],
        datasets: [],
      };
      for (const label of Object.getOwnPropertyNames(
        this.stats.gameStats.termsGuessedCorrectlyPerTopic
      ).slice(0, -1)) {
        let dataset = {
          label: label,
          labels: [
            label + " " + this.$t("statistic.correct"),
            label + " " + this.$t("statistic.wrong"),
          ],
          backgroundColor: [
            "hsl(" + this.topicColors[label] + ", 100%, 80%)",
            "hsl(" + this.topicColors[label] + ", 100%, 15%)",
          ],
          data: [],
        };
        this.gamesWonChart.chartData.datasets.push(dataset);
        dataset.data.push(
          this.stats.gameStats.termsGuessedCorrectlyPerTopic[label]
        );
        dataset.data.push(
          this.stats.gameStats.termsGuessedWronglyPerTopic[label]
        );
      }
    },
  },
  computed: {
    topicColors: function () {
      let colors = {};
      let labels = Object.getOwnPropertyNames(
        this.stats.gameStats.termsGuessedCorrectlyPerTopic
      ).slice(0, -1);
      for (const i in labels) {
        let label = labels[i];
        colors[label] = (i * 255) / labels.length;
      }
      return colors;
    },
    plugin: function () {
      return {
        id: "image-label",
        afterDraw: (chart) => {
          var ctx = chart.chart.ctx;
          var xAxis = chart.scales["x-axis-0"];
          var yAxis = chart.scales["y-axis-0"];
          if (
            this.highScoreChart.chartData.datasets[0] &&
            this.highScoreChart.chartData.datasets[0].data[0] > 0
          ) {
            var y = yAxis.getPixelForTick(0);
            var x = xAxis.getPixelForValue(
              this.highScoreChart.chartData.datasets[0].data[0]
            );
            var image = new Image();
            image.src = require(`@/assets/trophy.png`);
            ctx.drawImage(
              image,
              x - image.width * 0.2 - 5,
              y - (image.width * 0.2) / 2,
              image.width * 0.2,
              image.height * 0.2
            );
          }
        },
      };
    },
  },
  mounted() {
    this.fetchData();
  },
};
</script>
