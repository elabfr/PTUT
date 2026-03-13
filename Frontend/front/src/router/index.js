import { createRouter, createWebHistory } from 'vue-router'
import CatalogueView from '../views/CatalogueView.vue'
import RessourcesView from '../views/RessourcesView.vue'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        { path: '/catalogue', name: 'catalogue', component: CatalogueView },
        { path: '/ressources', name: 'ressources', component: RessourcesView },
    ]
})

export default router