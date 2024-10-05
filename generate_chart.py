import matplotlib.pyplot as plt
import csv
import sys

def generate_chart(csv_file):
    motifs = []
    durations = []
    
    # Lire les données du fichier CSV
    with open(csv_file, mode='r') as file:
        csv_reader = csv.reader(file)
        next(csv_reader)  # Skip header row
        for row in csv_reader:
            motifs.append(row[0])  # Ajouter les motifs dans l'ordre du fichier
            durations.append(int(row[1]))  # Ajouter les durées dans l'ordre du fichier

    # Créer une figure pour le diagramme à barres
    plt.figure(figsize=(6, 5))  # Taille ajustée du graphique

    # Couleurs différentes pour chaque barre
    colors = ['#FF5733', '#FFBD33', '#75FF33', '#33FFBD', '#335BFF', '#A833FF', '#FF33A8']
    if len(motifs) > len(colors):
        colors = colors * (len(motifs) // len(colors) + 1)

    # Diagramme à barres sans tri des motifs ou des durées
    bar_width = 0.8  # Réduire l'espace entre les barres
    plt.bar(motifs, durations, color=colors[:len(motifs)], width=bar_width)
    plt.xlabel('Motifs')
    plt.ylabel('Temps (ms)')
    plt.title('Temps d\'exécution pour chaque motif')
    plt.xticks(rotation=45, ha="right")  # Rotation des étiquettes des motifs pour meilleure lisibilité

    # Ajuster les espacements
    plt.tight_layout()
    plt.savefig('execution_times_chart.png')  # Sauvegarder le graphique
    plt.show()

if __name__ == '__main__':
    if len(sys.argv) != 2:
        print("Usage: python3 generate_chart.py <csv_file>")
    else:
        generate_chart(sys.argv[1])
