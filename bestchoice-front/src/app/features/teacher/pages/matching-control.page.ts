import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatchingService } from '../../../core/services/matching.service';
import { MatchingRunRequest } from '../../matching/models/matching.model';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-matching-control',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="control-container">
      <a routerLink="/app/teacher/dashboard" class="btn-retour">← Tableau de bord</a>
      <header class="page-header">
        <h1>Pilotage du Matching</h1>
        <p class="text-muted">Configurez l'importance relative des critères pour l'algorithme Hybride.</p>
      </header>

      <div class="control-grid">
        <section class="card settings-card">
          <h3>Réglage des Poids</h3>

          <div class="weight-row">
            <div class="label-group">
              <label>Compétences (Skills)</label>
              <span class="value-badge">{{ weights().skills }}%</span>
            </div>
            <input type="range" min="0" max="100" [value]="weights().skills"
                   (input)="updateWeight('skills', $event)">
          </div>

          <div class="weight-row">
            <div class="label-group">
              <label>Centres d'intérêt</label>
              <span class="value-badge">{{ weights().interests }}%</span>
            </div>
            <input type="range" min="0" max="100" [value]="weights().interests"
                   (input)="updateWeight('interests', $event)">
          </div>

          <div class="weight-row disabled">
            <div class="label-group">
              <label>Type de travail (Auto-calculé)</label>
              <span class="value-badge remainder">{{ remainingWeight() }}%</span>
            </div>
            <div class="info-text">Le reste est alloué à la correspondance du type de travail.</div>
          </div>

          <footer class="card-actions">
            <button class="btn-run" (click)="runHybridMatching()" [disabled]="isProcessing()">
              @if (isProcessing()) {
                <span class="spinner"></span> Calcul en cours...
              } @else {
                🚀 Lancer la session de Matching
              }
            </button>
          </footer>
        </section>

        <aside class="card summary-card">
          <h3>Résumé du Run</h3>
          <div class="summary-item">
            <label>Algorithme :</label>
            <span>HYBRID (Weighted + Stable)</span>
          </div>
          <div class="summary-item">
            <label>Portée :</label>
            <span>Toute la promotion (ALL_STUDENTS)</span>
          </div>
          <div class="summary-item">
            <label>Action :</label>
            <span>Ré-initialisation et Persistance</span>
          </div>
        </aside>
      </div>
    </div>
  `,
  styleUrl: './matching-control.page.scss'
})
export class MatchingControlPage {
  private matchingService = inject(MatchingService);

  // Signal pour les poids éditables
  weights = signal({ skills: 50, interests: 30 });
  isProcessing = signal(false);

  // Calcul automatique du troisième poids (WorkType)
  remainingWeight = computed(() => {
    const total = 100 - (this.weights().skills + this.weights().interests);
    return Math.max(0, total); // Empêche les nombres négatifs
  });

  // ✅ CORRECTION : La méthode manquante
  updateWeight(key: 'skills' | 'interests', event: Event) {
    const value = +(event.target as HTMLInputElement).value;

    this.weights.update(current => {
      const next = { ...current, [key]: value };

      // Sécurité : si la somme dépasse 100, on ajuste l'autre curseur
      if (next.skills + next.interests > 100) {
        if (key === 'skills') next.interests = 100 - value;
        else next.skills = 100 - value;
      }

      return next;
    });
  }

  runHybridMatching() {
    this.isProcessing.set(true);

    const request: MatchingRunRequest = {
      algorithm: 'HYBRID', // Utilise ta HybridMatchingStrategy
      scope: 'ALL_STUDENTS',
      persist: true,
      recompute: true, // Wipe les anciens résultats
      weights: {
        skills: this.weights().skills / 100,
        interests: this.weights().interests / 100,
        workType: this.remainingWeight() / 100
      }
    };

    this.matchingService.runMatching(request)
      .pipe(finalize(() => this.isProcessing.set(false)))
      .subscribe({
        next: (res) => {
          alert(`Succès ! ${res.resultsSaved} affectations stables ont été générées dans la session ${res.sessionId}.`);
        },
        error: (err) => {
          console.error(err);
          alert('Erreur lors du calcul. Vérifiez que les étudiants ont des compétences.');
        }
      });
  }
}
