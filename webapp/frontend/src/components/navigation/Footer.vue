<template>
  <v-footer dark padless class="mt-16">
    <v-row justify="center" no-gutters>
      <v-btn
        v-for="(link, index) in links"
        :key="index"
        color="white"
        text
        rounded
        class="my-2"
        :to="link.href"
      >
        {{ $t(`footer.${link.title}.title`) }}
      </v-btn>
      <v-col class="py-4 text-center white--text" cols="12">
        {{ new Date().getFullYear() }} — <strong>TimeGuess</strong>
      </v-col>
    </v-row>
  </v-footer>
</template>

<script>
export default {
  data: () => ({
    links: [
      { title: "lobby", href: "/gamelobby" },
      { title: "team", href: "/team" },
      { title: "tos", href: "/tos" },
      { title: "contact", href: "/contact" },
    ],
  }),
};
</script>
