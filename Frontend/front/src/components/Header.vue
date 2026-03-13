<template>
    <v-app-bar flat height="64"
        style="background: linear-gradient(135deg, #6B3FA0 0%, #8B4FC8 25%, #C4507A 65%, #E8734A 100%);">
        <img src="../assets/logo-isis.png" height="100%" class="ml-3" />

        <v-app-bar-title class="text-center">
            <v-btn variant="text" color="white" :to="{ name: 'catalogue' }" component="RouterLink"
                class="text-body-1 font-weight-bold" active-color="bleu">
                Catalogue
            </v-btn>
            <v-btn variant="text" color="white" :to="{ name: 'ressources' }" component="RouterLink"
                class="text-body-1 font-weight-bold" active-color="bleu">
                Ressources
            </v-btn>
        </v-app-bar-title>

        <v-btn variant="flat" color="bleu" rounded="xl" class="mx-3">
            <v-icon class="mr-2">mdi-account</v-icon>
            Mon Profil
        </v-btn>
    </v-app-bar>
</template>