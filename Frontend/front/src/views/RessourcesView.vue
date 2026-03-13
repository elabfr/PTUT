<script setup>
const sections = [
    {
        titre: 'GUIDES & PROCÉDURES',
        fichiers: [
            { nom: "Guide de l'Ambassadeur", type: 'PDF', taille: '2.4Mo' },
            { nom: 'Attestation de présence', type: 'PDF', taille: '2.4Mo' },
            { nom: "Guide de l'Ambassadeur", type: 'PDF', taille: '2.4Mo' },
            { nom: 'Attestation de présence', type: 'PDF', taille: '2.4Mo' },
        ]
    },
    {
        titre: 'IDENTITÉ VISUELLE & PRÉSENTATIONS',
        fichiers: [
            { nom: 'Présentation ISIS (Lycées)', type: 'PPTX', taille: '2.4Mo' },
            { nom: 'Pack Logo ISIS', type: 'ZIP', taille: '2.4Mo' },
            { nom: 'Flyer JPOISIS', type: 'PDF', taille: '2.4Mo' },
        ]
    }
]
</script>

<template>
    <v-container class="py-8">

        <h1 class="text-h4 font-weight-bold mb-6">Bibliothèque de documents</h1>

        <div v-for="section in sections" :key="section.titre" class="mb-8">

            <div class="d-flex align-center mb-4">
                <span class="text-h1 font-weight-bold" style="white-space: nowrap">
                    {{ section.titre }}
                </span>
                <v-divider class="ml-4" />
            </div>

            <v-row>
                <v-col v-for="fichier in section.fichiers" :key="fichier.nom" cols="12" sm="6" md="4" lg="3"
                    height="100%">
                    <v-card height="100%" rounded="lg" color="#F4F6F9" class="pa-3">
                        <div class="d-flex align-center justify-space-between">

                            <div class="d-flex align-center">
                                <v-sheet rounded="lg" width="44" height="44"
                                    class="d-flex align-center justify-center flex-shrink-0" color="#FFA726">
                                    <v-icon color="white" size="22">mdi-file-document</v-icon>
                                </v-sheet>

                                <div class="ml-3">
                                    <div class="text-body-2 font-weight-medium" style="line-height: 1.2">
                                        {{ fichier.nom }}
                                    </div>
                                    <div class="text-caption text-medium-emphasis">
                                        {{ fichier.type }} · {{ fichier.taille }}
                                    </div>
                                </div>
                            </div>

                            <v-btn icon variant="text" size="small">
                                <v-icon size="20">mdi-download</v-icon>
                            </v-btn>

                        </div>
                    </v-card>
                </v-col>
            </v-row>

        </div>
    </v-container>
</template>