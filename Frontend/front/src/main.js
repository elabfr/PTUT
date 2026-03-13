import { createApp } from 'vue'
import App from './App.vue'
import '@mdi/font/css/materialdesignicons.css'
import './assets/base.css'
import 'vuetify/styles'
import { createVuetify } from 'vuetify'
import router from './router'        // ✅ déjà bien importé

const vuetify = createVuetify({
    theme: {
        defaultTheme: 'isisTheme',
        themes: {
            isisTheme: {
                colors: {
                    bleu: '#2f0d73',
                    jaune: '#ffb43c',
                    vert: '#3cbebe',
                    formation: '#3CBEBE',
                    formation_fond: '#E0F2F1',
                    reseaux: '#7B1FA2',
                    reseaux_fond: '#F3E5F5',
                    lycee: '#856404',
                    lycee_fond: '#FFF3CD',
                    salon: '#0D47A1',
                    salont_fond: '#E3F2FD'
                }
            }
        }
    }
})

createApp(App)
    .use(vuetify)
    .use(router)
    .mount('#app')