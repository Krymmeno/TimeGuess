<template>
  <v-row>
    <TopicCard :topics="topics" />
    <ActiveGameCard :activeGames="activeGames" />
    <TimeFlipCard :timeFlips="timeFlips" />
  </v-row>
</template>

<script>
import TopicCard from "@/components/admin/dashboard/TopicCard";
import ActiveGameCard from "@/components/admin/dashboard/ActiveGameCard";
import TimeFlipCard from "@/components/admin/dashboard/TimeFlipCard";
export default {
  name: "ManagementCards",
  components: { TimeFlipCard, ActiveGameCard, TopicCard },
  props: {
    topics: Array,
    activeGames: Array,
    timeFlips: Array,
  },
};
</script>
