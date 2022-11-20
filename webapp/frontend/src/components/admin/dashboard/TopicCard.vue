<template>
  <v-col cols="12" md="4">
    <v-sheet elevation="2" rounded class="pb-3">
      <h3
        class="heading pa-2 primary rounded-t white--text d-flex justify-space-between align-center"
      >
        {{ $t("dashboard.topics.title") }}
        <v-btn x-small outlined color="amber" to="/game/settings">
          {{ $t("dashboard.topics.manage") }}
        </v-btn>
      </h3>
      <div class="pa-3 pt-4" :key="topic.name" v-for="topic in topics">
        <div class="d-flex justify-space-between">
          <span class="body-1">{{ topic.name }}</span>
          <span class="right text--secondary body-2">
            {{ topic.terms.length }} {{ $t("dashboard.topics.terms") }}
          </span>
        </div>
        <v-divider></v-divider>
      </div>
    </v-sheet>
  </v-col>
</template>

<script>
export default {
  name: "TopicCard",
  props: {
    topics: Array,
  },
};
</script>
