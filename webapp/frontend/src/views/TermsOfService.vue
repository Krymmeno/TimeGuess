<template>
  <v-container>
    <v-row justify="center">
      <v-col cols="12" xl="8">
        <h1 class="display-3 mt-16 mb-5">{{ $t("footer.tos.title") }}</h1>
        <v-divider class="mb-16"></v-divider>
        <h2 class="display-2 mb-5">{{ $t("footer.tos.dataUsage.title") }}</h2>
        <p v-html="$t('footer.tos.dataUsage.text')"></p>
        <h2 class="display-2 mt-10 mb-5">
          {{ $t("footer.tos.applicableLaw.title") }}
        </h2>
        <p>
          {{ $t("footer.tos.applicableLaw.text") }}
        </p>
        <h2 class="display-2 mt-10 mb-5">
          {{ $t("footer.tos.userConduct.title") }}
        </h2>
        <p>
          {{ $t("footer.tos.userConduct.text") }}
        </p>
      </v-col>
    </v-row>
  </v-container>
</template>
