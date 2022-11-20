<template>
  <v-container>
    <v-row justify="center">
      <v-col cols="12" xl="8">
        <v-row>
          <v-col cols="9">
            <h1
              class="display-3 mt-16 mb-5"
              v-html="$t('gameSettings.title')"
            />
          </v-col>
          <v-spacer />
          <v-col align-self="end" class="pa-5">
            <TopicDialog
              :create-topic="true"
              @dialogClosed="dialogClosed"
              :button="{ color: 'green', icon: 'mdi-plus', isIcon: false }"
            >
            </TopicDialog>
          </v-col>
        </v-row>
        <v-divider class="mb-5"></v-divider>
        <TopicList
          @dialogClosed="dialogClosed"
          @deleteTopic="deleteTopic"
          :topicList="topicList"
        />
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import TopicDialog from "@/components/game/settings/TopicDialog";
import TopicList from "@/components/game/settings/TopicList";
import { getTopics, deleteTopic } from "@/services/topics.service";

export default {
  name: "GameSettings",
  components: { TopicList, TopicDialog },
  data() {
    return {
      topicList: [],
    };
  },
  mounted() {
    this.getTopics();
  },
  methods: {
    dialogClosed() {
      this.getTopics();
    },
    deleteTopic(topic) {
      deleteTopic(topic.topicId)
        .then((topic) => {
          this.$notify({
            title: this.$t("notification.success"),
            text: this.$t("notification.confirmDeletion") + " " + topic.name,
            type: "success",
          });
        })
        .catch((error) => {
          this.$notify({
            title: this.$t("notification.error") + error.response.status,
            text: this.$t("notification.deletionFailed") + " " + topic.name,
            type: "error",
          });
        })
        .then(() => this.getTopics());
    },
    getTopics() {
      getTopics()
        .then((data) => {
          this.topicList = data;
        })
        .catch((error) => {
          this.$notify({
            title: this.$t("notification.error") + error.response.status,
            text: this.$t("notification.topicsNotLoaded"),
            type: "error",
          });
        });
    },
  },
};
</script>

<style scoped></style>
