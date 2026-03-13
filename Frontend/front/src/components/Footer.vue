<!-- Footer.vue -->
<template>
    <v-footer app height="50"
        style="padding: 0; background: #f5f5f5; border-top: 1px solid #e0e0e0; display: flex; justify-content: center; align-items: center;">
        <!-- UC Logo -->
        <v-col cols="auto" class="d-flex align-center">
            <img src="../assets/logo-inuc.png" height="30px" class="ml-3" />
            <img src="../assets/logo-insa.gif" height="50px" class="ml-3" />
        </v-col>

        <v-divider vertical class="mx-4 footer-divider" />

        <!-- Copyright -->
        <v-col cols="auto">
            <span class="footer-text">© ISIS 2026 • Tous droits réservés</span>
        </v-col>

        <v-divider vertical class="mx-4 footer-divider" />

        <!-- Links -->
        <v-col cols="auto">
            <v-btn variant="text" size="small" class="footer-link">Mentions Légales</v-btn>
        </v-col>

        <span class="footer-text mx-1">•</span>

        <v-col cols="auto">
            <v-btn variant="text" size="small" class="footer-link">Support</v-btn>
        </v-col>
    </v-footer>
</template>

<style scoped>
.footer-text {
    color: #888;
    font-size: 12px;
}

.footer-divider {
    border-color: #e0e0e0 !important;
    opacity: 1;
}

.footer-link {
    color: #888 !important;
    font-size: 12px !important;
    text-transform: none;
    min-width: unset;
    padding: 0 4px;
}

.footer-link:hover {
    color: #8B4FC8 !important;
}
</style>