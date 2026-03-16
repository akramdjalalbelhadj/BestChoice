import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatchingService } from '../../../core/services/matching.service';
import { MatchingRunRequest } from '../../matching/models/matching.model';
import { finalize } from 'rxjs';

@Component({
  selector: 'app-matching-control',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="control-container">
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
  styles: [`
    :host { display: block; padding: 2rem; background: #09090b; min-height: 100vh; color: #fff; }
    .page-header { margin-bottom: 2.5rem; }
    h1 { font-size: 2rem; font-weight: 800; }

    .control-grid { display: grid; grid-template-columns: 1.5fr 1fr; gap: 2rem; }
    .card { background: #18181b; border: 1px solid #27272a; border-radius: 16px; padding: 2rem; }

    .weight-row { margin-bottom: 2rem; }
    .label-group { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
    .label-group label { font-size: 0.9rem; font-weight: 600; color: #a1a1aa; }
    .value-badge { background: #3b82f6; color: #fff; padding: 4px 10px; border-radius: 6px; font-weight: 800; font-size: 0.85rem; }
    .remainder { background: #27272a; color: #71717a; }

    input[type="range"] { width: 100%; height: 6px; background: #27272a; border-radius: 3px; appearance: none; cursor: pointer; }
    input[type="range"]::-webkit-slider-thumb { appearance: none; width: 18px; height: 18px; background: #3b82f6; border-radius: 50%; }

    .btn-run { width: 100%; padding: 14px; background: #3b82f6; color: #fff; border: none; border-radius: 8px; font-weight: 700; cursor: pointer; transition: 0.2s; }
    .btn-run:hover { background: #2563eb; }
    .btn-run:disabled { opacity: 0.5; cursor: not-allowed; }

    .summary-item { display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid #27272a; font-size: 0.85rem; }
    .summary-item label { color: #71717a; }
    .info-text { font-size: 0.75rem; color: #71717a; font-style: italic; margin-top: 8px; }
  `]
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
