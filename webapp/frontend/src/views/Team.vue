<template>
  <v-container>
    <v-row justify="center">
      <v-col cols="12" xl="8">
        <h1 class="display-3 mt-16 mb-5">{{ $t("footer.team.title") }}</h1>
        <v-divider class="mb-16"></v-divider>
        <h2 class="headline text-center mb-16">
          {{ $t("footer.team.text") }}
        </h2>
        <h3 class="title text-center mt-16 mb-5">TimeFlip</h3>
        <v-row justify="center">
          <v-col
            cols="12"
            md="3"
            class="d-flex flex-column align-center"
            :key="index"
            v-for="(member, index) in timeFlip"
          >
            <v-avatar size="100">
              <img :src="member.image" :alt="member.name" />
            </v-avatar>
            <div class="subtitle-1 mt-1">{{ member.name }}</div>
          </v-col>
        </v-row>
        <h3 class="title text-center mt-16 mb-5">Backend</h3>
        <v-row justify="center">
          <v-col
            cols="12"
            md="3"
            class="d-flex flex-column align-center"
            :key="index"
            v-for="(member, index) in backend"
          >
            <v-avatar size="100">
              <img :src="member.image" :alt="member.name" />
            </v-avatar>
            <div class="subtitle-1 mt-1">{{ member.name }}</div>
          </v-col>
        </v-row>
        <h3 class="title text-center mt-16 mb-5">Frontend</h3>
        <v-row justify="center">
          <v-col
            cols="12"
            md="3"
            class="d-flex flex-column align-center"
            :key="index"
            v-for="(member, index) in frontend"
          >
            <v-avatar size="100">
              <img :src="member.image" :alt="member.name" />
            </v-avatar>
            <div class="subtitle-1 mt-1">{{ member.name }}</div>
          </v-col>
        </v-row>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
export default {
  name: "Team",
  data: () => ({
    timeFlip: [
      {
        name: "Thomas Oberroither",
        image: "/avatar_thomas.png",
      },
    ],
    backend: [
      {
        name: "Jonas Labermeier",
        image: "/avatar_jonas.png",
      },
      {
        name: "Simon Brandacher",
        image: "/avatar_simon.png",
      },
      {
        name: "Martin Auer",
        image: "/no_avatar.png",
      },
    ],
    frontend: [
      {
        name: "Felix Schuler",
        image: "/avatar_felix.png",
      },
      {
        name: "Fabian Amhof",
        image: "/no_avatar.png",
      },
    ],
  }),
};
</script>
